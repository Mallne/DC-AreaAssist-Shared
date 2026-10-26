# Opt-in API response tests

Shared replaces its Playwright API suite with a small Kotlin/JUnit 5 live test using the same response mapping and transformation pipeline as App/Codex. It does **not** test atomic schemas.

## Run only when requested

From the monorepo root in PowerShell, confirm mise Java 25:

```powershell
mise exec -- java -version
# Ordinary offline verification; excludes all live-provider tests:
mise exec -- .\gradlew.bat --no-daemon --console=plain :shared:jvmTest
# Explicit USER-invoked external checks only:
mise exec -- .\gradlew.bat --no-daemon --console=plain :shared:liveApiContractTest
```

No Codex instance, OIDC login, browser, or npm setup is needed. External checks are **entirely opt-in**: ordinary `jvmTest`, `check`, and dependency-update verification do not run them. The live task includes only `live-provider`, always reruns, and is not attached to check, CI, or a schedule.

## What executes

`src/jvmTest/.../contract/LiveApiContractTest.kt` has one tagged `@TestFactory`. It iterates production `APIs.apis`, round-trips each document through serialization like Codex transport, and uses normal `KtorAviatorServiceConverter.build` with `Shared.Serialization.serializers()` and JSON/XML/WFS/Esri plugins. Weaver is restricted to weather services, like App; production mappings are untouched.

Cases come from each document's registered **application services**, not every documented route. No-delegate routes are not fabricated into services; documents with no application services generate no request cases. Registry/service additions or removals automatically change tests without provider lists or test-set edits.

- Parcels use the normal pipeline to `List<ParcelCrateEntity>`; require nonempty results, IDs, and geometry.
- Weather uses Aviator + Weaver to decode API responses into Shared's `WeatherReport` (defined in
  `src/commonMain/.../model/weather/WeatherContract.kt`). Tests require Weaver's mapped payload,
  decode it with the shared serializer, compare it with the response adapter's result, and verify
  a JSON round-trip. Current endpoints must supply current conditions; forecasts must supply
  nonempty hourly observations. Known WMO codes must not lose their condition during mapping.
- Other endpoints use ordinary HTTP/nonempty-body probes, matching the former Playwright scope.

There are no custom JSON/XML/XSD validators, schema walkers, DescribeFeatureType/import requests, direct Weaver-engine calls, or test-local delegates. Samples supply coordinates/date. WFS sampling follows the deleted Playwright `providerFeature` check: `InterceptionPlugin.beforeRequesting` reads the actual `WfsAdapterPluginInstance` configuration and `ParcelServiceOptions`, switches even declared POST operations to read-only GET GetFeature with an empty body, count 1, configured output CRS, and `propertyName` from `options.keys.reference` plus `geometryPointer`/`nsPrefix`. It replaces the `KtorNetworkChain` URL as well as the request, because the Ktor client uses the chain URL. Only GET/HEAD or parcel-query POST is allowed; requests time out after 30 seconds, tests after 90 seconds, and clients close in `@AfterAll`.

The former Playwright checks inspected raw XML fields; the migrated tests require actual JSON/XML/WFS adapters to produce parcel models with IDs and geometry. Keep production definitions, mappings and CRSs untouched when maintaining this harness. No CRS overrides, extra validators, per-provider lists, retries or skips are used to make tests green; production mapping fixes need separate approval.

## Reports and verification

Standard HTML reports: `areaassist/shared/build/reports/tests/{jvmTest,liveApiContractTest}/index.html`. There is no custom provider-artifact writer; stale files elsewhere under `build` are not evidence from the current suite.

Earlier offline verification on mise Java 25: `:shared:jvmTest` succeeded with **40 tests, 0 failures/errors/skips**, before the final WFS sampling edit. Offline tests were not rerun afterward; final sampling compiled and executed in the live run below.

Latest explicitly requested live verification (2026-10-26), using the command above from the monorepo root under mise Java 25, completed **BUILD FAILED** (exit 1): **22 real request cases, 13 passed, 9 failed, no skips**. XML contains 23 testcase entries because an empty Esri dynamic container adds a success entry; that entry is not a request or provider acceptance.

- All three `Weaver→sharedWeatherReport` cases passed: BrightSky `GET /current_weather` and `GET /weather`, and Open-Meteo `GET /v1/forecast`. These exercise the normal HTTP/converter/Weaver pipeline and the mapped-payload, Shared deserialization, context-result equality, JSON round-trip, observation-capability, and known-WMO assertions described above. The passing BrightSky alerts/raw probe is not WeatherReport mapping acceptance.
- Thüringen now passes with the former Playwright GET sample. Seven HTTP 200 GML responses still fail parcel mapping: MecklenburgVorpommern, Brandenburg, Hamburg, Hessen, Sachsen, Berlin, and SachsenAnhalt. Niedersachsen returns HTTP 200 maintenance HTML; NordrheinWestfalen times out after 30 seconds (earlier runs returned GML but failed mapping).
- Earlier diagnostics exposed projected collection-envelope coordinates passed to geographic `Boundary` validation (latitude must be within -90..90), and Hamburg's short EPSG identifier passed to `Identifier.deconstructUrnId` (`NoSuchElementException`). These production mapping defects remain unresolved; not all failures are harness flakiness. An initially empty CRS registry was not established as the sole cause. Raw failure messages include HTTP status and a 2,000-character body preview.

The overall suite remains **red**. No production fixes or failure masking are claimed; temporary production experiments were fully reverted. Report: `areaassist/shared/build/reports/tests/liveApiContractTest/index.html`; captured log: `C:/Users/malln/AppData/Local/Temp/opencode/parcel-playwright-sampling-live.log`. This is live contract evidence, not App device/visual acceptance; no Notary or YouTrack updates were made.

## Separate tooling

Shared's Playwright setup is removed; the user's `.env` and unrelated pnpm tools/boundary-exporter are preserved. [Aviator npm smoke tests](../../aviator/npm-distribution/README.md) remain its own Node/TypeScript package checks, with no Shared dependency.
