package com.example.omiri.data.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add Bearer token authentication to all API requests
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Add Authorization header with Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${ApiConfig.API_TOKEN}")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}
