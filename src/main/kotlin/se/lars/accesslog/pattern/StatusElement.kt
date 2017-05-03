package se.lars.accesslog.pattern

class StatusElement : AccessLogElement {
    override fun formattedValue(values: LogData) = values.status.toString()

    companion object {
        @JvmStatic
        fun matcher() = matchPatterns("sc-status", "%s") {
            StatusElement()
        }
    }
}

