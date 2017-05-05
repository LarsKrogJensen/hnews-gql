package se.lars.hnews.services

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Vertx
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.kutil.complete
import se.lars.kutil.loggerFor
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class RedisHackerNewsCache
@Inject
constructor(
    vertx: Vertx
) : IHackerNewsCache {
    private val log = loggerFor<RedisHackerNewsCache>()

    private val redis: RedisClient = RedisClient.create(vertx, RedisOptions().apply {
        host = "localhost"
    })
    private val mapper = ObjectMapper().apply {
        registerModule(Jdk8Module())
        registerModule(KotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    override fun story(id: Int, loader: (Int) -> CompletableFuture<Story>): CompletableFuture<Story> {
       return getOrLoad(id, loader)
    }

    override fun comment(id: Int, loader: (Int) -> CompletableFuture<Comment>): CompletableFuture<Comment> {
        return getOrLoad(id, loader)
    }

    override fun topStories(loader: () -> CompletableFuture<List<Int>>): CompletableFuture<List<Int>> {
        return getOrLoad("topstories", { loader() })
    }

    inline private fun <reified TKey: Any, reified TValue: Any> getOrLoad(key: TKey, crossinline loader: (TKey) -> CompletableFuture<TValue>): CompletableFuture<TValue> {
        val promise = CompletableFuture<TValue>()
        redis.get(key.toString()) { result ->
            log.info("redis get<$key)> result ${result.succeeded()} value ${result.result()}")
            if (result.succeeded() && result.result() != null) {
                promise.complete(mapper.readValue(result.result()))
            } else {
                loader(key).whenComplete { value, ex ->
                    promise.complete(value, ex)
                    value?.let {
                        redis.set(key.toString(), mapper.writeValueAsString(value)) {
                            log.info("Redis set<$key> with result ${it.succeeded()}")
                        }
                    }
                }
            }
        }
        return promise
    }
}