package se.lars.sse

import io.vertx.core.http.HttpClientResponse

class ConnectionRefusedException(private val response: HttpClientResponse) : Exception(response.statusMessage()) {

    fun statusCode(): Int {
        return response.statusCode()
    }

    fun statusMessage(): String {
        return response.statusMessage()
    }

    companion object {

        private val serialVersionUID = -6440236022622195797L
    }
}