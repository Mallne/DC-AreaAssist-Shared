package cloud.mallne.dicentra.areaassist.model.parcel.recipes

sealed interface KeyRecipe {
    val type: RecipeType

    suspend fun execute()
}