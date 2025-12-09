package com.example.omiri.utils

import java.io.IOException
import retrofit2.HttpException

enum class NetworkErrorType {
    OFFLINE,
    SERVICE_DOWN,
    GENERIC
}

object NetworkErrorParser {
    fun parseError(throwable: Throwable): NetworkErrorType {
        return when (throwable) {
            is java.net.UnknownHostException -> NetworkErrorType.OFFLINE // No DNS = Likely Offline
            is java.net.ConnectException -> NetworkErrorType.SERVICE_DOWN // Connection Refused = Server Down
            is java.net.SocketTimeoutException -> NetworkErrorType.SERVICE_DOWN // Server slow/timeout
            is IOException -> NetworkErrorType.OFFLINE // Other IO errors (e.g. flight mode often gives unknown host or similar)
            is HttpException -> {
                // 5xx errors are Service Down
                if (throwable.code() >= 500) {
                    NetworkErrorType.SERVICE_DOWN
                } else {
                    NetworkErrorType.GENERIC // 4xx are client errors generally
                }
            }
            else -> NetworkErrorType.GENERIC
        }
    }
}
