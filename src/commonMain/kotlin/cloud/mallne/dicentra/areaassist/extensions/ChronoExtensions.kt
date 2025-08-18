package cloud.mallne.dicentra.areaassist.extensions

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


object ChronoExtensions {
    val dateTimeFormatter = LocalDateTime.Format {
        day()
        char('.')
        monthNumber()
        char('.')
        year()
        char(' ')
        hour()
        char(':')
        minute()
    }
    val dateFormatter = LocalDateTime.Format {
        day()
        char('.')
        monthNumber()
        char('.')
        year()
    }
    val timeFormatter = LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }

    @OptIn(ExperimentalTime::class)
    fun Instant.toMinimalString(wrap: Boolean = false): String =
        this.toLocalDateTime().toMinimalString(wrap)

    @OptIn(ExperimentalTime::class)
    fun LocalDateTime.Companion.now() =
        Clock.System.now().toLocalDateTime()

    fun LocalDateTime.copy(
        year: Int = this.year,
        month: Month = this.month,
        day: Int = this.day,
        hour: Int = this.hour,
        minute: Int = this.minute,
        second: Int = this.second,
        nanosecond: Int = this.nanosecond,
    ): LocalDateTime {
        return LocalDateTime(
            year = year,
            month = month,
            day = day,
            hour = hour,
            minute = minute,
            second = second,
            nanosecond = nanosecond
        )
    }

    @OptIn(ExperimentalTime::class)
    fun Instant.toLocalDateTime() = this.toLocalDateTime(TimeZone.currentSystemDefault())

    @OptIn(ExperimentalTime::class)
    fun LocalDateTime.isToday(): Boolean {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return this.year == now.year && this.month == now.month && day == now.day
    }

    @OptIn(ExperimentalTime::class)
    fun Instant.isToday(): Boolean = toLocalDateTime().isToday()
    fun LocalDateTime.toMinimalString(wrap: Boolean = false): String {
        return if (this.isToday()) {
            this.format(timeFormatter)
        } else {
            if (wrap) {
                this.format(dateTimeFormatter).replace(" ", "\n")
            } else {
                this.format(dateTimeFormatter)
            }
        }
    }
}