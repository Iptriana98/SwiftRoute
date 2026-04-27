package com.swiftroute.app.domain.model

data class Stop(
    val id: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String? = null
)
