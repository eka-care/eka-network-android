package com.eka.network.response

import com.eka.network.client.HttpException
import com.eka.network.response.NetworkResponse.Failure
import com.eka.network.response.NetworkResponse.Success
import java.io.IOException

typealias Headers = Map<String, List<String>>

sealed class NetworkResponse<out T> {
    data class Success<T>(
        val statusCode: Int,
        val headers: Headers,
        val value: T,
    ) : NetworkResponse<T>()

    sealed class Failure(val cause: Throwable) : NetworkResponse<Nothing>() {
        data class Http(
            val statusCode: Int,
            val headers: Headers,
            val errorBody: String,
        ) : Failure(HttpException(statusCode, headers))

        data class IO(
            val exception: IOException,
        ) : Failure(exception)

        data class Unknown(
            val exception: Throwable,
        ) : Failure(exception)
    }
}

fun <T> NetworkResponse<T>.getOrNull(): T? = when (this) {
    is Success -> value
    is Failure -> null
}

fun <T> NetworkResponse<T>.getOrThrow(): T = when (this) {
    is Success -> value
    is Failure.Http -> throw HttpException(statusCode, headers)
    is Failure.IO -> throw exception
    is Failure.Unknown -> throw exception
}

fun <T> NetworkResponse<T>.toResult(): Result<T> = Result.runCatching { getOrThrow() }

inline fun <T, R> NetworkResponse<T>.map(
    mapper: (T) -> R,
): NetworkResponse<R> = when (this) {
    is Success -> Success(statusCode, headers, mapper(value))
    is Failure -> this
}

inline fun <T, R> NetworkResponse<T>.mapCatching(
    mapper: (T) -> R,
): Result<NetworkResponse<R>> = when (this) {
    is Success -> Result.success(Success(statusCode, headers, mapper(value)))
    is Failure -> Result.failure(this.cause)
}

inline fun <T, R> NetworkResponse<T>.flatMap(
    mapper: (T) -> NetworkResponse<R>,
): NetworkResponse<R> = when (this) {
    is Success -> mapper(value)
    is Failure -> this
}

inline fun <T> NetworkResponse<T>.recover(
    mapper: (Failure) -> NetworkResponse<T>,
): NetworkResponse<T> = when (this) {
    is Success -> this
    is Failure -> mapper(this)
}
