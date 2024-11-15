package com.app.development.winter.shared.extension

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy

fun ImageView.setImageResOrGone(@DrawableRes res: Int) {
    val hasRes = res != -1 && res != 0
    isVisible = hasRes
    if (hasRes) {
        setImageResource(res)
    }
}

fun ImageView.setImageDrawableOrGone(drawable: Drawable?) {
    val hasDrawable = drawable != null
    isVisible = hasDrawable
    if (hasDrawable) {
        setImageDrawable(drawable)
    }
}


fun ImageView.loadAvatar(
    url: String?,
    requestManager: RequestManager = Glide.with(this),
    @DrawableRes placeholder: Int
) {
    requestManager.asBitmap()
        .load(url)
        .placeholder(placeholder)
        .error(placeholder)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.load(
    url: String,
    requestManager: RequestManager = Glide.with(this),
    @DrawableRes placeHolderRes: Int,
) {
    requestManager
        .load(url)
        .placeholder(placeHolderRes)
        .error(placeHolderRes)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .thumbnail(0.1f)
        .into(this)
}
