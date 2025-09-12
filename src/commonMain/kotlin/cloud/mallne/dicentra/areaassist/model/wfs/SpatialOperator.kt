package cloud.mallne.dicentra.areaassist.model.wfs

import cloud.mallne.dicentra.areaassist.model.wfs.WFSNamespaces.FES_NAMESPACE_URI
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

// Spatial Operators
@Serializable
sealed class SpatialOperator : FesPredicate() {
    @Serializable
    @XmlSerialName("BBOX", FES_NAMESPACE_URI, "fes")
    data class BBOX(
        val valueReference: ValueReference,
        val envelope: Envelope
    ) : SpatialOperator()

    @Serializable
    @XmlSerialName("Intersects", FES_NAMESPACE_URI, "fes")
    data class Intersects(
        val valueReference: ValueReference,
        val geometry: GmlGeometry,
    ) : SpatialOperator()
}