package com.swiftroute.app.domain.model

sealed interface Try<out T> {
    data class Success<T>(val value: T) : Try<T>
    data class Failure(val error: AppError) : Try<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data object Database : AppError
    data object Geocoding : AppError
    data object Unknown : AppError
}
