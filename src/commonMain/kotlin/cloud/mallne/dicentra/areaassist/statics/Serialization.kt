package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.geokit.geojson.GeoJson
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import nl.adaptivity.xmlutil.serialization.XML
import org.maplibre.spatialk.geojson.GeoJson as MLGeoJson

object Serialization {
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        serializersModule = GeoJson.jsonFormat.serializersModule + MLGeoJson.jsonFormat.serializersModule
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