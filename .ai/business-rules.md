# Business Rules -- AreaAssist Shared

Rules an agent must respect when writing code for this module.

## API Stability

- **Rule**: Models in `model.parcel` and `model.sync` are shared across the wire with the Codex server. Breaking changes must be coordinated with server-side releases.
- **Why**: Client and server must agree on data contracts. Uncoordinated changes break sync.

## Platform Portability

- **Rule**: Keep changes in `commonMain` wherever possible. Use `expect`/`actual` only for platform-specific implementations.
- **Why**: Shared is consumed by all AreaAssist targets (JVM, Android, iOS, JS, Wasm, Linux).

## Serialization

- **Rule**: All data models must use `@Serializable` from `kotlinx.serialization`.
- **Why**: Consistent serialization across all platforms and the wire protocol.

## GIS Adapters

- **Rule**: New GIS adapters must implement the Aviator plugin contracts defined in `aviator/`.
- **Why**: The adapter lifecycle (CRS handling, filter building) is managed by the Aviator plugin system.

## Edge Cases

- **GeoJSON inflation**: Three `InflationMode` strategies exist. Choose the correct one based on the source service's response format.
- **Pre-configured APIs**: Service definitions for German states are hardcoded. Changes affect all consumers.

## Overrides

- None. Shared follows root coding standards without module-specific overrides.
