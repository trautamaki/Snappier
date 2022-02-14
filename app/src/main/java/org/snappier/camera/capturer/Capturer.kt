package org.snappier.camera.capturer

import androidx.camera.core.UseCase
import androidx.core.content.ContextCompat
import org.snappier.camera.Configuration
import org.snappier.camera.MainActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

abstract class Capturer(protected val executor: Executor) {
    interface ICapturer {
        fun onStartTakingPicture()
        fun onPictureTaken()
        fun onVideoStart()
        fun onVideoStop()
        fun onVideoSaved()
        fun onError(msg: String)
    }

    protected var listener: ICapturer? = null

    protected open var fileFormat = ".jpg"

    fun setCapturerListener(listener: ICapturer) {
        this.listener = listener
    }

    protected fun createFileName(extension: String): String {
        return SimpleDateFormat(Configuration.FILE_NAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + extension
    }

    abstract fun getCapture(): UseCase
}