package com.app.development.winter.ui.profile.state


import android.os.Parcelable
import com.app.development.winter.shared.base.AdvanceBaseViewModel
import com.app.development.winter.shared.base.ViewState
import com.app.development.winter.ui.profile.model.ProfileEarnings
import kotlinx.parcelize.Parcelize

data class ProfileEarningState(
    val loadingState: Pair<String, AdvanceBaseViewModel.LoadingState> = Pair(
        "", AdvanceBaseViewModel.LoadingState.PROCESSING
    ),
    val errorMessage: String? = null,
    val daily: MutableList<ProfileEarnings>? = null,
    val weekly: MutableList<ProfileEarnings>? = null,
    val monthly: MutableList<ProfileEarnings>? = null,
    val selectedType: EarningFilterType = EarningFilterType.DAILY

) : ViewState {
    companion object {
        val defaultValue = ProfileEarningState(
            loadingState = Pair(
                "",
                AdvanceBaseViewModel.LoadingState.PROCESSING
            )
        )

        @Parcelize
        enum class EarningFilterType(val typeName: String) : Parcelable {
            DAILY("daily"),
            WEEKLY("weekly"),
            MONTHLY("monthly")
        }
    }
}