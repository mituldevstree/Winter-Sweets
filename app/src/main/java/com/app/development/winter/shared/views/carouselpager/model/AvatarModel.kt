package com.app.development.winter.shared.views.carouselpager.model

import androidx.annotation.Keep

@Keep
data class AvatarModel(
    val id: String? = null,
    val icon: Int? = null,
    val isSelected: Boolean = false,
)