package cloud.mallne.dicentra.areaassist.model.actions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ServersideActionHolderTest {
    @Test
    fun actionEnvelopeRetainsPolymorphicWireFormat() {
        val holder = ServersideActionHolder("id", OpenScreen("parcel"))
        val encoded = Json.encodeToString(ServersideActionHolder.serializer(), holder)
        val action = Json.parseToJsonElement(encoded).jsonObject.getValue("action").jsonObject
        assertEquals("cloud.mallne.dicentra.areaassist.model.actions.OpenScreen", action.getValue("action_type").jsonPrimitive.content)
        assertEquals(holder, Json.decodeFromString(ServersideActionHolder.serializer(), encoded))
    }
}
