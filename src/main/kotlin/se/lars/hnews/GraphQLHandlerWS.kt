package se.lars.hnews

import graphql.ExecutionResult
import graphql.execution.instrumentation.NoOpInstrumentation
import graphql.newGraphQL
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import reactor.core.Disposable
import reactor.core.publisher.Flux
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
        SubscriptionHandler(routingContext.vertx(), ws, hackerNews)
    }

}

// https://github.com/apollographql/subscriptions-transport-ws/blob/cacb8692f3601344a4101d802443d046d73f8b23/README.md#client-server-communication
class SubscriptionHandler(
    private val vertx: Vertx,
    private val ws: ServerWebSocket,
    private val hackerNews: IHackerNewsService) {

    private val log = loggerFor<SubscriptionHandler>()
    private val subscriptions: MutableMap<Int, Disposable> = mutableMapOf()
    private val heartbeatTimer: Long
    private val vertxContext = vertx.orCreateContext


    init {
        ws.textMessageHandler(this::handleMessage)
            .closeHandler(this::handleClose)
            .exceptionHandler(this::handleException)
        heartbeatTimer = vertx.setPeriodic(10_000, this::sendHeartbeat)
    }

    private fun handleMessage(message: String) {
        val input = JsonObject(message)
        log.info("handleMessage:  $input")

        when (input.getString("type")) {
            "init"               -> handleInit()
            "subscription_start" -> handleSubscriptionStart(input)
            "subscription_end"   -> handleSubscriptionEnd(input)
        }
    }

    private fun handleClose(v: Void?) {
        log.info("handleClose")
        subscriptions.values.forEach { it.dispose() }
        vertx.cancelTimer(heartbeatTimer)
        subscriptions.clear()
    }

    private fun handleSubscriptionEnd(input: JsonObject) {
        log.info("handleSubscriptionEnd:  $input")
        val id = input.getInteger("id")
        subscriptions.remove(id)?.dispose()
    }

    private fun handleSubscriptionStart(input: JsonObject) {
        log.info("handleSubscriptionStart:  $input")

        val subscriptionId = input.getInteger("id")
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

                    writeMessage("type" to "subscription_success",
                                 "id" to subscriptionId)
                    val data = result.data<Map<String, Flux<ExecutionResult>>>()

                    val disposables = data.map { (field: String, flux: Flux<ExecutionResult>) ->
                        flux.subscribe(
                                { data ->  onData(subscriptionId, field, data.data<Any>())},
                                { ex -> log.error("onError: $field - $ex") },
                                { log.info("onCompleted: $field") }
                            )
                    }
                    subscriptions[subscriptionId] = CompositeDisposable(disposables)

                } else {
                    log.error("subscribe error ${result.errors}")
                    writeMessage("type" to "subscription_fail",
                                 "id" to subscriptionId,
                                 "payload" to jsonObject("errors" to result.errors))
                }
            }
            .exceptionally {
                log.error("subscribe error", it)
                writeMessage("type" to "subscription_fail",
                             "id" to subscriptionId,
                             "payload" to jsonObject("errors" to "Internal Server Error"))
                null
            }
    }

    private fun onData(subscriptionId: Int, field: String, data: Any) {
        vertxContext.runOnContext {
            log.info("onNext: ${ws.textHandlerID()} $subscriptionId $field - ${data}")
            writeMessage("type" to "subscription_data",
                         "id" to subscriptionId,
                         "payload" to jsonObject("data" to jsonObject(field to data)))

        }
    }

    private fun handleInit() {
        log.info("handleInit")
        writeMessage("type" to "init_success")
    }

    private fun handleException(ex: Throwable) {
        log.info("handleException")
    }

    private fun sendHeartbeat(time: Long) {
        //writeMessage("type" to "keep_alive")
    }

    private fun writeMessage(vararg fields: Pair<String, Any?>) {
        writeMessage(JsonObject(mapOf(*fields)))
    }

    private fun writeMessage(json: JsonObject) {
        try {
            ws.writeFinalTextFrame(json.toString())
        } catch(e: Exception) {
            log.error("Failed to write message on web socket, closing")
        }
    }
}

class CompositeDisposable(val disposables: List<Disposable>) : Disposable {
    override fun dispose() {
        disposables.forEach { it.dispose() }
    }

}