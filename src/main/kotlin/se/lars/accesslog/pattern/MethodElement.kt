package se.lars.accesslog.pattern


class MethodElement : AccessLogElement {
    companion object {
        fun matcher() = matchPatterns("cs-method", "%m") {
            MethodElement()
        }
    }

    override fun formattedValue(values: LogData) = values.method.name
}
