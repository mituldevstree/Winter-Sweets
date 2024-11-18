package com.app.development.winter.utility

import android.view.View
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.RenderMode
import com.app.development.winter.R

object LottieAnimationUtil {
    @JvmStatic
    @BindingAdapter(value = ["android:setHomePageAnimation"], requireAll = false)
    fun setHomePageAnimation(animView: LottieAnimationView, autoPlayLottie: Boolean) {
        animView.imageAssetsFolder = "homepage"
        animView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        animView.enableMergePathsForKitKatAndAbove(true)
        animView.renderMode = RenderMode.HARDWARE
        animView.setAnimation(R.raw.lottie_home_page)
        if (autoPlayLottie) {
            animView.playAnimation()
        } else {
            animView.progress = 0f
            animView.cancelAnimation()
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["android:setSessionPageAnimation"], requireAll = false)
    fun setSessionPageAnimation(animView: LottieAnimationView, autoPlayLottie: Boolean) {
        animView.imageAssetsFolder = "sessionpage"
        animView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        animView.enableMergePathsForKitKatAndAbove(true)
        animView.renderMode = RenderMode.HARDWARE
        animView.setAnimation(R.raw.lottie_session_page)
        if (autoPlayLottie) {
            animView.playAnimation()
        } else {
            animView.progress = 0f
            animView.cancelAnimation()
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["android:setAppDefaultLoader"], requireAll = false)
    fun setAppDefaultLoader(animView: LottieAnimationView, autoPlayLottie: Boolean) {
        animView.imageAssetsFolder = "sessionpage"
        animView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        animView.enableMergePathsForKitKatAndAbove(true)
        animView.renderMode = RenderMode.HARDWARE
        animView.setAnimation(R.raw.lottie_session_page)
        if (autoPlayLottie) {
            animView.playAnimation()
        } else {
            animView.progress = 0f
            animView.cancelAnimation()
        }
    }
    @JvmStatic
    @BindingAdapter(value = ["android:setSplashAnimation"], requireAll = false)
    fun setSplashAnimation(animView: LottieAnimationView, autoPlayLottie: Boolean) {
        animView.imageAssetsFolder = "splashpage"
        animView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        animView.enableMergePathsForKitKatAndAbove(true)
        animView.renderMode = RenderMode.HARDWARE
        animView.setAnimation(R.raw.lottie_splash_page)
        if (autoPlayLottie) {
            animView.playAnimation()
        } else {
            animView.progress = 0f
            animView.cancelAnimation()
        }
    }
}