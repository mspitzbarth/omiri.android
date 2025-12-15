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

    fun daysBetween(start: String?, end: String?): Long {
        if (start == null || end == null) return 0L
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val d1 = format.parse(start) ?: return 0L
            val d2 = format.parse(end) ?: return 0L
            val diff = d2.time - d1.time
            if (diff < 0) 0L else TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            0L
        }
    }
}
