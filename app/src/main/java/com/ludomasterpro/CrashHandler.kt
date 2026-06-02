package com.ludomasterpro

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(
    private val context: Context
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {

        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))

        val crashText = """
            ===== CRASH REPORT =====
            Thread: ${thread.name}
            Time: ${System.currentTimeMillis()}

            ${sw}
        """.trimIndent()

        // 🔴 LOGCAT (visible dans GitHub Actions logs)
        Log.e("CRASH_HANDLER", crashText)

        try {
            // 🟢 Fichier accessible (GitHub + debug)
            val file = File(
                context.getExternalFilesDir(null),
                "crash_log.txt"
            )

            file.parentFile?.mkdirs()
            file.appendText(crashText + "\n\n")

            Log.e("CRASH_HANDLER", "Crash saved at: ${file.absolutePath}")

        } catch (e: Exception) {
            Log.e("CRASH_HANDLER", "Failed to save crash", e)
        }

        // 🔵 laisser Android gérer le crash final
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
