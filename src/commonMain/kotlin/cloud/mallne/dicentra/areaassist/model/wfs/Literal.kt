package cloud.mallne.dicentra.areaassist.model.wfs

import cloud.mallne.dicentra.areaassist.model.wfs.WFSNamespaces.FES_NAMESPACE_URI
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("Literal", FES_NAMESPACE_URI, "fes")
data class Literal(@XmlValue(true) val value: String)