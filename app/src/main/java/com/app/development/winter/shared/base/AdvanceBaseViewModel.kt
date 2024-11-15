package com.app.development.winter.shared.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class AdvanceBaseViewModel<INTENT : ViewIntent, STATE : ViewState>(defaultValue: STATE) :
    ViewModel() {

    var showProgress = MutableLiveData<Boolean?>()

    private var _customLoadingMsg = MutableLiveData<String?>()

    var toast = MutableLiveData<String?>()

    val customMessage: String? get() = _customLoadingMsg.value

    lateinit var lifecycleOwner: LifecycleOwner

    open fun init() {}

    val _state = StateReducerFlow(
        initialState = defaultValue, reduceState = ::reduceState, scope = viewModelScope
    )

    abstract fun reduceState(
        currentState: STATE,
        event: INTENT,
    ): STATE

    fun toast(msg: String) {
        toast.postValue(msg)
    }

    fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { block() }
    }

    fun showProgress() {
        _customLoadingMsg.postValue(null)
        showProgress.postValue(true)
    }

    fun showProgress(msg: String) {
        _customLoadingMsg.postValue(msg)
        showProgress.postValue(true)
    }

    fun hideProgress() {
        _customLoadingMsg.postValue(null)
        showProgress.postValue(false)
    }


    var apiProgress: MutableLiveData<Boolean> = MutableLiveData()
    var apiError: MutableLiveData<String?> = MutableLiveData()
    var apiSuccess: MutableLiveData<String?> = MutableLiveData()

    var jobs = HashMap<String, Job>()

    fun onError(message: String?) {
        apiError.postValue(message)
    }

    fun onResponse(message: String?) {
        apiSuccess.postValue(message)
    }

    fun addNewJob(name: String, job: Job) {
        this.jobs[name] = job
    }

    override fun onCleared() {
        super.onCleared()
        jobs.forEach { it.value.cancel() }
    }


    enum class LoadingType {
        SESSION_END, USER_STATE_DATA, GET_NOTIFICATION_STATE, START_NEW_SESSION, GET_ONGOING_SESSION, SYNC_FOCUS_TIME, GET_CYCLE_AD_REWARD, EMPTY_SATE, ERROR, GET_LEADERBOARD, GET_LEADERBOARD_TOP_3
    }

    enum class LoadingState {
        PROCESSING, COMPLETED, ERROR, RELOAD,
    }
}