package se.lars.hnews.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpVersion
import se.lars.hnews.defaultMapper
import se.lars.kutil.loggerFor
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

open class HttpClientBaseApi
@Inject constructor(
    vertx: Vertx,
    val baseUrl: String
) {
    protected val log = loggerFor<HackerNewsApi>()
    protected val httpClient: HttpClient
    protected val mapper = defaultMapper

    init {

        // Prepare http client options to run HTTP/2
        val options = HttpClientOptions().apply {
            protocolVersion = HttpVersion.HTTP_2
            isSsl = true
            isUseAlpn = true
            isTrustAll = true
            defaultHost = baseUrl
            defaultPort = 443
            logActivity = true
            connectTimeout = 1000
        }

        // Http client is thread safe an a single instance is sufficent
        httpClient = vertx.createHttpClient(options)
    }

    inline protected fun <reified T : Any> invokeQuery(query: String): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        log.info("Query: ${formatUrl(query)}")

        httpClient.get(query)
            .setTimeout(30_000)
            .exceptionHandler { ex -> future.completeExceptionally(ex) }
            .handler { response ->
                if (response.statusCode() == HttpResponseStatus.OK.code()) {
                    response.bodyHandler { buffer ->
                        try {
                            val typeObj = mapper.readValue(buffer.bytes, T::class.java)
                            future.complete(typeObj)
                        } catch(e: Exception) {
                            log.error("Failed to invoke ${formatUrl(query)}", e)
                            future.completeExceptionally(e)
                        }
                    }
                    response.exceptionHandler { e -> future.completeExceptionally(e) }
                } else if (response.statusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                    log.warn("Not found: ${formatUrl(query)}")
                    future.complete(null)
                } else {
                    log.error("Failed to invoke ${formatUrl(query)} status code ${response.statusCode()}")
                    future.completeExceptionally(Exception(response.statusMessage()))
                }
            }.end()
        return future
    }

    protected fun formatUrl(query: String) = "https://$baseUrl$query"
}