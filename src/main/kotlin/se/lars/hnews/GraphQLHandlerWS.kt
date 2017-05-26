package se.lars.hnews

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import reactor.core.Disposable
import se.lars.hnews.services.IHackerNewsService
import se.lars.kutil.jsonObject
import javax.inject.Inject

class GraphQLHandlerWS
@Inject
constructor(hackerNews: IHackerNewsService) : Handler<RoutingContext> {

    override fun handle(routingContext: RoutingContext) {

        // Upgrade request to WebSocket
        val ws = routingContext.request().upgrade()
        SubscriptionHandler(ws)
    }

}

class SubscriptionHandler(private val ws: ServerWebSocket) {
    private val subscriptions: Map<String, Disposable> = mutableMapOf()
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

    }

    private fun handleSubscriptionStart(ws: ServerWebSocket, input: JsonObject) {
        ws.writeFinalTextFrame(jsonObject("type" to "subscription_success",
                                          "id" to input.getInteger("id")).toString())
    }

    private fun handleInit(ws: ServerWebSocket) {
        ws.writeFinalTextFrame(jsonObject("type" to "init_success").toString())

    }

    private fun handleException(ex: Throwable) {
        println(ex)

    }
}