package com.app.development.winter.utility

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import com.app.development.winter.R
import com.app.development.winter.localcache.LocaleHelper
import org.joda.time.DateTime
import org.joda.time.Days
import java.sql.Time
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS


object DateTimeUtil {
    val EEEMMMDDYYYYHHMM =
        SimpleDateFormat("EEE, MMM dd, yyyy h:mm a", Locale(LocaleHelper.getLanguage().locale))
    val EEEDDMMYYYY = SimpleDateFormat("EEE dd MMM yyyy", Locale(LocaleHelper.getLanguage().locale))
    val EEEEDDMMMMYYYY =
        SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale(LocaleHelper.getLanguage().locale))
    val EEEDDMMHHMM =
        SimpleDateFormat("EEE, MMMM dd, h:mm a", Locale(LocaleHelper.getLanguage().locale))
    val DDMMMMYYYY = SimpleDateFormat("dd MMMM, yyyy", Locale(LocaleHelper.getLanguage().locale))
    val HHMMSS = SimpleDateFormat("HH:mm:ss", Locale(LocaleHelper.getLanguage().locale))
    val MMSS = SimpleDateFormat("mm:ss", Locale(LocaleHelper.getLanguage().locale))
    val HHMM = SimpleDateFormat("HH:mm", Locale(LocaleHelper.getLanguage().locale))
    val HHMMA = SimpleDateFormat("hh:mm a", Locale(LocaleHelper.getLanguage().locale))
    val HHMMSSAA = SimpleDateFormat("hh:mm:ss aa", Locale(LocaleHelper.getLanguage().locale))
    val HHMM00 = SimpleDateFormat("HH:mm:00", Locale(LocaleHelper.getLanguage().locale))
    val HMM = SimpleDateFormat("h:mm a", Locale(LocaleHelper.getLanguage().locale))
    val DDMMMYYYYHHMMAA =
        SimpleDateFormat("dd MMM yyyy h:mm aa", Locale(LocaleHelper.getLanguage().locale))
    val DDMMYYYYHHMMAA =
        SimpleDateFormat("dd-MM-yyyy h:mm a", Locale(LocaleHelper.getLanguage().locale))

    val YYYYMMDD = SimpleDateFormat("yyyy-MM-dd", Locale(LocaleHelper.getLanguage().locale))
    val DDMMYY = SimpleDateFormat("dd-MM-yyyy", Locale(LocaleHelper.getLanguage().locale))
    val DDMMYYYY = SimpleDateFormat("dd/MM/yyyy", Locale(LocaleHelper.getLanguage().locale))
    val DDMMMYYYY =
        SimpleDateFormat("dd MMM, yyyy, hh:mm a", Locale(LocaleHelper.getLanguage().locale))
    val DDMMYYYYEEEE =
        SimpleDateFormat("dd/MM/yyyy EEEE", Locale(LocaleHelper.getLanguage().locale))
    val UTC_FORMAT =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale(LocaleHelper.getLanguage().locale))
    val UTC_DATE_FORMAT =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss.SSS'Z'", Locale(LocaleHelper.getLanguage().locale))
    var DEVICE_TIME_FORMAT = "E MMM d HH:mm:ss Z yyyy"
    var UniversalDateTimeFormat = "yyyy-MM-dd'T'HH:mm:sss.SSS'Z'"//"yyyy-MM-dd`T`HH:mm:sss.SSS`Z`"
    var DDMMYYFORMAT = "dd-MM-yyyy"


    val MMMYYYY = SimpleDateFormat("MMM, yyyy", Locale(LocaleHelper.getLanguage().locale))
    val YYYYMM = SimpleDateFormat("yyyy-MM", Locale(LocaleHelper.getLanguage().locale))

    const val MAX = -1
    const val MIN = 1
    const val NORMAL = 0

    const val ONE_DAY: Long = 86400000
    val timeZone = Calendar.getInstance().timeZone

    fun currentTimeStamp() = Date().time

    fun currentDate(format: SimpleDateFormat): String {
        return format.format(Calendar.getInstance().time)
    }

    fun getCurrentMonth(): String {
        return MMMYYYY.format(Calendar.getInstance().time)
    }

    fun getNextMonth(month: String): String? {
        if (month.isEmpty()) return ""
        val date: Date = MMMYYYY.parse(month) as Date
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, +1)
        val preToPreMonthDate = cal.time
        return MMMYYYY.format(preToPreMonthDate)
    }

    fun getPreviousMonth(month: String): String? {
        if (month.isEmpty()) return ""
        val date: Date = MMMYYYY.parse(month) as Date
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, -1)
        val preToPreMonthDate = cal.time
        return MMMYYYY.format(preToPreMonthDate)
    }

    fun getMonth(month: String): Int {
        val date: Date = MMMYYYY.parse(month) as Date
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.MONTH) + 1
    }

    fun getYear(year: String): Int {
        val date: Date = MMMYYYY.parse(year) as Date
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.YEAR)
    }

    fun convertDateFormat(originalDate: String): String {
        val date: Date = MMMYYYY.parse(originalDate) as Date
        val formatedDate: String = YYYYMM.format(date)
        return formatedDate
    }

    fun convertDateFormat(
        originalDate: String, currentFormat: SimpleDateFormat, newFormat: SimpleDateFormat
    ): String {
        val date: Date = currentFormat.parse(originalDate) as Date
        val formatedDate: String = newFormat.format(date)
        return formatedDate
    }

    fun getCurrentMonthInt(): Int {
        val cal = Calendar.getInstance()
        val year = cal[Calendar.YEAR]
        val mouth = cal[Calendar.MONTH]
        val day = cal[Calendar.DATE]
        return (year - 1970) * 12 + mouth
    }

    fun isToday(date: Long): Boolean {
        return DateUtils.isToday(date)
    }

    fun isToday(date: String, sourceFormat: SimpleDateFormat = UTC_FORMAT): Boolean {
        return DateUtils.isToday(sourceFormat.parse(date)?.time ?: 0L)
    }

    fun getCalendar(time: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = Date(time)
        return calendar
    }

    fun getInSecond(date: Long): Long {
        return date / 1000
    }

    // This will convert HH:mm:ss to seconds. (Ex. 15:30:00)
    fun getSecondFromString(date: String): Long {
        var timeInSecond: Long? = null
        val hourToSecond = date.split(":")[0].toInt() * 60 * 60
        val minuteInSecond = (hourToSecond + (date.split(":")[1].toInt() * 60)).toLong()
        timeInSecond = (minuteInSecond + (date.split(":")[2].toInt()))
        return timeInSecond ?: 0L
    }

    fun getFormattedDateTime(value: Long?, simpleDateFormat: SimpleDateFormat): String {
        return simpleDateFormat.format(Date(value ?: 0))
    }

    fun getRelativeDateTime(
        context: Context,
        value: Long?,
        format: SimpleDateFormat = DDMMMMYYYY
    ): String {
        val startDate = DateTime(value)
        val today = DateTime()
        val days: Int = Days.daysBetween(
            today.withTimeAtStartOfDay(),
            startDate.withTimeAtStartOfDay()
        ).days

        val date = when (days) {
            -1 -> context.getString(R.string.yesterday)
            0 -> context.getString(R.string.today)
            1 -> context.getString(R.string.tomorrow)
            else -> return getFormattedDateTime(value, format)
        }
        return date //+ " " + getFormattedDateTime(value, HMM)
    }

    fun formatDate(fromFormat: String, toFormat: String?, strInputDate: String?): String {
        val inFormat = SimpleDateFormat(fromFormat, Locale(LocaleHelper.getLanguage().locale))
        val date: Date
        var strDate = ""
        try {
            date = inFormat.parse(strInputDate)
            val outFormat = SimpleDateFormat(toFormat, Locale(LocaleHelper.getLanguage().locale))
            strDate = outFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return strDate
    }

    fun getEndTime(start: String): String {
        val formatter =
            SimpleDateFormat("yyyy-MM-dd hh:mm", Locale(LocaleHelper.getLanguage().locale))
        val startDate = formatter.parse(start)
        val c = Calendar.getInstance()
        c.time = startDate
        c.set(Calendar.HOUR_OF_DAY, 1)
        val endDate = formatter.parse(
            formatDate(
                "E MMM d HH:mm:ss Z yyyy", "yyyy-MM-dd hh:mm", c.time.toString()
            )
        )
        return endDate.toString()
    }

    @Throws(ParseException::class)
    fun isTimeBetweenTwoTime(
        strStartTime: String,
        strEndTime: String,
        strEventTime: String,
    ): Boolean {
        val formatter = SimpleDateFormat("hh:mm", Locale(LocaleHelper.getLanguage().locale))

        val startDate = formatter.parse(strStartTime)

        val eventDate = formatter.parse(strEventTime)

        val startTime = Time(startDate.time)

        val eventTime = Time(eventDate.time)

        /*  val c = Calendar.getInstance()
          c.time = startTime
          c.set(Calendar.HOUR_OF_DAY, 1)

          val endDate = formatter.parse(formatDate(
              "E MMM d HH:mm:ss Z yyyy",
              "yyyy-MM-ddhh:mm",
              c.time.toString()))

          val endTime = Time(endDate.time)*/

        if (eventTime.before(startTime) && eventTime.after(startTime)) {
            Log.e("IS_IN_BETWEEN", "Yes time between")
            return true
        }
        return false
    }

    fun checktimings(startTime: String, endtime: String): Boolean {
        val pattern = "HH:mm"
        val sdf = SimpleDateFormat(pattern, Locale(LocaleHelper.getLanguage().locale))
        try {
            val startDate = sdf.parse(startTime)
            val middleDate = sdf.parse(endtime)
            if (startDate != null) {
                return startDate.before(middleDate)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    /*fun getRelativeDateTime(value: Long?, format: SimpleDateFormat = DDMMMMYYYY): String {
        val startDate = DateTime(value)
        val today = DateTime()
        val days: Int = Days.daysBetween(
            today.withTimeAtStartOfDay(),
            startDate.withTimeAtStartOfDay()
        ).days

        val date = when (days) {
            -1 -> "Yesterday"
            0 -> "Today"
            1 -> "Tomorrow"
            else -> return getFormattedDateTime(value, format)
        }
        return date //+ " " + getFormattedDateTime(value, HMM)
    }*/

    fun getDuration(timeInSecond: Long?): String {
        if (timeInSecond == null) return "0s"
        var different = timeInSecond * 1000

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24

        val elapsedDays: Long = different / daysInMilli
        different %= daysInMilli

        val elapsedHours: Long = different / hoursInMilli
        different %= hoursInMilli

        val elapsedMinutes: Long = different / minutesInMilli
        different %= minutesInMilli

        val elapsedSeconds: Long = different / secondsInMilli

        val stringBuilder = StringBuilder()
        if (elapsedDays > 0) stringBuilder.append("${elapsedDays}d ")
        if (elapsedHours > 0) stringBuilder.append("${elapsedHours}h ")
        if (elapsedMinutes > 0) stringBuilder.append("${elapsedMinutes}m ")
        if (elapsedSeconds > 0) stringBuilder.append("${elapsedSeconds}s")
        if (elapsedSeconds.toInt() == 0 && timeInSecond.toInt() == 0) stringBuilder.append("${elapsedSeconds}s")
        return stringBuilder.toString().trim()
    }


    fun String.toDate(format: SimpleDateFormat = HHMM): Date? {
        return format.parse(this)
    }

    fun Long.fromUTC(): Long {
        if (this == 0L) return 0
        return this + timeZone.getOffset(System.currentTimeMillis())
    }

    fun Long.toUTC(): Long {
        if (this == 0L) return 0
        return this - timeZone.getOffset(this)
    }

    fun getFormattedDate(time: Long): String {
        val smsTime = Calendar.getInstance()
        smsTime.timeInMillis = time
        val now = Calendar.getInstance()
        val timeFormatString = ", MMMM d"
        val dateTimeFormatString = "EEEE, MMMM d"
        val HOURS = (60 * 60 * 60).toLong()
        return if (now[Calendar.DATE] == smsTime[Calendar.DATE]) {
            "Today" + DateFormat.format(timeFormatString, smsTime)
        } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] == 1) {
            DateFormat.format(dateTimeFormatString, smsTime).toString()
        } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
            DateFormat.format(dateTimeFormatString, smsTime).toString()
        } else {
            DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString()
        }
    }

    fun clearTime(time: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.timeInMillis
    }

    fun changeFormat(
        mDate: String?,
        currentFormat: SimpleDateFormat,
        newFormat: SimpleDateFormat,
    ): String {
        if (mDate.isNullOrEmpty()) return ""
        try {
            val cDate = currentFormat.parse(mDate) ?: ""
            return newFormat.format(cDate)
        } catch (e: ParseException) {
            return ""
        }
    }

    fun formatTime(timeInMilli: Long): String {
        val timeInSeconds = kotlin.math.abs(timeInMilli / 1000)
        val hours = SECONDS.toHours(timeInSeconds)
        val minutes = SECONDS.toMinutes(timeInSeconds) - (SECONDS.toHours(timeInSeconds) * 60)
        val seconds = SECONDS.toSeconds(timeInSeconds) - (SECONDS.toMinutes(timeInSeconds) * 60)
        return if (hours > 0) String.format("%dh : %02dm : %02ds", hours, minutes, seconds)
        else String.format("%dm : %02ds", minutes, seconds)
    }

    fun formatNormalTime(timeInSecond: Long): String {
        val hours = SECONDS.toHours(timeInSecond)
        val minutes = SECONDS.toMinutes(timeInSecond) - (SECONDS.toHours(timeInSecond) * 60)
        val seconds = SECONDS.toSeconds(timeInSecond) - (SECONDS.toMinutes(timeInSecond) * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun getFormattedDateTimeFromUTCToLocal(
        mDate: String?,
        newPattern: SimpleDateFormat,
    ): String {
        if (mDate.isNullOrEmpty()) return ""
        try {
            UTC_DATE_FORMAT.timeZone = TimeZone.getTimeZone("UTC");
            val utcDate = UTC_DATE_FORMAT.parse(mDate) ?: return ""
            newPattern.timeZone = TimeZone.getDefault()
            return newPattern.format(utcDate)
        } catch (e: ParseException) {
            return ""
        }
    }

    fun getFormattedDateTimeFromLocalToUTC(
        mDate: String?,
        newPattern: SimpleDateFormat,
    ): String {
        if (mDate.isNullOrEmpty()) return ""
        try {
            UTC_FORMAT.timeZone = TimeZone.getDefault()
            val utcDate = UTC_FORMAT.parse(mDate) ?: return ""
            newPattern.timeZone = TimeZone.getTimeZone("UTC");
            return newPattern.format(utcDate)
        } catch (e: ParseException) {
            return ""
        }
    }

    fun getTimeAgo(dataDate: String, strFormat: String): String {
        var convTime: String? = null
        val suffix = "ago"
        try {
            val dateFormat = SimpleDateFormat(strFormat, Locale(LocaleHelper.getLanguage().locale))
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val pasTime: Date = dateFormat.parse(dataDate) as Date
            dateFormat.timeZone = TimeZone.getDefault()
            val nowTime = Date()
            val dateDiff: Long = nowTime.time - pasTime.time
            val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
            if (second < 60) {
                convTime = "$second seconds $suffix"
            } else if (minute < 60) {
                convTime = "$minute minutes $suffix"
            } else if (hour < 24) {
                convTime = "$hour hours $suffix"
            } else if (day >= 7) {
                convTime = if (day > 360) {
                    (day / 360).toString() + " years " + suffix
                } else if (day > 30) {
                    (day / 30).toString() + " months " + suffix
                } else {
                    (day / 7).toString() + " week " + suffix
                }
            } else if (day < 7) {
                convTime = "$day days $suffix"
            }
        } catch (e: ParseException) {
            e.message?.let { Log.e("ConvTimeE", it) }
        }
        return convTime.toString()
    }

    fun compareTwoDates(str1: String, str2: String, formatter: SimpleDateFormat): Boolean {
        val date1 = formatter.parse(str1)
        val date2 = formatter.parse(str2)
        if (date1 == null || date2 == null) return false
        if (date1 < date2) {
            return true
        }
        return false
    }

    fun getFormatChange(
        oldPattern: String?, newPattern: String?, mDate: String?
    ): String {
        val sdf = SimpleDateFormat(oldPattern)
        var `in`: Date? = null
        try {
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            `in` = sdf.parse(mDate)
        } catch (e: ParseException) {
            Log.e("Date parsing error:", e.toString())
        }
        val sdf1 = SimpleDateFormat(newPattern)
        sdf1.timeZone = TimeZone.getDefault()
        var newDate = sdf1.format(`in`)

        return newDate
    }

    fun parseTime(str: String?): Long {
        if (str.isNullOrEmpty()) return 0

        var h = 0
        var m = 0
        var s = 0
        val units = str.split(":")
        when (units.size) {
            2 -> {
                // mm:ss
                m = Integer.parseInt(units[0]);
                s = Integer.parseInt(units[1]);
            }

            3 -> {
                // hh:mm:ss
                h = Integer.parseInt(units[0]);
                m = Integer.parseInt(units[1]);
                s = Integer.parseInt(units[2]);
            }

            else -> {

            }
        }
        if (m < 0 || m > 60 || s < 0 || s > 60 || h < 0) return 0
        return (h * 3600 + m * 60 + s).toLong()
    }
}