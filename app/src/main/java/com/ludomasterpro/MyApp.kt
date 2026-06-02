package com.ludomasterpro

import android.app.Application
import android.util.Log

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("MyApp", "CrashHandler installé")

        Thread.setDefaultUncaughtExceptionHandler(
            CrashHandler(this)
        )
    }
}
