package com.app.development.winter.shared.extension

import android.animation.AnimatorInflater
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application.POWER_SERVICE
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.annotation.AnimatorRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.LayoutRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import com.app.development.winter.R
import com.app.development.winter.databinding.DialogGeneralBinding
import com.app.development.winter.shared.base.viewbase.AdvancedBaseActivity
import com.app.development.winter.shared.views.BlurView
import com.app.development.winter.utility.ViewUtil
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.RelativeCornerSize
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.util.Locale

fun Context.getDimen(@DimenRes dimenRes: Int) = resources.getDimension(dimenRes)
fun Context.getDimenPx(@DimenRes dimenRes: Int) = getDimen(dimenRes).toInt()
fun Context.getInteger(@IntegerRes res: Int) = resources.getInteger(res)
fun Context.getAnimator(@AnimatorRes res: Int) = AnimatorInflater.loadAnimator(this, res)!!

@ColorInt
fun Context.getColorCompat(@ColorRes res: Int) = ContextCompat.getColor(this, res)
fun Context.getColorListCompat(@ColorRes res: Int) = ContextCompat.getColorStateList(this, res)
fun Context.getDrawableCompat(@DrawableRes res: Int) = ContextCompat.getDrawable(this, res)!!

@ColorInt
fun Context.getThemeColor(@AttrRes res: Int) = MaterialColors.getColor(this, res, "")

fun Context.getQuantityString(@PluralsRes res: Int, quantity: Int = 0, vararg args: Any) =
    resources.getQuantityString(res, quantity, *args)

fun Context.getThemeAttrColorStateList(@AttrRes attr: Int, @StyleRes style: Int): ColorStateList? {
    val attrs = intArrayOf(attr)
    return obtainStyledAttributes(style, attrs).use { a ->
        a.getColorStateList(0)
    }
}

fun Context.dpToPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
    ).toInt()
}

fun Context.spToPx(sp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics
    ).toInt()
}

enum class DialogIconShape {
    Default, Rounded, Circle, ;
}

enum class DialogButtonStyle {
    Default, Green, ;
}

enum class DialogLayoutStyle {
    Vertical, Horizontal, ;
}

