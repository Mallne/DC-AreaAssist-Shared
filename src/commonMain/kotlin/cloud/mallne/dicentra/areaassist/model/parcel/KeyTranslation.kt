package cloud.mallne.dicentra.areaassist.model.parcel

import cloud.mallne.dicentra.polyfill.Validation
import kotlinx.serialization.Serializable

@Serializable
data class KeyTranslation(
    val predef: PreDefined? = null,
    val l18n: Map<String, String>? = null,
    val l18nDesc: Map<String, String>? = null
) {
    init {
        require(Validation.Null.one(predef, l18n, l18nDesc))
    }
}