package se.lars.hnews

import graphql.execution.instrumentation.NoOpInstrumentation
import graphql.newGraphQL
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import reactor.core.Disposable
import se.lars.hnews.services.IHackerNewsService
import se.lars.kutil.jsonObject
import se.lars.kutil.loggerFor
import se.lars.kutil.thenOn
import javax.inject.Inject

class GraphQLHandlerWS
@Inject
constructor(private val hackerNews: IHackerNewsService) : Handler<RoutingContext> {

    override fun handle(routingContext: RoutingContext) {

        // Upgrade request to WebSocket
        val ws = routingContext.request().upgrade()
        SubscriptionHandler(ws, hackerNews)
    }

}

// https://github.com/apollographql/subscriptions-transport-ws/blob/cacb8692f3601344a4101d802443d046d73f8b23/README.md#client-server-communication
class SubscriptionHandler(
    private val ws: ServerWebSocket,
    private val hackerNews: IHackerNewsService) {

    private val log = loggerFor<SubscriptionHandler>()
    private val subscriptions: MutableMap<Int, Disposable> = mutableMapOf()

    init {
        ws.handler(this::handleMessage).exceptionHandler(this::handleException)
    }

    private fun handleMessage(buffer: Buffer) {
        val input = buffer.toJsonObject()
        println("Received WS message: $input")

        when (input.getString("type")) {
            "init"               -> handleInit(ws)
            "subscription_start" -> handleSubscriptionStart(ws, input)
            "subscription_end"   -> handleSubscriptionEnd(ws, input)
        }
    }

    private fun handleSubscriptionEnd(ws: ServerWebSocket, input: JsonObject) {
        val id = input.getInteger("id")
        subscriptions.remove(id)?.dispose()
    }

    private fun handleSubscriptionStart(ws: ServerWebSocket, input: JsonObject) {
        val id = input.getInteger("id")
        val query = input.getString("query")
        val variables = input.getValue("variables").let {
            when (it) {
                is JsonObject -> it.map
                else          -> emptyMap<String, Any>()
            }
        }

        val graphQL = newGraphQL {
            schema = hackeNewsSchema
            instrumentation = NoOpInstrumentation.INSTANCE
        }

        val context = RequestContext(hackerNews)

        graphQL.execute(query, null, context, variables)
            .thenOn(Vertx.currentContext())
            .thenAccept { result ->
                if (result.succeeded()) {
                    ws.writeFinalTextFrame(jsonObject("type" to "subscription_success",
                                                      "id" to input.getInteger("id")).toString())

                    

                } else {
                    ws.writeFinalTextFrame(jsonObject("type" to "subscription_fail",
                                                      "id" to input.getInteger("id"),
                                                      "payload" to result.errors).toString())
                }
            }
            .exceptionally {
                log.error("Execution error", it)
                //handler(jsonObject("errors" to "Internal Server Error"))
                null
            }
    }

    private fun handleInit(ws: ServerWebSocket) {
        ws.writeFinalTextFrame(jsonObject("type" to "init_success").toString())

    }

    private fun handleException(ex: Throwable) {
        println(ex)

    }
}