package com.app.development.winter.utility

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.Html
import android.text.Spanned
import android.util.Log.e
import android.util.Patterns
import android.view.Window
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

/**
 * Created by
 */
class Util {

    companion object {

        fun isHuaweiDevice(): Boolean {
            val manufacturer = Build.MANUFACTURER
            val brand = Build.BRAND
            return manufacturer.lowercase(Locale.getDefault())
                .contains("huawei") || brand.lowercase(Locale.getDefault()).contains("huawei")
        }

        fun getCountryFromLocale(context: Context): String {

            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val countryCodeValue = tm.networkCountryIso
            var country = Locale("", countryCodeValue).getDisplayCountry(Locale.ENGLISH)
            if (country.isNullOrEmpty()) {
                val locale = Resources.getSystem().configuration.locales.get(0)
                country = locale.getDisplayCountry(Locale.ENGLISH)
            }
            return country
        }

        fun fromHTML(raw: String?): Spanned {
            var rawData = raw
            if (rawData == null) rawData = ""
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(rawData, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(rawData)
            }
        }

        fun setConfigChange(context: Context, langCode: String) {
            val locale = Locale(langCode)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }

        @SuppressLint("HardwareIds")
        private fun generateDeviceIdentifier(context: Context = com.app.development.winter.application.Controller.instance): String {
            val pseudoId = "35"
            +Build.BOARD.length % 10
            +Build.BRAND.length % 10
            +Build.SUPPORTED_ABIS[0].length % 10
            +Build.DEVICE.length % 10
            +Build.DISPLAY.length % 10
            +Build.HOST.length % 10
            +Build.ID.length % 10
            +Build.MANUFACTURER.length % 10
            +Build.MODEL.length % 10
            +Build.PRODUCT.length % 10
            +Build.TAGS.length % 10
            +Build.TYPE.length % 10
            +Build.USER.length % 10

            val androidId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val longId = pseudoId + androidId
            try {
                val messageDigest = MessageDigest.getInstance("MD5")
                messageDigest.update(longId.toByteArray(), 0, longId.length)
                val md5Bytes: ByteArray = messageDigest.digest()
                var identifier = ""
                for (md5Byte in md5Bytes) {
                    val b = 0xFF and md5Byte.toInt()
                    if (b <= 0xF) {
                        identifier += "0"
                    }
                    identifier += Integer.toHexString(b)
                }
                return identifier.uppercase(Locale.ENGLISH)
            } catch (e: java.lang.Exception) {
                e("TAG", e.toString())
            }
            return ""
        }

        fun getUniquePsuedoID(): String {
            val identifier = generateDeviceIdentifier()
            if (identifier.isNotEmpty()) return identifier

            val uniqueSerialCode = "35"
            +Build.BOARD.length % 10
            +Build.BRAND.length % 10
            +Build.SUPPORTED_ABIS[0].length % 10
            +Build.DEVICE.length % 10
            +Build.MANUFACTURER.length % 10
            +Build.MODEL.length % 10
            +Build.PRODUCT.length % 10

            var serial: String?
            try {
                serial = Build::class.java.getField("SERIAL")[null]!!.toString()
                return UUID(
                    uniqueSerialCode.hashCode().toLong(), serial.hashCode().toLong()
                ).toString()
            } catch (exception: Exception) {
                serial = "serial"
            }
            return UUID(uniqueSerialCode.hashCode().toLong(), serial.hashCode().toLong()).toString()
        }

        fun getIdThread(mContext: Context) {/* var adInfo: AdvertisingIdClient.Info? = null
             try {
                 adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext)
             } catch (exception: IOException) {
             } catch (exception: GooglePlayServicesAvailabilityException) {
             } catch (exception: GooglePlayServicesNotAvailableException) {
                 // Google Play services is not available entirely.
             }
             val id: String = adInfo?.id!!
             val isLAT: Boolean = adInfo.isLimitAdTrackingEnabled*/
        }

        fun encode(text: String): String {
            try {
                return URLEncoder.encode(text, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return text
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun daysOfWeekFromLocale(): Array<DayOfWeek> {
            val firstDayOfWeek = WeekFields.of(Locale.ENGLISH).firstDayOfWeek
            var daysOfWeek = DayOfWeek.values()
            // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
            // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
            if (firstDayOfWeek != DayOfWeek.MONDAY) {
                val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
                val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
                daysOfWeek = rhs + lhs
            }
            return daysOfWeek
        }


        fun clearAllPreviousActivities(intent: Intent) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        fun executeDelay(callback: () -> Unit) {
            Handler(Looper.getMainLooper()).postDelayed({
                callback.invoke()
            }, 300)
        }

        fun executeDelay(delay: Long, callback: () -> Unit) {
            Handler(Looper.getMainLooper()).postDelayed({
                callback.invoke()
            }, delay)
        }

        fun executeDelay(callback: () -> Unit, coroutineScope: CoroutineScope, delay: Long) {
            coroutineScope.launch {
                delay(delay)
                callback.invoke()
            }
        }

        fun executeJob(
            coroutineScope: CoroutineScope,
            coroutineContext: CoroutineContext,
            operation: () -> Unit,
            callback: (Job) -> Unit,
        ) {
            callback(coroutineScope.launch(coroutineContext) {
                operation.invoke()
            })
        }


        fun setNavigationBarLight(isLight: Boolean, window: Window?) {
            window?.let {
                if (isLight) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        it.insetsController?.setSystemBarsAppearance(
                            0, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                    } else {
                        ViewCompat.getWindowInsetsController(it.decorView)?.isAppearanceLightNavigationBars =
                            true
                    }

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        it.insetsController?.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                    } else {
                        ViewCompat.getWindowInsetsController(it.decorView)?.isAppearanceLightNavigationBars =
                            false
                    }
                }
            }
        }

        fun precacheImage(context: Context, strUrl: String) {
            Glide.with(context)
                .load(strUrl)
                .preload()
        }
    }


    fun formatUrl(url: String?, parameters: ArrayList<String?>): String {
        val sb = StringBuilder()
        sb.append(url)
        for (string in parameters) {
            sb.append("/")
            sb.append(string)
        }
        return sb.toString()
    }

    fun formatUrl(url: String?, vararg strings: String?): String {
        val sb = StringBuilder()
        sb.append(url)
        for (string in strings) {
            sb.append("/")
            sb.append(string)
        }
        return sb.toString()
    }

    fun isValidWeSite(url: String): Boolean {
        val p = Patterns.WEB_URL
        val m = p.matcher(url.lowercase())
        return m.matches()
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver, inImage, "Title", null
        )
        return Uri.parse(path)
    }


