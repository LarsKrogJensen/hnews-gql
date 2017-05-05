package se.lars.hnews.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpVersion
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import se.lars.kutil.loggerFor
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class HackerNewsApi
@Inject constructor(
        vertx: Vertx
) : IHackerNewsApi {
    private val baseUrl = "hacker-news.firebaseio.com";
    private val log = loggerFor<HackerNewsApi>()
    private val httpClient: HttpClient
    private val mapper: ObjectMapper

    init {

        // Prepare http client options to run HTTP/2
        val options = HttpClientOptions().apply {
            protocolVersion = HttpVersion.HTTP_2
            isSsl = true
            isUseAlpn = true
            isTrustAll = true
            defaultHost = "hacker-news.firebaseio.com"
            defaultPort = 443
            logActivity = true
            connectTimeout = 1000
        }

        // Http client is thread safe an a single instance is sufficent
        httpClient = vertx.createHttpClient(options)

        // Create a json deserializer and hint it to ingore unknown properties
        mapper = ObjectMapper().apply {
            registerModule(Jdk8Module())
            registerModule(KotlinModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    override fun topStories(): CompletableFuture<List<Int>> {
        return invokeQuery<List<Int>>("/v0/topstories.json")
    }

    override fun story(id: Int): CompletableFuture<Story> {
        return invokeQuery("/v0/item/$id.json")
    }

    override fun comment(id: Int): CompletableFuture<Comment> {
        return invokeQuery("/v0/item/$id.json")
    }

    override fun user(id: String): CompletableFuture<User> {
        return invokeQuery("/v0/user/$id.json")
    }

    inline private fun <reified T:Any> invokeQuery(query: String): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        log.info("Query: $query")

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

    private fun formatUrl(query: String) = "https://$baseUrl$query"
}