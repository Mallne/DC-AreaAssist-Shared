package cloud.mallne.dicentra.areaassist.model.areas

/** German-state metadata retained for existing parcel-service and parcel-ID use cases. */
interface StateDefinition : AdministrativeArea {
    val iso3166_2: String

    override val id: String
        get() = iso3166_2
}
