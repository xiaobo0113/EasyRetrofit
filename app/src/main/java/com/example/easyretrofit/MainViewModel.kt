package com.example.easyretrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.easyretrofit.api.API
import com.example.easyretrofit.api.Result
import com.example.easyretrofit.base.BaseViewModel

class MainViewModel : BaseViewModel() {

    private val _testLiveData: MutableLiveData<Result<List<Int>>> =
        MutableLiveData<Result<List<Int>>>()
    val testLiveData: LiveData<Result<List<Int>>> = _testLiveData

    fun getList() {
        /*viewModelScope.launch {
            try {
                val data = API.mApiService.getList()
                // 注意：
                // 1. 这里必须要用 data.data!!，因为 Gson 转换为 BaseData 实体的时候，
                // data 成员可能为 null，导致在 handleResult() 中取 data 成员时取到的为 null，
                // 导致空指针异常。
                // 2. 这里生成 Result.Success 的实例时，虽然传了 null 但是也没报错，
                // 因为参数的泛型 T，所以允许传 null。
                // 3. 使用了 data.data!! 时，如果 data.data 为 null，这里就会报错了，会被 catch
                _testLiveData.postValue(Result.Success(data.data!!))
                if (data.code != 200) {
                    ToastUtils.showShort(data.message)
                }
            } catch (e: Exception) {
                LogUtils.d(e)
                ToastUtils.showShort("请求失败，请稍后重试")
            }
        }*/

        handleApi(_testLiveData) {
            API.mApiService.getList()
        }
    }

}