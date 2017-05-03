package se.lars.accesslog

import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import se.lars.accesslog.pattern.LogData
import se.lars.accesslog.pattern.PatternResolver
import java.io.File
import java.net.URL
import java.util.*

class AccessLogHandlerImpl(pattern: String) : AccessLogHandler {
    private val logger = LoggerFactory.getLogger(AccessLogHandlerImpl::class.java)
    private val outputConfiguration: OutputConfiguration

    init {
        val patternResolver = PatternResolver()
        val resolvedPattern = patternResolver.resolvePattern(pattern)

        outputConfiguration = OutputConfiguration(resolvedPattern.resolvedPattern,
                resolvedPattern.logElements,
                listOf(logger))
    }


    override fun handle(context: RoutingContext) {
        val startTSmillis = System.currentTimeMillis()
        context.addBodyEndHandler {
            log(context, startTSmillis)
        }

        context.next()
    }

    private fun log(context: RoutingContext, startTSmillis: Long) {

        val request = context.request()
        val response = context.response()


        val data = LogData(
                uri = request.path(),
                query = request.query(),
                method = request.method(),
                status = response.statusCode,
                startMillis = startTSmillis,
                endMillis = System.currentTimeMillis(),
                version = request.version(),
                bytesSent = response.bytesWritten(),
                remoteHost = request.remoteAddress().host(),
                localHost = request.localAddress().host(),
                localPort = request.localAddress().port(),
                requestHeaders = request.headers(),
                responseHeaders = response.headers(),
                cookies = context.cookies()
        )


        outputConfiguration.doLog(data)

    }

}
