package se.lars.kutil

import io.vertx.core.Context
import io.vertx.core.Vertx
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

fun <T> T.complete(future: CompletableFuture<T>) {
    future.complete(this)
}

fun <T> CompletionStage<T>.thenOn(context: Context): CompletionStage<T> {
    val future = CompletableFuture<T>()
    whenComplete { result, error ->
        if (context == Vertx.currentContext()) {
            future.complete(result, error)
        } else {
            context.runOnContext {
                future.complete(result, error)
            }
        }
    }
    return future
}

fun <T> CompletableFuture<T>.complete(result: T?, error: Throwable?) {
    if (error != null)
        completeExceptionally(error)
    else
        complete(result)
}

fun <T> succeeded(value: T) = CompletableFuture.completedFuture(value)

fun <T> succeededNullable(value: T?) = CompletableFuture.completedFuture(value)


fun <T> succeededOptional(optional: Optional<T>): CompletableFuture<T> {
    return succeeded(if (optional.isPresent) optional.get() else null)
}

fun succeededOptionalInt(optional: OptionalInt): CompletableFuture<Int> {
    return if (optional.isPresent) CompletableFuture.completedFuture(optional.asInt)
    else CompletableFuture.completedFuture(null)
}
