package org.snappier.camera.capturer

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.core.VideoCapture
import androidx.camera.core.impl.ImageOutputConfig
import kotlinx.android.synthetic.main.activity_main.*
import org.snappier.camera.Configuration
import java.util.concurrent.Executor

class VideoCapturer(
    private val contentResolver: ContentResolver, private val lensFacing: Int,
    private val rotation: Int, executor: Executor
) :
    Capturer(executor), VideoCapture.OnVideoSavedCallback {

    lateinit var videoCapture: VideoCapture

    var recording: Boolean = false

    override var fileFormat = ".mp4"

    init {
        build()
    }

    @SuppressLint("RestrictedApi")
    private fun build() {
        val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing).build()

        videoCapture = VideoCapture.Builder()
                .setTargetRotation(rotation)
                .setCameraSelector(cameraSelector)
                .build()
    }

    @SuppressLint("RestrictedApi", "MissingPermission")
    fun startVideo() {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileFormat)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, createFileName(fileFormat))
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Configuration.IMAGE_RELATIVE_PATH)
        }

        val outputFileOptions = VideoCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build()

        // Set up video capture listener
        videoCapture.startRecording(outputFileOptions, executor, this)

        recording = true
        listener?.onVideoStart()
    }

    @SuppressLint("RestrictedApi")
    fun stopVideo() {
        videoCapture.stopRecording()
        listener?.onVideoStop()
    }

    @SuppressLint("RestrictedApi")
    override fun onVideoSaved(output: VideoCapture.OutputFileResults) {
        listener?.onVideoSaved()
    }

    @SuppressLint("RestrictedApi")
    override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
        listener?.onError(message)
    }

    override fun getCapture(): UseCase {
        return videoCapture
    }
}