@Suppress("LongParameterList")
fun Context.createAlertDialog(
    @StringRes titleRes: Int = UNDEFINED_RES,
    @StringRes subtitleRes: Int = UNDEFINED_RES,
    @StringRes messageRes: Int = UNDEFINED_RES,
    title: String? = null,
    subtitle: String? = null,
    message: String? = null,
    @StringRes positiveRes: Int = R.string.ok,
    positiveClick: ((itf: DialogInterface) -> Unit)? = null,
    @StringRes negativeRes: Int = UNDEFINED_RES,
    negativeClick: ((itf: DialogInterface) -> Unit)? = null,
    cancelable: Boolean = true,
    showClose: Boolean = false,
    onCancel: (() -> Unit)? = null,
    @DrawableRes imageRes: Int = UNDEFINED_RES,
    @DrawableRes imageShadowRes: Int = UNDEFINED_RES,
    imageUrl: String? = null,
    customView: View? = null,
    iconShape: DialogIconShape = DialogIconShape.Default,
    buttonLayoutStyle: DialogLayoutStyle = DialogLayoutStyle.Horizontal,
    onDismiss: (() -> Unit)? = null,
    titleClick: ((String) -> Unit)? = null,
): AlertDialog {
    val activity = this as AdvancedBaseActivity<*, *, *, *>
    val binding = DialogGeneralBinding.inflate(LayoutInflater.from(this))
    val dialog = MaterialAlertDialogBuilder(this).setView(customView ?: binding.root)
        .setCancelable(cancelable).setOnCancelListener {
            it.dismiss()
            onCancel?.invoke()
        }.setOnDismissListener {
            onDismiss?.invoke()
        }.create()
//    binding.layoutContent.show()
    dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    activity.let { act ->
        if (ViewUtil.isLowMemory(act)) {
            dialog.window?.setDimAmount(0.75f)
        } else {
            val blurView = BlurView(activity)
            blurView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            binding.layoutContent.addView(blurView, 0)
            ViewUtil.setBlurView(act, blurView, 35)
            dialog.window?.setDimAmount(0.0f)
        }
    }

    if (customView == null) {
        with(binding) {
            when {
                imageUrl != null -> {
                    imageViewIcon.isVisible = imageUrl.isNotNullOrEmpty()
                    imageViewIcon.load(imageUrl, placeHolderRes = R.drawable.app_logo)
                }

                imageRes != UNDEFINED_RES -> {
                    imageViewIcon.isVisible = true
                    imageViewIcon.setImageResource(imageRes)
                }

                else -> imageViewIcon.isVisible = false
            }

            val shapeAppearanceModel = imageViewIcon.shapeAppearanceModel.toBuilder()
            when (iconShape) {
                DialogIconShape.Rounded -> {
                    shapeAppearanceModel.setAllCorners(
                        CornerFamily.ROUNDED, resources.getDimension(com.intuit.sdp.R.dimen._18sdp)
                    )
                    val params = imageViewIcon.layoutParams
                    params.height = dpToPx(141)
                    params.width = dpToPx(106)
                    imageViewIcon.layoutParams = params
                    imageViewIcon.scaleType = ImageView.ScaleType.CENTER_CROP
                }

                DialogIconShape.Circle -> {
                    shapeAppearanceModel.setAllCornerSizes(RelativeCornerSize(0.5f))
                }

                DialogIconShape.Default -> {
                    // ignore
                }
            }
            imageViewIcon.shapeAppearanceModel = shapeAppearanceModel.build()

            val titleResult = when {
                title != null -> title
                titleRes != UNDEFINED_RES -> getString(titleRes).fromHtml()
                else -> ""
            }
            tvTitle.setTextOrGone(titleResult)
            tvTitle.setSafeOnClickListener { titleClick?.invoke(titleResult.toString()) }

            val subtitleResult = when {
                subtitle != null -> subtitle
                subtitleRes != UNDEFINED_RES -> getString(subtitleRes).fromHtml()
                else -> null
            }
            tvSubTitle.setTextOrGone(subtitleResult)

            val messageResult = when {
                message != null -> message
                messageRes != UNDEFINED_RES -> getString(messageRes)
                else -> null
            }
            tvMessage.setTextOrGone(messageResult)

            buttonClose.isVisible = showClose
            buttonClose.setSafeOnClickListener {
                onCancel?.invoke()
                dialog.dismiss()
            }

            buttonPositive.setTextResOrGone(positiveRes)
            buttonPositive.setSafeOnClickListener {
                dialog.dismiss()
                positiveClick?.invoke(dialog)
            }

            buttonNegative.setTextResOrGone(negativeRes)
            buttonNegative.setSafeOnClickListener {
                dialog.dismiss()
                negativeClick?.invoke(dialog)
            }
        }
    }
    return dialog
}

fun Context.forceRestart() {
    val pm = packageManager
    val intent = pm.getLaunchIntentForPackage(packageName)
    val mainIntent = Intent.makeRestartActivityTask(intent?.component)
    startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}


