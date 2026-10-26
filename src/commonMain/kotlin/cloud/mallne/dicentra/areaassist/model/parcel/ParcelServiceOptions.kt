package cloud.mallne.dicentra.areaassist.model.parcel

import cloud.mallne.dicentra.areaassist.model.DisplayConstraints
import cloud.mallne.dicentra.areaassist.model.areas.Custom
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import io.ktor.openapi.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position
import kotlin.uuid.Uuid

@Serializable
data class ParcelServiceOptions(
    val boundary: MultiPolygon = MultiPolygon(coordinates = emptyList<List<List<Position>>>()),
    /** Wire format version for the multipart polygon boundary. */
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaryFormatVersion: Int = 2,
    val minimalDefinition: Boolean = true,
    /** Optional literal prefix used to route parcel IDs to this service. Null disables inference. */
    val parcelIdPrefix: String? = null,
    val correspondsTo: String = Custom.id,
    val parcelLinkReference: String = Uuid.random().toString(),
    val license: OpenApiInfo.License? = null,
    val keys: List<ParcelKey> = listOf(),
    val staticFeatures: List<StaticFeature> = StaticFeature.entries,
    val constraints: DisplayConstraints = DisplayConstraints(),
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions {
        return GenericElementWrapper(this, serializer())
    }
}
