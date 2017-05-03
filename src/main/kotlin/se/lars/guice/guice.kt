package se.lars.guice

import io.vertx.core.DeploymentOptions


inline fun <reified T : Any> GuiceVertxDeploymentManager.deploy(options: DeploymentOptions) {
    this.deployVerticle(T::class.java, options)
}

inline fun <reified T : Any> GuiceVertxDeploymentManager.deploy() {
    this.deployVerticle(T::class.java)
}