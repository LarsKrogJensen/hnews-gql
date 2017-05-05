package se.lars.hnews

import com.google.inject.name.Names.named
import io.vertx.core.json.JsonObject
import se.lars.guice.ModuleBase
import se.lars.hnews.api.HackerNewsApi
import se.lars.hnews.api.IHackerNewsApi
import se.lars.hnews.services.HackerNewsService
import se.lars.hnews.services.IHackerNewsCache
import se.lars.hnews.services.IHackerNewsService
import se.lars.hnews.services.RedisHackerNewsCache


class BootstrapModule(private val config: JsonObject) : ModuleBase() {

    override fun configure() {
        bind(JsonObject::class.java).annotatedWith(named("config")).toInstance(config)
        bind<IServerOptions>().to<ServerOptions>().asSingleton()
        bind<IRouterFactory>().to<RouterFactory>().asSingleton()
        bind<IHackerNewsApi>().to<HackerNewsApi>().asSingleton()
        bind<IHackerNewsService>().to<HackerNewsService>().asSingleton()
        bind<IHackerNewsCache>().to<RedisHackerNewsCache>().asSingleton()
//        bind<IHackerNewsCache>().to<HeapHackerNewsCache>().asSingleton()
        bind<GraphQLHandler>()
    }
}