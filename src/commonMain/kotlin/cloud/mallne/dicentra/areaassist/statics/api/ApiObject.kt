package cloud.mallne.dicentra.areaassist.statics.api

import io.ktor.openapi.*
import kotlin.reflect.KProperty

sealed interface ApiObject {
    val value: OpenApiDoc

    operator fun getValue(thisRef: Any?, property: KProperty<*>): OpenApiDoc = value
}