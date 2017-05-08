package se.lars.hnews

import com.google.inject.Guice
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.cli.CLI
import io.vertx.core.cli.CommandLine
import io.vertx.core.cli.Option
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions
import se.lars.guice.GuiceVerticleFactory
import se.lars.guice.GuiceVertxDeploymentManager
import se.lars.guice.VertxModule
import se.lars.guice.deploy
import se.lars.kutil.jsonObject
import se.lars.kutil.shutdownHook
import java.net.URL


fun main(args: Array<String>) {
    displayBanner()
    configureVertxLogging()

    val commandLine = parseCommandLine(args)
    val configStores = loadConfigStores(commandLine)

    val vertx = Vertx.vertx(VertxOptions())

//    vertx.fileSystem().readFile("banner.txt") {
//        println(it.result().toString())
//    }

    ConfigRetriever.create(vertx, configStores).getConfig { configLoadResult ->
        if (configLoadResult.failed()) {
            throw RuntimeException("Failed to read config", configLoadResult.cause())
        } else {
            val injector = Guice.createInjector(BootstrapModule(configLoadResult.result()), VertxModule(vertx))
            vertx.registerVerticleFactory(GuiceVerticleFactory(injector))

            val cores = Runtime.getRuntime().availableProcessors()
            val wsOptions = DeploymentOptions().setInstances(cores)

            GuiceVertxDeploymentManager(vertx).apply {
                deploy<WebServerVerticle>(wsOptions)
            }

            shutdownHook { completion ->
                vertx.close {
                    completion.complete(Unit)
                }
            }
        }
    }
}


private fun loadConfigStores(commandLine: CommandLine): ConfigRetrieverOptions {
    val config = commandLine.getOptionValue<String>("config")?: "dev.yml"

    return ConfigRetrieverOptions(stores = listOf(
        ConfigStoreOptions(type = "file", format = "yaml", config = jsonObject("path" to config)),
        ConfigStoreOptions(type = "sys"),
        ConfigStoreOptions(type = "env")
    ))
}

private fun parseCommandLine(args: Array<String>): CommandLine {
    return CLI.create("server").apply {
        summary = "API Service"

        addOption(Option().apply {
            shortName = "c"
            longName = "config"
            description = "server config"
            isRequired = false
        })
    }.parse(args.toList(), true)
}

private fun configureVertxLogging() {
    System.setProperty("vertx.logger-delegate-factory-class-name",
                       "io.vertx.core.logging.SLF4JLogDelegateFactory")
}

fun displayBanner() {
    val resource: URL = ClassLoader.getSystemClassLoader().getResource("banner.txt")
    println(resource.readText())
}

