package com.docta.drpc.core.result

import kotlinx.serialization.Serializable

@Serializable
sealed interface ResultData<out D, out E> {

    @Serializable
    data class Success<out D, out E>(val data: D): ResultData<D, E>

    @Serializable
    data class Error<out D, out E>(val error: E): ResultData<D, E>


    fun getDataOrNull(): D? = (this as? Success)?.data
    fun getErrorOrNull(): E? = (this as? Error)?.error

    fun <R> mapData(transform: (D) -> R): ResultData<R, E> {
        return when (this) {
            is Success -> Success<R, E>(this.data.let(transform))
            is Error -> Error(this.error)
        }
    }

    fun <R> mapError(transform: (E) -> R): ResultData<D, R> {
        return when (this) {
            is Success -> Success(this.data)
            is Error -> Error<D, R>(this.error.let(transform))
        }
    }

    fun <S> toDefaultResult(success: S): Result<S, E> {
        return when (this) {
            is Success -> Result.Success(success)
            is Error -> Result.Error(this.error)
        }
    }

}

inline fun <D, E> ResultData<D, E>.getOrElse(action: (E) -> Nothing): D {
    return when (this) {
        is ResultData.Success -> this.data
        is ResultData.Error -> action(this.error)
    }
}

inline fun <D, E> ResultData<D, E>.onError(action: (E) -> Nothing) {
    if (this is ResultData.Error) action(this.error)
}
