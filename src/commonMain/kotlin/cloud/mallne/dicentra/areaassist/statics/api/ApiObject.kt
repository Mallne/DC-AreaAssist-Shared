package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.aviator.koas.OpenAPI
import kotlin.reflect.KProperty

sealed interface ApiObject {
    val value: OpenAPI

    operator fun getValue(thisRef: Any?, property: KProperty<*>): OpenAPI = value
}