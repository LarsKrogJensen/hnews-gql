package se.lars.kutil

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


inline fun <reified T : Any> Any.cast() = this as T

inline fun <reified T : Any> Any.cast(default: T): T {
    if (this is T)
        return this
    else
        return default
}

fun shutdownHook(hook: (stopFuture: CompletableFuture<Unit>) -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread {
        val promise = CompletableFuture<Unit>()
        hook(promise)
        promise.get(10L, TimeUnit.SECONDS)
    })
}