@Suppress("LongParameterList")
fun Context.showAlertDialog(
    @StringRes titleRes: Int = UNDEFINED_RES,
    @StringRes subtitleRes: Int = UNDEFINED_RES,
    @StringRes messageRes: Int = UNDEFINED_RES,
    title: String? = null,
    subtitle: String? = null,
    message: String? = null,
    @StringRes positiveRes: Int = R.string.ok,
    positiveClick: ((itf: DialogInterface) -> Unit)? = null,
    @StringRes negativeRes: Int = UNDEFINED_RES,
    negativeClick: ((itf: DialogInterface) -> Unit)? = null,
    cancelable: Boolean = true,
    showClose: Boolean = false,
    onCancel: (() -> Unit)? = null,
    @DrawableRes imageRes: Int = R.drawable.app_logo,
    @DrawableRes imageShadowRes: Int = UNDEFINED_RES,
    imageUrl: String? = null,
    customView: View? = null,
    iconShape: DialogIconShape = DialogIconShape.Default,
    positiveButtonStyle: DialogButtonStyle = DialogButtonStyle.Default,
    buttonLayoutStyle: DialogLayoutStyle = DialogLayoutStyle.Horizontal,
    onDismiss: (() -> Unit)? = null,
    titleClick: ((String) -> Unit)? = null,
): AlertDialog {
    return createAlertDialog(
        titleRes = titleRes,
        subtitleRes = subtitleRes,
        messageRes = messageRes,
        title = title,
        subtitle = subtitle,
        message = message,
        positiveRes = positiveRes,
        positiveClick = positiveClick,
        negativeRes = negativeRes,
        negativeClick = negativeClick,
        cancelable = cancelable,
        showClose = showClose,
        onCancel = onCancel,
        imageRes = imageRes,
        imageShadowRes = imageShadowRes,
        imageUrl = imageUrl,
        customView = customView,
        iconShape = iconShape,
        buttonLayoutStyle = buttonLayoutStyle,
        onDismiss = onDismiss,
        titleClick = titleClick
    ).apply {
        MainScope().launch {
            show()
        }
    }
}

/**
 * System dialog for edge cases
 */
@Suppress("LongParameterList")
fun Context.showSystemAlertDialog(
    @StringRes titleRes: Int = -1,
    @StringRes messageRes: Int = -1,
    title: String? = null,
    message: String? = null,
    @StringRes positiveRes: Int = R.string.ok,
    positiveClick: ((itf: DialogInterface) -> Unit)? = null,
    @StringRes negativeRes: Int = -1,
    negativeClick: ((itf: DialogInterface) -> Unit)? = null,
    cancelable: Boolean = true,
    onCancel: (() -> Unit)? = null,
    customView: View? = null,
    @LayoutRes customViewRes: Int = -1,
): AlertDialog {
    val titleResult = if (titleRes != -1) getString(titleRes) else title
    val messageResult = if (messageRes != -1) getString(messageRes) else message
    val builder = MaterialAlertDialogBuilder(this).setTitle(titleResult).setMessage(messageResult)
        .setCancelable(cancelable)

    if (positiveRes != -1) {
        builder.setPositiveButton(positiveRes) { itf, _ -> positiveClick?.invoke(itf) }
    }
    if (negativeRes != -1) {
        builder.setNegativeButton(negativeRes) { itf, _ -> negativeClick?.invoke(itf) }
    }
    if (onCancel != null) {
        builder.setOnCancelListener { onCancel() }
    }
    if (customView != null) {
        builder.setView(customView)
    } else if (customViewRes != -1) {
        builder.setView(customViewRes)
    }
    Log.d(TAG, "createAlertDialog: title=[$titleResult] message=[$messageResult]")
    return builder.create()
}

fun Context.rotateImage(imageUri: Uri) {
    val bitmap = if (Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(
        this.contentResolver, imageUri
    )
    else ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, imageUri))
    val matrix = Matrix()
    matrix.postRotate(getImageRotation(contentResolver.openInputStream(imageUri) ?: return))
    val bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    val os = contentResolver.openOutputStream(imageUri)
    bmp.compress(Bitmap.CompressFormat.JPEG, 50, os ?: return)
}

fun getImageRotation(iss: InputStream): Float {
    fun exifToDegrees(rotation: Int): Float {
        return when (rotation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }

    var exif: ExifInterface? = null
    var exifRotation = 0
    try {
        exif = ExifInterface(iss)
        exifRotation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return if (exif == null) 0f else exifToDegrees(exifRotation)
}

@Deprecated("Wrong value cause of manual locale management", ReplaceWith("Account::countryCode"))
fun Context.getLocalUserCountryCode(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0].country
    } else {
        resources.configuration.locale.country
    }
}

fun Context.getFileName(uri: Uri): String {
    val returnCursor = contentResolver.query(uri, null, null, null, null) ?: return ""
    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val name = returnCursor.getString(nameIndex)
    returnCursor.close()
    return name
}

