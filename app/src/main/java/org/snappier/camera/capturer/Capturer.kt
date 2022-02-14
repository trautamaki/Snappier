package org.snappier.camera.capturer

import androidx.camera.core.UseCase
import androidx.core.content.ContextCompat
import org.snappier.camera.Configuration
import org.snappier.camera.MainActivity
import java.text.SimpleDateFormat
import java.util.*

abstract class Capturer(activity: MainActivity) {
    protected val executor = ContextCompat.getMainExecutor(activity)
    protected open var fileFormat = ".jpg"

    protected fun createFileName(extension: String): String {
        return SimpleDateFormat(Configuration.FILE_NAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + extension
    }

    abstract fun getCapture(): UseCase
}