package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.dicentra.areaassist.model.weather.CurrentConditions
import cloud.mallne.dicentra.areaassist.aviator.SourceResponseCapture
import cloud.mallne.dicentra.areaassist.aviator.requireJsonBody
import cloud.mallne.dicentra.areaassist.aviator.toArazzoSnapshot
import cloud.mallne.dicentra.areaassist.model.weather.HourlyForecast
import cloud.mallne.dicentra.areaassist.model.weather.WeatherReport
import cloud.mallne.dicentra.areaassist.model.weather.WeatherSection
import cloud.mallne.dicentra.areaassist.statics.api.WeatherWorkflows
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.client.ktor.KtorEngineDefinitionConverter
import cloud.mallne.dicentra.aviator.client.ktor.ktorEngineTemplate
import cloud.mallne.dicentra.aviator.core.NoBody
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.engine.AviatorEngine
import cloud.mallne.dicentra.aviator.core.engine.EngineAuthorizationDecision
import cloud.mallne.dicentra.aviator.core.engine.InMemoryDefinitionCoordinator
import cloud.mallne.dicentra.aviator.core.engine.OpenApiClientInput
import cloud.mallne.dicentra.aviator.core.engine.OpenApiOperationDefinitionRequest
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.core.plugins.ClientPluginRegistration
import cloud.mallne.dicentra.aviator.core.plugins.DefaultAviatorPluginRegistry
import cloud.mallne.dicentra.aviator.koas.typed.routes
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.dicentra.aviator.orchestration.arazzo.execution.ArazzoEngineStepExecutor
import cloud.mallne.dicentra.aviator.orchestration.arazzo.execution.ArazzoRunExecutor
import cloud.mallne.dicentra.aviator.orchestration.arazzo.execution.resolveArazzoEngineAction
import cloud.mallne.dicentra.aviator.orchestration.arazzo.validation.ArazzoValidationResult
import cloud.mallne.dicentra.aviator.orchestration.arazzo.validation.ArazzoValidator
import cloud.mallne.dicentra.aviator.orchestration.model.BodyValue
import cloud.mallne.dicentra.aviator.orchestration.model.CapabilityOutcome
import cloud.mallne.dicentra.aviator.orchestration.model.CapabilityResult
import cloud.mallne.dicentra.aviator.orchestration.model.ConstituentIdentity
import cloud.mallne.dicentra.aviator.orchestration.model.ConstituentOutcome
import cloud.mallne.dicentra.aviator.orchestration.model.ConstituentResult
import cloud.mallne.dicentra.aviator.orchestration.model.ExecutionOutcome
import cloud.mallne.dicentra.aviator.orchestration.model.ExecutionFailure
import cloud.mallne.dicentra.aviator.orchestration.model.RunId
import cloud.mallne.dicentra.aviator.orchestration.model.WorkflowIdentity
import cloud.mallne.dicentra.aviator.orchestration.policy.CapabilityPolicy
import cloud.mallne.dicentra.aviator.orchestration.policy.ConstituentOrdering
import cloud.mallne.dicentra.aviator.orchestration.policy.EmptySelectionPolicy
import cloud.mallne.dicentra.aviator.orchestration.policy.ExecutionStrategy
import cloud.mallne.dicentra.aviator.orchestration.policy.PartialFailurePolicy
import cloud.mallne.dicentra.aviator.plugin.adapter.json.JsonAdapter
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WeatherWorkflowExecutionTest {
    @Test
    fun concurrentWorkflowCapturesRemainIsolated() = runTest {
        listOf(
            async { checkWorkflow("open-meteo", "current", WeatherWorkflows.OPEN_METEO_SOURCE, "openMeteo", "OpenMeteoForecast", OPEN_METEO_RESPONSE) },
            async { checkWorkflow("brightsky", "current", WeatherWorkflows.BRIGHTSKY_SOURCE, "brightsky", "CurrentSources", BRIGHTSKY_CURRENT_RESPONSE) },
            async { checkWorkflow("brightsky", "forecast", WeatherWorkflows.BRIGHTSKY_SOURCE, "brightsky", "WeatherForecast", BRIGHTSKY_FORECAST_RESPONSE) },
        ).awaitAll()
    }

    @Test
    fun openMeteoWorkflowRunsThroughEngineAndWeaver() = runTest {
        checkWorkflow("open-meteo", "forecast", WeatherWorkflows.OPEN_METEO_SOURCE, "openMeteo", "OpenMeteoForecast", OPEN_METEO_RESPONSE)
    }

    @Test
    fun openMeteoCurrentWorkflowRunsThroughEngineAndWeaver() = runTest {
        checkWorkflow("open-meteo", "current", WeatherWorkflows.OPEN_METEO_SOURCE, "openMeteo", "OpenMeteoForecast", OPEN_METEO_RESPONSE)
    }

    @Test
    fun brightSkyCurrentWorkflowRunsThroughEngineAndWeaver() = runTest {
        checkWorkflow("brightsky", "current", WeatherWorkflows.BRIGHTSKY_SOURCE, "brightsky", "CurrentSources", BRIGHTSKY_CURRENT_RESPONSE)
    }

    @Test
    fun brightSkyForecastWorkflowRunsThroughEngineAndWeaver() = runTest {
        checkWorkflow("brightsky", "forecast", WeatherWorkflows.BRIGHTSKY_SOURCE, "brightsky", "WeatherForecast", BRIGHTSKY_FORECAST_RESPONSE)
    }

    @Test
    fun missingCoordinateFailsBeforeTransport() = runTest {
        checkWorkflow(
            "open-meteo", "forecast", WeatherWorkflows.OPEN_METEO_SOURCE, "openMeteo",
            "OpenMeteoForecast", OPEN_METEO_RESPONSE,
            input = """{"lat":52.52}""", expectedFailure = "input_mapping",
        )
    }

    @Test
    fun providerErrorKeepsSourceResponseAndFailsWorkflow() = runTest {
        checkWorkflow(
            "open-meteo", "forecast", WeatherWorkflows.OPEN_METEO_SOURCE, "openMeteo",
            "OpenMeteoForecast", """{"error":true,"reason":"unavailable"}""",
            status = HttpStatusCode.ServiceUnavailable,
            expectedFailure = "success_criteria",
        )
    }

    private suspend fun checkWorkflow(
        provider: String,
        workflowId: String,
        sourceUri: String,
        sourceName: String,
        operation: String,
        fixture: String,
        input: String = """{"lat":52.52,"lon":13.41,"date":"2026-01-01T00:00:00Z"}""",
        expectedFailure: String? = null,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) {
        val requests = mutableListOf<String>()
        val sourceCapture = SourceResponseCapture()
        var transformedReport: WeatherReport? = null
        val client = HttpClient(MockEngine { request ->
            requests += request.url.toString()
            respond(fixture, status, headersOf(
                HttpHeaders.ContentType to listOf("application/json"),
                "X-Provider" to listOf(provider),
            ))
        })
        try {
            val registration = APIs.weatherWorkflowDefinitions.single {
                it.documentId == provider && it.workflowId == workflowId
            }
            val document = assertIs<ArazzoValidationResult.Valid>(
                ArazzoValidator.parseAndValidate(registration.document.toString()),
            ).document
            val workflow = document.workflows.single { it.workflowId == registration.workflowId }
            val source = WeatherWorkflows.sources.getValue(sourceUri)
            val route = source.routes().single { it.operationId == operation }
            val sourceLocator = ServiceLocator(assertNotNull(route.`x-dicentra-aviator-serviceDelegateCall`))
            val identity = ConstituentIdentity(sourceLocator, provider, sourceUri, "1", route.operationId)
            val converter = KtorEngineDefinitionConverter(KtorAviatorServiceConverter(client, serializers = Serialization.serializers()))
            val definition = converter.convert(OpenApiOperationDefinitionRequest(source, route, identity.locator) {
                install(JsonAdapter)
            })
            val engine = AviatorEngine(
                InMemoryDefinitionCoordinator(listOf(definition)),
                { EngineAuthorizationDecision.Allowed },
                { _, _, _ -> null },
                plugins = DefaultAviatorPluginRegistry(clients = listOf(ClientPluginRegistration(WeaverPlugin.install {}))),
            )
            val policy = CapabilityPolicy(
                ExecutionStrategy.AGGREGATE, ConstituentOrdering.RESOLUTION,
                PartialFailurePolicy.REQUIRE_ALL, EmptySelectionPolicy.FAIL,
            )
            val executor = ArazzoEngineStepExecutor(engine, policy) { step, inputs ->
                resolveArazzoEngineAction(
                    step, inputs, listOf(identity), policy,
                    sourceIdsByName = mapOf(sourceName to identity.sourceId),
                    template = ktorEngineTemplate<WeatherReport>(),
                    matchesDefinition = { requested, candidate -> requested == identity && candidate === definition },
                    inputFor = { _, input, _ ->
                        val query = input.jsonObject.getValue("parameters").jsonObject.getValue("query").jsonObject
                        OpenApiClientInput<NoBody>(parameters = RequestParameters(
                            query.mapValuesTo(mutableMapOf()) { RequestParameter.Single(it.value.jsonPrimitive.content) },
                        ))
                    },
                    mapResult = { context ->
                        val response = assertNotNull(context.networkChain.single().response)
                        val sourceResponse = sourceCapture.responses.single()
                        assertEquals(sourceResponse.status, response.status.value)
                        assertEquals(provider, sourceResponse.headers.entries.single {
                            it.key.equals("X-Provider", ignoreCase = true)
                        }.value.single())
                        val rawBody = assertNotNull(sourceResponse.body)
                        val snapshot = sourceResponse.toArazzoSnapshot()
                        if (sourceResponse.status !in 200..299) {
                            assertTrue(assertNotNull(response.content).contentEquals(rawBody))
                            val failure = ExecutionFailure("http_error", "Provider returned ${sourceResponse.status}")
                            CapabilityResult(
                                identity.locator, CapabilityOutcome.Failed(failure),
                                listOf(ConstituentResult(identity, ConstituentOutcome.Failed(failure), snapshot)),
                            )
                        } else {
                            assertTrue(!assertNotNull(response.content).contentEquals(rawBody))
                            val mapped = assertNotNull(context.bundle[WeaverPlugin.RESULT])
                            transformedReport = context.result
                            val transformed = BodyValue.JsonValue(mapped)
                            CapabilityResult(
                                identity.locator, CapabilityOutcome.Succeeded(transformed),
                                listOf(ConstituentResult(identity, ConstituentOutcome.Succeeded(transformed), snapshot)),
                            )
                        }
                    },
                )
            }
            val result = withContext(sourceCapture) {
                ArazzoRunExecutor(executor).execute(
                    workflow, WorkflowIdentity("arazzo", "1.1.0", registration.documentId, "1"), RunId("weather"),
                    inputs = input,
                )
            }
            if (expectedFailure != null) {
                assertEquals(expectedFailure, assertIs<ExecutionOutcome.Failed>(result.outcome).failure.code)
                if (expectedFailure == "input_mapping") {
                    assertTrue(requests.isEmpty())
                    assertTrue(sourceCapture.responses.isEmpty())
                } else {
                    assertEquals(1, requests.size)
                    assertEquals(status.value, sourceCapture.responses.single().status)
                    assertEquals(Json.parseToJsonElement(fixture), sourceCapture.responses.single().requireJsonBody())
                }
                return
            }
            val output = assertIs<ExecutionOutcome.Succeeded>(result.outcome, result.outcome.toString()).outputs
            val sourceResponse = Json.parseToJsonElement(output).jsonObject.getValue("providerResponse").jsonObject
            assertEquals(Json.parseToJsonElement(fixture), sourceResponse)
            val report = assertNotNull(transformedReport)
            if (provider == "brightsky" && workflowId == "forecast") {
                val hourly = assertIs<WeatherSection.Available<List<HourlyForecast>>>(report.hourly).value
                assertEquals(18.5, hourly.single().measurements.temperatureCelsius)
                assertEquals(1.0, hourly.single().measurements.windSpeedMetersPerSecond?.value)
            } else {
                val current = assertIs<WeatherSection.Available<CurrentConditions>>(report.current).value
                assertEquals(18.5, current.measurements.temperatureCelsius)
                if (provider == "brightsky") assertEquals(1.0, current.measurements.windSpeedMetersPerSecond?.value)
            }
            assertEquals(52.52, report.context.latitude)
            assertEquals(1, requests.size)
            val latitude = if (provider == "open-meteo") "latitude" else "lat"
            val longitude = if (provider == "open-meteo") "longitude" else "lon"
            assertTrue(requests.single().contains("$latitude=52.52"), requests.single())
            assertTrue(requests.single().contains("$longitude=13.41"), requests.single())
            if (provider == "open-meteo") {
                assertTrue(requests.single().contains("current="), requests.single())
                assertTrue(requests.single().contains("hourly="), requests.single())
                assertTrue(requests.single().contains("timezone=auto"), requests.single())
                assertTrue(requests.single().contains("wind_speed_unit=ms"), requests.single())
            }
        } finally {
            client.close()
        }
    }

    private companion object {
        const val BRIGHTSKY_CURRENT_RESPONSE = """
            {"sources":[{"lat":52.52,"lon":13.41}],
             "weather":{"timestamp":"2026-10-19T12:00:00+02:00","condition":"dry","temperature":18.5,
               "cloud_cover":40,"dew_point":10,"pressure_msl":1013,"relative_humidity":60,"visibility":20000,
               "wind_direction_10":180,"precipitation_60":0.2,"wind_speed_10":3.6,"wind_gust_speed_10":5.4,
               "solar_60":0.1,"sunshine_60":15}}
        """
        const val BRIGHTSKY_FORECAST_RESPONSE = """
            {"sources":[{"lat":52.52,"lon":13.41}],
             "weather":[{"timestamp":"2026-10-19T12:00:00+02:00","condition":"dry","temperature":18.5,
               "cloud_cover":40,"dew_point":10,"pressure_msl":1013,"relative_humidity":60,"visibility":20000,
               "wind_direction":180,"precipitation":0.2,"precipitation_probability":10,"wind_speed":3.6,
               "wind_gust_speed":5.4,"solar":0.1,"sunshine":15}]}
        """
        const val OPEN_METEO_RESPONSE = """
            {"latitude":52.52,"longitude":13.41,"timezone":"Europe/Berlin",
             "current":{"time":"2026-10-19T12:00:00+02:00","temperature_2m":18.5,"weather_code":2,
               "apparent_temperature":18.0,"relative_humidity_2m":60,"cloud_cover":40,"pressure_msl":1013.2,
               "precipitation":0.2,"wind_speed_10m":3.6,"wind_direction_10m":180,"wind_gusts_10m":5.4},
             "hourly":{"time":["2026-10-19T12:00:00+02:00"],"temperature_2m":[18.5],"weather_code":[2],
               "apparent_temperature":[18.0],"dew_point_2m":[10.0],"relative_humidity_2m":[60],
               "cloud_cover":[40],"precipitation":[0.2],"precipitation_probability":[10],"pressure_msl":[1013.2],
               "visibility":[20000],"wind_speed_10m":[3.6],"wind_direction_10m":[180],
               "wind_gusts_10m":[5.4],"shortwave_radiation":[100],"sunshine_duration":[900]},
             "daily":{"time":["2026-10-19"],"temperature_2m_min":[12.0],"temperature_2m_max":[20.0],
               "precipitation_sum":[1.2],"precipitation_probability_max":[20]}}
        """
    }
}
