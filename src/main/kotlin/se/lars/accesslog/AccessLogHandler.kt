package se.lars.accesslog

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext


interface AccessLogHandler : Handler<RoutingContext> {
    companion object {
        fun create(pattern : String) : AccessLogHandler = AccessLogHandlerImpl(pattern)
    }
}