package com.example.omiri.data.models

data class MembershipCard(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val cardNumber: String,
    val storeId: String? = null,
    val colorHex: String = "#FF9900", // Default Orange
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
