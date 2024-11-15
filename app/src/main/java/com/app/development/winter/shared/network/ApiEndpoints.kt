package com.app.development.winter.shared.network


import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_OK

object ApiEndpoints {


    //headers
    const val HEADER_CONNECTION = "Connection"
    const val HEADER_LANGUAGE = "language"
    const val HEADER_PACKAGE_NAME = "packageName"
    const val HEADER_VERSION = "version"
    const val HEADER_VERSION_CODE = "versionCode"
    const val HEADER_CURRENCY = "currency"
    const val HEADER_SESSION = "authcode"

    // response code
    const val RESPONSE_OK = HTTP_OK
    const val RESPONSE_CREATED = HTTP_CREATED
    const val RESPONSE_ERROR = HTTP_BAD_REQUEST
    const val RESPONSE_VERTS_ERROR = 444

    /**
     * User Enrollment apis
     */
    const val VERSION_CHECK = "checkVersionEstablished"
    const val SOCIAL_LOGIN = "uuli"
    const val REGISTER_DEVICE = "loginEstablished"
    const val DELETE_USER = "deleteUserEstablished"
    const val ADD_FIRST_TIME_PROFILE = "populateUser"
    const val UPLOAD_USER_MEDIA_URL = "uploadUserPhotoWithUrl"
    const val UPLOAD_USER_PHOTO = "uploadUserPhotoEstablished"
    const val UPDATE_USER_PROFILE = "changeUserFieldEstablished"
    const val GET_USER = "getUser"

    /**
     * Session APis
     */
    const val CREATE_NEW_SESSION = "createNewSession"
    const val GET_ACTIVE_SESSION = "getActiveSession"
    const val SAVE_FOCUSED_TIME = "saveFocusedTime"
    const val END_SESSION = "endActiveSession"
    const val GET_PETS = "getPetList"

    /**
     * Notification APis
     */
    const val GET_NOTIFICATION = "getUserNotificationsEstablished"
    const val REGISTER_FCM_TOKEN = "registerForNotificationEstablished"
    const val GET_LATEST_NOTIFICATION="getNewNotificationsForApp"
    /**
     * Profile
     */
    const val GET_PROFILE_EARNINGS = "getEarningStatistics"
    const val GET_USER_STATISTICS = "getUserStatisticData"

    /**
     * General APis
     */
    const val SEND_FEEDBACK = "sendMessageEstablished"

    /**
     * Earning
     */

    /**
     * Leaderboard
     */
    const val GET_LEADERBOARD_USERS = "getOverallLeaderBoard"

    const val CHECK_FOR_AD_TYPE = "canWatchVideo"
    const val GET_AD_REWARD = "getPresentReward"
    const val CLICK_ON_AD = "userClickOnAd"
    const val GET_CYCLIC_AD_REWARD = "getCycleAdsReward"

    /**
     * General API - Hide Login API calls.
     */
    const val HIDE_LOGIN = "hideLogin"
}