    fun encodeToNonLossyAscii(original: String): String {
        var originalData = original
        originalData = encode(originalData)
        val asciiCharset = Charset.forName("US-ASCII")
        if (asciiCharset.newEncoder().canEncode(originalData)) {
            return originalData
        }
        val stringBuilder = StringBuilder()
        for (element in originalData) {
            when {
                element.digitToInt() < 128 -> {
                    stringBuilder.append(element)
                }

                element.digitToInt() < 256 -> {
                    val octal = Integer.toOctalString(element.digitToInt())
                    stringBuilder.append("\\")
                    stringBuilder.append(octal)
                }

                else -> {
                    val hex = Integer.toHexString(element.digitToInt())
                    stringBuilder.append("\\u")
                    stringBuilder.append(hex)
                }
            }
        }
        return stringBuilder.toString()
    }

    private val unicodeHexPattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})")
    private val unicodeOctPattern = Pattern.compile("\\\\([0-7]{3})")

    fun decodeFromNonLossyAscii(original: String): String { //        Log.e("Raw", original);
        var originalData = original
        originalData = decode(originalData)
        var matcher = unicodeHexPattern.matcher(originalData)
        var charBuffer = StringBuffer(originalData.length)
        while (matcher.find()) {
            val match = matcher.group(1)
            val unicodeChar = match?.toInt(16)?.toChar()
            matcher.appendReplacement(charBuffer, (unicodeChar!!).toString())
        }
        matcher.appendTail(charBuffer)
        val parsedUnicode = charBuffer.toString()
        matcher = unicodeOctPattern.matcher(parsedUnicode)
        charBuffer = StringBuffer(parsedUnicode.length)
        while (matcher.find()) {
            val match = matcher.group(1)
            val unicodeChar = match?.toInt(8)?.toChar()
            matcher.appendReplacement(charBuffer, (unicodeChar!!).toString())
        }
        matcher.appendTail(charBuffer)
        return charBuffer.toString()
    }

    private fun decode(text: String): String {
        try {
            return URLDecoder.decode(text, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return text
    }


}