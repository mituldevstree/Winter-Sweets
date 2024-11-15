package com.app.development.winter.utility

import android.os.Handler
import android.os.Looper
import java.util.concurrent.TimeUnit

class TimerCounterUtil(
    private val countUpInterval: Long,
    private var mListeners: TimerUpdateListeners?
) {
    private var millisUntilFinished: Long = 0
    private var mHandler: Handler? = null
    private var mRunnable: Runnable? = null
    private var hasTimerStarted: Boolean = false

    init {
        initTimer()
    }

    private fun initTimer() {
        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable {
            val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
            val minutes =
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                )
            val seconds =
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                )
            millisUntilFinished += countUpInterval
            mListeners?.onTimerUpdate(hours, minutes, seconds)
            if (hasTimerStarted) startCallBack()
        }
    }

    interface TimerUpdateListeners {
        fun onTimerUpdate(hour: Long, minutes: Long, seconds: Long)
        fun onTimerFinish(millisSecond: Long)
    }

    private fun removeCallBack() {
        mRunnable?.let { mHandler?.removeCallbacks(it) }
    }

    private fun startCallBack() {
        mRunnable?.let { mHandler?.postDelayed(it, countUpInterval) }
    }


    fun setTimerUpdateListener(mListeners: TimerUpdateListeners) {
        this.mListeners = mListeners
    }

    fun setStartTimer(millisUntilFinished: Long) {
        this.millisUntilFinished = millisUntilFinished
    }

    fun finishTimer() {
        hasTimerStarted = false
        mListeners?.onTimerFinish(millisUntilFinished)
        removeCallBack()
    }

    fun destroyTimer() {
        hasTimerStarted = false
        removeCallBack()
    }

    fun pauseTimer() {
        hasTimerStarted = false
        removeCallBack()
    }

    fun resumeTimer() {
        hasTimerStarted = true
        removeCallBack()
        startCallBack()
    }

    fun startTimer() {
        hasTimerStarted = true
        removeCallBack()
        startCallBack()
    }

    fun getTimeInMilliseconds(): Long {
        return millisUntilFinished
    }

    fun isTimerRunning(): Boolean {
        return hasTimerStarted
    }
}