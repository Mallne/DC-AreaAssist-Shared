package cloud.mallne.dicentra.areaassist.model.actions

import cloud.mallne.dicentra.aviator.client.ktor.invoke
import cloud.mallne.dicentra.aviator.core.engine.AviatorEngine
import cloud.mallne.dicentra.aviator.core.engine.OpenApiClientInput
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.model.ServiceLocator

suspend fun AviatorEngine.action(locator: ServiceLocator, id: String): ServersideActionHolder? =
    invoke<ServersideActionHolder>(
        locator,
        OpenApiClientInput(parameters = RequestParameters(mutableMapOf("id" to RequestParameter.Single(id)))),
    ).single().result.getOrThrow().result
