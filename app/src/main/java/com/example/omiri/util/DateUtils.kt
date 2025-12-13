package com.example.omiri.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    fun getDaysRemaining(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null

        return try {
            // Try parsing generic ISO format
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = format.parse(dateString) ?: return null
            
            val now = Calendar.getInstance().time
            val diff = date.time - now.time
            
            if (diff < 0) return 0L
            
            TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            // Fallback or log if needed
            null
        }
    }
}
