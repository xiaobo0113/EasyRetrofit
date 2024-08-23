package com.example.easyretrofit

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import com.blankj.utilcode.util.LogUtils
import com.example.easyretrofit.base.BaseActivity

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initApi()
        initView()
    }

    private fun initApi() {
        // 方式 1
        registerAction(viewModel.testLiveData) {
            // update ui
            LogUtils.d(it)
        }

        // 方式 2，为了展示两种使用方式，特意写了两种调用方式，
        // 实际使用时不要对同一个 LiveData 对象使用两次，否则会同时收到回调
        registerAction(viewModel.testLiveData, {
            // return true means: handle loading myself
            // handleLoading(true)
            true
        }, {
            // stop loading + show error
            // handleLoading(false)
        }) {
            // stop loading + update ui
            // handleLoading(false)
            LogUtils.d(it)
        }
    }

    private fun initView() {
        Button(this).apply {
            text = "click me"
            setOnClickListener {
                viewModel.getList()
            }
            setContentView(this)
        }
    }

}
