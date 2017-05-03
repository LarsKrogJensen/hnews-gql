package se.lars.guice

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.file.FileSystem
import io.vertx.core.shareddata.SharedData

/**
 * Guice [ModuleBase] for vertx and container injections.
 */
class VertxModule(private val vertx: Vertx) : ModuleBase() {

    override fun configure() {
        bind<Vertx>().toInstance(this.vertx)
        bind<EventBus>().toInstance(this.vertx.eventBus())
        bind<FileSystem>().toInstance(this.vertx.fileSystem())
        bind<SharedData>().toInstance(this.vertx.sharedData())
    }
}
