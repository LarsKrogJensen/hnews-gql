package se.lars.hnews.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpVersion
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
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
    protected val webClient: WebClient

    init {

        // Prepare http client options to run HTTP/2
        val options = HttpClientOptions().apply {
            protocolVersion = HttpVersion.HTTP_2
            isSsl = true
            isUseAlpn = true
            defaultHost = baseUrl
            defaultPort = 443
            logActivity = true
            connectTimeout = 1000
        }

        // Http client is thread safe an a single instance is sufficent
        httpClient = vertx.createHttpClient(options)

        val wcOptions = WebClientOptions().apply {
            protocolVersion = HttpVersion.HTTP_2
            isSsl = true
            isUseAlpn = true
            defaultHost = baseUrl
            defaultPort = 443
            logActivity = true
            connectTimeout = 1000
        }
        webClient = WebClient.create(vertx, wcOptions)
    }

    inline protected fun <reified T : Any> invokeQuery(query: String): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        webClient.get(query)
            .send { queryResult ->
                if (queryResult.succeeded()) {
                    val response: HttpResponse<Buffer> = queryResult.result()
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        log.info("Protocol version: ${response.version()}")
                        try {
                            val typeObj = mapper.readValue(response.body().bytes, T::class.java)
                            future.complete(typeObj)
                        } catch (e: Exception) {
                            log.error("Failed to invoke ${formatUrl(query)}", e)
                            future.completeExceptionally(e)
                        }
                    } else if (response.statusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                        log.warn("Not found: ${formatUrl(query)}")
                        future.complete(null)
                    } else {
                        log.error("Failed to invoke ${formatUrl(query)} status code ${response.statusCode()}")
                        future.completeExceptionally(Exception(response.statusMessage()))
                    }
                } else {
                    future.completeExceptionally(queryResult.cause())
                }

            }
        log.info("Query: ${formatUrl(query)}")

        return future
    }

    protected fun formatUrl(query: String) = "https://$baseUrl$query"
}