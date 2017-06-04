package se.lars.hnews

import io.vertx.core.Handler
import io.vertx.ext.dropwizard.MetricsService
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class MetricsHandler
@Inject
constructor(
    private val metricsService: MetricsService
) : Handler<RoutingContext> {

    override fun handle(rc: RoutingContext) {
        val metrics = metricsService.getMetricsSnapshot(rc.vertx())
        metrics.mergeIn(metricsService.getMetricsSnapshot("hnews"))
        rc.response().end(metrics.toString())
    }
}