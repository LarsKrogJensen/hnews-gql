package se.lars.hnews

import io.vertx.core.json.JsonObject
import se.lars.kutil.resolveBool
import se.lars.kutil.resolveInt
import se.lars.kutil.resolveString
import javax.inject.Inject
import javax.inject.Named

interface IServerOptions {
    val httpPort: Int
    val useSsl: Boolean
    val enableMetrics: Boolean
    val redisHost: String
    val redisPort: Int
}

class ServerOptions
@Inject
constructor(
    @Named("config") private val config: JsonObject
) : IServerOptions {

    override val httpPort: Int
        get() {
            // PORT env var is used in  heroku
            return config.resolveInt("PORT") ?: config.resolveInt("http.port") ?: 8080
        }

    override val useSsl: Boolean
        get() = config.resolveBool("http.useSsl") ?: false

    override val enableMetrics: Boolean
        get() = config.resolveBool("metrics.enable") ?: false

    override val redisHost: String
        get() = resolveHerokuRedisUrl()?.first ?: config.resolveString("redis.host") ?: "localhost"

    override val redisPort: Int
        get() = resolveHerokuRedisUrl()?.second ?: config.resolveInt("redis.port") ?: 6379

    private fun resolveHerokuRedisUrl(): Pair<String, Int>? {
        return config.resolveString("REDIS_URL")?.let { url ->
            val hostAndPort = url.removePrefix("redis://")
            val portIdx = hostAndPort.lastIndexOf(":")
            Pair(hostAndPort.substring(0, portIdx), hostAndPort.substring(portIdx + 1).toInt())
        }
    }
}

