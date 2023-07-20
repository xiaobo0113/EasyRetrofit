package com.example.easyretrofit.api

import com.example.easyretrofit.base.BaseData
import retrofit2.http.POST

interface ApiService {

    @POST("/list")
    suspend fun getList(): BaseData<List<Int>>

}