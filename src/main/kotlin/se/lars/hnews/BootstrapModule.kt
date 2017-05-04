package se.lars.hnews

import com.google.inject.name.Names.named
import io.vertx.core.json.JsonObject
import se.lars.guice.ModuleBase
import se.lars.hnews.api.HackerNewsApi
import se.lars.hnews.api.IHackerNewsApi


class BootstrapModule(private val config: JsonObject) : ModuleBase() {

    override fun configure() {
        bind(JsonObject::class.java).annotatedWith(named("config")).toInstance(config)
        bind<IServerOptions>().to<ServerOptions>().asSingleton()
        bind<IHackerNewsApi>().to<HackerNewsApi>().asSingleton()
        bind<GraphQLHandler>()
    }
}