package se.lars.sse

import io.vertx.core.AsyncResult

class SSEAsyncResult<T>(
    private val cause: Throwable? = null,
    private val result: T? = null
) : AsyncResult<T> {

    override fun result() = result

    override fun cause() = cause

    override fun succeeded() = cause == null

    override fun failed() = cause != null
}