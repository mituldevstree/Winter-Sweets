package com.app.development.winter.ui.user.viewmodel

import androidx.lifecycle.viewModelScope
import com.app.development.winter.BuildConfig
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.localcache.LocaleHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.model.SessionInfo
import com.app.development.winter.shared.model.User
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.shared.network.ApiEndpoints.RESPONSE_CREATED
import com.app.development.winter.shared.network.ApiEndpoints.RESPONSE_OK
import com.app.development.winter.shared.network.domain.UserRepository
import com.app.development.winter.shared.network.security.TimeStampManager
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.state.UserState.Companion.defaultValue
import com.app.development.winter.utility.Util
import com.app.development.winter.utility.ViewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONObject

class UserViewModel() : AdvanceBaseViewModel<UserEvents, UserState>(defaultValue) {

    fun getUserUiState() = _state

    override fun reduceState(
        currentState: UserState,
        event: UserEvents,
    ): UserState = when (event) {
        is UserEvents.RequestAppConfig -> {
            versionCheck()
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.VERSION_CHECK, LoadingState.PROCESSING
                )
            )
        }

        is UserEvents.OnAppConfigAvailable -> {
            val config = event.data
            if (config != null && (config.hasUpdate || config.isForceUpdate)) {
                currentState.copy(
                    loadingState = Pair(
                        ApiEndpoints.VERSION_CHECK, LoadingState.COMPLETED
                    )
                )
            } else {
                registerDevice()
                currentState.copy(
                    loadingState = Pair(
                        ApiEndpoints.REGISTER_DEVICE, LoadingState.PROCESSING
                    )
                )
            }
        }

        is UserEvents.RegisterDevice -> {
            registerDevice()
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.REGISTER_DEVICE, LoadingState.PROCESSING
                )
            )
        }

        is UserEvents.DeleteUser -> {
            deleteUserAccount()
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.DELETE_USER, LoadingState.PROCESSING
                )
            )
        }

        is UserEvents.SendFcmToken -> {
            val param: MutableMap<String, String> = HashMap()
            param["notificationToken"] = event.token
            setFcmToken(param)
            currentState.copy(
                loadingState = Pair(
                    "", LoadingState.PROCESSING
                )
            )
        }

        is UserEvents.OnAppSessionAvailable -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.REGISTER_DEVICE, LoadingState.COMPLETED
                ), appConfig = null, appSession = event.data
            )
        }

        is UserEvents.OnDeleteUserAccount -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.DELETE_USER, LoadingState.COMPLETED
                ), deleteUser = event.data
            )
        }

        is UserEvents.OnDeleteUserAccountFailure -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.DELETE_USER, LoadingState.ERROR
                ), errorMessage = event.message
            )
        }

        is UserEvents.RequestUserViaGoogleLogin -> {
            performSocialLogin(event.externalUser, event.clientId)
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.SOCIAL_LOGIN, LoadingState.PROCESSING
                ),
            )

        }

        is UserEvents.OnGoogleLoginSuccess -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.SOCIAL_LOGIN, LoadingState.COMPLETED
                ), userInfo = event.userInfo
            )
        }

        is UserEvents.HiddenLogin -> {
            hiddenLogin()
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.HIDE_LOGIN, LoadingState.PROCESSING
                ),
            )
        }

        is UserEvents.OnHiddenLoginResponse -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.HIDE_LOGIN, LoadingState.COMPLETED
                )
            )
        }

        is UserEvents.OnApiFailure -> {
            currentState.copy(
                loadingState = Pair(
                    "", LoadingState.ERROR
                ), errorMessage = event.message
            )
        }

        is UserEvents.PopulateUserData -> {
            updateRegisteredDeviceUserInfo(event.userInfo, null)
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.ADD_FIRST_TIME_PROFILE, LoadingState.PROCESSING
                ),
            )
        }

        is UserEvents.OnPopulateUserDataResponse -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.ADD_FIRST_TIME_PROFILE, LoadingState.COMPLETED
                ), userInfo = event.userInfo
            )
        }

        is UserEvents.UpdateUserProfile -> {
            updateUserProfile(event.userInfo)
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.UPDATE_USER_PROFILE, LoadingState.PROCESSING
                ),
            )
        }

        is UserEvents.OnUpdateUserProfileResponse -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.UPDATE_USER_PROFILE, LoadingState.COMPLETED
                ), userInfo = event.userInfo
            )
        }

        is UserEvents.UploadUserProfilePhoto -> {
            uploadUserProfilePhoto(event.image)
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.UPLOAD_USER_PHOTO, LoadingState.PROCESSING
                ),
            )
        }

        is UserEvents.OnUploadUserProfilePhotoResponse -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.UPLOAD_USER_PHOTO, LoadingState.COMPLETED
                ), userInfo = event.userInfo
            )
        }


        is UserEvents.SendFeedback -> {
            sendFeedback(name = event.name, email = event.email, message = event.message)
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.SEND_FEEDBACK, LoadingState.PROCESSING
                ),
            )
        }

        is UserEvents.FeedbackResponse -> {
            currentState.copy(
                loadingState = Pair(
                    ApiEndpoints.SEND_FEEDBACK, LoadingState.COMPLETED
                ),
            )
        }
    }


    /**
     * Version check configs.
     */
    private fun versionCheck() {
        val params: MutableMap<String, Any> = HashMap()
        params["version"] = BuildConfig.VERSION_NAME
        viewModelScope.launch {
            delay(400)
            UserRepository.versionCheck(params).catch { result ->
                result.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    LocalDataHelper.setAppConfig(result.data)
                    result.data?.date?.let { date ->
                        TimeStampManager.backendTimeStamp = date
                        TimeStampManager.completedTimeStamp = System.currentTimeMillis()
                    }
                    _state.handleEvent(UserEvents.OnAppConfigAvailable(data = result.data))
                } else {
                    result?.message?.let { error ->
                        _state.handleEvent(UserEvents.OnApiFailure(error))
                    }
                }
            }
        }

    }


    /**
     * Device registration and generating user for that device.
     */
    private fun registerDevice() {
        val params: MutableMap<String, Any> = HashMap()
        params["deviceId"] = Util.getUniquePsuedoID()
        params["deviceType"] = "Android"
        val user = LocalDataHelper.getUserDetail()
        if (user != null && user.externalUserID.isNullOrEmpty().not()) {
            params["externalId"] = user.externalUserID.toString()
        }
        if (user != null && user.externalSessionCode.isNullOrEmpty().not()) {
            params["userId"] = user.id ?: ""
            params["rqCode"] = user.externalSessionCode ?: ""
        }
        viewModelScope.launch {
            UserRepository.registerDevice(params, Dispatchers.IO).collect {
                if (it != null && it.code == RESPONSE_OK) {
                    LocalDataHelper.setUserDetail(it.data?.user)
                    LocalDataHelper.setUserConfig(it.data?.userConfig)
                    LocalDataHelper.setSessionId(it.data?.sessionId)
                    _state.handleEvent(UserEvents.OnAppSessionAvailable(data = it.data))
                } else {
                    it?.message?.let { error -> _state.handleEvent(UserEvents.OnApiFailure(error)) }
                }
            }
        }
    }

    /**
     * Linking registered device user with social media external user details.
     */
    private fun performSocialLogin(
        externalUserInfo: User?, clientId: String?
    ) {
        val params: MutableMap<String, Any> = HashMap()
        LocalDataHelper.getUserDetail() ?: return
        val savedUser: User = LocalDataHelper.getUserDetail()!!

        externalUserInfo ?: return
        params["userId"] = savedUser.id.toString()
        params["externalId"] = externalUserInfo.externalUserID.toString()
        params["deviceId"] = Util.getUniquePsuedoID()
        params["externalValidationToken"] = externalUserInfo.externalAuthToken.toString()
        params["forceAssociate"] = true
        params["isGoogleLogin"] = externalUserInfo.loginType.equals(User.LOGIN_GOOGLE)
        if (externalUserInfo.loginType.equals(User.LOGIN_GOOGLE)) {
            params["audience"] = clientId ?: ""
        }
        viewModelScope.launch {
            UserRepository.socialLogin(params, Dispatchers.IO).onStart {

            }.onCompletion {

            }.catch { exception ->
                exception.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    sendUserCallBack(result.data, externalUserInfo) {
                        _state.handleEvent(UserEvents.OnGoogleLoginSuccess(savedUser))
                    }
                } else result?.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }

            }
        }
    }

    /**
     * Delete user Account Api call.
     */
    private fun deleteUserAccount() {
        val params: MutableMap<String, Any> = HashMap()
        val user = LocalDataHelper.getUserDetail() ?: return
        params["userId"] = user.id.toString()
        viewModelScope.launch {
            UserRepository.deleteUser(params, Dispatchers.IO).onStart {

            }.onCompletion {

            }.catch { exception ->
                exception.message?.let { _state.handleEvent(UserEvents.OnDeleteUserAccountFailure(it)) }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    _state.handleEvent(UserEvents.OnDeleteUserAccount(data = result.data))
                } else {
                    result?.message?.let { error ->
                        _state.handleEvent(
                            UserEvents.OnDeleteUserAccountFailure(
                                error
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Device registration and generating user for that device.
     */
    private fun performLoginViaWebSession(sessionCode: String) {
        val user = LocalDataHelper.getUserDetail()
        user ?: return
        user.externalSessionCode = sessionCode
        LocalDataHelper.setUserDetail(user)
        val params: MutableMap<String, Any> = HashMap()
        params["deviceId"] = Util.getUniquePsuedoID()
        params["deviceType"] = "Android"
        if (user.externalSessionCode.isNullOrEmpty().not()) {
            params["userId"] = user.id ?: ""
            params["qrCode"] = user.externalSessionCode ?: ""
        }
        viewModelScope.launch {
            UserRepository.registerDevice(params, Dispatchers.IO).collect {
                if (it != null && it.code == RESPONSE_OK) {
                    LocalDataHelper.setUserConfig(it.data?.userConfig)
                    sendUserCallBack(it.data?.user, user) { session ->
                        _state.handleEvent(UserEvents.OnGoogleLoginSuccess(session?.user))
                    }
                } else {
                    it?.message?.let { error -> _state.handleEvent(UserEvents.OnApiFailure(error)) }
                }
            }
        }
    }

    private fun sendUserCallBack(
        sessionInfo: User?, externalUserInfo: User, onRemoteCallback: ((SessionInfo?) -> Unit)?
    ) {
        sessionInfo ?: return
        viewModelScope.launch {
            sessionInfo.name = (externalUserInfo.name)
            sessionInfo.externalUserID = (externalUserInfo.externalUserID)
            sessionInfo.externalAuthToken = (externalUserInfo.externalAuthToken)
            sessionInfo.email = (externalUserInfo.email)
            sessionInfo.loginType = (externalUserInfo.loginType)
            if (sessionInfo.photo == null) {
                if (externalUserInfo.photo != null) sessionInfo.photo = externalUserInfo.photo
            }
            LocalDataHelper.setUserDetail(sessionInfo)
            updateRegisteredDeviceUserInfo(externalUserInfo) {
                onRemoteCallback?.invoke(it)
            }
        }
    }

    /**
     * Updating information of registered device users info
     * userName and country.
     */
    private fun updateRegisteredDeviceUserInfo(
        externalUser: User?,
        onRemoteCallback: ((SessionInfo?) -> Unit)?,
    ) {
        externalUser ?: return
        val params: MutableMap<String, Any> = HashMap()
        LocalDataHelper.getUserDetail()?.id?.let { userId ->
            params["userId"] = userId
            params["name"] = externalUser.name ?: ""
            params["username"] = externalUser.name ?: ""
            if (externalUser.photo?.isEmpty()?.not() == true) params["photo"] =
                externalUser.photo ?: ""
            if (externalUser.country?.isEmpty()?.not() == true) params["country"] =
                externalUser.country ?: ""

            viewModelScope.launch {
                UserRepository.updateRegisteredDevicesUserInfo(params, Dispatchers.IO).catch {
                    onRemoteCallback?.invoke(null)
                }.collect { result ->
                    if (result != null && result.code == RESPONSE_OK) {
                        if (onRemoteCallback != null) onRemoteCallback.invoke(result.data)
                        else {
                            _state.handleEvent(UserEvents.OnPopulateUserDataResponse(result.data?.user))
                        }
                    } else {
                        result?.message?.let { onError(it) }
                        if (onRemoteCallback != null) onRemoteCallback.invoke(null)
                        else {
                            _state.handleEvent(UserEvents.OnPopulateUserDataResponse(null))
                        }

                    }
                }
            }
        }
    }

    /**
     * Update user profile details.
     */
    private fun updateUserProfile(userInfo: User?) {
        val params: MutableMap<String, Any> = HashMap()
        LocalDataHelper.getUserDetail()?.id?.let { userId ->
            params["userId"] = userId
            params["name"] = userInfo?.name ?: ""
            params["username"] = userInfo?.name ?: ""
            params["photo"] = userInfo?.photo ?: ""
            if (userInfo?.country?.isEmpty()?.not() == true) params["country"] =
                userInfo.country ?: ""
        }

        viewModelScope.launch {
            UserRepository.updateUserProfile(params, Dispatchers.IO).onStart {

            }.onCompletion {

            }.catch { exception ->
                exception.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    _state.handleEvent(UserEvents.OnUpdateUserProfileResponse(result.data))
                } else result?.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }

            }
        }
    }

    /**
     * Upload user profile image as a Base64String.
     */
    private fun uploadUserProfilePhoto(imageUrl: String) {
        val params = JSONObject()
        params.put("photo", imageUrl)
        params.put("userId", LocalDataHelper.getUserDetail()?.id)

        viewModelScope.launch {
            UserRepository.uploadUserProfilePhoto(params.toString(), Dispatchers.IO).onStart {

            }.onCompletion {

            }.catch { exception ->
                exception.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }.collect { result ->
                if (result != null && (result.code == RESPONSE_OK || result.code == RESPONSE_CREATED)) {
                    _state.handleEvent(UserEvents.OnUploadUserProfilePhotoResponse(result.data))
                } else result?.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }

            }
        }
    }

    /**
     * Linking social media platforms profile media with registered device user.
     */
    private fun linkSocialMediaProfileURL(onRemoteCallback: ((User?) -> Unit)?) {
        val params = JSONObject()
        LocalDataHelper.getUserDetail() ?: return
        LocalDataHelper.getUserDetail()?.id ?: return
        LocalDataHelper.getUserDetail()?.photo ?: return
        params.put("userId", LocalDataHelper.getUserDetail()?.id)
        params.put("photoUrl", LocalDataHelper.getUserDetail()?.photo)
        viewModelScope.launch {
            UserRepository.linkSocialMediaProfile(params.toString(), Dispatchers.IO).onStart {

            }.onCompletion {

            }.catch {

            }.collect { result ->
                if (result != null && (result.code == RESPONSE_OK || result.code == RESPONSE_CREATED)) {
                    onRemoteCallback?.invoke(result.data)
                } else result?.message?.let { onError(it) }
            }
        }
    }

    /**
     *  Send fcm token to server
     */
    private fun setFcmToken(requestParam: MutableMap<String, String>) {
        requestParam["deviceId"] = Util.getUniquePsuedoID()
        requestParam["language"] = LocaleHelper.getLanguage().locale
        requestParam["deviceType"] = "Android"

        viewModelScope.launch {
            UserRepository.setFcmToken(requestParam).collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    ViewUtil.printLog("Was success", result.message)
                }
            }
        }
    }

    fun getUser(onRemoteCallback: (() -> Unit)?) {
        val params: HashMap<String, Any> = HashMap()
        params["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        viewModelScope.launch {
            UserRepository.getUser(params, Dispatchers.IO).catch { exception ->
                exception.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    if (result.data != null) {
                        val savedUser = result.data
                        com.app.development.winter.localcache.LocalDataHelper.updateUserCoinsData(
                            savedUser
                        )
//                        LocalDataHelper.setUserDetail(savedUser)
                        onRemoteCallback?.invoke()
                    }
                } else {
                    result?.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
                }
            }
        }
    }

    /**
     * Send feedback
     */
    private fun sendFeedback(name: String, email: String, message: String) {
        val params = HashMap<String, Any>()
        if (LocalDataHelper.getUserDetail()?.id != null) {
            params["userId"] = LocalDataHelper.getUserDetail()?.id.toString()
        }
        params["name"] = name
        params["email"] = email
        params["message"] = message

        viewModelScope.launch {
            UserRepository.sendFeedback(params, Dispatchers.IO).onStart {

            }.onCompletion {

            }.catch { exception ->
                exception.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    _state.handleEvent(UserEvents.FeedbackResponse)
                } else result?.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }
        }
    }

    /**
     * Linking registered device user with social media external user details.
     */
    private fun hiddenLogin() {
        val params: MutableMap<String, Any> = HashMap()
        params["userId"] = LocalDataHelper.getUserDetail()?.id ?: ""
        params["packageName"] = BuildConfig.APPLICATION_ID

        viewModelScope.launch {
            UserRepository.hiddenLogin(params, Dispatchers.IO).onStart {

            }.onCompletion {

            }.catch { exception ->
                exception.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }.collect { result ->
                if (result != null && result.code == RESPONSE_OK) {
                    _state.handleEvent(UserEvents.OnHiddenLoginResponse)
                } else result?.message?.let { _state.handleEvent(UserEvents.OnApiFailure(it)) }
            }
        }
    }
}
