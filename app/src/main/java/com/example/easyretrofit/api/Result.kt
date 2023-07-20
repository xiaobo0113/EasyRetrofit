package com.example.easyretrofit.api

sealed class Result<out R> {

    object Loading : Result<Nothing>()
    class Success<out T>(val result: T) : Result<T>()
    class Error(val exception: Exception) : Result<Nothing>()

}