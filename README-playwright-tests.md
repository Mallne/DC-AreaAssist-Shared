# Aviator definition contract tests

These Playwright API tests fetch **all** built-in Aviator definitions from Codex and validate each document as OpenAPI. They dynamically find every active WFS/Esri translation operation, read its current path, plugin configuration, and parcel-key references from the served spec, then query its concrete provider endpoint and verify mapped source attributes. They do not keep a second copy of any service's field mappings.

Copy `.env.example` to `.env` in this directory and set all test connection, OIDC, and SuperAdmin test-user values there. Use the issuer and client ID for your test identity-provider realm, and set `CODEX_OIDC_REDIRECT_URI` to the exact URI registered for that client. Playwright loads only `areaassist/shared/.env` automatically (existing process environment values take precedence); this module does not read configuration from the independent server repository. Run the suite against a local or otherwise non-production Codex test instance. `CODEX_BASE_URL` defaults to `http://127.0.0.1:8081`; `CODEX_OIDC_SCOPE` defaults to `openid`. No pre-issued bearer token or Codex client secret is used.

```sh
pnpm install
npx playwright install
pnpm run test:e2e
pnpm run check:types
```
