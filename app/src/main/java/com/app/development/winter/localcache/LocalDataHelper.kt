package com.app.development.winter.localcache


import com.app.development.winter.localcache.PreferenceManager.Companion.getInstance
import com.app.development.winter.localcache.SharedPrefConstant.APP_CONFIG
import com.app.development.winter.localcache.SharedPrefConstant.IS_FIRST_LOGIN
import com.app.development.winter.localcache.SharedPrefConstant.IS_NOTIFICATION_ON
import com.app.development.winter.localcache.SharedPrefConstant.LAST_SEEN_NOTIFICATION_ID
import com.app.development.winter.localcache.SharedPrefConstant.SESSION_COUNT
import com.app.development.winter.localcache.SharedPrefConstant.SESSION_ID
import com.app.development.winter.localcache.SharedPrefConstant.SESSION_STATE
import com.app.development.winter.localcache.SharedPrefConstant.SHOW_OVERLAY_TRANSPARENT
import com.app.development.winter.localcache.SharedPrefConstant.USER_CONFIG
import com.app.development.winter.localcache.SharedPrefConstant.USER_DETAILS
import com.app.development.winter.localcache.SharedPrefConstant.WAS_FEEDBACK_DIALOG_SHOWN
import com.app.development.winter.shared.model.AppConfig
import com.app.development.winter.shared.model.User
import com.app.development.winter.shared.model.UserConfig
import com.app.development.winter.ui.session.model.SessionData
import com.app.development.winter.utility.ViewUtil
import com.google.gson.Gson

object LocalDataHelper {
    private var user: User? = null

    fun setFirstLogin(isFirstLogin: Boolean) {
        getInstance().putBoolean(IS_FIRST_LOGIN, isFirstLogin)
    }

    fun isFirstLogin(): Boolean {
        return getInstance().getBoolean(IS_FIRST_LOGIN, true)
    }

    fun setWasFeedbackGiven(wasAccepted: Boolean) {
        getInstance().putBoolean(WAS_FEEDBACK_DIALOG_SHOWN, wasAccepted)
    }

    fun wasFeedbackGiven(): Boolean {
        return getInstance().getBoolean(WAS_FEEDBACK_DIALOG_SHOWN, false)
    }

    fun setSessionId(token: String?) {
        token ?: return
        return getInstance().putString(SESSION_ID, token)
    }

    fun getSessionId(): String? {
        return getInstance().getString(SESSION_ID)
    }

    fun getAuthCode(): String {
        var authCode = ""
        getSessionId()?.let { id ->
            if (id.isNotEmpty()) authCode =
                authCode.plus(ViewUtil.sessionIdEncode(id.substring(3, 8), 4))
        }

        getUserDetail()?.id?.let { id ->
            if (id.isNotEmpty()) authCode =
                authCode.plus(ViewUtil.sessionIdEncode(id.substring(5, 11), 5))
        }
        return authCode
    }

    fun isOverlayTransparent(): Boolean {
        return getInstance().getBoolean(SHOW_OVERLAY_TRANSPARENT, false)
    }

    fun setOverlayTransparent(isTransparent: Boolean) {
        getInstance().putBoolean(SHOW_OVERLAY_TRANSPARENT, isTransparent)
    }

    fun updateSessionCount(operation: (Int) -> Int) {
        var currentSessionCount = getSessionEnteredCount()
        currentSessionCount = operation(currentSessionCount)
        setSessionsEnteredCount(currentSessionCount)
    }

    private fun setSessionsEnteredCount(sessionCount: Int) {
        getInstance().putInt(SESSION_COUNT, sessionCount)
    }

    fun getSessionEnteredCount(): Int {
        return getInstance().getInt(SESSION_COUNT, 1)
    }

    fun getLastSeenNotificationID(): String? {
        return getInstance().getString(LAST_SEEN_NOTIFICATION_ID, null)
    }

    fun setLastSeenNotificationID(lastSeenNotificationId: String) {
        getInstance().putString(LAST_SEEN_NOTIFICATION_ID, lastSeenNotificationId)
    }

   fun setUserDetail(updatedUser: User?) {
        user = updatedUser
        user?.notifyChange()
        getInstance().putString(USER_DETAILS, Gson().toJson(updatedUser))
    }

    fun getUserDetail(): User? {
        if (user == null) {
            user = Gson().fromJson(getInstance().getString(USER_DETAILS), User::class.java)
        }
        return user
    }

    fun setUserConfig(user: UserConfig?) {
        getInstance().putString(USER_CONFIG, Gson().toJson(user))
    }

    fun getUserConfig(): UserConfig? {
        return Gson().fromJson(getInstance().getString(USER_CONFIG), UserConfig::class.java)
    }

    fun setAppConfig(appConfig: AppConfig?) {
        if (appConfig == null) return
        getInstance().putString(APP_CONFIG, Gson().toJson(appConfig))
    }

    fun getAppConfig(): AppConfig? {
        return Gson().fromJson(getInstance().getString(APP_CONFIG), AppConfig::class.java)
    }

    var isNotificationOn: Boolean
        get() {
            return getInstance().getBoolean(IS_NOTIFICATION_ON, true)
        }
        set(value) {
            return getInstance().putBoolean(IS_NOTIFICATION_ON, value)
        }

    var haveActiveSession: Boolean
        get() {
            return getInstance().getBoolean(SESSION_STATE, false)
        }
        set(value) {
            return getInstance().putBoolean(SESSION_STATE, value)
        }


    fun isOnProduction(): Boolean {
        return getUserDetail()?.isOnProduction ?: false
    }

    fun logout() {
        getInstance().logout()
    }

    fun clearUserPreference() {
        getInstance().clearUserPreference()
    }

    fun updateUserCoinsData(externalUser: User?) {
        externalUser?.let {
            //user?.cashOutPercent = it.cashOutPercent
            user?.coinBalance = it.coinBalance
            user?.userBalance = it.userBalance
            user?.userBalanceDouble = it.userBalanceDouble
        }
        setUserDetail(user)
    }


    fun updateUserXPData(data: SessionData.CurrentEarningsData?) {
        data?.let {
            user?.coinBalance = it.userCredits
            user?.userBalance = it.userCreditsInUSD
            user?.userBalanceDouble = it.userCreditsInUSD
        }
        setUserDetail(user)
    }

}