package app.newsnap.capturer

import androidx.core.content.ContextCompat
import app.newsnap.MainActivity
import app.newsnap.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class Capturer(private val activity: MainActivity) {
    protected var lastFile: File? = null
    protected val executor = ContextCompat.getMainExecutor(activity)
    protected val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
        File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    protected var outputDirectory: File = if (mediaDir != null && mediaDir.exists())
        mediaDir else activity.filesDir

    protected val fileNameFormat = "yyyy-MM-dd-HH-mm-ss-SSS"

    protected fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension)

}