package se.lars.sse

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions

interface IEventSource {

    fun connect(path: String, handler: (AsyncResult<Void>) -> Unit): IEventSource

    fun connect(path: String, lastEventId: String?, handler: (AsyncResult<Void>) -> Unit): IEventSource

    fun close(): IEventSource

    fun onMessage(handler: (Event) -> Unit): IEventSource

    fun onClose(handler: () -> Unit): IEventSource

    fun lastId(): String?

    companion object {

        fun create(client: HttpClient): IEventSource {
            return EventSource(client)
        }
    }
}

data class Event (
    val event: String?,
    val data: String?,
    val id: String?,
    val retry: Int?

)