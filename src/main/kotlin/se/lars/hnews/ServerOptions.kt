package se.lars.hnews

import io.vertx.core.json.JsonObject
import se.lars.kutil.resolveBool
import se.lars.kutil.resolveInt
import javax.inject.Inject
import javax.inject.Named

interface IServerOptions {
    val httpPort: Int
    val useSsl: Boolean
    val enableMetrics: Boolean
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
}

