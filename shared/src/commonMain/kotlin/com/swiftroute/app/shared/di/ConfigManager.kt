package com.swiftroute.app

import java.io.File
import java.util.Properties

/**
 * Configuration manager that loads API keys from .env file.
 * The .env file should NEVER be committed to version control.
 */
object ConfigManager {
    
    private var apiKeys: Map<String, String> = emptyMap()
    
    val googleMapsApiKey: String
        get() = apiKeys["GOOGLE_MAPS_API_KEY"] ?: ""
    
    val mapboxAccessToken: String
        get() = apiKeys["MAPBOX_ACCESS_TOKEN"] ?: ""
    
    /**
     * Load configuration from .env file in project root.
     * Call this once at app startup.
     */
    fun load(projectRoot: String = System.getProperty("user.dir")) {
        val envFile = File(projectRoot, ".env")
        
        if (!envFile.exists()) {
            // Try alternative locations
            val altPaths = listOf(
                File(projectRoot).parentFile?.absolutePath?.let { File(it, ".env") },
                File(System.getProperty("user.home"), ".swiftroute", ".env")
            )
            
            for (path in altPaths) {
                if (path != null && path.exists()) {
                    parseEnvFile(path)
                    return
                }
            }
            
            // No .env file found - keys will be empty
            return
        }
        
        parseEnvFile(envFile)
    }
    
    private fun parseEnvFile(file: File) {
        val keys = mutableMapOf<String, String>()
        
        file.forEachLine { line ->
            val trimmed = line.trim()
            // Skip comments and empty lines
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine
            
            // Parse KEY=VALUE
            val parts = trimmed.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim().removeSurrounding("\"", "'")
                keys[key] = value
            }
        }
        
        apiKeys = keys
    }
}
