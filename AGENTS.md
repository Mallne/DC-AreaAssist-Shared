# DiCentra AreaAssist Shared

## Project Overview
**DiCentra AreaAssist Shared** is a Kotlin Multiplatform (KMP) library that serves as the core data and integration layer for the AreaAssist application framework. It provides a unified model for handling geographical parcel data, supporting various spatial data formats and sources including ESRI ArcGIS and OGC WFS.

The project is designed to be highly portable, targeting JVM, Android, iOS, JS, WasmJS, and Linux. It leverages the "Aviator" data integration framework to abstract data fetching and "Geokit" for spatial operations.

## Published Documentation
The Shared module is fully documented in the DiCentra documentation hub (Notary server) under the AreaAssist parent collection:

| Doc | URL |
|-----|-----|
| **Shared** (parent hub) | `https://docs.mallne.cloud/doc/shared-rZXRBbfbPz` |
| **Parcel Data Model** | `https://docs.mallne.cloud/doc/parcel-data-model-4DjwLp5Ujp` |
| **Sync Protocol Types** | `https://docs.mallne.cloud/doc/sync-protocol-types-OvsyhXHbHf` |
| **Aviator GIS Adapters** (hub) | `https://docs.mallne.cloud/doc/aviator-gis-adapters-U9PDL0LGMc` |
| └ **ESRI ArcGIS Adapter** 🗺️ | `https://docs.mallne.cloud/doc/esri-arcgis-adapter-IWqCPl9gGG` |
| └ **OGC WFS 2.0.0 Adapter** 🌐 | `https://docs.mallne.cloud/doc/ogc-wfs-200-adapter-WHjGgIdZ2N` |
| **Static API Definitions & Service Registry** | `https://docs.mallne.cloud/doc/static-api-definitions-service-registry-fWUNpNjKyI` |

## Key Features
- **Spatial Adapters:** Built-in support for ESRI ArcGIS (through AreaAssist ArcGIS REST adapter) and WFS 2.0.0 (through OGC WFS adapter) via the Aviator plugin system.
- **Parcel Data Model:** Comprehensive domain models (`ParcelCrate`, `ParcelPacket`, `ParcelProxy`) with `PreDefined` keys, `KeyRecipe` formatting, and a `DefaultKeys` catalog.
- **Sync Protocol:** Differential sync data model (`SyncPacket` hierarchy, `SyncEntry` domain, API contracts) shared between the Codex server and the AreaAssist app.
- **Service Registry:** Pre-configured API definitions for 10 German state GIS services (ESRI ArcGIS + OGC WFS), BrightSky weather, OSM map styles, and associated service option models.
- **GeoJSON Inflation:** Three `InflationMode` strategies for extracting domain-specific properties from GeoJSON features.
- **Query DSL:** Unified query parameters for spatial and attribute-based searches via `ArcGisParameters` and `WfsParameters`.

## Package Structure
- `cloud.mallne.dicentra.areaassist.model.parcel` — Parcel data model: `ParcelCrate`, `ParcelPacket`, `ParcelProxy`, `PreDefined`, `ParcelKey`, `KeyRecipe`, `DefaultKeys`
- `cloud.mallne.dicentra.areaassist.model.sync` — Differential sync protocol: `SyncPacket`, `SyncEntryDomain`, `SyncUpsert`, `SyncState`, `SyncStrategy`, API DTOs, `RejectionReason`
- `cloud.mallne.dicentra.areaassist.aviator` — Aviator GIS adapter plugin contracts: `AreaAssistParameters`, ESRI adapter lifecycle, WFS adapter lifecycle, CRS handling, FES XML filter building
- `cloud.mallne.dicentra.areaassist.aviator.esri` — ESRI ArcGIS (REST) adapter implementation
- `cloud.mallne.dicentra.areaassist.aviator.wfs` — OGC WFS 2.0.0 adapter implementation
- `cloud.mallne.dicentra.areaassist.statics` — Shared constants, serialization config, service locator enums
- `cloud.mallne.dicentra.areaassist.statics.api` — Pre-configured API definitions for German states, BrightSky weather, map styles
- `cloud.mallne.dicentra.areaassist.model.actions` — Action-related domain types
- `cloud.mallne.dicentra.areaassist.model.role` — Role/permission models
- `cloud.mallne.dicentra.areaassist.model.bundeslaender` — German federal state enums and metadata
- `cloud.mallne.dicentra.areaassist.model.map` — Map/display configuration types
- `cloud.mallne.dicentra.areaassist.model.curator` — Curator data model

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

## Development Conventions
- **Version Catalog:** All dependencies must be managed via `gradle/libs.versions.toml`.
- **Monorepo Context:** The project consumes sibling modules (`polyfill`, `aviator`, `geokit`, `units`) via `includeBuild` when inside the monorepo; externally these are published as Maven artifacts.
- **Surgical Updates:** When modifying models, ensure compatibility across all targets by keeping changes in `commonMain` wherever possible.
- **Documentation Reconciliation:** After publishing changes to the Notary docs (see Published Documentation above), update this file to reflect any structural changes.
- **API Stability:** Models in `model.parcel` and `model.sync` are shared across the wire with Codex — breaking changes must be coordinated with server-side releases.
