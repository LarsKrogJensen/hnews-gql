package se.lars.hnews

import io.vertx.ext.web.RoutingContext
import se.lars.hnews.services.IHackerNewsService
import javax.inject.Inject

class GraphQLHandler
@Inject
constructor(hackerNews: IHackerNewsService) : GraphQLHandlerBase(hackerNews) {

    override fun handle(routingContext: RoutingContext) {
        executeGraphQL(routingContext.body.toString()) {
            routingContext.response()
                    .putHeader("Content-Type", "application/json")
                    .end(it.encode())
        }
    }

}