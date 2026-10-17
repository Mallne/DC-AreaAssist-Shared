package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.parcel.ParcelKey
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.DefaultKeys

/** Shared schema identifiers and standard ALKIS parcel-field mappings. */
object DE {
    const val SIMPLIFIED_ALKIS_NAMESPACE =
        "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
    const val SIMPLIFIED_ALKIS_PREFIX = "ave"
    const val SIMPLIFIED_ALKIS_TYPE_NAME = "$SIMPLIFIED_ALKIS_PREFIX:Flurstueck"
    const val SIMPLIFIED_ALKIS_GEOMETRY = "geometrie"

    fun simplifiedAlkisKeys(): List<ParcelKey> = DefaultKeys.fillIn(
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
        plotNumerator = "flstnrzae",
        plotDenominator = "flstnrnen",
        location = "lagebeztxt",
        usageHint = "tntxt",
        usageLegalDeviation = "abwrecht"
    )
}
