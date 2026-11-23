package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.areaassist.model.Point
import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions

/** A configured provider together with the service object used to execute its Aviator operation. */
data class WeatherProviderCandidate<T>(
    val options: WeatherServiceOptions,
    val service: T,
)

/**
 * Orders configured providers for a location and capability. Area coverage is only an eligibility
 * hint; callers must still fall through to the next candidate if the selected provider fails.
 */
object WeatherProviderSelection {
    fun <T> select(
        candidates: List<WeatherProviderCandidate<T>>,
        latitude: Double,
        longitude: Double,
    ): List<WeatherProviderCandidate<T>> {
        return order(candidates.filter { isEligible(it.options, latitude, longitude) })
    }

    fun <T> order(candidates: List<WeatherProviderCandidate<T>>): List<WeatherProviderCandidate<T>> =
        candidates.sortedWith(
            compareBy<WeatherProviderCandidate<T>> {
                it.options.coverage?.coordinates?.isNotEmpty() != true
            }
                .thenBy { it.options.isGlobalDefault }
                .thenBy { it.options.priority }
                .thenBy { it.options.providerId.orEmpty() }
        )

    fun isEligible(options: WeatherServiceOptions, latitude: Double, longitude: Double): Boolean {
        val boundary = options.coverage ?: return true
        if (boundary.coordinates.isEmpty()) return true
        return CalculationExtensions.rayCast(Point(longitude, latitude), boundary)
    }
}
