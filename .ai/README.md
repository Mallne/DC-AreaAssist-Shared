# `.ai/` -- Agent Documentation for AreaAssist Shared

This folder is the AI-agent onboarding guide for the Shared submodule.

## Prerequisites

Read these root `.ai/` articles first:

- `../../.ai/coding-standards.md` -- shared lint/format conventions
- `../../.ai/testing-guidelines.md` -- shared test strategy
- `../../.ai/glossary.md` -- shared domain terms (see Parcel, Sync, GeoJSON, WFS)

## Index

| Article | Contents |
|---------|----------|
| [architecture.md](architecture.md) | Data models, GIS adapters, sync protocol |
| [business-rules.md](business-rules.md) | Model stability, API compatibility |
| [commands.md](commands.md) | Scripts, env vars, local dev setup |

## Published Documentation

- [Shared Hub](https://docs.mallne.cloud/doc/shared-rZXRBbfbPz)
- [Parcel Data Model](https://docs.mallne.cloud/doc/parcel-data-model-4DjwLp5Ujp)
- [Sync Protocol Types](https://docs.mallne.cloud/doc/sync-protocol-types-OvsyhXHbHf)
- [Aviator GIS Adapters](https://docs.mallne.cloud/doc/aviator-gis-adapters-U9PDL0LGMc)
- [ESRI ArcGIS Adapter](https://docs.mallne.cloud/doc/esri-arcgis-adapter-IWqCPl9gGG)
- [OGC WFS 2.0.0 Adapter](https://docs.mallne.cloud/doc/ogc-wfs-200-adapter-WHjGgIdZ2N)
- [Static API Definitions](https://docs.mallne.cloud/doc/static-api-definitions-service-registry-fWUNpNjKyI)

## Maintenance

- Update the relevant article on **every** edit that touches architecture, structure, commands, or rules.
- Whenever you discover an **inconsistency** between the docs and the code, fix it here.
- Do not duplicate content that lives in `../../.ai/` -- reference it instead.
