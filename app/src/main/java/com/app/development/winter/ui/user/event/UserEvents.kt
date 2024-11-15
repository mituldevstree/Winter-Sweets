package com.app.development.winter.ui.user.event

import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.shared.model.AppConfig
import com.app.development.winter.shared.model.SessionInfo
import com.app.development.winter.shared.model.User
import com.app.development.winter.shared.network.data.BaseModel

sealed class UserEvents : ViewIntent {

    data object RequestAppConfig : UserEvents()

    data object RegisterDevice : UserEvents()

    data class OnAppConfigAvailable(val data: AppConfig?) : UserEvents()

    data class OnAppSessionAvailable(val data: SessionInfo?) : UserEvents()

    data class SendFcmToken(val token: String) : UserEvents()

    data class OnApiFailure(val message: String) : UserEvents()

    data object DeleteUser : UserEvents()

    data class OnDeleteUserAccount(val data: BaseModel?) : UserEvents()

    data class OnDeleteUserAccountFailure(val message: String) : UserEvents()

    data object HiddenLogin : UserEvents()

    data object OnHiddenLoginResponse : UserEvents()

    data class RequestUserViaGoogleLogin(val externalUser: User?, val clientId: String?) : UserEvents()

    data class OnGoogleLoginSuccess(val userInfo: User?) : UserEvents()

    data class PopulateUserData(val userInfo: User?) : UserEvents()

    data class OnPopulateUserDataResponse(val userInfo: User?) : UserEvents()

    data class UpdateUserProfile(val userInfo: User?) : UserEvents()

    data class OnUpdateUserProfileResponse(val userInfo: User?) : UserEvents()

    data class UploadUserProfilePhoto(val image: String) : UserEvents()

    data class OnUploadUserProfilePhotoResponse(val userInfo: User?) : UserEvents()

    data class SendFeedback(val name: String, val email: String, val message: String) : UserEvents()

    data object FeedbackResponse : UserEvents()
}