package com.example.easyretrofit

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

open class BaseActivity : AppCompatActivity() {
    protected fun <T> registerAction(liveData: LiveData<Result<T>>, block: (T) -> Unit) {
        liveData.observe(this) {
            LogUtils.d(it)
            CommonUtil.handleResult(it, block)
        }
    }
}

class MainActivity : BaseActivity() {
    init {
        API.init()
    }

    private val viewModel: MyViewModel by viewModels()
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

class MyViewModel : ViewModel() {
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

        CommonUtil.handleApi(_testLiveData, viewModelScope) {
            API.mApiService.getList()
        }
    }
}

object API {
    lateinit var mApiService: ApiService

    fun init() {
        val client = OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            // .callTimeout(3, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool())
            .addInterceptor(MockInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://www.baidu.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mApiService = retrofit.create(ApiService::class.java)
    }
}

interface ApiService {
    @POST("/list")
    suspend fun getList(): BaseData<List<Int>>
}

object CommonUtil {
    // 在 ViewModel 中调用
    fun <T> handleApi(
        liveData: MutableLiveData<Result<T>>,
        viewModelScope: CoroutineScope,
        block: suspend () -> BaseData<T>
    ) {
        liveData.postValue(Result.Loading)
        viewModelScope.launch {
            try {
                // TODO show block().message if block().code!=200
                liveData.postValue(Result.Success(block().data!!))
            } catch (e: Exception) {
                liveData.postValue(Result.Error(e))
                ToastUtils.showShort("请求失败，请稍后重试")
            }
        }
    }

    // 在 Activity 中调用
    fun <T> handleResult(result: Result<T>, block: (T) -> Unit) {
        if (result is Result.Success) {
            block(result.result)
        }
    }
}

data class BaseData<T>(val code: Int, val message: String, val data: T)

sealed class Result<out R> {
    object Loading : Result<Nothing>()
    class Success<out T>(val result: T) : Result<T>()
    class Error(val exception: Exception) : Result<Nothing>()
}

class MockInterceptor : Interceptor {
    companion object {
        private const val DATA_JSON = """
                {
                    "code": 200,
                    "data": [
                        0,
                        1
                    ],
                    "message": "success"
                }
            """
        private const val NULL_JSON = """
                {
                    "code": 200,
                    "data": null,
                    "message": "success"
                }
            """
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        when (chain.request().url().pathSegments().last()) {
            "list" -> {
                Thread.sleep(1500L)

                val body = ResponseBody.create(MediaType.parse("application/json"), DATA_JSON)
                return Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .body(body)
                    .message("OK")
                    .build()
            }
            else -> {
                Thread.sleep(3000L)
                return chain.proceed(chain.request())
            }
        }
    }
}