package se.lars.hnews

import com.google.inject.name.Names.named
import io.vertx.core.json.JsonObject
import se.lars.guice.ModuleBase
import se.lars.hnews.api.AlgoliaSearchApi
import se.lars.hnews.api.HackerNewsApi
import se.lars.hnews.api.IHackerNewsApi
import se.lars.hnews.api.ISearchApi
import se.lars.hnews.services.*


class BootstrapModule(private val config: JsonObject) : ModuleBase() {

    override fun configure() {
        bind(JsonObject::class.java).annotatedWith(named("config")).toInstance(config)
        bind<IServerOptions>().to<ServerOptions>().asSingleton()
        bind<IRouterFactory>().to<RouterFactory>().asSingleton()
        bind<IHackerNewsApi>().to<HackerNewsApi>()
        bind<ISearchApi>().to<AlgoliaSearchApi>()
        bind<IHackerNewsService>().to<HackerNewsService>()
        bind<IHackerNewsCache>().to<RedisHackerNewsCache>()
        bind<GraphQLHandler>()
        bind<IHackerNewsStreamService>().to<HackerNewsStreamService>().asEagerSingleton()
    }
}