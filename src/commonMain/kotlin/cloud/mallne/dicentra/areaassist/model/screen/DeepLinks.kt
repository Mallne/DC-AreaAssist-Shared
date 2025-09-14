package cloud.mallne.dicentra.areaassist.model.screen

import cloud.mallne.dicentra.areaassist.model.curator.Query
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
        val action = "$base/action"

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

        fun generateSearchDeeplink(
            queries: List<Query>? = null,
            startImmediately: Boolean = false,
            serviceHints: List<String>? = null
        ): String {
            val queryParams = mutableListOf<String>()
            if (queries != null) {
                queryParams.add("queries=${Serialization().encodeToString(queries).encodeURLParameter()}")
            }
            queryParams.add(
                "start_immediately=${
                    Serialization().encodeToString(startImmediately).encodeURLParameter()
                }"
            )
            if (serviceHints != null) {
                queryParams.add("service_hints=${Serialization().encodeToString(serviceHints).encodeURLParameter()}")
            }
            return "$search${queryParams.joinToString("&", prefix = "?")}"
        }

        fun generateActionDeeplink(
            actionId: String,
        ): String = "$action/${actionId.encodeURLParameter()}"
    }
}