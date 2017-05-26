package se.lars.hnews

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import se.lars.accesslog.AccessLogHandler
import se.lars.kutil.router
import javax.inject.Inject

class RouterFactory
@Inject constructor(
        val graphQLHandler: GraphQLHandler,
        val graphQLHandlerWS: GraphQLHandlerWS,
        val vertx: Vertx
) : IRouterFactory {
    override fun router(): Router {
        // configure cross domain access
        val corsHandler = with(CorsHandler.create("*")) {
            allowCredentials(true)
            allowedMethod(HttpMethod.POST)
            allowedHeaders(setOf("content-type", "authorization"))
        }

        return router(vertx) {
            route().handler(AccessLogHandler.create("%r %s \"%{Content-Type}o\" %D %T %B"))
            route().handler(corsHandler)
            route().handler(BodyHandler.create())
            route("/graphql").handler(graphQLHandler)
            route("/graphqlws").handler(graphQLHandlerWS)
            route("/*").handler(StaticHandler.create().setCachingEnabled(false))
        }
    }
}