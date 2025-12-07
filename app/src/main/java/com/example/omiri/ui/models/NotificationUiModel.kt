package com.example.omiri.ui.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NotificationUiModel {
    abstract val id: String
    abstract val title: String
    abstract val time: String
    abstract val isRead: Boolean
    abstract val icon: ImageVector
    abstract val iconColor: Color // The main color for icon and side bar

    data class FlashSale(
        override val id: String,
        override val title: String,
        override val time: String,
        override val isRead: Boolean,
        override val icon: ImageVector,
        override val iconColor: Color,
        val description: String,
        val timeLeft: String,
        val savings: String,
        val actionLabel: String = "View Deal"
    ) : NotificationUiModel()

    data class PriceDrop(
        override val id: String,
        override val title: String,
        override val time: String,
        override val isRead: Boolean,
        override val icon: ImageVector,
        override val iconColor: Color,
        val description: String,
        val currentPrice: String,
        val originalPrice: String,
        val discountPercentage: String
    ) : NotificationUiModel()

    data class ListUpdate(
        override val id: String,
        override val title: String,
        override val time: String,
        override val isRead: Boolean,
        override val icon: ImageVector,
        override val iconColor: Color,
        val description: String,
        val locationTag: String
    ) : NotificationUiModel()

    data class Reward(
        override val id: String,
        override val title: String,
        override val time: String,
        override val isRead: Boolean,
        override val icon: ImageVector,
        override val iconColor: Color,
        val description: String,
        val pointsTag: String
    ) : NotificationUiModel()

    data class General(
        override val id: String,
        override val title: String,
        override val time: String,
        override val isRead: Boolean,
        override val icon: ImageVector,
        override val iconColor: Color,
        val description: String,
        val tag: String? = null,
        val actionLabel: String? = null
    ) : NotificationUiModel()
}
