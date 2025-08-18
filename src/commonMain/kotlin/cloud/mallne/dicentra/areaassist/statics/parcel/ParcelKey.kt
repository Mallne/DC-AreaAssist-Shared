package cloud.mallne.dicentra.areaassist.statics.parcel

import kotlinx.serialization.Serializable

@Serializable
data class ParcelKey(
    val identifier: String,
    val reference: String? = null,
    val type: KeyType = KeyType.String,
    val format: KeyFormat = KeyFormat(),
    val translations: KeyTranslation,
    val readonly: Boolean = false, //mark the Property as Readonly, note that an empty readonly Property will be hidden in the UI
    val hideInUI: Boolean = false, // will hide the Property in the UI
    val icon: KeyIcon? = null,
) {
    fun isGraphObject(ofGraph: String): Boolean {
        val id = identifier.split(".")
        val graphs = ofGraph.split(".")
        return id.containsAll(graphs)
    }

    companion object Extensions {
        fun List<ParcelKey>.forIdentifier(identifier: String) =
            find { it.identifier == identifier }
    }
}