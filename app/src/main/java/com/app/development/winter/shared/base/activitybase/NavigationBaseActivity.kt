package com.app.development.winter.shared.base.activitybase

import android.content.Intent
import android.view.LayoutInflater
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.app.development.winter.BottomMenuNavGraphDirections
import com.app.development.winter.R
import com.app.development.winter.application.Controller
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewIntent
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.shared.base.viewbase.AdvancedBaseActivity
import com.app.development.winter.shared.extension.showAlertDialog
import com.app.development.winter.ui.country.BottomSheetCountry
import com.app.development.winter.ui.home.HomeActivity
import com.app.development.winter.ui.language.BottomSheetLanguage
import com.app.development.winter.ui.language.model.Language
import com.app.development.winter.ui.notification.NotificationsActivity
import com.app.development.winter.ui.rate.BottomSheetFeedback
import com.app.development.winter.ui.rate.BottomSheetRate
import com.app.development.winter.ui.session.SessionActivity
import com.app.development.winter.ui.session.service.AdvanceBaseFloatingViewService.Companion.isServiceRunning
import com.app.development.winter.ui.settings.EditProfileActivity
import com.app.development.winter.ui.user.LandingActivity
import com.app.development.winter.ui.user.SocialLoginActivity
import com.app.development.winter.ui.user.TutorialActivity
import com.app.development.winter.ui.user.WelcomeActivity
import com.app.development.winter.utility.ViewUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class NavigationBaseActivity<VDB : ViewDataBinding, INTENT : ViewIntent, STATE : ViewState, VM : AdvanceBaseViewModel<INTENT, STATE>>(
    bindingFactory: (LayoutInflater) -> VDB,
    modelClass: Class<VM>,
) : AdvancedBaseActivity<VDB, INTENT, STATE, VM>(bindingFactory, modelClass) {

    open fun onTabChangeRequest(itemId: Int) {}
    open fun checkAndStartSession() {}

    fun openSplashActivity() {
        val intent = Intent(this, LandingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }

    fun openLoginActivity() {
        val intent = Intent(this, SocialLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun openWelComeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun openWithdrawFragment() {
        val action = BottomMenuNavGraphDirections.actionFromThisToWithdrawFragment()
        findNavController(R.id.fragmentContainerView).navigate(action)
    }

    fun openSettingsFragment() {
        val action = BottomMenuNavGraphDirections.actionFromThisToSettingsFragment()
        findNavController(R.id.fragmentContainerView).navigate(action)
    }

    fun openNotificationActivity() {
        val intent = Intent(this, NotificationsActivity::class.java)
        startActivity(intent)
    }

    fun openEditProfileActivity() {
        val intent = Intent(this, EditProfileActivity::class.java)
        startActivity(intent)
    }

    fun openTutorialActivity(intentType: Int = REQUEST_DEFAULT) {
        val intent = Intent(this, TutorialActivity::class.java)
        intent.putExtra(INTENT_KEY, intentType)
        startActivity(intent)
        if (intentType == REQUEST_DEFAULT) finish()
    }

    fun openHomeActivity(intentType: Int = REQUEST_DEFAULT) {
        if (intentType == FORCE_REFRESH_HOME) {
            onBackPressedDispatcher.onBackPressed()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra(INTENT_KEY, intentType)
            startActivity(intent)
            finishAffinity()
        }
    }

    fun openSessionActivity() {
        val intent = Intent(this, SessionActivity::class.java)
        startActivity(intent)
    }

    fun showCountryDialog(selectedCountry: String?, callback: (String?) -> Unit) {
        BottomSheetCountry.newInstance(selectedCountry) {
            val country = it
            callback.invoke(country)
        }.show(this.supportFragmentManager)
    }

    fun showLanguageDialog(selectedLanguage: Language?, callback: (Language) -> Unit) {
        BottomSheetLanguage.newInstance(selectedLanguage) {
            it?.let {
                val language = it
                callback.invoke(language)
            }
        }.show(this.supportFragmentManager)
    }

    fun openTermsConditionLink() {
        LocalDataHelper.getUserConfig()?.termsAndConditionUrl?.let { url ->
            if (url.isNotEmpty()) {
                ViewUtil.openWebPage(this, url)
            }
        }
    }

    fun openPrivacyPolicyLink() {
        LocalDataHelper.getUserConfig()?.privacyPolicyUrl?.let { url ->
            if (url.isNotEmpty()) {
                ViewUtil.openWebPage(this, url)
            }
        }
    }

    override fun showAppRatingDialog() {
        if (isServiceRunning.not() && LocalDataHelper.haveActiveSession.not()) openAppRatingDialog()
    }

    private fun openAppRatingDialog() {
        if (isFinishing || isDestroyed
            || (Controller.foregroundActivity is HomeActivity).not()
            || LocalDataHelper.isOnProduction().not()
        ) return

        val isValidate =
            !LocalDataHelper.wasFeedbackGiven() && !LocalDataHelper.isFirstLogin() && Controller.needToShowRatingFlow

        if (isValidate) {
            Controller.needToShowRatingFlow = false
            BottomSheetRate.newInstance {
                when (it) {
                    BottomSheetRate.MODE_RATING -> {
                        LocalDataHelper.setWasFeedbackGiven(true)
                        ViewUtil.openAppRating(this@NavigationBaseActivity)
                    }

                    BottomSheetRate.MODE_FEEDBACK -> {
                        openFeedbackDialog()
                    }

                    else -> {
                    }
                }
            }.show(supportFragmentManager)
        } else {
            Controller.needToShowRatingFlow = false
            LocalDataHelper.setFirstLogin(false)
        }
    }

    private fun openFeedbackDialog() {
        lifecycleScope.launch {
            delay(10)
            BottomSheetFeedback.newInstance {
                if (it) {
                    openFeedbackThankYouDialog()
                } else {
//                    checkAndStartGameSession()
                }
                LocalDataHelper.setWasFeedbackGiven(true)
            }.show(supportFragmentManager)
        }
    }

    private fun openFeedbackThankYouDialog() {
        lifecycleScope.launch {
            delay(10)
            LocalDataHelper.setWasFeedbackGiven(true)
            showAlertDialog(title = getString(R.string.thank_you_for_your_feedback),
                message = getString(R.string.thank_you_for_your_feedback_message),
                positiveRes = R.string.back_to_app,
                positiveClick = {

                },
                onCancel = {

                },
                onDismiss = {
//                    checkAndStartGameSession()
                })
        }
    }

    open fun showUserStatistics() {}

    companion object {
        const val REQUEST_PROFILE = 1
        const val REQUEST_DEFAULT = 0
        const val REQUEST_TUTORIAL = 2
        const val REQUEST_LANGUAGE = 3

        const val INTENT_KEY: String = "intent_type"
        const val FORCE_REFRESH_HOME: Int = 7

        const val RESULT_TYPE = "resultType"
        const val PROFILE_EDITED = "profileEdited"
        const val SAVE_N_START_OVERLAY = "saveAndStartOverlay"
        const val RESULT_KEY = "fragmentResult"
    }
}

