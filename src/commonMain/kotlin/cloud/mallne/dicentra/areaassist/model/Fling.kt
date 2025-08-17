package cloud.mallne.dicentra.areaassist.model

import kotlinx.serialization.Serializable

@Serializable
data class Fling(
    val message: String?,
    val stacktrace: String?,
) {
    constructor(thr: Throwable) : this(thr.message, thr.stackTraceToString())

    override fun toString(): String {
        var ret = ""
        if (message != null && message.isNotBlank()) {
            ret += message
        }
        if (stacktrace != null && stacktrace.isNotBlank()) {
            if (ret.isNotBlank()) {
                ret += "\n"
            }
            ret += stacktrace
        }
        return ret
    }

    companion object {
        fun Throwable.toFling(): Fling = Fling(this)
    }
}