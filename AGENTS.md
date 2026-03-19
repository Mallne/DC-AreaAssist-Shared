# DiCentra AreaAssist Shared

## Project Overview
**DiCentra AreaAssist Shared** is a Kotlin Multiplatform (KMP) library that serves as the core data and integration layer for the AreaAssist application framework. It provides a unified model for handling geographical parcel data, supporting various spatial data formats and sources including ESRI ArcGIS and OGC WFS.

The project is designed to be highly portable, targeting JVM, Android, iOS, JS, WasmJS, and Linux. It leverages the "Aviator" data integration framework to abstract data fetching and "Geokit" for spatial operations.

## Key Features
- **Spatial Adapters:** Built-in support for ESRI ArcGIS and WFS through the Aviator plugin system.
- **Parcel Data Model:** Comprehensive domain models for land parcels, including properties, landmarks, and usage.
- **GeoJSON Inflation:** Strategies for extracting domain-specific properties from GeoJSON features.
- **Query DSL:** Unified query parameters for spatial and attribute-based searches.

## Technologies & Architecture
- **Language:** Kotlin 2.3.20 (Kotlin Multiplatform)
- **Target Platforms:** JVM 21, Android, iOS, JS, WasmJS, Linux.
- **Data Handling:**
  - `kotlinx.serialization` (JSON, XML)
  - `MapLibre SpatialK` (GeoJSON handling)
- **Integration Frameworks:**
  - `DiCentra Aviator`: Orchestration of data adapters and clients.
  - `Mallne Geokit`: Low-level spatial and coordinate calculations.
- **Networking:** `Ktor` Client for cross-platform HTTP requests.

## Building and Running
The project uses the Gradle Kotlin DSL.

- **Build all targets:**
  ```bash
  ./gradlew build
  ```
- **Local installation (Maven Local):**
  ```bash
  ./gradlew publishToMavenLocal
  ```
- **Dependency Updates:**
  ```bash
  ./gradlew versionCatalogUpdate
  ```

### TODO:
- [ ] Add automated tests (`commonTest`).
- [ ] Document specific configuration for ESRI/WFS adapters in `GEMINI.md`.

## Project Structure
- `src/commonMain/kotlin/.../aviator`: Adapters and integration logic for data sources.
- `src/commonMain/kotlin/.../model`: Domain entities (`Parcel`, `Point`, `Landmark`, etc.).
- `src/commonMain/kotlin/.../statics`: Shared constants and serialization configurations.
- `OpenData.http`: Example REST API requests for ESRI and WFS services.

## Development Conventions
- **Version Catalog:** All dependencies must be managed via `gradle/libs.versions.toml`.
- **Monorepo Context:** The project is designed to optionally include other DiCentra modules (`polyfill`, `aviator`, `units`, `geokit`) via `includeBuild` if they are present in the parent directory.
- **Surgical Updates:** When modifying models, ensure compatibility across all targets by keeping changes in `commonMain` wherever possible.
