package com.swiftroute.app.domain.repository

import com.swiftroute.app.domain.model.Stop
import com.swiftroute.app.domain.model.DomainResult

interface GeocodingRepository {
    suspend fun searchAddress(query: String): DomainResult<List<Stop>>
}
