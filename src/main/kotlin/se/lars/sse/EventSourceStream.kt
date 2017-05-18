package se.lars.sse

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import reactor.core.publisher.Flux

class EventSourceStream(
    vertx: Vertx,
    private val host: String,
    private val port: Int = 443,
    private val useSsl: Boolean = true
) {
    private val client: HttpClient

    init {
        val options = HttpClientOptions().apply {
            defaultHost = host
            defaultPort = port
            isKeepAlive = true
            idleTimeout = 90
            isSsl = useSsl
        }
        client = vertx.createHttpClient(options)
    }

    fun connect(path: String): Flux<Event> {
        return Flux.create { sink ->
            createEventSource(client).connect(path) {
                if(it.failed()) {
                    sink.error(it.cause())
                }
            }.onClose {
                sink.complete()
            }.onMessage {
                sink.next(it)
            }
        }
    }

}