val deviceName: String
    get() {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.capitalize()
        } else "${manufacturer.capitalize()} $model"
    }

fun Context.loadJSONFromAsset(name: String): String {
    return try {
        val inputStream: InputStream = assets.open(name)
        inputStream.bufferedReader().use { it.readText() }
    } catch (ex: IOException) {
        ex.printStackTrace()
        return ""
    }
}

val Context.isAppInForeground: Boolean
    get() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val services = activityManager?.runningAppProcesses ?: return false
        val processInfo = services[0]
        return processInfo.processName.equals(
            packageName, ignoreCase = true
        ) && processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

fun Context.openSystemNotificationsSettings() {
    val intent = Intent().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:$packageName")
        }
    }
    startActivity(intent)
}

fun Context.openDeeplinkFor(url: String) {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW, Uri.parse(url)
            )
        )
    } catch (e: ActivityNotFoundException) {
        ViewUtil.printLog(TAG, "There are no activity for deeplink url: $url")
    }
}

fun Context.openIntent(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "There are no activity for intent: $intent")
    }
}

@Suppress("DEPRECATION")
fun Context.changeLanguage(language: String): Context {
    Log.d(TAG, "changeLanguage: $language")
    var context = this
    val locale = Locale(language)
    Locale.setDefault(locale)
    val configuration = Configuration(resources.configuration).apply {
        setLocale(locale)
    }
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        context = createConfigurationContext(configuration)
    }
    resources.updateConfiguration(configuration, resources.displayMetrics)
    return context
}

fun Context.hasNotificationReadAccess(): Boolean {
    return Settings.Secure.getString(
        applicationContext.contentResolver, "enabled_notification_listeners"
    ).contains(applicationContext.packageName)
}

fun Context.openPermissions(
    activityLauncher: com.app.development.winter.shared.base.BetterActivityResult<Intent, ActivityResult>?,
    onPermissionGranted: (Boolean) -> Unit
) {
    try {
        val settingsIntent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        activityLauncher?.launch(settingsIntent) {
            if (hasNotificationReadAccess()) {
                onPermissionGranted.invoke(true)
            } else {
                onPermissionGranted.invoke(false)
            }
        }
    } catch (e: ActivityNotFoundException) {
        onPermissionGranted.invoke(false)
        e.printStackTrace()
    }
}

fun Context.checkAndEnableNotificationReadSetting(
    activityLauncher: com.app.development.winter.shared.base.BetterActivityResult<Intent, ActivityResult>?,
    onPermissionGranted: (Boolean) -> Unit
) {
    if (hasNotificationReadAccess().not()) {
        openPermissions(activityLauncher) {
            onPermissionGranted.invoke(it)
        }
    } else {
        onPermissionGranted.invoke(true)
    }/* openPermissions(activityLauncher) {
         onPermissionGranted.invoke(it)
     }*/
}


fun AppCompatActivity.setWindowBackground(background: Int) {
    window?.setBackgroundDrawableResource(background)
}

fun Service.isVoIpCallActive(): Boolean {
    val am = this.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
    val mode = am.mode
    return AudioManager.MODE_IN_COMMUNICATION == mode
}

fun Context.hasOverLayPermissionGranted(): Boolean {
    return Settings.canDrawOverlays(this)
}

fun Context.hasPostNotificationPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        return true
    }
}

fun Context.lifecycleOwner(): LifecycleOwner? {
    var curContext = this
    var maxDepth = 20
    while (maxDepth-- > 0 && curContext !is LifecycleOwner) {
        curContext = (curContext as ContextWrapper).baseContext
    }
    return if (curContext is LifecycleOwner) {
        curContext as LifecycleOwner
    } else {
        null
    }
}

fun Context.isScreenOffWithLockButton(): Boolean {
    val powerManager = getSystemService(POWER_SERVICE) as PowerManager
    return !powerManager.isInteractive
}

const val TAG = "ContextExt"
