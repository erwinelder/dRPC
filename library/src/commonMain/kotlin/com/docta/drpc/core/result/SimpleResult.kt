package com.docta.drpc.core.result

import kotlinx.serialization.Serializable

@Serializable
sealed interface SimpleResult<out E> {

    @Serializable
    class Success<out E>: SimpleResult<E>

    @Serializable
    data class Error<out E>(val error: E): SimpleResult<E>


    fun getErrorOrNull(): E? = (this as? Error)?.error

}

inline fun <E> SimpleResult<E>.onSuccess(action: () -> Nothing) {
    if (this is SimpleResult.Success) action()
}

inline fun <E> SimpleResult<E>.runOnSuccess(action: () -> Unit) {
    if (this is SimpleResult.Success) action()
}

inline fun <E> SimpleResult<E>.onError(action: (E) -> Nothing) {
    if (this is SimpleResult.Error) action(this.error)
}
