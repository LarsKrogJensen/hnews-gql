package se.lars.accesslog.pattern


class DurationElement(private val timeUnit: TimeUnit) : AccessLogElement {

    enum class TimeUnit {
        SECONDS,
        MILLISECONDS
    }

    override fun formattedValue(values: LogData): String {
        var duration = values.endMillis - values.startMillis
        if (TimeUnit.SECONDS == this.timeUnit) {
            duration = duration / 1000
        }

        return if (duration > 0) duration.toString() else "0"
    }

    companion object {
        fun matcher() = matchPatterns("%D", "%T") { pattern: String ->
            if (pattern == "%D") DurationElement(TimeUnit.MILLISECONDS)
            else DurationElement(TimeUnit.SECONDS)
        }
    }
}
