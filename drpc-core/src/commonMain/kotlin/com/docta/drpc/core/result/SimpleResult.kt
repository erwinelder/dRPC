package com.docta.drpc.core.result

import kotlinx.serialization.Serializable

@Serializable
sealed interface SimpleResult<out E> {

    @Serializable
    class Success<out E>: SimpleResult<E>

    @Serializable
    data class Error<out E>(val error: E): SimpleResult<E>


    fun getErrorOrNull(): E? = (this as? Error)?.error


    fun <ER> map(
        transformError: (E) -> ER
    ): SimpleResult<ER> {
        return when (this) {
            is Success -> Success()
            is Error -> Error(error = transformError(error))
        }
    }


    fun fold(
        onSuccess: () -> Unit,
        onError: (E) -> Unit
    ): SimpleResult<E> {
        return when (this) {
            is Success -> {
                onSuccess()
                this
            }
            is Error -> {
                onError(error)
                this
            }
        }
    }

    suspend fun foldSuspended(
        onSuccess: suspend () -> Unit,
        onError: suspend (E) -> Unit
    ): SimpleResult<E> {
        return when (this) {
            is Success -> {
                onSuccess()
                this
            }
            is Error -> {
                onError(error)
                this
            }
        }
    }


    fun <S> toResult(success: S): Result<S, E> {
        return when (this) {
            is Success -> Result.Success(success = success)
            is Error -> Result.Error(error = error)
        }
    }

    fun <D> toResultData(data: D): ResultData<D, E> {
        return when (this) {
            is Success -> ResultData.Success(data = data)
            is Error -> ResultData.Error(error = error)
        }
    }

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

inline fun <E> SimpleResult<E>.runOnError(action: (E) -> Unit) {
    if (this is SimpleResult.Error) action(this.error)
}
