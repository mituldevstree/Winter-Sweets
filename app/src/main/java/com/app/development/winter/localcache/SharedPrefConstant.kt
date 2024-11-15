package com.app.development.winter.localcache

import com.app.development.winter.BuildConfig


object SharedPrefConstant {
    const val PREF_NAME = BuildConfig.APPLICATION_ID
    const val IS_LOGIN = "is_login"
    const val IS_FIRST_LOGIN = "is_first_login"
    const val SESSION_COUNT = "session_count"
    const val TIME_STAMP = "time_stamp"
    const val WAS_FEEDBACK_DIALOG_SHOWN = "was_feedback_shown"
    const val USER_DETAILS = "user_details"
    const val APP_CONFIG = "app_config"
    const val USER_CONFIG = "user_config"
    const val EARNING_ESTIMATE = "earning_estimate"
    const val APP_LANGUAGE = "app_language"
    const val SESSION_ID = "session_id"
    const val IS_SHOW_TUTORIAL = "is_show_tutorial"
    const val IS_NOTIFICATION_ON = "is_notification_on"
    const val SESSION_STATE = "set_session_state"
    const val SHOW_OVERLAY_TRANSPARENT = "show_overlay_transparent"
    const val SHOW_APP_INSTALLED_DOUBLE_REWARD = "app_installed_double_reward"
    const val LAST_SEEN_NOTIFICATION_ID = "last_seen_notification_id"
}
