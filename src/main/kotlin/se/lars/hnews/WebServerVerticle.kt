package se.lars.hnews

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.net.PemKeyCertOptions
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import se.lars.accesslog.AccessLogHandler
import se.lars.kutil.loggerFor
import se.lars.kutil.router
import javax.inject.Inject

class WebServerVerticle
@Inject
constructor(
        private val serverOptions: IServerOptions
) : AbstractVerticle() {

    private val _log = loggerFor<WebServerVerticle>()
    private lateinit var httpServer: HttpServer

    override fun start(startFuture: Future<Void>) {
        val options = HttpServerOptions().apply {
            isCompressionSupported = true
            if (serverOptions.useSsl) {
                isUseAlpn = true
                isSsl = true
                pemKeyCertOptions = PemKeyCertOptions().apply {
                    keyPath = "tls/server-key.pem"
                    certPath = "tls/server-cert.pem"
                }
            }
            websocketSubProtocols = "graphql-subscriptions"
        }

        // configure cross domain access
        val corsHandler = with(CorsHandler.create("*")) {
            allowCredentials(true)
            allowedMethod(HttpMethod.POST)
            allowedHeaders(setOf("content-type", "authorization"))
        }


        val router = router(vertx) {
            route().handler(AccessLogHandler.create("%r %s \"%{Content-Type}o\" %D %T %B"))
            route().handler(corsHandler)
            route().handler(BodyHandler.create())
//            route("/graphql").handler(graphQLHandler)
            route("/*").handler(StaticHandler.create().setCachingEnabled(false))
        }
        httpServer = vertx.createHttpServer(options)
                .requestHandler { router.accept(it) }
                .listen(serverOptions.httpPort) {
                    when (it.succeeded()) {
                        true  -> {
                            _log.info("Http service started. on port " + it.result().actualPort())
                            startFuture.succeeded()
                        }
                        false -> {
                            _log.error("Http service failed to started.")
                            startFuture.fail(it.cause())
                        }
                    }
                }
    }

    override fun stop(stopFuture: Future<Void>) {
        _log.info("Stopping Http service...")
        httpServer.close {
            _log.info("Http service stopped")
            stopFuture.complete()
        }

    }
}