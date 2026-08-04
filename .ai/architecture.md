# Architecture -- AreaAssist Shared

## Purpose

Shared is the core data and integration layer for AreaAssist. It provides unified models for parcel data, spatial data format adapters (ESRI ArcGIS, OGC WFS), differential sync protocol types, and pre-configured API definitions for German GIS services.

## Tech Stack

- **Language:** Kotlin Multiplatform (KMP)
- **Serialization:** kotlinx.serialization (JSON)
- **XML:** xmlutil (for GML/WFS)
- **Build:** Gradle Kotlin DSL

## Package Structure

| Package | Contents |
|---------|----------|
| `model.parcel` | `ParcelCrate`, `ParcelPacket`, `ParcelProxy`, `PreDefined`, `KeyRecipe`, `DefaultKeys` |
| `model.sync` | `SyncPacket`, `SyncEntryDomain`, `SyncUpsert`, `SyncState`, API DTOs |
| `aviator` | Aviator GIS adapter plugin contracts, CRS handling, FES XML filter building |
| `aviator.esri` | ESRI ArcGIS (REST) adapter implementation |
| `aviator.wfs` | OGC WFS 2.0.0 adapter implementation |
| `statics` | Shared constants, serialization config, service locator enums |
| `statics.api` | Pre-configured API definitions for German states, BrightSky, map styles |
| `model.actions` | Action-related domain types |
| `model.role` | Role/permission models |
| `model.bundeslaender` | German federal state enums and metadata |
| `model.map` | Map/display configuration types |
| `model.curator` | Curator data model |

## Key Features

- **Spatial Adapters**: ESRI ArcGIS (REST) and OGC WFS 2.0.0 via Aviator plugin system
- **Parcel Data Model**: `ParcelCrate`/`ParcelPacket`/`ParcelProxy` with `PreDefined` keys and `KeyRecipe` formatting
- **Sync Protocol**: Differential sync types shared between Codex server and AreaAssist app
- **Service Registry**: Pre-configured API definitions for 10 German state GIS services
- **GeoJSON Inflation**: Three `InflationMode` strategies for extracting properties from GeoJSON features
- **Query DSL**: Unified query parameters via `ArcGisParameters` and `WfsParameters`

## Dependencies on Other Modules

- **Aviator** -- Core API framework, plugin system
- **Geokit** -- Spatial operations, GeoJSON models
- **Units** -- Physical unit types
- **Polyfill** -- Utility functions

## Non-negotiable Rules

- Models in `model.parcel` and `model.sync` are shared across the wire with Codex -- breaking changes must be coordinated
- Keep changes in `commonMain` for cross-platform compatibility
- All dependencies via `gradle/libs.versions.toml`
