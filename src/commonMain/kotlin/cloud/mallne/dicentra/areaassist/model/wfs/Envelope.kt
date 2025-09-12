package cloud.mallne.dicentra.areaassist.model.wfs

import cloud.mallne.dicentra.areaassist.model.wfs.WFSNamespaces.GML_NAMESPACE_URI
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Envelope", GML_NAMESPACE_URI, "gml")
data class Envelope(
    @XmlSerialName("lowerCorner", GML_NAMESPACE_URI, "gml")
    val lowerCorner: Pos,
    @XmlSerialName("upperCorner", GML_NAMESPACE_URI, "gml")
    val upperCorner: Pos
)