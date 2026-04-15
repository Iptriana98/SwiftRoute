package com.swiftroute.app

/**
 * Provides API keys loaded from .env configuration.
 * Keys are loaded at app startup via ConfigManager.
 */
object ApiKeys {
    
    /**
     * Google Maps API key for Android.
     * Must be configured in .env file.
     */
    val googleMaps: String
        get() = ConfigManager.googleMapsApiKey
    
    /**
     * Mapbox access token for map rendering.
     * Must be configured in .env file.
     */
    val mapbox: String
        get() = ConfigManager.mapboxAccessToken
    
    /**
     * Check if all required API keys are configured.
     */
    val isConfigured: Boolean
        get() = googleMaps.isNotEmpty() || mapbox.isNotEmpty()
}
