package com.example.omiri.data.repository

import com.example.omiri.ui.models.NotificationUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationRepository {
    // Singleton pattern for simple shared state without Dagger/Hilt for now
    companion object {
        val instance = NotificationRepository()
    }

    private val _notifications = MutableStateFlow<List<NotificationUiModel>>(emptyList())
    val notifications: StateFlow<List<NotificationUiModel>> = _notifications.asStateFlow()

    fun addNotification(notification: NotificationUiModel) {
        _notifications.update { current ->
            listOf(notification) + current
        }
    }
    
    fun setNotifications(notifications: List<NotificationUiModel>) {
        _notifications.value = notifications
    }
}
