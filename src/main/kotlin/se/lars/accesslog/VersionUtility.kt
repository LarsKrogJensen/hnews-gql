package se.lars.accesslog

import io.vertx.core.http.HttpVersion
import se.lars.accesslog.pattern.LogData

object VersionUtility {

    private val EMPTY_VERSION = "-"

    fun formattedValue(values: LogData) = when (values.version) {
        HttpVersion.HTTP_1_0 -> "HTTP/1.0"
        HttpVersion.HTTP_1_1 -> "HTTP/1.1"
        HttpVersion.HTTP_2   -> "HTTP/2.0"
        else                 -> EMPTY_VERSION
    }
}