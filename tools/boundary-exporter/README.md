# Boundary Coverage Tool

A small internal TypeScript tool for generating and raycasting against country and German state coverage models. It lives in the existing Shared pnpm workspace and does not change the Kotlin library or its contract-test package.

## Run locally

```sh
# Run from areaassist/shared
pnpm install
pnpm --filter @dicentra/boundary-exporter dev
```

Open the local URL printed by Vite. This tool is for local/internal use, not deployment. Map and boundary data are bundled locally; no tile server, API key, remote font, or account is used.

## Refresh bundled boundaries

`pnpm --filter @dicentra/boundary-exporter prepare:data` downloads and filters Natural Earth 5.1.2 1:110m countries and the low-resolution Germany states file pinned to commit `4090d4e1f89c1184b436b3d9ccaf332b4c5b43d2`. It checks all 16 Bundesland IDs and writes SHA-256 checksums in `public/data/manifest.json`. Runtime needs only the generated local assets. Data sources are generalized, can omit small features, and may reflect disputed boundary conventions; they are not legal/cadastral products. Notices are in `public/data/LICENSES/`.

## Coverage and raycasting

The primary download is a Kotlin object in the `cloud.mallne.dicentra.areaassist.model.areas` package, ready to copy into `model/areas/`. Country models implement `AdministrativeArea`; German-state models implement `StateDefinition`, including its ISO identifier, so they can replace the corresponding state definition. Both use GeoKit's serializable GeoJSON `MultiPolygon` directly, preserving separate polygons and hole rings. Parcel service metadata serializes this same model under `ParcelServiceOptions.boundary` with `boundaryFormatVersion = 2`.

At 100%, the exported model uses the exact bundled source geometry. Below 100%, fidelity sets a point budget. The simplifier removes inward-notch vertices first, filling notches rather than cutting across the source. If the budget is lower than the source hull can safely achieve, it builds a compact minimum-area enclosing k-gon. Coarser models can include more outside area and omit hole semantics; some shapes may use more points than the requested budget when no safe simplification is available. Map clicks test the resulting model with a ray-cast point-in-polygon check, including holes and multiple polygons. Boundaries are generalized and are not legal or cadastral geometry.

Codex's built-in discovery response returns all built-in OpenAPI service definitions together; the parcel-service definitions each carry one state boundary. Auto-select therefore evaluates the aggregate set of bundled state geometries and chooses the knee in total serialized geometry size versus added covered area. It applies that shared fidelity to the selected state. The displayed bundle size measures serialized boundary geometries, not the surrounding OpenAPI document overhead. Country-only selections are evaluated as a single geometry because they are not part of the built-in parcel-service bundle.

MapLibre GL JS is the sole runtime dependency and renders bundled GeoJSON over a local background style (no basemap requests). Vite and TypeScript are development/build dependencies only. The MapLibre worker is bundled locally by Vite; no CDN assets are used.

The package-level `.npmrc` disables dependency install scripts; the selected dependencies use their shipped platform binaries and do not need lifecycle build steps.

```sh
# Run from areaassist/shared
pnpm --filter @dicentra/boundary-exporter test
pnpm --filter @dicentra/boundary-exporter build
```
