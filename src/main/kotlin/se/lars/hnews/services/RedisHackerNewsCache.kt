package se.lars.hnews.services

import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Vertx
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import se.lars.hnews.IServerOptions
import se.lars.hnews.defaultMapper
import se.lars.hnews.types.Comment
import se.lars.hnews.types.Story
import se.lars.hnews.types.User
import se.lars.kutil.awaitAll
import se.lars.kutil.complete
import se.lars.kutil.loggerFor
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class RedisHackerNewsCache
@Inject
constructor(
    vertx: Vertx,
    options: IServerOptions
) : IHackerNewsCache {
    private val log = loggerFor<RedisHackerNewsCache>()

    private val redis: RedisClient = RedisClient.create(vertx, RedisOptions().apply {
        host = options.redisHost
        port = options.redisPort
        auth = options.redisAuth
        log.info("Connection to Redis on '${options.redisHost}:${options.redisPort}'")
    })
    private val mapper = defaultMapper

    override fun story(id: Int, loader: (Int) -> CompletableFuture<Story>) = getOrLoad(id, loader)

    override fun stories(ids: List<Int>, loader: (Int) -> CompletableFuture<Story>) = getOrLoadMany(ids, loader) {
        it.id
    }

    override fun comments(ids: List<Int>, loader: (Int) -> CompletableFuture<Comment>) = getOrLoadMany(ids, loader) {
        it.id
    }

    override fun comment(id: Int, loader: (Int) -> CompletableFuture<Comment>) = getOrLoad(id, loader)

    override fun topStories(loader: () -> CompletableFuture<List<Int>>) = getOrLoad("topstories", { loader() })

    override fun user(id: String, loader: (String) -> CompletableFuture<User>): CompletableFuture<User> {
        return getOrLoad(id, loader)
    }

    inline private fun <reified TKey : Any, reified TValue : Any> getOrLoad(key: TKey, crossinline loader: (TKey) -> CompletableFuture<TValue>): CompletableFuture<TValue> {
        val promise = CompletableFuture<TValue>()
        redis.get(key.toString()) { result ->
            log.info("redis get<$key)> result ${result.succeeded()} value ${result.result()}")
            if (result.succeeded() && result.result() != null) {
                try {
                    val readValue = mapper.readValue<TValue>(result.result())
                    promise.complete(readValue)
                } catch(e: Exception) {
                    log.error("Failed to deserialize json: ${result.result()}")
                    promise.completeExceptionally(e)
                }
            } else {
                loader(key).whenComplete { value, ex ->
                    promise.complete(value, ex)
                    storeItem<TValue>(key.toString(), value)
                }
            }
        }
        return promise
    }

    inline private fun <reified TKey : Any, reified TValue : Any> getOrLoadMany(
        ids: List<TKey>,
        crossinline loader: (TKey) -> CompletableFuture<TValue>,
        crossinline idResolver: (TValue) -> String
    ): CompletableFuture<List<TValue>> {

        val promise = CompletableFuture<List<TValue>>()

        redis.mgetMany(ids.map { it.toString() }) { response ->
            if (response.succeeded() && response.result() != null) {
                val stories = response.result()
                    .filterNotNull()
                    .map { mapper.readValue<TValue>(it as String) }
                log.info("Redis mget found items: ${stories.size}")
                if (stories.size == ids.size) {
                    promise.complete(stories)
                } else {
                    val found = stories.map(idResolver).toSet()
                    ids.filter { !found.contains(it.toString()) }
                        .map(loader)
                        .awaitAll()
                        .whenComplete { result, ex ->
                            promise.complete(stories + result, ex)
                            result.map { storeItem(idResolver(it), it) }
                        }
                }
            } else {
                ids.map(loader).awaitAll().whenComplete { result, ex ->
                    promise.complete(result, ex)
                }
            }
        }

        return promise
    }

    private fun <TValue : Any> storeItem(key: String, value: TValue?) {
        value?.let {
            val writeValueAsString = mapper.writeValueAsString(value)
            redis.set(key, writeValueAsString) {
                log.info("Redis set<$key> with result ${it.succeeded()} value: $writeValueAsString")
            }
        }
    }
}