package com.app.development.winter.localcache

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.app.development.winter.localcache.PreferenceManager.Companion.getInstance
import com.app.development.winter.manager.ReferralLibInitializer
import com.app.development.winter.shared.extension.toJson
import com.app.development.winter.ui.language.model.Language
import com.app.development.winter.ui.language.model.Language.Companion.EN
import com.google.gson.Gson
import java.util.Locale

object LocaleHelper {
    fun onAttach(context: Context): Context {
        var locale = getLanguage().locale
        if (locale.isEmpty()) {
            locale = EN
        }
        return setLocale(context, locale)
    }

    fun setLocale(context: Context, language: String?): Context {
        return updateResources(context, language)
    }

    fun setLanguage(language: Language?) {
        getInstance().putString(SharedPrefConstant.APP_LANGUAGE, Gson().toJson(language))
        ReferralLibInitializer.onConfigurationChange()
    }
    fun getLanguage(): Language {
        return Gson().fromJson(
            getInstance().getString(
                SharedPrefConstant.APP_LANGUAGE, getDeviceLanguage()
            ), Language::class.java
        )
    }

    private fun getDeviceLanguage(): String? {
        val locale = Resources.getSystem().configuration.locales.get(0)
        var language = Language.getLanguagesList()[0]
        Language.getLanguagesList().forEach { item ->
            if (locale.language.equals(item.locale)) {
                language = item
                return language.toJson()
            }
        }
        return language.toJson()
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String?): Context {
        val locale = Locale(language ?: EN)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, language: String?): Context {
        val locale = Locale(language!!)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, context.resources.displayMetrics)
        return context
    }
}