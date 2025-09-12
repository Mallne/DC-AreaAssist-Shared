package cloud.mallne.dicentra.areaassist.model.wfs

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

// The main `Filter` element. A filter can contain a single logical, comparison, or spatial operator.
@Serializable
@XmlSerialName("Filter", WFSNamespaces.FES_NAMESPACE_URI, "fes")
data class Filter(
    val predicate: FesPredicate,
)