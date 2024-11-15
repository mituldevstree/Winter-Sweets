package com.app.development.winter.localcache

import android.content.Context
import com.app.development.winter.R
import com.app.development.winter.shared.views.carouselpager.model.AvatarModel
import com.app.development.winter.ui.settings.model.Setting
import com.app.development.winter.ui.settings.model.Setting.SettingType

object StaticData {


    fun getAvatarImage(photoId: String?): AvatarModel? {
        if (photoId == null) return null
        return getAvatarList().find { it.id?.equals(photoId, true) == true }
    }

    fun getAvatarList(): ArrayList<AvatarModel> {
        val list = ArrayList<AvatarModel>()
        list.add(AvatarModel(id = "avatar_1", icon = R.drawable.avatar_1))
        list.add(AvatarModel(id = "avatar_2", icon = R.drawable.avatar_2))
        list.add(AvatarModel(id = "avatar_3", icon = R.drawable.avatar_3))
        list.add(AvatarModel(id = "avatar_4", icon = R.drawable.avatar_4))
        list.add(AvatarModel(id = "avatar_5", icon = R.drawable.avatar_5))
        list.add(AvatarModel(id = "avatar_6", icon = R.drawable.avatar_6))
        list.add(AvatarModel(id = "avatar_7", icon = R.drawable.avatar_7))
        list.add(AvatarModel(id = "avatar_8", icon = R.drawable.avatar_8))
        list.add(AvatarModel(id = "avatar_9", icon = R.drawable.avatar_9))
        list.add(AvatarModel(id = "avatar_10", icon = R.drawable.avatar_10))
        list.add(AvatarModel(id = "avatar_11", icon = R.drawable.avatar_11))
        list.add(AvatarModel(id = "avatar_12", icon = R.drawable.avatar_12))
        list.add(AvatarModel(id = "avatar_13", icon = R.drawable.avatar_13))
        list.add(AvatarModel(id = "avatar_14", icon = R.drawable.avatar_14))
        list.add(AvatarModel(id = "avatar_15", icon = R.drawable.avatar_15))
        list.add(AvatarModel(id = "avatar_16", icon = R.drawable.avatar_16))
        list.add(AvatarModel(id = "avatar_17", icon = R.drawable.avatar_17))
        list.add(AvatarModel(id = "avatar_18", icon = R.drawable.avatar_18))
        list.add(AvatarModel(id = "avatar_19", icon = R.drawable.avatar_19))
        list.add(AvatarModel(id = "avatar_20", icon = R.drawable.avatar_20))
        return list
    }

    fun getSettings(context: Context): ArrayList<Setting> {
        val items: ArrayList<Setting> = ArrayList()

        items.add(
            Setting(
                SettingType.PROFILE,
                title = context.getString(R.string.edit_profile),
                icon = R.drawable.ic_settings_editprofile,
                desc = context.getString(R.string.customize_your_profile_your_way)
            )
        )
        items.add(
            Setting(
                SettingType.GET_SPECIAL_REWARD,
                title = if (LocalDataHelper.isOnProduction()) context.getString(R.string.get_special_rewards) else context.getString(
                    R.string.enter_referral_code
                ),
                icon = R.drawable.ic_settings_reward,
                desc = if (LocalDataHelper.isOnProduction()) context.getString(R.string.get_special_rewards_desc) else context.getString(
                    R.string.enter_the_code_to_unlock
                )
            )
        )
        if (LocalDataHelper.isOnProduction()) {
            items.add(
                Setting(
                    type = SettingType.CONTACT_US,
                    title = context.getString(R.string.contact_us),
                    icon = R.drawable.ic_settings_contact_us,
                    desc = context.getString(R.string.shoot_us_a_message)
                )
            )
        } else {
            items.add(
                Setting(
                    SettingType.XP_OVERLAY,
                    title = context.getString(R.string.show_user_statistics),
                    icon = R.drawable.ic_user_stats,
                    desc = context.getString(R.string.show_user_statistics_message)
                )
            )

        }
        items.add(
            Setting(
                type = SettingType.POLICY,
                title = context.getString(R.string.privacy_policy),
                icon = R.drawable.ic_settings_privacy,
                desc = context.getString(R.string.you_can_consult_our_privacy_policy)
            )
        )
        items.add(
            Setting(
                type = SettingType.TERMS,
                title = context.getString(R.string.terms_and_conditions),
                icon = R.drawable.ic_settings_terms,
                desc = context.getString(R.string.you_can_consult_our_terms_conditions)
            )
        )
        items.add(
            Setting(
                type = SettingType.TUTORIAL,
                title = context.getString(R.string.tutorial),
                icon = R.drawable.ic_settings_tutorial,
                desc = context.getString(R.string.check_out_our_tutorials_for_app_clarity)
            )
        )
        items.add(
            Setting(
                type = SettingType.DELETE,
                title = context.getString(R.string.delete_account),
                icon = R.drawable.ic_settings_delete_account,
                desc = context.getString(R.string.we_ll_miss_you_if_you_leave_us)
            )
        )

        return items
    }

}