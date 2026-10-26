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
        val point = Point(longitude, latitude)
        val eligible = candidates.filter { candidate ->
            candidate.options.coverage?.let { boundary ->
                boundary.coordinates.isEmpty() || CalculationExtensions.rayCast(point, boundary)
            } ?: true
        }
        return eligible.sortedWith(
            compareBy<WeatherProviderCandidate<T>> {
                it.options.coverage?.coordinates?.isNotEmpty() != true
            }
                .thenBy { it.options.priority }
        )
    }
}
