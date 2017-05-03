package se.lars.kutil

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

fun router(vertx: Vertx, block: Router.() -> Unit): Router {
    return Router.router(vertx).apply {
        block()
    }
}
