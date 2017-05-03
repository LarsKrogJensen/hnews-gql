package se.lars.accesslog.pattern


import io.vertx.core.MultiMap
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpVersion
import io.vertx.ext.web.Cookie


data class LogData(val uri: String,
                   val query: String?,
                   val method: HttpMethod,
                   val status: Int,
                   val startMillis: Long,
                   val endMillis: Long,
                   val version: HttpVersion,
                   val bytesSent: Long,
                   val remoteHost: String,
                   val localHost: String,
                   val localPort: Int,
                   val requestHeaders: MultiMap,
                   val responseHeaders: MultiMap,
                   val cookies: Set<Cookie>
                  )