package se.lars.sse

import io.vertx.core.AsyncResult
import io.vertx.core.VertxException
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient

class EventSource(val client: HttpClient) : IEventSource {

    private var connected: Boolean = false
    private var lastId: String? = null
    private var messageHandler: (Event) -> Unit = {}
    private val currentPacket: SSEPacket = SSEPacket()
    private var closeHandler: () -> Unit = {}

    override fun connect(path: String, handler: (AsyncResult<Void>) -> Unit): IEventSource {
        return connect(path, null, handler)
    }

    override fun connect(path: String, lastEventId: String?, handler: (AsyncResult<Void>) -> Unit): IEventSource {
        if (connected) {
            throw VertxException("SSEConnection already connected")
        }
        val request = client.get(path) { response ->
            if (response.statusCode() != 200) {
                handler(SSEAsyncResult(ConnectionRefusedException(response)))
            } else {
                connected = true
                response.handler(this::handleMessage)
                response.endHandler {
                    closeHandler()
                }
                handler(SSEAsyncResult())
            }
        }
        with(request) {
            setFollowRedirects(true)
            lastEventId?.let {
                headers().add("Last-Event-ID", lastEventId)
            }
            isChunked = true
            headers().add("Accept", "text/event-stream")
            end()
        }

        return this
    }

    override fun close(): IEventSource {
        client.close()
        connected = false
        return this
    }

    override fun onMessage(handler: (Event) -> Unit): IEventSource {
        this.messageHandler = handler
        return this
    }

    override fun onClose(handler: () -> Unit): IEventSource {
        this.closeHandler = handler
        return this
    }

    override fun lastId() = lastId

    private fun handleMessage(buffer: Buffer) {
        currentPacket.append(buffer).forEach(messageHandler)
    }
}


