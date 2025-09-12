package cloud.mallne.dicentra.areaassist.model.wfs

import cloud.mallne.dicentra.areaassist.model.wfs.WFSNamespaces.FES_NAMESPACE_URI
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

// A sealed class hierarchy is perfect for representing the different types of predicates.
// It ensures compile-time safety and a clear data structure.
@Serializable
sealed class FesPredicate {
    // Logical Operators
    @Serializable
    @XmlSerialName("And", FES_NAMESPACE_URI, "fes")
    data class And(val predicates: List<FesPredicate>) : FesPredicate()

    @Serializable
    @XmlSerialName("Or", FES_NAMESPACE_URI, "fes")
    data class Or(val predicates: List<FesPredicate>) : FesPredicate()

    @Serializable
    @XmlSerialName("Not", FES_NAMESPACE_URI, "fes")
    data class Not(val predicate: FesPredicate) : FesPredicate()
}

