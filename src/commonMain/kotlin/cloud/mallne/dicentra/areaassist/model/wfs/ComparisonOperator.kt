package cloud.mallne.dicentra.areaassist.model.wfs

import cloud.mallne.dicentra.areaassist.model.wfs.WFSNamespaces.FES_NAMESPACE_URI
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

// Comparison Operators
@Serializable
sealed class ComparisonOperator : FesPredicate() {
    @Serializable
    @XmlSerialName("PropertyIsEqualTo", FES_NAMESPACE_URI, "fes")
    data class PropertyIsEqualTo(
        val valueReference: ValueReference,
        val literal: Literal,
        val matchCase: Boolean = true
    ) : ComparisonOperator()

    @Serializable
    @XmlSerialName("PropertyIsGreaterThan", FES_NAMESPACE_URI, "fes")
    data class PropertyIsGreaterThan(
        val valueReference: ValueReference,
        val literal: Literal,
    ) : ComparisonOperator()

    @Serializable
    @XmlSerialName("PropertyIsLessThan", FES_NAMESPACE_URI, "fes")
    data class PropertyIsLessThan(
        val valueReference: ValueReference,
        val literal: Literal,
    ) : ComparisonOperator()
}