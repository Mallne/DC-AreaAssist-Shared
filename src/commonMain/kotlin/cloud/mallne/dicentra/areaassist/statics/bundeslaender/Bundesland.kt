package cloud.mallne.dicentra.areaassist.statics.bundeslaender

@Suppress("SpellCheckingInspection")
enum class Bundesland(
    definition: BundeslandDefinition,
) : BundeslandDefinition by definition {
    SCHLESWIG_HOLSTEIN(definition = SchleswigHolstein),
    HAMBURG(definition = Hamburg),
    NIEDERSACHSEN(definition = Niedersachsen),
    BREMEN(definition = Bremen),
    NORDRHEIN_WESTFALEN(definition = NordrheinWestfalen),
    HESSEN(definition = Hessen),
    RHEINLAND_PFALZ(definition = RheinlandPfalz,),
    BADEN_WUERTTEMBERG(definition = BadenWuerttemberg,),
    BAYERN(definition = Bayern,),
    SAARLAND(definition = Saarland,),
    BERLIN(definition = Berlin,),
    BRANDENBURG(definition = Brandenburg,),
    MECKLENBURG_VORPOMMERN(definition = MecklenburgVorpommern,),
    SACHSEN(definition = Sachsen,),
    SACHSEN_ANHALT(definition = SachsenAnhalt,),
    THUERINGEN(definition = Thueringen,),
    CUSTOM(definition = Custom);

    val iso3166_2_DE: String
        get() = iso3166_2.substringAfter("DE-")

    companion object {
        fun getByIso(iso: String): Bundesland = entries.find { it.iso3166_2 == iso } ?: CUSTOM
        fun getByDEBId(id: Int): Bundesland = entries.find { it.deBId == id } ?: CUSTOM
        fun getByShortIso(countryIso: String, iso: String): Bundesland =
            entries.find { it.iso3166_2.substringAfter("$countryIso-") == iso } ?: CUSTOM
    }
}