package se.lars.hnews

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricSet
import com.codahale.metrics.jvm.BufferPoolMetricSet
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.codahale.metrics.jvm.ThreadStatesGaugeSet
import io.vertx.core.AbstractVerticle
import se.lars.kutil.loggerFor
import java.lang.management.ManagementFactory
import javax.inject.Inject

class MetricsVerticle
@Inject
constructor(private val registry: MetricRegistry) : AbstractVerticle() {

    private val log = loggerFor<MetricsVerticle>()

    override fun start() {
        log.info("Metrics service started")
        registerAll("gc", GarbageCollectorMetricSet(), registry)
        registerAll("buffers", BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()), registry);
        registerAll("memory", MemoryUsageGaugeSet(), registry)
        registerAll("threads", ThreadStatesGaugeSet(), registry)

    }

    fun registerAll(prefix: String, metricSet: MetricSet, registry: MetricRegistry) {
        for ((key, value) in metricSet.metrics) {
            if (value is MetricSet) {
                registerAll(prefix + "." + key, value, registry)
            } else {
                registry.register(prefix + "." + key, value)
            }
        }

    }
}