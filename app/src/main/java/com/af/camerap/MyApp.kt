package com.af.camerap

import android.app.Application

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/04/29 - 16:40
 * Author     Payne.
 * About      类描述：
 */
class MyApp : Application() {
    companion object {
        lateinit var instance: MyApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}