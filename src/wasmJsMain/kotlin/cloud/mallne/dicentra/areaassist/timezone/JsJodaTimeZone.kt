@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cloud.mallne.dicentra.areaassist.timezone

import kotlin.js.JsModule

@JsModule("@js-joda/timezone")
external object JsJodaTimeZoneModule

private val jsJodaTimeZone = JsJodaTimeZoneModule
