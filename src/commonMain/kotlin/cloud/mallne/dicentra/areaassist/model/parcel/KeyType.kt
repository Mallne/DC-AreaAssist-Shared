package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
enum class KeyType {
    @SerialName("String")
    STRING,

    @SerialName("Number")
    NUMBER,

    @SerialName("Boolean")
    BOOLEAN,

    @SerialName("Nothing")
    NOTHING;

    val dataTypeE: DataTypeE?
        get() = when (this) {
            STRING -> DataTypeE.STRING
            NUMBER -> DataTypeE.DOUBLE
            BOOLEAN -> DataTypeE.BOOLEAN
            NOTHING -> null
        }

    val serializer: KSerializer<out Comparable<*>>?
        get() = when (this) {
            STRING -> String.serializer()
            NUMBER -> Double.serializer()
            BOOLEAN -> Boolean.serializer()
            NOTHING -> null
        }
}