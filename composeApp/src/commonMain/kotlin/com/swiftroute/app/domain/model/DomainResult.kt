package com.swiftroute.app.domain.model

sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Failure(val error: AppError) : DomainResult<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data object Database : AppError
    data object Geocoding : AppError
    data object Unknown : AppError
}
