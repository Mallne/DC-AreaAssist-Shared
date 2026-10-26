# Commands and Environment -- AreaAssist Shared

## Scripts

```bash
# Build all targets
./gradlew build

# Publish to Maven Local
./gradlew publishToMavenLocal

# Check for dependency updates
./gradlew versionCatalogUpdate

# Run tests
./gradlew test
```

## Local Dev Setup

1. Ensure JDK 17+ is installed
2. Run `./gradlew build` from the `areaassist/shared/` directory
3. Run `./gradlew publishToMavenLocal` to make available for local consumption

## Opt-in API Response Tests

From the monorepo root, use the pinned mise Java 25 runtime (verify `mise exec -- java -version` first):

```powershell
# Ordinary offline verification:
mise exec -- .\gradlew.bat --no-daemon --console=plain :shared:jvmTest
# External checks only when explicitly requested:
mise exec -- .\gradlew.bat --no-daemon --console=plain :shared:liveApiContractTest
```

- `:shared:jvmTest` excludes `live-provider`. External checks are entirely opt-in: normal `check` and dependency-upgrade verification make no live-test calls. `:shared:liveApiContractTest` includes only that tag, always reruns, and is not hooked into check, CI, or schedules.
- One `LiveApiContractTest.kt` tagged `@TestFactory` discovers registered **application services** per production `APIs.apis` document, not every provider endpoint. No provider allowlist, provider-specific tests, or test-local delegates.
- Documents JSON-round-trip as in Codex, then use normal `KtorAviatorServiceConverter.build` with `Shared.Serialization.serializers()` and actual JSON/XML/WFS/Esri plugins. Weaver is scoped to weather, as in App. No manual Ktor service assembly or direct Weaver engine calls.
- Assess response mapping/transformation only: actual WFS plugin/Geokit handles parcels; typed `List<ParcelCrateEntity>` must be nonempty with IDs and geometry. Weather passes through the actual pipeline to `WeatherReport` with available observations. Other services use HTTP-success/nonempty-body probes, matching former Playwright scope. No atomic schema testing, custom validators, XML parsers, XSD walkers/import fetchers, or schema-validation layer.
- Common coordinates/date inputs and the existing interception plugin provide the former Playwright read-only GET GetFeature sample: count 1, configured output CRS and reference/geometry properties from actual WFS configuration/options. Requests time out at 30 seconds, tests at 90 seconds; resources close in `@AfterAll`. Production definitions/mappings/CRSs are not changed for test acceptance.
- Only standard HTML/XML test reports; HTML: `areaassist/shared/build/reports/tests/{jvmTest,liveApiContractTest}/index.html` from the monorepo root. No custom artifact writer. See [AGENTS.md](../AGENTS.md) and [response-test guide](../README-api-contract-tests.md#reports-and-verification) for current verification and its limits.

## Environment Variables

| Variable | Purpose | Default |
|----------|---------|---------|
| `dc.username` | Maven registry username (for publish) | Required for publish |
| `dc.password` | Maven registry password (for publish) | Required for publish |

## Runtime Notes

- When inside the monorepo, sibling modules (`polyfill`, `aviator`, `geokit`, `units`) are consumed via `includeBuild`.
- Externally, these must be published as Maven artifacts.
