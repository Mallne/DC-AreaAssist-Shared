package cloud.mallne.dicentra.areaassist.model.wfs

import cloud.mallne.dicentra.areaassist.model.wfs.WFSNamespaces.GML_NAMESPACE_URI
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
sealed class GmlGeometry {
    @Serializable
    @XmlSerialName("Point", GML_NAMESPACE_URI, "gml")
    data class GmlPoint(val pos: Pos) : GmlGeometry()
}