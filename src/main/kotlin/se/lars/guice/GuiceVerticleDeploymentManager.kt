package se.lars.guice

import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Vertx

/**
 * Implements convenient methods to deploy verticles programmatically by specified
 * class using @[GuiceVerticleFactory] factory.
 */
class GuiceVertxDeploymentManager(private val vertx: Vertx) {

    /**
     * Deploy a verticle instance given a class of the verticle using default deployment options
     * and [GuiceVerticleFactory] factory.

     * @param verticleClazz the class of the verticle to deploy.
     */
    fun deployVerticle(verticleClazz: Class<*>) {
        deployVerticle(verticleClazz, DeploymentOptions())
    }

    /**
     * Like [.deployVerticle] but [io.vertx.core.DeploymentOptions] are provided to configure the
     * deployment.

     * @param verticleClazz  the class of the verticle to deploy.
     * *
     * @param options  the deployment options.
     */
    fun deployVerticle(verticleClazz: Class<*>, options: DeploymentOptions) {
        this.vertx.deployVerticle(fullVerticleName(verticleClazz), options)
    }

    /**
     * Like [.deployVerticle] but handler can be provided
     * which will be notified when the deployment is complete.

     * @param verticleClazz  the class of the verticle to deploy.
     * *
     * @param options  the deployment options.
     * *
     * @param completionHandler  a handler which will be notified when the deployment is complete.
     */
    fun deployVerticle(verticleClazz: Class<*>, options: DeploymentOptions, completionHandler: Handler<AsyncResult<String>>) {
        this.vertx.deployVerticle(fullVerticleName(verticleClazz), options, completionHandler)
    }

    /**
     * Gets the name of the verticle with adding prefix required to notify vertx to use
     * @{@link GuiceVerticleFactory} factory for verticle creation.
     * *
     * *
     * @param verticleClazz the class of the verticle to deploy.
     * *
     * @return Name of the verticle which can be used for deployment to vertx.
     */
    private fun fullVerticleName(verticleClazz: Class<*>): String {
        return GuiceVerticleFactory.PREFIX + ":" + verticleClazz.canonicalName
    }
}