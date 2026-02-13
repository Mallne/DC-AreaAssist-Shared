package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface Translatable {
    @Serializable
    data class Bundled(val value: PreDefined) : Translatable

    @Serializable
    data class Localization(val title: Map<String, String>, val description: Map<String, String>? = null) :
        Translatable {

        companion object {
            fun nonTranslatable(title: String, description: String? = null): Translatable {
                return Localization(
                    mapOf(
                        ENGLISH to title,
                        GERMAN to title
                    ), description?.let {
                        mapOf(
                            ENGLISH to description,
                            GERMAN to description
                        )
                    })
            }

            const val ENGLISH = "en"
            const val GERMAN = "de"
        }
    }
}