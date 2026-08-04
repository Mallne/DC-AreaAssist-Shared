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

## Environment Variables

| Variable | Purpose | Default |
|----------|---------|---------|
| `dc.username` | Maven registry username (for publish) | Required for publish |
| `dc.password` | Maven registry password (for publish) | Required for publish |

## Runtime Notes

- When inside the monorepo, sibling modules (`polyfill`, `aviator`, `geokit`, `units`) are consumed via `includeBuild`.
- Externally, these must be published as Maven artifacts.
