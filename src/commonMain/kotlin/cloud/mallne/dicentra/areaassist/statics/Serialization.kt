package cloud.mallne.dicentra.areaassist.statics

import kotlinx.serialization.SerialFormat
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import org.maplibre.spatialk.geojson.GeoJson

object Serialization {
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        serializersModule = GeoJson.jsonFormat.serializersModule
    }

    val xml = XML {
        defaultPolicy {
            autoPolymorphic = true
            //ignoreUnknownChildren()
        }
    }

    operator fun invoke() = json

    fun serializers(): List<SerialFormat> = listOf(json, xml)
}