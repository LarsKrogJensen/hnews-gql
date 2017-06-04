package se.lars.hnews

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.net.PemKeyCertOptions
import se.lars.kutil.loggerFor
import javax.inject.Inject

class WebServerVerticle
@Inject
constructor(
        private val serverOptions: IServerOptions,
        private val routerFactory: IRouterFactory
) : AbstractVerticle() {

    private val log = loggerFor<WebServerVerticle>()
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
            websocketSubProtocols = "graphql-subscriptions, graphql-ws"
        }


        httpServer = vertx.createHttpServer(options)
                .requestHandler { routerFactory.router().accept(it) }
                .listen(serverOptions.httpPort) {
                    when (it.succeeded()) {
                        true  -> {
                            log.info("Http service started. on port " + it.result().actualPort())
                            startFuture.succeeded()
                        }
                        false -> {
                            log.error("Http service failed to started.")
                            startFuture.fail(it.cause())
                        }
                    }
                }
    }

    override fun stop(stopFuture: Future<Void>) {
        log.info("Stopping Http service...")
        httpServer.close {
            log.info("Http service stopped")
            stopFuture.complete()
        }

    }
}