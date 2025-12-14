package com.example.omiri.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun onAttach(context: Context): Context {
        // We can't easily read DataStore here synchronously without blocking.
        // Usually we pass the language code here.
        // For now, we will assume standard context attaching if invoked manually.
        return context
    }

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
    
    fun getSystemLanguage(): String {
        return Locale.getDefault().language
    }
}
