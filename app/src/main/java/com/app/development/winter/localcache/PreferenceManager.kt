package com.app.development.winter.localcache

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager {
    companion object {
        private var instance: PreferenceManager? = null
        fun getInstance(): PreferenceManager {
            if (instance == null) instance = PreferenceManager()
            return instance!!
        }

        fun getInstance(context: Context): PreferenceManager {
            if (instance == null) instance = PreferenceManager(context)
            return instance!!
        }
    }

    private var sp: SharedPreferences

    private constructor() {
        sp = com.app.development.winter.application.Controller.instance.getSharedPreferences(
            SharedPrefConstant.PREF_NAME, Context.MODE_PRIVATE
        )
    }

    private constructor(context: Context) {
        sp = context.getSharedPreferences(SharedPrefConstant.PREF_NAME, Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String?) {
        sp.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return sp.getString(key, "")
    }

    fun getStringNull(key: String): String? {
        return sp.getString(key, null)
    }

    fun getString(key: String, defaultString: String?): String? {
        return sp.getString(key, defaultString)
    }

    fun putBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sp.getBoolean(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        sp.edit().putLong(key, value).apply()
    }

    fun getLong(key: String): Long {
        return sp.getLong(key, 0)
    }

    fun removeKey(key: String) {
        sp.edit().remove(key).apply()
    }

    fun putFloat(key: String, value: Float) {
        sp.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, default: Float = 0f): Float {
        return sp.getFloat(key, default)
    }

    fun putInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, default: Int = 0): Int {
        return sp.getInt(key, default)
    }

    fun logout() {
        sp.edit().remove(SharedPrefConstant.IS_LOGIN).apply()
        sp.edit().remove(SharedPrefConstant.TIME_STAMP).apply()
        sp.edit().remove(SharedPrefConstant.USER_DETAILS).apply()
        sp.edit().remove(SharedPrefConstant.EARNING_ESTIMATE).apply()
        sp.edit().remove(SharedPrefConstant.SESSION_ID).apply()
        sp.edit().remove(SharedPrefConstant.IS_NOTIFICATION_ON).apply()
        sp.edit().remove(SharedPrefConstant.WAS_FEEDBACK_DIALOG_SHOWN).apply()
        sp.edit().remove(SharedPrefConstant.SHOW_APP_INSTALLED_DOUBLE_REWARD).apply()
        sp.edit().remove(SharedPrefConstant.IS_SHOW_TUTORIAL).apply()
        sp.edit().remove(SharedPrefConstant.IS_FIRST_LOGIN).apply()
        sp.edit().remove(SharedPrefConstant.SHOW_OVERLAY_TRANSPARENT).apply()
        sp.edit().remove(SharedPrefConstant.SESSION_STATE).apply()
    }

    fun clearUserPreference() {
        sp.edit().remove(SharedPrefConstant.USER_DETAILS).apply()
    }
}