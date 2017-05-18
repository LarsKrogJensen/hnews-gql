package se.lars.sse

import io.vertx.core.http.HttpClientResponse

class ConnectionRefusedException(private val response: HttpClientResponse) : Exception(response.statusMessage()) {

    val statusCode: Int
        get() = response.statusCode()

    val statusMessage: String
        get() = response.statusMessage()
}