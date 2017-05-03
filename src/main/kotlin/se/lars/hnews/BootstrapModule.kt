package se.lars

import com.google.inject.name.Names.named
import io.vertx.core.json.JsonObject
import se.lars.guice.ModuleBase
import se.lars.hnews.IServerOptions
import se.lars.hnews.ServerOptions


class BootstrapModule(private val config: JsonObject) : ModuleBase() {

    override fun configure() {
        bind(JsonObject::class.java).annotatedWith(named("config")).toInstance(config)
        bind<IServerOptions>().to<ServerOptions>().asSingleton()
//        bind<IMyService>().to<MyService>().asSingleton()
//        bind<IApiController>().to<ApiController>()
//        bind<ISearchController>().to<SearchController>()
//        bind<AuthProvider>().to<ApiAuthProvider>()
//        bind<GraphQLHandler>()
//        bind<GraphQLHandler>().annotatedWith(named("mock")).toInstance(GraphQLHandler(MockApiController(),MockSearchController(), eventBus))
//        bind<GraphQLHandlerOverWS>()
//        bind<GraphQLHandlerOverWS>().annotatedWith(named("mock")).toInstance(GraphQLHandlerOverWS(MockApiController(), MockSearchController(), eventBus))
//        bind<ChatSystemHandler>().asSingleton()
//        bind<MetricsVerticle>().asEagerSingleton()
    }
}