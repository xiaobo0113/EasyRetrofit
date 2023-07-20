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
            // show loading
            true
        }, {
            // stop loading + show error
        }) {
            // stop loading + update ui
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
