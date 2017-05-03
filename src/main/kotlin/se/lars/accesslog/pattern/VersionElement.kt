package se.lars.accesslog.pattern

import se.lars.accesslog.VersionUtility

class VersionElement : AccessLogElement {
    companion object {
        fun matcher() = matchPatterns("%H") { VersionElement() }
    }

    override fun formattedValue(values: LogData) = VersionUtility.formattedValue(values)
}
