package org.snappier.camera.capturer

import android.net.Uri
import androidx.camera.core.UseCase
import org.snappier.camera.Configuration
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

abstract class Capturer(protected val executor: Executor) {
    var listener: ICapturerCallback? = null

    protected open var fileFormat = ".jpg"

    interface ICapturerCallback {
        fun onFileSaved(fileUri: Uri?)
    }

    protected fun createFileName(extension: String): String {
        return SimpleDateFormat(Configuration.FILE_NAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + extension
    }

    abstract fun getCapture(): UseCase
}