package com.example.omiri.data.api

object ApiConfig {
    // Base URL - Use 10.0.2.2 for Android Emulator to access localhost
    // For physical device, replace with your computer's local IP (e.g., 192.168.1.x)
    const val BASE_URL = "http://192.168.1.220:8000/"
    
    // API Version
    const val API_VERSION = "v1"
    
    // Timeouts
    const val CONNECT_TIMEOUT = 60L // seconds
    const val READ_TIMEOUT = 60L // seconds
    const val WRITE_TIMEOUT = 60L // seconds
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 250
    
    // API Token - Replace with actual token or implement secure storage
    const val API_TOKEN = "your_api_token_here" // TODO: Move to secure storage
}
