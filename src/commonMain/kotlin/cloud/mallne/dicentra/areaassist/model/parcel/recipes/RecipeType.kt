package cloud.mallne.dicentra.areaassist.model.parcel.recipes

import kotlinx.serialization.Serializable

@Serializable
enum class RecipeType {
    GETTER,
    PERSISTABLE
}