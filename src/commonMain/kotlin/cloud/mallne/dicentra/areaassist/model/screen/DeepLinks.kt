package cloud.mallne.dicentra.areaassist.model.screen

import cloud.mallne.dicentra.areaassist.model.geokit.GeokitPosition
import cloud.mallne.dicentra.areaassist.statics.Serialization
import io.ktor.http.*

object DeepLinks {
    const val BASE = "https://areaassist.mallne.cloud"
    const val BASE_PRIVATE = "dcaa://areaassist.mallne.cloud"

    object HTTPS : DeepLinkTarget(BASE)
    object DCAA : DeepLinkTarget(BASE_PRIVATE)

    sealed class DeepLinkTarget(val base: String) {
        val parcel = "$base/parcel"
        val singleParcel = parcel
        val search = "$base/search"
        val compass = "$base/computist/compass"
        val agrimensor = "$base/computist/agrimensor"
        val login = "$base/login"

        fun generateCompassDeeplink(positions: List<GeokitPosition>): String =
            "$compass?r=${Serialization().encodeToString(positions).encodeURLParameter()}"

        fun generateAgrimensorDeeplink(
            parcelId: String,
        ): String =
            "$agrimensor/${parcelId.encodeURLParameter()}"

        fun generateParcelDeeplink(
            parcelId: String,
        ): String =
            "$singleParcel/${parcelId.encodeURLParameter()}"
    }
}