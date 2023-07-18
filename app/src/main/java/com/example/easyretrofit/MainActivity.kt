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
    protected fun <T> registerAction(
        liveData: LiveData<Result<T>>,
        loadingBlock: () -> Unit = {},
        errorBlock: (Result.Error) -> Unit = {},
        successBlock: (T) -> Unit
    ) {
        liveData.observe(this) {
            LogUtils.d(it)

            when (it) {
                is Result.Loading -> loadingBlock()
                is Result.Error -> errorBlock(it)
                is Result.Success -> {
                    // 通常 DataBean 中的成员都声明为非空，这里如果出现了 NPE 那一定是服务端返回的 null
                    try {
                        successBlock(it.result)
                    } catch (e: NullPointerException) {
                        LogUtils.d(e)
                        ToastUtils.showShort("请求失败，请稍后重试")
                    }
                }
            }
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

        registerAction(viewModel.testLiveData, {
            // show loading
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