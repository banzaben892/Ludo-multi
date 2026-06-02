package com.ludomasterpro

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(
    private val context: Context
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable
    ) {

        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))

            val crashText = """
                ===== CRASH =====
                Thread: ${thread.name}
                
                ${sw}
            """.trimIndent()

            Log.e("CRASH_HANDLER", crashText)

            val file = File(
                context.filesDir,
                "crash_log.txt"
            )

            file.appendText(crashText + "\n\n")
        } catch (e: Exception) {
            Log.e("CRASH_HANDLER", "Failed to save crash", e)
        }

        defaultHandler?.uncaughtException(thread, throwable)
    }
}
