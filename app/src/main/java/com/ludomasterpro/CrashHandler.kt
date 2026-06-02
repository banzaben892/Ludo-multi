package com.ludomasterpro

import android.content.Context
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {

        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val error = sw.toString()

        Log.e("CRASH_HANDLER", error)

        try {
            val file = context.openFileOutput("crash_log.txt", Context.MODE_PRIVATE)
            file.write(error.toByteArray())
            file.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        defaultHandler?.uncaughtException(thread, throwable)
    }
}
