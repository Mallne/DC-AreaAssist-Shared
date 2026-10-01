package cloud.mallne.dicentra.areaassist

import cloud.mallne.dicentra.areaassist.model.validateComputistLinkUrl
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.api.RVRComputistLink
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ComputistLinkServiceOptionsTest {
    @Test
    fun onlyHttpAndHttpsLinksAreUsable() {
        assertEquals("https://example.org", validateComputistLinkUrl("https://example.org")?.trimEnd('/'))
        assertEquals("http://example.org", validateComputistLinkUrl("http://example.org")?.trimEnd('/'))
        assertNull(validateComputistLinkUrl("javascript:alert(1)"))
        assertNull(validateComputistLinkUrl("not a URL"))
    }

    @Test
    fun generatedLinkIsAnAviatorServiceUsingTheComputistLocator() {
        val document = RVRComputistLink.create(
            id = "Tool.CodexLink.docs",
            title = "Docs",
            url = "https://example.org",
            description = "Read the documentation",
        )
        val locator = APIs.Services.COMPUTIST_LINK.locator(ServiceMethods.GATHER)

        assertEquals("&.computist.link", locator.locator)
        assertEquals("https://example.org", document.servers?.firstOrNull()?.url?.trimEnd('/'))
        assertEquals("Docs", document.info.title)
        assertEquals("Read the documentation", document.info.description)
        assertEquals(locator.locator, AviatorServiceUtils.extractServiceLocators(document).single().first.locator)
    }

    @Test
    fun rvrIsProvidedAsTheFirstBuiltinComputistLinkService() {
        assertEquals("RVR", APIs.rvrComputistLink.info.title)
        assertEquals("&.computist.link", AviatorServiceUtils.extractServiceLocators(APIs.rvrComputistLink).single().first.locator)
        assertEquals("RVR", APIs.apiOverrideVersion().firstOrNull { it.info.title == "RVR" }?.info?.title)
    }
}
