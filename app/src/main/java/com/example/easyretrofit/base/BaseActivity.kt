package com.example.easyretrofit.base

import androidx.lifecycle.LiveData
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.easyretrofit.api.Result
import top.gangshanghua.lib_loading.LoadingActivity

open class BaseActivity : LoadingActivity() {

    protected fun <T> registerAction(
        liveData: LiveData<Result<T>>,
        // if false, show loading automatically
        loadingBlock: () -> Boolean = { false },
        errorBlock: (Result.Error) -> Unit = {},
        successBlock: (T) -> Unit
    ) {
        // if not handled by user, show loading automatically
        var loadingHandled = false
        liveData.observe(this) {
            LogUtils.d(it)

            when (it) {
                is Result.Loading -> {
                    loadingHandled = loadingBlock()
                    if (!loadingHandled) {
                        handleLoading(true)
                    }
                }

                is Result.Error -> {
                    errorBlock(it)
                    if (!loadingHandled) {
                        handleLoading(false)
                    }
                }

                is Result.Success -> {
                    // 通常 DataBean 中的成员都声明为非空，这里如果出现了 NPE 那一定是服务端返回的 null
                    try {
                        successBlock(it.result)
                    } catch (e: NullPointerException) {
                        LogUtils.d(e)
                        ToastUtils.showShort("请求失败，请稍后重试")
                    }

                    if (!loadingHandled) {
                        handleLoading(false)
                    }
                }
            }
        }
    }

}