import {
  chromium,
  expect,
  request as playwrightRequest,
  test,
  type APIRequestContext,
  type APIResponse,
  type TestInfo,
} from '@playwright/test';
import { createHash, randomBytes } from 'node:crypto';
import { createRequire } from 'node:module';
import { readFileSync } from 'node:fs';
import type { Browser } from '@playwright/test';
import { parse } from 'toml';

const require = createRequire(import.meta.url);
const SwaggerParser = require('@apidevtools/swagger-parser') as typeof import('@apidevtools/swagger-parser');

type JsonRecord = Record<string, any>;
type AdapterKind = 'wfs' | 'esri';

type AdapterOperation = {
  serviceTitle: string;
  spec: JsonRecord;
  path: string;
  method: string;
  operation: JsonRecord;
  pluginIdentity: string;
  plugin: JsonRecord;
  options: JsonRecord;
  kind: AdapterKind;
  featureUrl: string;
};

type WfsProviderResult = {
  featureXml: string;
  schemaXml: string;
  mappingSchemaXml: string;
  expectedFailure?: string;
};

type ProviderException = {
  service_title: string;
  path: string;
  status: number;
  body_includes: string;
  reason: string;
  schema_url?: string;
};

const providerExceptions = (
  parse(readFileSync(new URL('./provider-exceptions.toml', import.meta.url), 'utf8')) as {
    provider_exception?: ProviderException[];
  }
).provider_exception ?? [];

function providerExceptionFor(contract: AdapterOperation): ProviderException | undefined {
  return providerExceptions.find(
    (exception) => exception.service_title === contract.serviceTitle && exception.path === contract.path,
  );
}

function record(value: unknown): JsonRecord {
  return value !== null && typeof value === 'object' ? (value as JsonRecord) : {};
}

function findParcelOptions(value: unknown): JsonRecord | undefined {
  const candidate = record(value);
  if (Array.isArray(candidate.keys)) return candidate;
  for (const nested of Object.values(candidate)) {
    const found = findParcelOptions(nested);
    if (found) return found;
  }
  return undefined;
}

function serviceTitle(spec: JsonRecord): string {
  return String(record(spec.info).title ?? '<untitled service>');
}

async function attachJson(testInfo: TestInfo, name: string, value: unknown): Promise<void> {
  await testInfo.attach(name, {
    body: JSON.stringify(value, null, 2),
    contentType: 'application/json',
  });
}

async function attachExchange(
  testInfo: TestInfo,
  name: string,
  requestDetails: JsonRecord,
  response: APIResponse,
): Promise<string> {
  await attachJson(testInfo, `${name}-request`, requestDetails);
  const body = await response.body();
  await testInfo.attach(`${name}-response`, {
    body,
    contentType: response.headers()['content-type'] ?? 'application/octet-stream',
  });
  return body.toString('utf8');
}

