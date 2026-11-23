package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.dicentra.areaassist.statics.api.WeatherWorkflows
import cloud.mallne.dicentra.aviator.orchestration.arazzo.validation.ArazzoValidationResult
import cloud.mallne.dicentra.aviator.orchestration.arazzo.validation.ArazzoValidator
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverPlugin
import cloud.mallne.dicentra.aviator.koas.typed.routes
import io.ktor.openapi.PathItem
import io.ktor.openapi.ReferenceOr
import io.ktor.openapi.ParameterType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import cloud.mallne.dicentra.areaassist.statics.api.WeatherWorkflowDefinition

class WeatherWorkflowsTest {
    @Test
    fun bundledWorkflowsValidateAndReferenceRealSourceOperations() {
        assertEquals(2, APIs.weatherWorkflows.size)
        val seen = mutableSetOf<String>()
        for (json in APIs.weatherWorkflows) {
            val result = ArazzoValidator.parseAndValidate(json.toString())
            val document = assertIs<ArazzoValidationResult.Valid>(result, result.toString()).document
            val sources = document.sourceDescriptions.associateBy { it.name }
            for (workflow in document.workflows) {
                val step = workflow.steps.single()
                val reference = assertNotNull(step.operationId).removePrefix("\$sourceDescriptions.")
                val (sourceName, operationId) = reference.split('.', limit = 2)
                val source = assertNotNull(sources[sourceName])
                val api = assertNotNull(WeatherWorkflows.sources[source.url])
                val operations = api.paths.orEmpty().values.mapNotNull { (it as? ReferenceOr.Value<PathItem>)?.value?.get }
                val operation = assertNotNull(operations.singleOrNull { it.operationId == operationId }, operationId)
                assertNotNull(operation.`x-dicentra-aviator-pluginMaterialization`?.get(WeaverPlugin.identity))
                val expectedParameters = if (sourceName == "openMeteo") {
                    setOf("lat", "lon", "current", "hourly", "daily", "wind_speed_unit", "timezone")
                } else if (operationId == "WeatherForecast") setOf("lat", "lon", "date")
                else setOf("lat", "lon")
                assertEquals(expectedParameters, step.parameters.map { it.name }.toSet())
                val declaredQuery = api.routes().single { it.operationId == operationId }.parameter
                    .filter { it.`in` == ParameterType.query }.map { it.name }.toSet()
                val workflowAliases = if (sourceName == "openMeteo") setOf("lat", "lon") else emptySet()
                assertEquals(
                    emptySet(),
                    expectedParameters - declaredQuery - workflowAliases,
                    "$operationId has undeclared workflow parameters",
                )
                assertEquals("\$steps.weather.outputs.providerResponse", workflow.outputs?.get("providerResponse"))
                seen += operationId
            }
        }
        assertEquals(setOf("CurrentSources", "WeatherForecast", "OpenMeteoForecast"), seen)
    }

    @Test
    fun registrationsReferenceExistingWorkflowsAndRoundTrip() {
        val definitions = APIs.weatherWorkflowDefinitions
        assertEquals(4, definitions.size)
        assertEquals(4, definitions.map { it.documentId to it.workflowId }.toSet().size)
        definitions.forEach { definition ->
            val validated = assertIs<ArazzoValidationResult.Valid>(
                ArazzoValidator.parseAndValidate(definition.document.toString()),
            )
            assertNotNull(validated.document.workflows.singleOrNull { it.workflowId == definition.workflowId })
            assertEquals(definition, Json.decodeFromString<WeatherWorkflowDefinition>(Json.encodeToString(WeatherWorkflowDefinition.serializer(), definition)))
        }
    }
}
