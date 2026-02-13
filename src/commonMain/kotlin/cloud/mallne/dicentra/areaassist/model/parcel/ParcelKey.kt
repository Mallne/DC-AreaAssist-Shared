package cloud.mallne.dicentra.areaassist.model.parcel

import cloud.mallne.dicentra.areaassist.model.parcel.recipes.KeyRecipe
import kotlinx.serialization.Serializable

@Serializable
data class ParcelKey(
    val identifier: String,
    val reference: String? = null,
    val recipe: KeyRecipe? = null,
    val type: KeyType = KeyType.STRING,
    val format: KeyFormat = KeyFormat(),
    val translations: Translatable,
    val readonly: Boolean = false, //mark the Property as Readonly, note that an empty readonly Property will be hidden in the UI
    val hideInUI: Boolean = false, // will hide the Property in the UI
    val icon: KeyIcon? = null,
) {
    fun isGraphObject(ofGraph: String): Boolean {
        val id = identifier.split(".")
        val graphs = ofGraph.split(".")
        return id.containsAll(graphs)
    }

    fun toParcelProperty(): ParcelPropertyKeys? {
        return if (type == KeyType.NOTHING) {
            null
        } else {
            try {
                ParcelPropertyKeys(type, identifier)
            } catch (e: ClassCastException) {
                null
            }
        }
    }

    companion object Extensions {
        fun List<ParcelKey>.forIdentifier(identifier: String) =
            find { it.identifier == identifier }
    }
}