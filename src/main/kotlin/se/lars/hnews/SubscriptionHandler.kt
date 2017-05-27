package se.lars.hnews

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import se.lars.hnews.services.IHackerNewsService
import se.lars.kutil.loggerFor
import javax.inject.Inject

class SubscriptionHandler
@Inject
constructor(private val hackerNews: IHackerNewsService) : Handler<RoutingContext> {
    private val log = loggerFor<SubscriptionHandler>()

    override fun handle(routingContext: RoutingContext) {

        // Upgrade request to WebSocket
        val ws = routingContext.request().upgrade()

        val vertx = routingContext.vertx()

        vertx.deployVerticle(SubscriptionVerticle(ws.textHandlerID(), hackerNews)) { event: AsyncResult<String> ->
            val eventBus = vertx.eventBus()
            ws.textMessageHandler { msg: String ->
                eventBus.send(ws.textHandlerID() + "-sub", JsonObject(msg))
            }.closeHandler {
                vertx.undeploy(event.result())
            }.exceptionHandler {
                log.error("WebSocket failure", it)
            }
        }
    }

}