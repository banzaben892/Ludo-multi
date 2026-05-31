package com.ludomasterpro

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(
            CrashHandler(this)
        )
    }
}
