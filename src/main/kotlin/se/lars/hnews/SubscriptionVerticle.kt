package se.lars.hnews

import com.codahale.metrics.SharedMetricRegistries
import graphql.ExecutionResult
import graphql.execution.instrumentation.NoOpInstrumentation
import graphql.newGraphQL
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import reactor.core.Disposable
import reactor.core.publisher.Flux
import se.lars.hnews.services.IHackerNewsService
import se.lars.kutil.jsonObject
import se.lars.kutil.loggerFor
import se.lars.kutil.thenOn


// https://github.com/apollographql/subscriptions-transport-ws/blob/cacb8692f3601344a4101d802443d046d73f8b23/README.md#client-server-communication
class SubscriptionVerticle(
    private val wsId: String,
    private val hackerNews: IHackerNewsService
) : AbstractVerticle() {

    private val log = loggerFor<ServerWebSocket>()
    private val subscriptions: MutableMap<Int, Disposable> = mutableMapOf()
    private var keepAliveId: Long = -1
    private val metricsRegistry = SharedMetricRegistries.getOrCreate("hnews-metrics")

    override fun start(startFuture: Future<Void>) {
        vertx.eventBus().consumer<JsonObject>(wsId + "-sub") { msg: Message<JsonObject> ->
            val json = msg.body()
            log.info("handleMessage:  $json")

            when (json.getString("type")) {
                "init"               -> handleInit()
                "subscription_start" -> handleSubscriptionStart(json)
                "subscription_end"   -> handleSubscriptionEnd(json)
            }
        }

        vertx.setPeriodic(30_000L) {
            sendMessage("type" to "keepalive")
        }

        SharedMetricRegistries.getOrCreate("vertx")
        
        startFuture.complete()
    }

    override fun stop() {
        log.info("Stopping")
        subscriptions.values.forEach { it.dispose() }
        subscriptions.clear()
        vertx.cancelTimer(keepAliveId)
    }

    private fun handleInit() {
        log.info("handleInit")
        sendMessage("type" to "init_success")
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

//        metricsRegistry.
        graphQL.execute(query, null, context, variables)
            .thenOn(super.context)
            .thenAccept { result ->
                if (result.succeeded()) {

                    sendMessage("type" to "subscription_success",
                                "id" to subscriptionId)
                    val data = result.data<Map<String, Flux<ExecutionResult>>>()

                    val disposables = data.map { (field: String, flux: Flux<ExecutionResult>) ->
                        flux.subscribe(
                            { data -> onData(subscriptionId, field, data.data<Any>()) },
                            { ex -> log.error("onError: $field - $ex") },
                            { log.info("onCompleted: $field") }
                        )
                    }
                    subscriptions[subscriptionId] = CompositeDisposable(disposables)

                } else {
                    log.error("subscribe error ${result.errors}")
                    sendMessage("type" to "subscription_fail",
                                "id" to subscriptionId,
                                "payload" to jsonObject("errors" to result.errors))
                }
            }
            .exceptionally {
                log.error("subscribe error", it)
                sendMessage("type" to "subscription_fail",
                            "id" to subscriptionId,
                            "payload" to jsonObject("errors" to "Internal Server Error"))
                null
            }
    }

    private fun onData(subscriptionId: Int, field: String, data: Any) {
//        log.info("onNext: $wsId $subscriptionId $field - $data")
        sendMessage("type" to "subscription_data",
                    "id" to subscriptionId,
                    "payload" to jsonObject("data" to jsonObject(field to data)))

    }

    private fun sendMessage(vararg fields: Pair<String, Any?>) {
        sendMessage(JsonObject(mapOf(*fields)))
    }

    private fun sendMessage(json: JsonObject) {
        vertx.eventBus().send(wsId, json.toString())
    }
}

class CompositeDisposable(val disposables: List<Disposable>) : Disposable {
    override fun dispose() {
        disposables.forEach { it.dispose() }
    }

}