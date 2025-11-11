package cloud.mallne.dicentra.areaassist.statics

import kotlinx.serialization.SerialFormat
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML

object Serialization {
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val xml = XML {
        defaultPolicy { autoPolymorphic = true }
    }

    operator fun invoke() = json

    fun serializers(): List<SerialFormat> = listOf(json, xml)
}