package com.example.easyretrofit.base

data class BaseData<T>(val code: Int, val message: String, val data: T)