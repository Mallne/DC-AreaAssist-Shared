# DiCentra AreaAssist Shared

[![DiCentra](https://img.shields.io/badge/DiCentra-grey.svg)](https://code.mallne.cloud)
[![Kotlin](https://img.shields.io/badge/kotlin-grey.svg?logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)

**DiCentra AreaAssist Shared** is the foundational Kotlin Multiplatform library for the AreaAssist framework. It
provides a standardized data model for land parcels (Flurstücke) and powerful integration adapters to fetch spatial data
from various public and private GIS services.

## 🚀 Key Features

- **Multiplatform Core:** Shared logic and models for Android, iOS, JVM, JS, WasmJS, and Linux.
- **Unified Parcel Model:** A robust domain model that abstracts away the differences between various GIS data
  providers.
- **Aviator Integration:** Leverages the [Aviator](https://github.com/mallne/DC-Aviator) framework for flexible, staged
  data fetching and transformation.
- **Spatial Adapters:** Built-in support for **ESRI ArcGIS REST API** and **OGC Web Feature Service (WFS)**.

---

## 🧩 Aviator Plugins

AreaAssist Shared uses the Aviator plugin system to handle the complexities of different GIS protocols. These plugins
act as "interceptors" and "transformers" in the data fetching pipeline.

### 📍 EsriAdapterPlugin

The `EsriAdapterPlugin` is designed to interact with ESRI ArcGIS Feature Servers.

- **Capabilities:**
    - Automatic conversion of `AreaAssistParameters` (like boundaries and queries) into ESRI-specific `where` clauses
      and `geometry` envelopes.
    - Handles coordinate system transformations (defaults to EPSG:4326).
    - **ExceededTransferLimit Handling:** Automatically detects when a service returns partial results due to transfer
      limits and throws a specialized exception.
    - **GeoJSON Inflation:** Efficiently parses ESRI GeoJSON responses into high-level `ParcelCrateEntity` objects.

### 🌐 WfsAdapterPlugin

The `WfsAdapterPlugin` provides support for the OGC Web Feature Service (WFS 2.0.0) standard, commonly used by European
public "Open Data" portals.

- **Capabilities:**
    - **XML/GML Support:** Generates complex `GetFeature` POST requests with Filter Encoding (FES).
    - **CRS Management:** Integrated with `Geokit` to handle on-the-fly coordinate transformations between the client (
      usually WGS84) and the service (e.g., ETRS89 / UTM).
    - **Flexible Configuration:** Supports custom `typeNames`, namespaces, and geometry pointers.

---

## 📋 Predefined Values (`PreDefined`)

To ensure interoperability between different data sources (e.g., Thuringia vs. Saxony), AreaAssist uses a set of *
*PreDefined** keys. These keys act as "semantic anchors" for parcel data.

When configuring a service, you map the provider's specific field names (references) to these predefined identifiers.

| Key                | Description                                                     |
|:-------------------|:----------------------------------------------------------------|
| `PARCELID`         | Unique identifier for the parcel (e.g., Flurstückskennzeichen). |
| `PLOT`             | The human-readable parcel number (e.g., 123/4).                 |
| `PLOT_NUMERATOR`   | The "Zähler" of the parcel number.                              |
| `PLOT_DENOMINATOR` | The "Nenner" of the parcel number.                              |
| `AREA`             | The official area size (usually in m²).                         |
| `DISTRICT`         | The "Gemarkung" (Cadastral district).                           |
| `DISTRICT_ID`      | The official ID of the Gemarkung.                               |
| `LOCATION`         | Descriptive location/address (Lagebezeichnung).                 |
| `USAGE`            | Information about the land use (Nutzungsart).                   |
| `USAGE_BUILDINGS`  | Boolean flag indicating if the parcel is built upon.            |

**Example Mapping (Thuringia):**

```kotlin
DefaultKeys.fillIn(
    parcelId = "flstkennz",
    area = "flaeche",
    district = "gemarkung",
    plotNumerator = "flstnrzae",
    plotDenominator = "flstnrnen"
)
```

---

## 🛠 Tech Stack

- **Data:** `kotlinx.serialization` (JSON & XML), `MapLibre SpatialK`
- **Network:** `Ktor` Client
- **Spatial:** `Mallne Geokit` (Coordinates, Projections, GeoJSON)
- **Integration:** `DiCentra Aviator`

## 📦 Installation

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://registry.mallne.cloud/repository/DiCentraArtefacts/")
}

dependencies {
    implementation("cloud.mallne.dicentra.areaassist:shared:1.0.0-SNAPSHOT")
}
```

---

## ⚖️ License

Licensed under the [Apache License, Version 2.0](LICENSE).

---
<p align="center">
  Built with ❤️ by Mallne under the DiCentra umbrella
</p>