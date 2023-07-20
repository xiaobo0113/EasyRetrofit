package com.example.easyretrofit.api

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ToastUtils
import com.example.easyretrofit.base.BaseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object ApiUtil {

    fun <T> handleApi(
        liveData: MutableLiveData<Result<T>>,
        viewModelScope: CoroutineScope,
        block: suspend () -> BaseData<T>
    ) {
        liveData.postValue(Result.Loading)
        viewModelScope.launch {
            try {
                // show message if code!=200
                val response = block()
                if (response.code != 200) {
                    liveData.postValue(Result.Error(Exception(response.message)))

                    // 非 200 状态码统一 toast
                    ToastUtils.showShort(response.message)
                } else {
                    // 1. 使用 data!! 来确保 data 不会为 null，
                    // 2. 这里依然会有潜在问题，当 data 不是基本数据类型时，其内部成员还是有可能为 null，
                    // 但是在 kotlin 定义类的时候可能是定义的非空类型，那么在 Observer#onChanged() 回调中
                    // 直接使用则会导致空指针异常。
                    // 3. 可以使用 moshi 这个 json 库来解决，参考 https://juejin.cn/post/6969841959082917901
                    // 4. Android开发中应用最广的 json 库当属 Gson，毫无疑问它是一个非常成熟的库，
                    // 但是迁移到 Kotlin 以后，gson就出现了两个问题，class 中字段默认值在某些情况下失效，
                    // 非空类型有可能被赋值为 null。实际上这两种情况都是同一个原因，在 gson issue #1550 中被提及。
                    liveData.postValue(Result.Success(response.data!!))
                }
            } catch (e: Exception) {
                liveData.postValue(Result.Error(e))

                // 异常统一 toast
                ToastUtils.showShort("请求失败，请稍后重试")
            }
        }
    }

}