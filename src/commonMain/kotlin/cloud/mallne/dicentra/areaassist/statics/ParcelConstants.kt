package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPluginConfig
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPluginConfig
import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.KeyFormat
import cloud.mallne.dicentra.areaassist.model.parcel.KeyIcon
import cloud.mallne.dicentra.areaassist.model.parcel.KeyType
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelKey
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.PreDefined
import cloud.mallne.dicentra.areaassist.model.parcel.UnitFormat
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.io.Schema
import cloud.mallne.dicentra.aviator.koas.parameters.Parameter
import cloud.mallne.dicentra.aviator.model.SemVer
import cloud.mallne.units.Area
import cloud.mallne.units.Units
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

object ParcelConstants {
    val endpointVersion = SemVer(1, 0, 2, "c")
    val locator = APIs.Services.PARCEL_SERVICE.locator(
        ServiceMethods.GATHER
    )

    object DefaultKeys {
        val PLOT = ParcelKey(
            identifier = "plot",
            translations = PreDefined.PLOT.toTranslatable(),
            hideInUI = true, //The Plot gets handled differently
            icon = KeyIcon.ShareLocation
        )
        val PLOT_NUMERATOR = ParcelKey(
            identifier = "plot.numerator",
            translations = PreDefined.PLOT_NUMERATOR.toTranslatable(),
            hideInUI = true, //The Plot gets handled differently
            icon = KeyIcon.ShareLocation
        ) //plotNumerator/Nenner
        val PLOT_SEPARATOR = ParcelKey(
            identifier = "plot.separator",
            translations = PreDefined.PLOT_SEPARATOR.toTranslatable(),
            hideInUI = true, //The Plot gets handled differently
            icon = KeyIcon.ShareLocation
        ) //plotSeparator/Teiler
        val PLOT_DENOMINATOR = ParcelKey(
            identifier = "plot.denominator",
            translations = PreDefined.PLOT_DENOMINATOR.toTranslatable(),
            hideInUI = true, //The Plot gets handled differently
            icon = KeyIcon.ShareLocation
        )//plotDenominator/Zähler
        val AREA = ParcelKey(
            identifier = "area",
            type = KeyType.NUMBER,
            translations = PreDefined.AREA.toTranslatable(),
            format = KeyFormat(
                unit = UnitFormat(
                    unitSystem = Units.Companion.SISystems.AREA,
                    ingestUnit = Area.Companion.UnitStore.SQUAREMETERS.unit.databasePrimitive!!
                )
            ),
            icon = KeyIcon.ZoomOutMap
        ) //area
        val DISTRICT = ParcelKey(
            identifier = "district",
            translations = PreDefined.DISTRICT.toTranslatable(),
            icon = KeyIcon.LocationCity
        ) //district/Gemarkung
        val DISTRICT_ID = ParcelKey(
            identifier = "district.id",
            translations = PreDefined.DISTRICT_ID.toTranslatable()
        ) // districtKey/Gemarkungsschlüssel
        val DISTRICT_COMPARTMENT = ParcelKey(
            identifier = "district.compartment",
            translations = PreDefined.DISTRICT_COMPARTMENT.toTranslatable()
        ) //_/Flur
        val DISTRICT_COMPARTMENT_ID = ParcelKey(
            identifier = "district.compartment.id",
            translations = PreDefined.DISTRICT_COMPARTMENT_ID.toTranslatable()
        ) //areaNumber/Flurschlüssel
        val DISTRICT_MUNICIPALITY = ParcelKey(
            identifier = "district.municipality",
            translations = PreDefined.DISTRICT_MUNICIPALITY.toTranslatable()
        )//_/Gemeinde
        val DISTRICT_MUNICIPALITY_ID = ParcelKey(
            identifier = "district.municipality.id",
            translations = PreDefined.DISTRICT_MUNICIPALITY_ID.toTranslatable()
        ) //areaCode/Gemeindeschlüssel
        val DISTRICT_REGION = ParcelKey(
            identifier = "district.region",
            translations = PreDefined.DISTRICT_REGION.toTranslatable()
        )//_/Kreis
        val DISTRICT_REGION_ID = ParcelKey(
            identifier = "district.region.id",
            translations = PreDefined.DISTRICT_REGION_ID.toTranslatable()
        )//_/Kreisschlüssel
        val LOCATION = ParcelKey(
            identifier = "location",
            translations = PreDefined.LOCATION.toTranslatable(),
            icon = KeyIcon.Flag
        )//locationDesignation/Lagebezeichnung
        val LANDREGISTERNUMBER = ParcelKey(
            identifier = "landRegisterNumber",
            translations = PreDefined.LANDREGISTERNUMBER.toTranslatable(),
            icon = KeyIcon.MenuBook
        )//landRegisterNumber/Grundbuchnummer
        val USAGE = ParcelKey(
            identifier = "usage",
            type = KeyType.NOTHING,
            translations = PreDefined.USAGE.toTranslatable(),
            icon = KeyIcon.Info
        )//_/Nutzung (von der API)
        val USAGE_HINT = ParcelKey(
            identifier = "usage.hint",
            translations = PreDefined.USAGE_HINT.toTranslatable(),
            readonly = true
        )//_/Nutzung (von der API)
        val USAGE_BUILDINGS = ParcelKey(
            identifier = "usage.buildings",
            type = KeyType.BOOLEAN,
            translations = PreDefined.USAGE_BUILDINGS.toTranslatable(),
            icon = KeyIcon.House
        )//buildings/Bebauung
        val USAGE_LEGALDEVIATION = ParcelKey(
            identifier = "usage.legalDeviation",
            type = KeyType.BOOLEAN,
            translations = PreDefined.USAGE_LEGALDEVIATION.toTranslatable()
        )//_/Abweichender Rechtszustand
        val PARCELID = ParcelKey(
            identifier = "id",
            translations = PreDefined.PARCELID.toTranslatable(),
            readonly = true,
            icon = KeyIcon.Fingerprint
        )//_/Abweichender Rechtszustand

