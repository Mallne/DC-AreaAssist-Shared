@file:OptIn(ExperimentalJsExport::class)

package cloud.mallne.dicentra.areaassist.timezone

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsModule
import kotlin.js.JsNonModule

@JsModule("@js-joda/timezone")
@JsNonModule
external object JsJodaTimeZoneModule

@JsExport
val jsJodaTimeZone = JsJodaTimeZoneModule
