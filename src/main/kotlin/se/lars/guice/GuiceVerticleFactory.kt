package se.lars.guice

import com.google.inject.Injector
import io.vertx.core.Verticle
import io.vertx.core.spi.VerticleFactory

@Suppress("UNCHECKED_CAST")
class GuiceVerticleFactory(private val injector: Injector) : VerticleFactory {
    override fun prefix(): String {
        return PREFIX
    }

    override fun createVerticle(verticleName: String, classLoader: ClassLoader): Verticle {
        val clazz: Class<Any> = classLoader.loadClass(VerticleFactory.removePrefix(verticleName)) as Class<Any>

        return this.injector.getInstance<Any>(clazz) as Verticle
    }

    companion object {
        val PREFIX = "java-guice"
    }
}
