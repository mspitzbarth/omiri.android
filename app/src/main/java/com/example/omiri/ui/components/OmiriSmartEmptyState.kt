package com.example.omiri.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.omiri.utils.NetworkErrorType

@Composable
fun OmiriSmartEmptyState(
    networkErrorType: NetworkErrorType? = null,
    error: String? = null,
    defaultIcon: ImageVector,
    defaultTitle: String,
    defaultMessage: String,
    onRetry: () -> Unit = {},
    retryButtonText: String = "Retry",
    modifier: Modifier = Modifier
) {
    if (error != null) {
        // Error State
        when (networkErrorType) {
            NetworkErrorType.OFFLINE -> {
                OmiriEmptyState(
                    icon = Icons.Outlined.WifiOff,
                    title = "You are offline", // User preference "No Connection" or "You are offline"?
                    // Previous: "No Connection" in Home, "You are offline" in Chat. User said "same for all".
                    // Let's stick to "You are offline" as it's friendly.
                    message = "We couldn't connect to the server. Please check your internet connection.",
                    buttonText = retryButtonText,
                    onButtonClick = onRetry,
                    modifier = modifier
                )
            }
            NetworkErrorType.SERVICE_DOWN -> {
                OmiriEmptyState(
                    icon = Icons.Outlined.CloudOff,
                    title = "Service Unavailable",
                    message = "Our servers are currently down. Please try again later.",
                    buttonText = retryButtonText,
                    onButtonClick = onRetry,
                    modifier = modifier
                )
            }
            else -> {
                // Generic Error
                OmiriEmptyState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = "Something went wrong",
                    message = "We encountered an error. Please try again.", // Generic message to avoid technical jargon? 
                    // Or use 'error' string if it's safe? The 'error' string often has "Failed to load..."
                    // Let's use the passed error string but maybe truncate or genericize if needed?
                    // For now, let's use a generic title and the error message as subtitle if concise, or just generic.
                    // User complained about "No deals found" when service down.
                    // Let's use "Something went wrong" + Retry.
                    buttonText = retryButtonText,
                    onButtonClick = onRetry,
                    modifier = modifier
                )
            }
        }
    } else {
        // Default Empty State (No Error, just empty)
        OmiriEmptyState(
            icon = defaultIcon,
            title = defaultTitle,
            message = defaultMessage,
            buttonText = retryButtonText,
            onButtonClick = onRetry,
            modifier = modifier
        )
    }
}
