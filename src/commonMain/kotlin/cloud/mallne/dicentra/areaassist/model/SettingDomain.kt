package cloud.mallne.dicentra.areaassist.model

import kotlinx.serialization.json.JsonElement

interface SettingDomain {
    val key: String
    val value: JsonElement
}