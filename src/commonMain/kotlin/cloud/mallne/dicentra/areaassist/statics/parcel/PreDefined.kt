package cloud.mallne.dicentra.areaassist.statics.parcel;

import kotlinx.serialization.Serializable

@Serializable
        enum class PreDefined {
            PLOT,
            PLOT_NUMERATOR,
            PLOT_SEPARATOR,
            PLOT_DENOMINATOR,
            AREA,
            DISTRICT,
            DISTRICT_ID,
            DISTRICT_COMPARTMENT,
            DISTRICT_COMPARTMENT_ID,
            DISTRICT_MUNICIPALITY,
            DISTRICT_MUNICIPALITY_ID,
            DISTRICT_REGION,
            DISTRICT_REGION_ID,
            LOCATION,
            LANDREGISTERNUMBER,
            USAGE,
            USAGE_HINT,
            USAGE_BUILDINGS,
            USAGE_MANAGED,
            USAGE_LEGALDEVIATION,
            PARCELID,

            //Commons
            ID;

            fun toTranslatable() = KeyTranslation(predef = this)
        }