        fun fillIn(
            plot: String? = null,
            plotNumerator: String? = null,
            plotDenominator: String? = null,
            plotSeparator: String? = null,
            area: String? = null,
            district: String? = null,
            districtId: String? = null,
            districtCompartment: String? = null,
            districtCompartmentId: String? = null,
            districtMunicipality: String? = null,
            districtMunicipalityId: String? = null,
            districtRegion: String? = null,
            districtRegionId: String? = null,
            location: String? = null,
            landRegisterNumber: String? = null,
            usage: String? = null,
            usageHint: String? = null,
            usageBuildings: String? = null,
            usageLegalDeviation: String? = null,
            parcelId: String? = null,
        ): List<ParcelKey> = listOf(
            PLOT.copy(reference = plot),
            PLOT_NUMERATOR.copy(reference = plotNumerator),
            PLOT_SEPARATOR.copy(reference = plotSeparator),
            PLOT_DENOMINATOR.copy(reference = plotDenominator),
            AREA.copy(reference = area),
            DISTRICT.copy(reference = district),
            DISTRICT_ID.copy(reference = districtId),
            DISTRICT_COMPARTMENT.copy(reference = districtCompartment),
            DISTRICT_COMPARTMENT_ID.copy(reference = districtCompartmentId),
            DISTRICT_MUNICIPALITY.copy(reference = districtMunicipality),
            DISTRICT_MUNICIPALITY_ID.copy(reference = districtMunicipalityId),
            DISTRICT_REGION.copy(reference = districtRegion),
            DISTRICT_REGION_ID.copy(reference = districtRegionId),
            LOCATION.copy(reference = location),
            LANDREGISTERNUMBER.copy(reference = landRegisterNumber),
            USAGE.copy(reference = usage),
            USAGE_HINT.copy(reference = usageHint),
            USAGE_BUILDINGS.copy(reference = usageBuildings),
            USAGE_LEGALDEVIATION.copy(reference = usageLegalDeviation),
            PARCELID.copy(reference = parcelId),
        )
    }

