package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.dicentra.areaassist.model.AuthServiceOptions
import cloud.mallne.dicentra.areaassist.model.screen.DeepLinks
import cloud.mallne.dicentra.areaassist.statics.api.*
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.model.SemVer
import cloud.mallne.dicentra.aviator.model.ServiceLocator

object APIs {
    val mapLight by MapLight
    val mapDark by MapDark
    val esri by Esri
    val thueringenWfs by DE_TH
    val badenWuerttembergWfs by DE_BW
    val brightSky by Brightsky

    object OAuth2 {
        const val CLIENT_ID = "client_id"
        const val REDIRECT_URI = "redirect_uri"
        const val STATE = "state"
        const val RESPONSE_TYPE = "response_type"
        const val CODE = "code"
        const val GRANT_TYPE = "grant_type"
        const val REFRESH_TOKEN = "refresh_token"
        const val REFERRER_URI = "referrer_uri"
        const val REFERRER = "referrer"

        val APP_REDIRECT_URI = DeepLinks.DCAA.login

        fun paramsForAuthorization(
            redirectUri: String = APP_REDIRECT_URI,
            state: String,
            responseType: String = "code",
            serviceOptions: AuthServiceOptions,
        ): RequestParameters = serviceOptions.asParameter(
            mapOf(
                REDIRECT_URI to RequestParameter.Single(redirectUri),
                STATE to RequestParameter.Single(state),
                RESPONSE_TYPE to RequestParameter.Single(responseType),
            )
        )

        fun paramsForAuthConsole(
            referrerUri: String = APP_REDIRECT_URI,
            serviceOptions: AuthServiceOptions,
            referrer: String = serviceOptions.clientId,
        ): RequestParameters = serviceOptions.asParameter(
            mapOf(
                REFERRER_URI to RequestParameter.Single(referrerUri),
                REFERRER to RequestParameter.Single(referrer),
            )
        )

        fun paramsForToken(
            code: String,
            redirectUri: String = APP_REDIRECT_URI,
            grantType: String = "authorization_code",
            serviceOptions: AuthServiceOptions,
        ): RequestParameters = serviceOptions.asParameter(
            mapOf(
                REDIRECT_URI to RequestParameter.Single(redirectUri),
                CODE to RequestParameter.Single(code),
                GRANT_TYPE to RequestParameter.Single(grantType),
            )
        )

        fun paramsForRefreshToken(
            refreshToken: String,
            grantType: String = "refresh_token",
            serviceOptions: AuthServiceOptions,
        ): RequestParameters = serviceOptions.asParameter(
            mapOf(
                REFRESH_TOKEN to RequestParameter.Single(refreshToken),
                GRANT_TYPE to RequestParameter.Single(grantType),
            )
        )
    }

    val apis = listOf(
        mapLight,
        mapDark,
        esri,
        thueringenWfs,
        badenWuerttembergWfs,
        brightSky,
    )

    fun apiOverrideVersion(version: SemVer = ParcelConstants.endpointVersion): List<OpenAPI> {
        return apis.map { it.copy(info = it.info.copy(version = version.toString())) }
    }

    enum class Services(
        private val serviceLocator: String,
    ) {
        DISCOVERY_SERVICE("DCAACodexDiscoveryBundle"),
        SERVERSIDE_ACTIONS("DCAACodexServerSideActions"),
        WEATHER_SERVICE_CURRENT("&.scribe.weatherService.current"),
        WEATHER_SERVICE_WARNING("&.scribe.weatherService.warning"),
        WEATHER_SERVICE_FORECAST("&.scribe.weatherService.forecast"),
        MAPLAYER("&.surveyor.map"),
        PARCEL_SERVICE("&.curator.parcelService"),
        AUTH_TOKEN("&.warden.token"),
        AUTH_AUTHORIZATION("&.warden.auth"),
        AUTH_ACCOUNT("&.warden.account");

        fun locator(flavour: ServiceMethods): ServiceLocator {
            return ServiceLocator(
                this.serviceLocator,
                flavour
            )
        }
    }
}