package se.lars.hnews

import com.codahale.metrics.MetricRegistry
import io.vertx.ext.web.RoutingContext
import se.lars.hnews.services.IHackerNewsService
import javax.inject.Inject

class GraphQLHandler
@Inject
constructor(
    hackerNews: IHackerNewsService,
    metricRegistry: MetricRegistry
) : GraphQLHandlerBase(hackerNews, metricRegistry) {

    override fun handle(routingContext: RoutingContext) {
        executeGraphQL(routingContext.body.toString()) {
            routingContext.response()
                    .putHeader("Content-Type", "application/json")
                    .end(it.encode())
        }
    }

}