    val esriAdapterConfig =
        Serialization().encodeToJsonElement(mapOf(EsriAdapterPlugin.identity to EsriAdapterPluginConfig(active = true)))

    fun wfsAdapterConfig(config: WfsAdapterPluginConfig.() -> Unit): JsonElement {
        val c = WfsAdapterPluginConfig(true)
        c.config()
        return Serialization().encodeToJsonElement(mapOf(WfsAdapterPlugin.IDENTITY to c))
    }

    object Path {
        val esriParams = mapOf(
            EsriAdapterPlugin.Parameters.WHERE to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.WHERE,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            EsriAdapterPlugin.Parameters.OUT_FIELDS to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.OUT_FIELDS,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            EsriAdapterPlugin.Parameters.GEOMETRY to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.GEOMETRY,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            EsriAdapterPlugin.Parameters.RETURN_GEOMETRY to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.RETURN_GEOMETRY,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            EsriAdapterPlugin.Parameters.OUT_SR to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.OUT_SR,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            EsriAdapterPlugin.Parameters.IN_SR to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.IN_SR,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            EsriAdapterPlugin.Parameters.GEOMETRY_TYPE to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.GEOMETRY_TYPE,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            EsriAdapterPlugin.Parameters.SPATIAL_REL to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.SPATIAL_REL,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            EsriAdapterPlugin.Parameters.FILE to ReferenceOr.value(
                Parameter(
                    EsriAdapterPlugin.Parameters.FILE,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            )
        )
        val wfsParams = mapOf(
            WfsAdapterPlugin.Parameters.COUNT to ReferenceOr.value(
                Parameter(
                    WfsAdapterPlugin.Parameters.COUNT,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            WfsAdapterPlugin.Parameters.BBOX to ReferenceOr.value(
                Parameter(
                    WfsAdapterPlugin.Parameters.BBOX,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            WfsAdapterPlugin.Parameters.SRS_NAME to ReferenceOr.value(
                Parameter(
                    WfsAdapterPlugin.Parameters.SRS_NAME,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            WfsAdapterPlugin.Parameters.TYPE_NAMES to ReferenceOr.value(
                Parameter(
                    WfsAdapterPlugin.Parameters.TYPE_NAMES,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            WfsAdapterPlugin.Parameters.REQUEST to ReferenceOr.value(
                Parameter(
                    WfsAdapterPlugin.Parameters.REQUEST,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            WfsAdapterPlugin.Parameters.VERSION to ReferenceOr.value(
                Parameter(
                    WfsAdapterPlugin.Parameters.VERSION,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            WfsAdapterPlugin.Parameters.SERVICE to ReferenceOr.value(
                Parameter(
                    WfsAdapterPlugin.Parameters.SERVICE,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            WfsAdapterPlugin.Parameters.FILTER to ReferenceOr.value(
                Parameter(
                    WfsAdapterPlugin.Parameters.FILTER,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            )
        )
    }

    private val LC_DL_ZERO = License(
        name = "Datenlizenz Deutschland – Zero",
        url = "https://www.govdata.de/dl-de/zero-2-0"
    )
    const val LC_DL_BY = "https://www.govdata.de/dl-de/by-2-0"
    private const val LC_CC_BY = "https://creativecommons.org/licenses/by/4.0/"

    val DE_TH = PathItem(
        summary = "Flurstücke Thüringen",
        get = Operation(
            operationId = Bundesland.THUERINGEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.THUERINGEN.roughBoundaries,
                    correspondsTo = Bundesland.THUERINGEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "flaeche",
                        parcelId = "flstkennz",
                        district = "gemarkung",
                        districtId = "gemaschl",
                        districtCompartment = "flur",
                        districtCompartmentId = "flurschl",
                        districtMunicipality = "gemeinde",
                        districtMunicipalityId = "gmdschl",
                        districtRegion = "kreis",
                        districtRegionId = "kreisschl",
                        usageLegalDeviation = "abwrecht",
                        plot = "flurstnr",
                        plotNumerator = "flstnrzae",
                        plotDenominator = "flstnrnen",
                        location = "lagebeztxt",
                        usageHint = "tntext"
                    ),
                    parcelLinkReference = Bundesland.THUERINGEN.iso3166_2 + "_default",
                    license = License(
                        name = "© GDI-Th",
                        identifier = "Flurstücke Thüringen",
                        url = LC_DL_BY
                    )
                ).usable()
            ),
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
    val DE_SN = PathItem(
        summary = "Flurstücke Sachsen",
        get = Operation(
            operationId = Bundesland.SACHSEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.SACHSEN.roughBoundaries,
                    correspondsTo = Bundesland.SACHSEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "AREA_m2",
                        parcelId = "NATIONALCA",
                        district = "ADMIN_UNIT",
                        districtId = "ZONING",
                        plotNumerator = "LABEL",
                    ),
                    parcelLinkReference = Bundesland.SACHSEN.iso3166_2 + "_default",
                    license = License(
                        name = "© Geobasisinformation und Vermessung Sachsen (GeoSN)",
                        identifier = "Flurstücke Sachsen",
                        url = LC_DL_BY
                    )
                ).usable(),
            )
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
    val DE_BB = PathItem(
        summary = "Flurstücke Brandenburg",
        get = Operation(
            operationId = Bundesland.BRANDENBURG.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.BRANDENBURG.roughBoundaries,
                    correspondsTo = Bundesland.BRANDENBURG.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "flaeche",
                        parcelId = "flstkennz",
                        district = "gemarkung",
                        districtCompartmentId = "flur",
                        plotNumerator = "flurstnr",
                        districtMunicipalityId = "gmdschl",
                        location = "lagebeztxt"
                    ),
                    parcelLinkReference = Bundesland.BRANDENBURG.iso3166_2 + "_default",
                    license = License(
                        name = "GeoBasis-DE/LGB, 2023",
                        identifier = "Flurstücke Brandenburg",
                        url = LC_DL_BY
                    )
                ).usable(),
            )
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
    val DE_HE = PathItem(
        summary = "Flurstücke Hessen",
        get = Operation(
            operationId = Bundesland.HESSEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.HESSEN.roughBoundaries,
                    correspondsTo = Bundesland.HESSEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "amtlicheFlaeche",
                        parcelId = "flurstueckskennzeichen",
                        districtId = "gemarkung_AX_Gemarkung_Schluess",
                        plotNumerator = "flurstuecksnummer_AX_Flurstueck",
                        plotDenominator = "flurstuecksnummer_AX_Flurstue_1",
                    ),
                    parcelLinkReference = Bundesland.HESSEN.iso3166_2 + "_default",
                    license = LC_DL_ZERO.copy(identifier = "Flurstücke Hessen")
                ).usable(),
            )
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
    val DE_HH = PathItem(
        summary = "Flurstücke Hamburg",
        get = Operation(
            operationId = Bundesland.HAMBURG.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.HAMBURG.roughBoundaries,
                    correspondsTo = Bundesland.HAMBURG.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "areaValue",
                        parcelId = "nationalCadastralReference",
                        plotNumerator = "label",
                    ),
                    parcelLinkReference = Bundesland.HAMBURG.iso3166_2 + "_default",
                    license = License(
                        name = "Freie und Hansestadt Hamburg, Landesbetrieb Geoinformation und Vermessung (LGV)",
                        url = LC_DL_BY,
                        identifier = "Flurstücke Hamburg"
                    )
                ).usable(),
            )
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
    val DE_NW = PathItem(
        summary = "Flurstücke Nordrhein-Westfalen",
        get = Operation(
            operationId = Bundesland.NORDRHEIN_WESTFALEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.NORDRHEIN_WESTFALEN.roughBoundaries,
                    correspondsTo = Bundesland.NORDRHEIN_WESTFALEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "amtlicheFlaeche",
                        parcelId = "flurstueckskennzeichen",
                        districtId = "gemarkung_AX_Gemarkung_Schluess",
                        districtCompartmentId = "flurnummer",
                        plotNumerator = "flurstuecksnummer_AX_Flurstueck",
                        plotDenominator = "flurstuecksnummer_AX_Flurstue_1",
                    ),
                    parcelLinkReference = Bundesland.NORDRHEIN_WESTFALEN.iso3166_2 + "_default",
                    license = LC_DL_ZERO.copy(identifier = "Flurstücke Nordrhein-Westfalen")
                ).usable(),
            )
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
    val DE_ST = PathItem(
        summary = "Flurstücke Sachsen-Anhalt",
        get = Operation(
            operationId = Bundesland.SACHSEN_ANHALT.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.SACHSEN_ANHALT.roughBoundaries,
                    correspondsTo = Bundesland.SACHSEN_ANHALT.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "flaeche",
                        parcelId = "flstkennz",
                        district = "gemarkung",
                        districtId = "gemaschl",
                        districtCompartmentId = "flur",
                        plotNumerator = "flstnrzae",
                        plotDenominator = "flstnrnen",
                        districtMunicipalityId = "gmdschl",
                        location = "lagebeztxt"
                    ),
                    parcelLinkReference = Bundesland.SACHSEN_ANHALT.iso3166_2 + "_default",
                    license = License(
                        name = "GeoBasis-DE / LVermGeo LSA, [2023]",
                        identifier = "Flurstücke Sachsen-Anhalt",
                        url = "https://www.lvermgeo.sachsen-anhalt.de/datei/anzeigen/id/3567,501/Nutzungsbedingungen.pdf"
                    )
                ).usable(),
            )
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
    val DE_BE = PathItem(
        summary = "Flurstücke Berlin",
        get = Operation(
            operationId = Bundesland.BERLIN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.BERLIN.roughBoundaries,
                    correspondsTo = Bundesland.BERLIN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "afl",
                        parcelId = "fsko",
                        district = "namgem",
                        districtId = "gmk",
                        districtCompartmentId = "fln",
                        plotNumerator = "zae",
                        plotDenominator = "nen",
                    ),
                    parcelLinkReference = Bundesland.BERLIN.iso3166_2 + "_default",
                    license = License(
                        name = "Geoportal Berlin / ALKIS Berlin - Flurstücke",
                        identifier = "Flurstücke Berlin",
                        url = LC_DL_BY
                    )
                ).usable(),
            )
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
    val DE_NI = PathItem(
        summary = "Flurstücke Niedersachsen",
        get = Operation(
            operationId = Bundesland.NIEDERSACHSEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.PluginMaterialization.O.key to esriAdapterConfig,
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.NIEDERSACHSEN.roughBoundaries,
                    correspondsTo = Bundesland.NIEDERSACHSEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        district = "gmk__bez",
                        districtCompartmentId = "fln",
                        plotNumerator = "fsn__zae",
                        plotDenominator = "fsn__nen",
                        parcelId = "fsk",
                        area = "afl",
                        location = "gem__bez"
                    ),
                    parcelLinkReference = Bundesland.NIEDERSACHSEN.iso3166_2 + "_default",
                    license = License(
                        name = "LGLN Open Geodata",
                        identifier = "Flurstücke Niedersachsen",
                        url = LC_CC_BY
                    )
                ).usable(),
            )
        ),
        parameters = Path.esriParams.keys.map { ReferenceOr.parameters(it) }
    )
}