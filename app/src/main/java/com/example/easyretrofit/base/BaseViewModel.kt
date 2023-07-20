package com.example.easyretrofit.base

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyretrofit.api.ApiUtil
import com.example.easyretrofit.api.Result

/**
 * 提供自定义 Factory 和 viewModel 扩展，使得定义 ViewModel 时可以传参
 *
 * <code>
 * private val model by viewModel { XxxViewModel(1, 2, 3) }
 * </code>
 */
class ParamViewModelFactory<VM : ViewModel>(
    private val block: () -> VM,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = block() as T
}

inline fun <reified VM : ViewModel> AppCompatActivity.viewModel(
    noinline block: () -> VM,
): Lazy<VM> = viewModels { ParamViewModelFactory(block) }

inline fun <reified VM : ViewModel> Fragment.viewModel(
    noinline block: () -> VM,
): Lazy<VM> = viewModels { ParamViewModelFactory(block) }

open class BaseViewModel : ViewModel() {

    fun <T> handleApi(
        liveData: MutableLiveData<Result<T>>,
        block: suspend () -> BaseData<T>
    ) {
        ApiUtil.handleApi(liveData, viewModelScope, block)
    }

}