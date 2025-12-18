package com.docta.drpc.core.network.context


fun DrpcContext.asRoutingContext(): RoutingContext {
    return this as? RoutingContext ?: throw IllegalStateException("DrpcContext is not of type RoutingContext")
}


inline fun <R> callCatching(block: EmptyContext.() -> R): Result<R> {
    return try {
        with(EmptyContext) { block() }.let { Result.success(it) }
    } catch (e: Throwable) {
        Result.failure(e)
    }
}