package com.app.development.winter.shared.base.viewbase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.development.winter.shared.network.data.BaseModel
import com.app.development.winter.shared.network.data.ParserHelper
import retrofit2.Response

open class BaseViewModel : ViewModel() {

    private var apiError: MutableLiveData<String> = MutableLiveData()
    private var baseApiResponse: MutableLiveData<BaseModel> = MutableLiveData()


    fun onError(message: String) {
        apiError.postValue(message)
    }

    fun <T> onError(response: Response<T>) {
        onError(ParserHelper.baseError(response).message)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun getApiError(): LiveData<String> = apiError
    fun getBaseApiResponse(): MutableLiveData<BaseModel> = baseApiResponse


}

