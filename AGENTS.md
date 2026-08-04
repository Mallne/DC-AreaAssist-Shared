# AreaAssist Shared

**Stack**: KMP library. Parcel data model, sync protocol, GIS adapters.

> **Full docs**: [areaassist/shared/.ai/](.ai/) | [Notary](https://docs.mallne.cloud/doc/shared-rZXRBbfbPz)

## Critical Rules

1. Models in `model.parcel` and `model.sync` are wire-shared with Codex -- breaking changes require coordination
2. Keep changes in `commonMain` for cross-platform compatibility
3. All data models must use `@Serializable` from kotlinx.serialization
4. New GIS adapters must implement Aviator plugin contracts

## Build

```bash
./gradlew build
./gradlew publishToMavenLocal
```
