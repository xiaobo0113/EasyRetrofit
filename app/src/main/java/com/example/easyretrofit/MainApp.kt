package com.example.easyretrofit

import android.app.Application
import com.example.easyretrofit.api.API

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        API.init()
    }

}