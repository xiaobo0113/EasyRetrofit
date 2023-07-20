package com.example.easyretrofit.api

import com.example.easyretrofit.mock.MockInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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