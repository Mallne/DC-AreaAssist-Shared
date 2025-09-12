package cloud.mallne.dicentra.areaassist.model.wfs

import cloud.mallne.dicentra.areaassist.model.wfs.WFSNamespaces.FES_NAMESPACE_URI
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

// Common Helper Classes
@Serializable
@XmlSerialName("ValueReference", FES_NAMESPACE_URI, "fes")
data class ValueReference(@XmlValue(true) val value: String)