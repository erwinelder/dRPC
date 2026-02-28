package com.docta.drpc.core.result

import kotlinx.serialization.Serializable

@Serializable
sealed interface ResultData<out D, out E> {

    @Serializable
    data class Success<out D, out E>(val data: D): ResultData<D, E>

    @Serializable
    data class Error<out D, out E>(val error: E): ResultData<D, E>


    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error


    fun getDataOrNull(): D? = (this as? Success)?.data
    fun getErrorOrNull(): E? = (this as? Error)?.error


    fun <DR, ER> map(
        transformData: (D) -> DR,
        transformError: (E) -> ER
    ): ResultData<DR, ER> {
        return when (this) {
            is Success -> Success(data = transformData(data))
            is Error -> Error(error = transformError(error))
        }
    }

    fun <R> mapData(transform: (D) -> R): ResultData<R, E> {
        return when (this) {
            is Success -> Success(data = transform(data))
            is Error -> Error(error = error)
        }
    }

    fun <R> mapError(transform: (E) -> R): ResultData<D, R> {
        return when (this) {
            is Success -> Success(data = data)
            is Error -> Error(error = transform(error))
        }
    }


    fun onEach(
        onSuccess: (D) -> Unit,
        onError: (E) -> Unit
    ): ResultData<D, E> {
        return when (this) {
            is Success -> {
                onSuccess(data)
                this
            }
            is Error -> {
                onError(error)
                this
            }
        }
    }

    suspend fun onEachSuspend(
        onSuccess: suspend () -> Unit,
        onError: suspend (E) -> Unit
    ): ResultData<D, E> {
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

    fun toSimpleResult(): SimpleResult<E> {
        return when (this) {
            is Success -> SimpleResult.Success()
            is Error -> SimpleResult.Error(error = this.error)
        }
    }

}

inline fun <D, E> ResultData<D, E>.getOrElse(action: (E) -> Nothing): D {
    return when (this) {
        is ResultData.Success -> data
        is ResultData.Error -> action(error)
    }
}

inline fun <D, E> ResultData<D, E>.onSuccess(action: (D) -> Nothing) {
    if (this is ResultData.Success) action(data)
}

inline fun <D, E> ResultData<D, E>.runOnSuccess(action: (D) -> Unit) {
    if (this is ResultData.Success) action(data)
}

inline fun <D, E> ResultData<D, E>.onError(action: (E) -> Nothing) {
    if (this is ResultData.Error) action(error)
}

inline fun <D, E> ResultData<D, E>.runOnError(action: (E) -> Unit) {
    if (this is ResultData.Error) action(this.error)
}
