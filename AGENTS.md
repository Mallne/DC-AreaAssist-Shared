# AreaAssist Shared

**Stack**: KMP library. Parcel data model, sync protocol, GIS adapters.

> **Full docs**: [areaassist/shared/.ai/](.ai/) | [Notary](https://docs.mallne.cloud/doc/shared-rZXRBbfbPz)

## Critical Rules

1. Models in `model.parcel` and `model.sync` are wire-shared with Codex -- breaking changes require coordination
2. Keep changes in `commonMain` for cross-platform compatibility
3. All data models must use `@Serializable` from kotlinx.serialization
4. New GIS adapters must implement Aviator plugin contracts
5. Always import classes; do not use fully qualified class references in code bodies, including tests

## Build

```bash
./gradlew build
./gradlew publishToMavenLocal
```

## Doc Log

### Weather — current summary (2026-10-26)

- User-authoritative `SharedParcelConstants.endpointVersion` is **1.0.2-b** (`SemVer(1, 0, 2, "b")`); no speculative version jumps. Package and endpoint versions are distinct; Codex `Builtin.kt`'s `autoReleaseVersion` strips prerelease/build metadata for builtin publishing. Cache policy tests use synthetic versions independent of bundled versions.
- Coverage metadata selects BrightSky for Germany and Open-Meteo globally, including Paris. `BrightSkyWeatherSchemas` current arithmetic uses `Wol.divideCurrent`/`multiplyCurrent`; forecast reads `weather[index].field` row-first and passes individual rows to `hourlyMeasurements`.
- `CurrentConditions`/`HourlyForecast` default conditions derive from `measurements.wmoWeatherCode` through `WeatherCondition.fromWmo`; explicit conditions win and null/unrecognized codes remain Unknown. `WeatherReport` resolves offset-free timestamps with `report.context.timezone`, never device timezone; explicit offsets are preserved and missing report timezone is rejected. Unknown precipitation periods are not hourly.
- Canonical weather contracts already live in Shared `commonMain/.../model/weather/WeatherContract.kt`: `WeatherReport`, `CurrentConditions`, `HourlyForecast`, `WeatherMeasurements`, and `WeatherCondition`. Do not move or duplicate them into App; legacy UI/FWI adapters remain App-owned. Unused App `WeatherLocation.state` and its serializer were removed entirely, with no `AdministrativeArea` replacement; the temporary state test was deleted before final compilation.
- Latest deterministic verification and device/live limits are recorded once in [App weather summary](../app/AGENTS.md#weather--current-summary-2026-10-26).

### Opt-in contract-test migration (separate earlier request)

- 2026-10-23 — Shared's response checks are one small `src/jvmTest/.../contract/LiveApiContractTest.kt`, using the App/Codex mapping/transformation pipeline, not atomic schema tests. Its tagged `@TestFactory` discovers registered application services from production `APIs.apis`; actual Aviator plugins/Geokit handle WFS. No custom validators, schema walkers, or fabricated delegates. See [.ai/commands.md](.ai/commands.md) and [response-test guide](README-api-contract-tests.md).
- External checks are entirely opt-in via `:shared:liveApiContractTest`, never ordinary check/dependency verification, CI, or schedules. Latest explicitly requested live verification on 2026-10-26 under root mise Java 25: **BUILD FAILED** (exit 1), **22 real requests, 13 passed, 9 failed, no skips**. Thüringen now passes with the migrated Playwright GET sample; all three `Weaver→sharedWeatherReport` cases still pass. Seven providers returned HTTP 200 GML but no parcel models; Niedersachsen returned maintenance HTML and NordrheinWestfalen timed out. See [response-test guide](README-api-contract-tests.md#reports-and-verification). No production mapping fix or device/visual acceptance is claimed.
- Parcel sampling follows the deleted Playwright `providerFeature` check: test-only interception uses actual WFS configuration/options for read-only GET GetFeature, count 1, configured output CRS and reference/geometry properties, with a 30-second request timeout. Tests assert real model transformation, not just XML fields. Production definitions, mappings and CRSs remain untouched; do not mask failures with overrides, retries, skips, provider lists or extra validators. Production mapping fixes require separate approval.
- Weather assertions strengthen the existing single tagged dynamic factory, without provider lists, new frameworks, or per-document test classes. Dynamic weather names explicitly identify `Weaver→sharedWeatherReport`. `requestContextful<WeatherReport, NoBody>` uses the actual converter and installed Weaver in the normal pipeline; it requires `context.bundle[WeaverPlugin.RESULT]`, decodes that Weaver-mapped `JsonElement` with Shared serialization, compares it with `context.result`, and JSON-round-trips the shared report. Current endpoints require `Available<CurrentConditions>`; forecasts require nonempty `Available<List<HourlyForecast>>`. Known WMO codes must not become Unknown conditions. No separately executed Weaver engine or atomic-schema JSON/XML validator is used; [response-test guide](README-api-contract-tests.md) describes these checks.
- Earlier offline verification from root under mise Java 25: `mise exec -- .\gradlew.bat --no-daemon --console=plain :shared:jvmTest :app:composeApp:compileKotlinJvm` succeeded after `WeatherLocation` cleanup and initial live assertions. Subsequent `:shared:jvmTest` passed **40 offline tests, 0 failures/errors/skips**, before the final sampling edit; it was not rerun afterward. The final sampling code compiled and executed in the live run above. No device installation or version change; the existing App public-doc/ticket pending marker remains authoritative.
- Shared Playwright setup is removed; the user's `.env` and unrelated pnpm tools/boundary exporter are preserved. Aviator standalone Node/TypeScript smoke remains separate. Published [response-test guide](https://docs.mallne.cloud/doc/opt-in-api-response-tests-appcodex-pipeline-eDXvmSek5M); YouTrack integration is read-only, so no ticket update was performed.

- 2026-08-13 — `DataTypeE` gained additive enum value `DEFAULT_LOCATION` (wire-shared with Codex, non-breaking; existing values unchanged) for the AreaAssist app's offline-first default location (YouTrack AA-204). Documented on Notary: "Default Location (offline fallback)" (https://docs.mallne.cloud/doc/default-location-offline-fallback-xjxl994Dbr).
