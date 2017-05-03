package se.lars.kutil

import io.vertx.core.eventbus.EventBus
import java.util.concurrent.CompletableFuture

inline fun <reified T : Any, R> EventBus.sendWithReply(msg: T) : CompletableFuture<R> {
    val promise = CompletableFuture<R>()
    this.send<R>(T::class.java.name, msg) {
        if (it.succeeded())
            promise.complete(it.result().body() as R)
        else
            promise.completeExceptionally(it.cause())
    }
    return promise
}