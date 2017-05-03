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
import se.lars.hnews.types.Story
import se.lars.kutil.loggerFor
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class HackerNewsApi
@Inject constructor(
        vertx: Vertx
) : IHackerNewsApi {
    private val _log = loggerFor<HackerNewsApi>()
    private val _httpClient: HttpClient
    private val _mapper: ObjectMapper

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
        _httpClient = vertx.createHttpClient(options)

        // Create a json deserializer and hint it to ingore unknown properties
        _mapper = ObjectMapper().apply {
            registerModule(Jdk8Module())
            registerModule(KotlinModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    override fun topStories(): CompletableFuture<List<Int>> {
        return invokeQuery("/v0/topstories.json")
    }

    override fun story(id: Int): CompletableFuture<Story> {
        return invokeQuery("/v0/item/$id.json")
    }

    inline private fun <reified T:Any> invokeQuery(query: String): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        _log.info("Query: $query")

        _httpClient.get(query)
                .setTimeout(2000)
                .exceptionHandler { ex -> future.completeExceptionally(ex) }
                .handler { response ->
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        response.bodyHandler { buffer ->
                            try {
                                val typeObj = _mapper.readValue(buffer.bytes, T::class.java)
                                future.complete(typeObj)
                            } catch(e: Exception) {
                                _log.error("Failed to invoke $query", e)
                                future.completeExceptionally(e)
                            }
                        }
                        response.exceptionHandler { e -> future.completeExceptionally(e) }
                    } else if (response.statusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                        _log.warn("Not found: $query")
                        future.complete(null)
                    } else {
                        _log.error("Failed to invoke $query status code ${response.statusCode()}")
                        future.completeExceptionally(Exception(response.statusMessage()))
                    }
                }.end()
        return future
    }
}