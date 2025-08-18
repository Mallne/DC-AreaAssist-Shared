package cloud.mallne.dicentra.areaassist.extensions

import kotlin.math.pow
import kotlin.math.round


object NumericalExtensions {
    /**
     * Rounds a Double to the specified number of decimal places.
     *
     * @param decimals The number of decimal places to round to. Must be non-negative.
     * @return The rounded Double.
     * @throws IllegalArgumentException if the number of decimals is negative.
     */
    fun Double.round(decimals: Int): Double {
        require(decimals >= 0) { "Decimals must be non-negative" }
        if (decimals == 0) {
            return round(this)
        }
        val multiplier = 10.0.pow(decimals)
        return round(this * multiplier) / multiplier
    }

    fun Double.removeTrailingZeros(): String {
        val string = this.toString()
        return if (!string.contains('.')) {
            string
        } else {
            string.trimEnd('0').trimEnd('.')
        }
    }

    fun Float.round(decimals: Int): Double {
        return this.toDouble().round(decimals)
    }

    fun Int.positive(): Boolean = this >= 0
    fun Int.negative(): Boolean = this < 0
}