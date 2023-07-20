package com.example.easyretrofit.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyretrofit.api.ApiUtil
import com.example.easyretrofit.api.Result

open class BaseViewModel : ViewModel() {

    fun <T> handleApi(
        liveData: MutableLiveData<Result<T>>,
        block: suspend () -> BaseData<T>
    ) {
        ApiUtil.handleApi(liveData, viewModelScope, block)
    }

}