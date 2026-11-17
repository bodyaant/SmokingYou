package com.smokingtracker.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.smokingtracker.MainActivity
import com.smokingtracker.ui.theme.findActivity
import java.util.Locale

object LocaleHelper {
    fun changeLanguage(context: Context, languageTag: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales = LocaleList.forLanguageTags(languageTag)
        } else {
            context.applicationContext
                .getSharedPreferences(MainActivity.LOCALE_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(MainActivity.KEY_LANGUAGE_TAG, languageTag)
                .apply()
            val locale = Locale.forLanguageTag(languageTag)
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context.findActivity()?.recreate()
        }
    }
}
