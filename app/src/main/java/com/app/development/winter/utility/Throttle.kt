package com.app.development.winter.utility

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object Throttle {
    fun <T> throttleLatest(
        intervalMs: Long = 300L,
        coroutineScope: CoroutineScope,
        destinationFunction: (T) -> Unit,
    ): (T) -> Unit {
        var throttleJob: Job? = null
        var latestParam: T
        return { param: T ->
            latestParam = param
            if (throttleJob?.isCompleted != false) {
                throttleJob = coroutineScope.launch {
                    delay(intervalMs)
                    latestParam.let(destinationFunction)
                }
            }
        }
    }

    fun <T> throttleFirst(
        skipMs: Long = 300L,
        coroutineScope: CoroutineScope,
        destinationFunction: (T) -> Unit,
    ): (T) -> Unit {
        var throttleJob: Job? = null
        return { param: T ->
            if (throttleJob?.isCompleted != false) {
                throttleJob = coroutineScope.launch {
                    destinationFunction(param)
                    delay(skipMs)
                }
            }
        }
    }

    fun debounce(
        delayMs: Long = 500L,
        coroutineContext: CoroutineContext,
        action: () -> Unit,
    ): Job {
        val debounceJob: Job?
        debounceJob = CoroutineScope(coroutineContext).launch {
            action()
            delay(delayMs)
        }
        return debounceJob
    }
}