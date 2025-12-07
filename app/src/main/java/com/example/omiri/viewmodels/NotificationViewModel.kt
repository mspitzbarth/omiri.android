package com.example.omiri.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omiri.data.repository.NotificationRepository
import com.example.omiri.ui.models.NotificationUiModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository.instance
    
    val notifications: StateFlow<List<NotificationUiModel>> = repository.notifications

    init {
        // Initialize with default mock data if empty
        if (notifications.value.isEmpty()) {
            val initialNotifications = listOf(
                NotificationUiModel.FlashSale(
                    id = "1",
                    title = "Flash Sale Alert!",
                    time = "5m ago",
                    isRead = false,
                    icon = Icons.Outlined.LocalFireDepartment,
                    iconColor = Color(0xFFEF4444),
                    description = "Hamburger patties at Target now 30% off! Limited time only.",
                    timeLeft = "2 hours left",
                    savings = "Save $3.60"
                ),
                NotificationUiModel.PriceDrop(
                    id = "2",
                    title = "Price Drop",
                    time = "1h ago",
                    isRead = false,
                    icon = Icons.Outlined.ArrowDownward,
                    iconColor = Color(0xFF10B981),
                    description = "Wireless Headphones dropped from $89 to $64 at Best Buy",
                    currentPrice = "$64",
                    originalPrice = "$89",
                    discountPercentage = "28% off"
                ),
                NotificationUiModel.ListUpdate(
                    id = "3",
                    title = "Shopping List Update",
                    time = "2h ago",
                    isRead = false,
                    icon = Icons.AutoMirrored.Outlined.List,
                    iconColor = Color(0xFF3B82F6),
                    description = "3 items from \"BBQ Party List\" are now on sale nearby",
                    locationTag = "Walmart • 2.3 mi"
                ),
                NotificationUiModel.Reward(
                    id = "4",
                    title = "Reward Earned!",
                    time = "3h ago",
                    isRead = true,
                    icon = Icons.Outlined.CardGiftcard,
                    iconColor = Color(0xFFA855F7), // Purple
                    description = "You've earned 500 points at Target. Redeem for $5 off!",
                    pointsTag = "500 points"
                ),
                // Yesterday
                NotificationUiModel.General(
                    id = "5",
                    title = "New Coupon Available",
                    time = "1d ago",
                    isRead = true,
                    icon = Icons.Outlined.LocalOffer,
                    iconColor = Color(0xFFF97316), // Orange
                    description = "$10 off your next purchase of $50+ at Whole Foods",
                    tag = "Expires in 7 days"
                ),
                NotificationUiModel.General(
                    id = "6",
                    title = "Deal Ending Soon",
                    time = "1d ago",
                    isRead = true,
                    icon = Icons.Outlined.AccessTime,
                    iconColor = Color(0xFFEAB308), // Yellow
                    description = "Your saved deal on Hot Dogs expires tomorrow at Target",
                    tag = "1 day left"
                ),
                NotificationUiModel.PriceDrop(
                    id = "7",
                    title = "Wishlist Item on Sale",
                    time = "1d ago",
                    isRead = true,
                    icon = Icons.Outlined.Favorite,
                    iconColor = Color(0xFFEC4899), // Pink
                    description = "Summer Beach Towel Set is now 40% off at Amazon",
                    currentPrice = "$23.99",
                    originalPrice = "$39.99",
                    discountPercentage = "40% off"
                ),
                // This Week
                NotificationUiModel.General(
                    id = "8",
                    title = "Weekly Savings Report",
                    time = "3d ago",
                    isRead = true,
                    icon = Icons.Outlined.Notifications,
                    iconColor = Color(0xFF6366F1), // Indigo
                    description = "You saved $47.30 this week across 8 purchases!",
                    actionLabel = "View Report"
                )
            )
            repository.setNotifications(initialNotifications)
        }
    }
}