function adapterOperations(specs: JsonRecord[]): AdapterOperation[] {
  const operations: AdapterOperation[] = [];
  const methods = ['get', 'post', 'put', 'patch', 'delete'];

  for (const spec of specs) {
    const title = serviceTitle(spec);
    const servers = Array.isArray(spec.servers) ? spec.servers : [];
    const serverUrl = record(servers[0]).url;
    expect(serverUrl, `${title} must define a server URL`).toBeTruthy();

    for (const [path, pathValue] of Object.entries(record(spec.paths))) {
      const pathItem = record(pathValue);
      for (const method of methods) {
        const operation = record(pathItem[method]);
        if (!Object.keys(operation).length) continue;

        const materializations = record(operation['x-dicentra-aviator-pluginMaterialization']);
        for (const [pluginIdentity, configValue] of Object.entries(materializations)) {
          const plugin = record(configValue);
          if (plugin.active !== true) continue;

          const kind: AdapterKind | undefined =
            pluginIdentity === 'WfsAdapter' || typeof plugin.typeNames === 'string'
              ? 'wfs'
              : pluginIdentity === 'EsriAdapter'
                ? 'esri'
                : undefined;
          expect(kind, `${title} ${method.toUpperCase()} ${path} uses an unsupported active translator ${pluginIdentity}`).toBeTruthy();

          const options = findParcelOptions(operation['x-dicentra-aviator-serviceOptions']);
          expect(options?.keys, `${title} ${method.toUpperCase()} ${path} must publish parcel-key mappings`).toBeInstanceOf(Array);
          const base = String(serverUrl).endsWith('/') ? String(serverUrl) : `${serverUrl}/`;
          operations.push({
            serviceTitle: title,
            spec,
            path,
            method,
            operation,
            pluginIdentity,
            plugin,
            options: options!,
            kind: kind!,
            featureUrl: new URL(path.replace(/^\//, ''), base).toString(),
          });
        }
      }
    }
  }

  return operations;
}

async function discoverServedSpecs(): Promise<JsonRecord[]> {
  const browser = await chromium.launch();
  const baseURL = process.env.CODEX_BASE_URL ?? 'http://127.0.0.1:8081';
  const codex = await playwrightRequest.newContext({ baseURL });
  try {
    const token = await obtainCodexToken(browser);
    return await loadBuiltinSpecs(codex, token);
  } finally {
    await codex.dispose();
    await browser.close();
  }
}

async function obtainCodexToken(browser: Browser): Promise<string> {
  const issuer = process.env.CODEX_OIDC_ISSUER;
  const clientId = process.env.AREAASSIST_OIDC_CLIENT_ID;
  const username = process.env.CODEX_TEST_USERNAME;
  const password = process.env.CODEX_TEST_PASSWORD;
  const redirectUri = process.env.CODEX_OIDC_REDIRECT_URI ?? 'dcaa://areaassist.mallne.cloud/login';
  // The AreaAssist client accepts `openid`; group membership is carried in the
  // realm roles and normalized by Synapse, so don't request the rejected groups scope.
  const scope = (process.env.CODEX_OIDC_SCOPE ?? 'openid')
    .split(/[\s,]+/)
    .filter(Boolean)
    .join(' ');
  expect(issuer, 'Set CODEX_OIDC_ISSUER in areaassist/shared/.env').toBeTruthy();
  expect(clientId, 'Set AREAASSIST_OIDC_CLIENT_ID in areaassist/shared/.env').toBeTruthy();
  expect(username, 'Set CODEX_TEST_USERNAME to a SuperAdmin user login').toBeTruthy();
  expect(password, 'Set CODEX_TEST_PASSWORD to that user password').toBeTruthy();

  const oidc = await playwrightRequest.newContext();
  const context = await browser.newContext();
  let clearPassword: (() => Promise<void>) | undefined;
  try {
    const configurationUrl = new URL('.well-known/openid-configuration', issuer!.endsWith('/') ? issuer! : `${issuer}/`);
    const configurationResponse = await oidc.get(configurationUrl.toString());
    expect(configurationResponse.ok(), 'OIDC discovery document should be available').toBeTruthy();
    const configuration = record(await configurationResponse.json());
    expect(configuration.token_endpoint, 'OIDC discovery must provide token_endpoint').toBeTruthy();
    expect(configuration.authorization_endpoint, 'OIDC discovery must provide authorization_endpoint').toBeTruthy();

    const verifier = randomBytes(32).toString('base64url');
    const challenge = createHash('sha256').update(verifier).digest('base64url');
    const state = randomBytes(24).toString('base64url');
    const authorizationUrl = new URL(String(configuration.authorization_endpoint));
    authorizationUrl.search = new URLSearchParams({
      client_id: clientId!,
      redirect_uri: redirectUri,
      response_type: 'code',
      scope,
      state,
      nonce: randomBytes(24).toString('base64url'),
      code_challenge: challenge,
      code_challenge_method: 'S256',
    }).toString();

    const page = await context.newPage();
    let callbackLocation: string | undefined;
    let resolveCallback!: (location: string) => void;
    const callback = new Promise<string>((resolve) => {
      resolveCallback = resolve;
    });
    page.on('response', (response) => {
      const location = response.headers().location;
      if (location?.startsWith(redirectUri)) {
        callbackLocation = location;
        resolveCallback(location);
      }
    });

    try {
      await page.goto(authorizationUrl.toString(), { waitUntil: 'domcontentloaded' });
    } catch (error) {
      // A browser cannot navigate to the app's custom dcaa:// scheme. The
      // authorization code is captured from the preceding HTTP redirect.
      if (!callbackLocation) throw error;
    }

    const loginPageText = await page.locator('body').innerText().catch(() => '');
    expect(
      loginPageText,
      `OIDC rejected the redirect URI. Set CODEX_OIDC_REDIRECT_URI to a URI registered for AREAASSIST_OIDC_CLIENT_ID (${redirectUri}).`,
    ).not.toContain('Invalid parameter: redirect_uri');

    if (!callbackLocation) {
      const usernameInput = page.locator('input[name="username"], input#username, input[type="email"]').first();
      const passwordInput = page.locator('input[name="password"], input#password').first();
      clearPassword = async () => {
        if (await passwordInput.isVisible()) await passwordInput.fill('');
      };
      const clickAndWaitForNavigation = async (locator: ReturnType<typeof page.getByRole>) => {
        await locator.click();
        await page.waitForLoadState('domcontentloaded', { timeout: 15_000 }).catch(() => undefined);
      };
      const isVisibleWithin = async (locator: ReturnType<typeof page.getByRole>, timeout = 5_000) => {
        try {
          await expect(locator).toBeVisible({ timeout });
          return true;
        } catch {
          return false;
        }
      };
      await expect(usernameInput, 'OIDC frontchannel login must show a username field').toBeVisible();
      await usernameInput.fill(username!);
      if (!(await passwordInput.isVisible())) {
        await clickAndWaitForNavigation(page.getByRole('button', { name: /sign in|continue|next/i }));
      }
      if (!(await passwordInput.isVisible())) {
        const anotherWay = page.getByRole('link', { name: /try another way|einen anderen weg versuchen/i });
        if (await isVisibleWithin(anotherWay)) await clickAndWaitForNavigation(anotherWay);
      }
      if (!(await passwordInput.isVisible())) {
        const usernamePasswordMethod = page.getByRole('button', {
          name: /username and password|benutzername und passwort/i,
        });
        if (await isVisibleWithin(usernamePasswordMethod, 15_000)) {
          await clickAndWaitForNavigation(usernamePasswordMethod);
        }
      }
      await expect(passwordInput, 'OIDC frontchannel login must show a password field').toBeVisible({ timeout: 15_000 });
      await passwordInput.fill(password!);
      await page.getByRole('button', { name: /sign in|log in|anmelden/i }).click();
      await expect.poll(() => callbackLocation, { timeout: 90_000 }).toBeTruthy();
    }

    const finalCallbackLocation = callbackLocation ?? (await callback);
    const callbackUrl = new URL(finalCallbackLocation);
    expect(callbackUrl.searchParams.get('state'), 'OIDC state must match the login request').toBe(state);
    expect(callbackUrl.searchParams.get('error'), 'OIDC authorization must not return an error').toBeNull();
    const code = callbackUrl.searchParams.get('code');
    expect(code, 'OIDC callback must contain an authorization code').toBeTruthy();

    const tokenResponse = await oidc.post(String(configuration.token_endpoint), {
      form: {
        grant_type: 'authorization_code',
        client_id: clientId!,
        code: code!,
        redirect_uri: redirectUri,
        code_verifier: verifier,
      },
    });
    expect(tokenResponse.ok(), 'OIDC authorization-code exchange should succeed').toBeTruthy();
    const tokenPayload = record(await tokenResponse.json());
    expect(tokenPayload.access_token, 'OIDC token response must contain access_token').toBeTruthy();
    return String(tokenPayload.access_token);
  } finally {
    try {
      await clearPassword?.();
    } catch {
      // The identity provider may already have redirected away from the login form.
    }
    await context.close();
    await oidc.dispose();
  }
}

async function loadBuiltinSpecs(request: APIRequestContext, token: string): Promise<JsonRecord[]> {
  const response = await request.get('/services/builtin', {
    headers: { Authorization: `Bearer ${token}` },
  });
  const responseBody = await response.text();
  const tokenParts = token.split('.');
  let tokenClaimSummary = 'unavailable (access token is not a readable JWT)';
  if (tokenParts.length >= 2) {
    try {
      const claims = record(JSON.parse(Buffer.from(tokenParts[1], 'base64url').toString('utf8')));
      const groups = claims.groups;
      const realmRoles = record(claims.realm_access).roles;
      const configuredSuperAdmin = String(process.env.CODEX_SUPERADMIN_GROUP ?? '').replace(/^\/+/, '');
      tokenClaimSummary = JSON.stringify({
        scope: claims.scope,
        groupsClaimPresent: Object.hasOwn(claims, 'groups'),
        configuredGroupFoundInGroupsClaim: Array.isArray(groups)
          ? groups.includes(process.env.CODEX_SUPERADMIN_GROUP)
          : false,
        configuredSuperadminFoundInRealmRoles: Array.isArray(realmRoles)
          ? realmRoles.includes(configuredSuperAdmin)
          : false,
      });
    } catch {
      // Opaque or encrypted access token: claims are only available through introspection.
    }
  }
  expect(
    response.status(),
    response.status() === 403
      ? `Frontchannel login succeeded, but Codex rejected this identity for /services/builtin: ${responseBody}. Decoded authorization claims: ${tokenClaimSummary}`
      : `Codex must return its built-in service definitions: ${responseBody}`,
  ).toBe(200);

  const discovery = record(JSON.parse(responseBody));
  expect(discovery.services, 'Codex discovery must return a services array').toBeInstanceOf(Array);
  expect(discovery.services.length, 'Codex must return at least one built-in API definition').toBeGreaterThan(0);
  return discovery.services as JsonRecord[];
}

async function providerFeature(
  request: APIRequestContext,
  contract: AdapterOperation,
  testInfo: TestInfo,
  attachmentName: string,
): Promise<JsonRecord | WfsProviderResult> {
  const keys = contract.options.keys as JsonRecord[];
  const references = keys
    .map((key) => key.reference)
    .filter((reference): reference is string => typeof reference === 'string' && reference.length > 0);

  if (contract.kind === 'wfs') {
    const prefix = String(contract.plugin.nsPrefix ?? '');
    const geometry = String(contract.plugin.geometryPointer ?? '');
    const properties = [...new Set([...references, geometry].filter(Boolean))]
      .map((property) => (prefix ? `${prefix}:${property}` : property))
      .join(',');
    const params = {
      service: 'WFS',
      version: '2.0.0',
      request: 'GetFeature',
      typeNames: String(contract.plugin.typeNames),
      count: '1',
      srsName: String(contract.plugin.outputCRS ?? 'urn:ogc:def:crs:EPSG::4326'),
      propertyName: properties,
    };
    const response = await request.get(contract.featureUrl, {
      params,
      timeout: 30_000,
    });
    const xml = await attachExchange(
      testInfo,
      attachmentName,
      { method: 'GET', url: contract.featureUrl, query: params },
      response,
    );
    const providerException = providerExceptionFor(contract);
    const expectedFailure =
      providerException &&
      response.status() === providerException.status &&
      xml.includes(providerException.body_includes)
        ? providerException.reason
        : undefined;
    expect(
      response.ok() || expectedFailure,
      `${contract.serviceTitle} provider request should succeed (${response.status()}): ${contract.featureUrl}; ${xml}`,
    ).toBeTruthy();
    if (response.ok()) {
      expect(xml, `${contract.serviceTitle} must return a WFS FeatureCollection`).toContain('FeatureCollection');
      expect(xml, `${contract.serviceTitle} returned a WFS exception`).not.toContain('ExceptionReport');
    }

    const schemaParams = {
      service: 'WFS',
      version: '2.0.0',
      request: 'DescribeFeatureType',
      typeNames: String(contract.plugin.typeNames),
    };
    const schemaResponse = await request.get(contract.featureUrl, {
      params: schemaParams,
      timeout: 30_000,
    });
    const schemaXml = await attachExchange(
      testInfo,
      `${attachmentName}-schema`,
      { method: 'GET', url: contract.featureUrl, query: schemaParams },
      schemaResponse,
    );
    expect(
      schemaResponse.ok(),
      `${contract.serviceTitle} DescribeFeatureType should succeed (${schemaResponse.status()}): ${contract.featureUrl}; ${schemaXml}`,
    ).toBeTruthy();
    expect(schemaXml, `${contract.serviceTitle} must return a feature schema`).toContain('schema');
    let mappingSchemaXml = schemaXml;
    if (providerException?.schema_url) {
      const mappingSchemaResponse = await request.get(providerException.schema_url, { timeout: 30_000 });
      const includedSchemaXml = await attachExchange(
        testInfo,
        `${attachmentName}-mapping-schema`,
        { method: 'GET', url: providerException.schema_url },
        mappingSchemaResponse,
      );
      expect(
        mappingSchemaResponse.ok(),
        `${contract.serviceTitle} included mapping schema should load (${mappingSchemaResponse.status()})`,
      ).toBeTruthy();
      mappingSchemaXml = `${schemaXml}\n${includedSchemaXml}`;
    }
    return { featureXml: xml, schemaXml, mappingSchemaXml, expectedFailure };
  }

  const params = {
    f: 'json',
    where: '1=1',
    outFields: references.join(',') || '*',
    returnGeometry: 'true',
    outSR: '4326',
    resultRecordCount: '1',
  };
  const response = await request.get(contract.featureUrl, {
    params,
    timeout: 30_000,
  });
  const responseBody = await attachExchange(
    testInfo,
    attachmentName,
    { method: 'GET', url: contract.featureUrl, query: params },
    response,
  );
  expect(response.ok(), `${contract.serviceTitle} provider request should succeed: ${contract.featureUrl}`).toBeTruthy();
  const payload = record(JSON.parse(responseBody));
  expect(
    payload.error,
    `${contract.serviceTitle} provider returned an ArcGIS error at ${contract.featureUrl}: ${JSON.stringify(payload.error)}`,
  ).toBeUndefined();
  expect(payload.features, `${contract.serviceTitle} must return feature data`).toBeInstanceOf(Array);
  expect(payload.features.length, `${contract.serviceTitle} must return at least one feature`).toBeGreaterThan(0);
  return record(payload.features[0]);
}

async function probeUntranslatedOperation(
  request: APIRequestContext,
  spec: JsonRecord,
  path: string,
  method: string,
  operation: JsonRecord,
  testInfo: TestInfo,
): Promise<void> {
  const title = serviceTitle(spec);
  const baseUrl = record(Array.isArray(spec.servers) ? spec.servers[0] : {}).url;
  expect(baseUrl, `${title} must declare its provider URL`).toBeTruthy();
  const url = new URL(path.replace(/^\//, ''), `${String(baseUrl).replace(/\/$/, '')}/`);
  // WFS services support GetCapabilities without requiring feature filters.
  if (/wfs/i.test(String(baseUrl)) || String(operation.operationId ?? '').toLowerCase().includes('wfs')) {
    url.searchParams.set('service', 'WFS');
    url.searchParams.set('request', 'GetCapabilities');
    url.searchParams.set('version', '2.0.0');
  }
  const providerHost = new URL(String(baseUrl)).hostname;
  if (providerHost === 'api.brightsky.dev') {
    const operationName = String(operation.operationId ?? '').toLowerCase();
    if (['currentsources', 'currentweather', 'weathersources', 'weatherforecast'].includes(operationName)) {
      url.searchParams.set('lat', '50.0');
      url.searchParams.set('lon', '10.0');
    }
    if (['weathersynop', 'weatherforecast'].includes(operationName)) {
      url.searchParams.set('date', new Date().toISOString().slice(0, 10));
    }
    if (operationName === 'weathersynop') url.searchParams.set('wmo_station_id', '10739');
  }

  if (providerHost === 'identity.mallne.cloud' && url.pathname.endsWith('/protocol/openid-connect/auth')) {
    const verifier = randomBytes(32).toString('base64url');
    url.searchParams.set(
      'client_id',
      String(process.env.AREAASSIST_OIDC_CLIENT_ID ?? 'areaassist'),
    );
    url.searchParams.set('redirect_uri', process.env.CODEX_OIDC_REDIRECT_URI ?? 'dcaa://areaassist.mallne.cloud/login');
    url.searchParams.set('response_type', 'code');
    url.searchParams.set('scope', 'openid');
    url.searchParams.set('state', randomBytes(18).toString('base64url'));
    url.searchParams.set('nonce', randomBytes(18).toString('base64url'));
    url.searchParams.set('code_challenge', createHash('sha256').update(verifier).digest('base64url'));
    url.searchParams.set('code_challenge_method', 'S256');
  }

  if (providerHost === 'identity.mallne.cloud' && url.pathname.endsWith('/protocol/openid-connect/token')) {
    const form = {
        grant_type: 'authorization_code',
        client_id: String(process.env.AREAASSIST_OIDC_CLIENT_ID ?? 'areaassist'),
        code: 'invalid-contract-probe',
        redirect_uri: process.env.CODEX_OIDC_REDIRECT_URI ?? 'dcaa://areaassist.mallne.cloud/login',
        code_verifier: randomBytes(32).toString('base64url'),
      };
    const response = await request.post(url.toString(), {
      form,
      timeout: 30_000,
    });
    const responseBody = await attachExchange(
      testInfo,
      'provider',
      { method: 'POST', url: url.toString(), form: { ...form, code: '<intentionally-invalid-test-code>' } },
      response,
    );
    const payload = record(JSON.parse(responseBody));
    expect(response.status(), `${title} token endpoint should reject an invalid test code with OAuth 400`).toBe(400);
    expect(payload.error, `${title} token endpoint should return an OAuth error payload`).toBeTruthy();
    return;
  }

  const response = await request.fetch(url.toString(), { method: method.toUpperCase(), timeout: 30_000 });
  const body = await attachExchange(
    testInfo,
    'provider',
    { method: method.toUpperCase(), url: url.toString() },
    response,
  );
  expect(
    response.ok(),
    `${title} ${method.toUpperCase()} ${path} provider endpoint should respond successfully (${response.status()}): ${url}; ${body}`,
  ).toBeTruthy();
}

let servedSpecs: JsonRecord[] = [];
let discoveryFailure: unknown;
try {
  servedSpecs = await discoverServedSpecs();
} catch (error) {
  discoveryFailure = error;
}
const allContracts = adapterOperations(servedSpecs);

if (discoveryFailure) {
  test('Codex discovery: load dynamically served OpenAPI definitions', async () => {
    throw discoveryFailure;
  });
}

for (const spec of servedSpecs) {
  const title = serviceTitle(spec);
  for (const [path, pathValue] of Object.entries(record(spec.paths))) {
    for (const [method, operationValue] of Object.entries(record(pathValue))) {
      if (!['get', 'post', 'put', 'patch', 'delete'].includes(method.toLowerCase())) continue;
      const operation = record(operationValue);
      const contracts = allContracts.filter(
        (contract) => contract.spec === spec && contract.path === path && contract.method === method.toLowerCase(),
      );

      test(`${title}: ${method.toUpperCase()} ${path}`, async ({ request }, testInfo) => {
        test.setTimeout(120_000);
        testInfo.annotations.push({
          type: 'service-operation',
          description: `${title} ${method.toUpperCase()} ${path}`,
        });
        await attachJson(testInfo, 'openapi-model', spec);
        await SwaggerParser.validate(spec as any);

        if (contracts.length === 0) {
          await probeUntranslatedOperation(request, spec, path, method, operation, testInfo);
          return;
        }
        for (const [contractIndex, contract] of contracts.entries()) {
          const keys = contract.options.keys as JsonRecord[];
          const references = keys
            .map((key) => key.reference)
            .filter((reference): reference is string => typeof reference === 'string' && reference.length > 0);
          expect(references.length, `${title} ${path} must map source attributes`).toBeGreaterThan(0);
          expect(
            keys.some((key) => key.identifier === 'id' && typeof key.reference === 'string'),
            `${title} ${path} must map the parcel ID state key`,
          ).toBe(true);

          // Exercise the configured provider endpoint and verify its real data
          // against the mappings declared by this individual operation.
          const providerResult = await providerFeature(request, contract, testInfo, `provider-${contractIndex + 1}`);
          if (contract.kind === 'wfs') {
            const { featureXml: xml, mappingSchemaXml, expectedFailure } = providerResult as WfsProviderResult;
            for (const reference of references) {
              expect(mappingSchemaXml, `${title} provider schema is missing mapped source attribute ${reference}`).toMatch(
                new RegExp(`name=["']${reference}["']`, 'i'),
              );
            }
            if (expectedFailure) {
              testInfo.annotations.push({ type: 'expected-provider-failure', description: expectedFailure });
              console.warn(`Expected provider failure for ${title} ${path}: ${expectedFailure}`);
              continue;
            }
            const parcelId = keys.find((key) => key.identifier === 'id')!;
            const parcelIdQName = '(?:[\\w.-]+:)?';
            expect(xml, `${title} provider returned an empty parcel ID`).toMatch(
              new RegExp(`<${parcelIdQName}${parcelId.reference}[^>]*>\\s*[^<\\s]`, 'i'),
            );
            const geometry = String(contract.plugin.geometryPointer ?? '');
            if (geometry) {
              expect(mappingSchemaXml, `${title} provider schema is missing configured geometry ${geometry}`).toMatch(
                new RegExp(`name=["']${geometry}["']`, 'i'),
              );
              expect(xml, `${title} provider response is missing configured geometry ${geometry}`).toMatch(
                new RegExp(`<${parcelIdQName}${geometry}(?:\\s[^>]*)?(?:>|\\s*/>)`, 'i'),
              );
            }
          } else {
            const feature = record(providerResult);
            const attributes = record(feature.attributes);
            for (const reference of references) {
              expect(attributes, `${title} provider response is missing mapped source attribute ${reference}`).toHaveProperty(reference);
            }
            const parcelId = keys.find((key) => key.identifier === 'id')!;
            expect(attributes[parcelId.reference], `${title} provider returned an empty parcel ID`).toBeTruthy();
            expect(feature.geometry, `${title} provider returned no geometry for the mapped feature`).toBeTruthy();
          }
        }
      });
    }
  }
}
