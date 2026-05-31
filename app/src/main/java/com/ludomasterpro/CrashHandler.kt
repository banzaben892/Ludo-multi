import android.content.Context
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {

        // Convertir erreur en texte
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val error = sw.toString()

        // Logcat (visible dans GitHub Actions logs)
        Log.e("CRASH_HANDLER", error)

        // Sauvegarde dans fichier
        try {
            val file = context.openFileOutput("crash_log.txt", Context.MODE_PRIVATE)
            file.write(error.toByteArray())
            file.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Laisser Android fermer l'app
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
