package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Serializable

@Serializable
enum class DataTypeE {
    BOOLEAN,
    STRING,
    INT,
    LONG,
    DOUBLE,
    INSTANT,
    DURATION,
    AREA_UNIT,
    LOCATION_ENGINE,
    ORIENTATION_ENGINE,
    GYROSCOPE_ENGINE,
    NAV_BAR_CONFIGURATION,
    MAP_STYLE_SETTING,
    COLOR,
    JSON
}
