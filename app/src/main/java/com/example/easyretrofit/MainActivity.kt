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
        registerAction(viewModel.testLiveData) {
            // update ui
            LogUtils.d(it)
        }

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
