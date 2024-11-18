package com.app.development.winter.ui.home

import android.view.Gravity
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.app.development.winter.R
import com.app.development.winter.databinding.ActivityHomeBinding
import com.app.development.winter.databinding.LayoutToolbarBinding
import com.app.development.winter.localcache.LocalDataHelper
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.activitybase.CyclicAdsBaseActivity
import com.app.development.winter.shared.extension.handleVisualOverlaps
import com.app.development.winter.shared.network.ApiEndpoints
import com.app.development.winter.ui.session.service.AdvanceBaseFloatingViewService
import com.app.development.winter.ui.user.event.UserEvents
import com.app.development.winter.ui.user.state.UserState
import com.app.development.winter.ui.user.viewmodel.UserViewModel
import com.app.development.winter.utility.Util
import com.givvy.facetec.FaceTecUncaughtException
import com.givvy.invitefriends.builder.ReferralLibManager
import com.givvy.invitefriends.builder.ReferralLibManager.userIdRentApp

class HomeActivity :
    CyclicAdsBaseActivity<ActivityHomeBinding, UserEvents, UserState, UserViewModel>(
        ActivityHomeBinding::inflate, UserViewModel::class.java
    ) {

    override fun getToolBarBinding(): LayoutToolbarBinding? {
        return null
    }

    override fun initDATA() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        mBinding?.root?.handleVisualOverlaps(true,Gravity.BOTTOM)
        Thread.setDefaultUncaughtExceptionHandler(FaceTecUncaughtException(this))
        if (AdvanceBaseFloatingViewService.isServiceRunning.not()) {
            ReferralLibManager.setReferralUser(LocalDataHelper.getUserDetail()?.id,
                deviceID = Util.getUniquePsuedoID(),
                onReferralInitProcessingComplete = { value, isSuccess -> },
                onReferralLinkEstablished = { value, isSuccess -> },
                onRentAppUserMigrated = { s, data, success ->
                    if (success == true) {
                        data?.let {
                            if (it.cycleAdsForRentApp) {
                                userIdRentApp = null
                                LocalDataHelper.getUserDetail()?.apply {
                                    id = it.id
                                    cycleAdsForRentApp = it.cycleAdsForRentApp
                                }
                                recreate()
                            }
                        }
                    }
                })
        }
    }

    override fun initUI() {
        setBottomNavigation()
    }

    private fun setBottomNavigation() {
        setSupportActionBar(Toolbar(this))
        val navController = findNavController(R.id.fragmentContainerView)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            currentNavController = controller
            mBinding?.ivHome?.isSelected = destination.id == R.id.navHome
        }
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navHome,
                R.id.navWithdraw,
                R.id.navOffers,
                R.id.navReferral,
                R.id.navSetting
            )
        )
        mBinding?.bottomMenu?.setOnApplyWindowInsetsListener(null)
        mBinding?.bottomMenu?.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        // always show selected Bottom Navigation item as selected (return true)
        mBinding?.bottomMenu?.setOnItemSelectedListener { item ->
            // In order to get the expected behavior, you have to call default Navigation method manually
            NavigationUI.onNavDestinationSelected(item, navController)
            return@setOnItemSelectedListener true
        }
        mBinding?.bottomMenu?.itemIconTintList = null
        mBinding?.ivHome?.setOnClickListener {
            onTabChangeRequest(R.id.navHome)
        }
    }

    override fun render(state: UserState) {
        if (state.loadingState.first == ApiEndpoints.GET_USER) {
            if (state.loadingState.second == AdvanceBaseViewModel.LoadingState.COMPLETED) {
                onUserInfoUpdated()
            }
        }
    }

    override fun onTabChangeRequest(itemId: Int) {
        mBinding?.bottomMenu?.selectedItemId = itemId
    }
}