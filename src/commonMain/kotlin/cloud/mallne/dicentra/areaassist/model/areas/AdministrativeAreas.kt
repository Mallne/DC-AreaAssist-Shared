package cloud.mallne.dicentra.areaassist.model.areas

/** Built-in areas available for service selection and display. */
object AdministrativeAreas {
    val entries: List<AdministrativeArea> = Bundesland.entries + France

    fun getById(id: String): AdministrativeArea? = entries.firstOrNull { it.id == id }
}
