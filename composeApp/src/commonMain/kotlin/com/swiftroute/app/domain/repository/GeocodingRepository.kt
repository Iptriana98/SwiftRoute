package com.swiftroute.app.domain.repository

import com.swiftroute.app.domain.model.Stop
import com.swiftroute.app.domain.model.Try

interface GeocodingRepository {
    suspend fun searchAddress(query: String): Try<List<Stop>>
}
