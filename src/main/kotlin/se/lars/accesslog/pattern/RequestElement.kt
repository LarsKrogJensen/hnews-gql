package se.lars.accesslog.pattern

import se.lars.accesslog.VersionUtility

class RequestElement private constructor(requestLogMode: RequestLogMode) : AccessLogElement {
    private val requestLogMode: RequestLogMode? = requestLogMode

    private enum class RequestLogMode {
        APACHE_FIRST_REQUEST_LINE, // Identical to %m %U%q %H" will log the method, path, query-string, and protocol
        URI,
        QUERY_STRING,
        URI_QUERY,
        UNKNOWN
    }

    override fun formattedValue(values: LogData): String {

        val sb = StringBuilder()

        if (RequestLogMode.APACHE_FIRST_REQUEST_LINE == this.requestLogMode) {
            sb.append(values.method).append(' ')
        }

        if (RequestLogMode.QUERY_STRING != this.requestLogMode) {
            sb.append(values.uri)
        }

        if (RequestLogMode.URI != this.requestLogMode && values.query?.isEmpty() ?: false) {
            sb.append('?').append(values.query)
        }

        if (RequestLogMode.APACHE_FIRST_REQUEST_LINE == this.requestLogMode) {
            sb.append(' ').append(VersionUtility.formattedValue(values))
        }

        return sb.toString()
    }

    companion object {
        fun matcher() = matchPatterns("%r", "%U", "cs-uri-stem", "%q", "cs-uri-query", "cs-uri") {
            RequestElement(when (it) {
                "%r" -> RequestLogMode.APACHE_FIRST_REQUEST_LINE
                "%U", "cs-uri-stem" -> RequestLogMode.URI
                "%q", "cs-uri-query" -> RequestLogMode.QUERY_STRING
                "cs-uri" -> RequestLogMode.URI_QUERY
                else -> RequestLogMode.UNKNOWN
            })
        }
    }
}
