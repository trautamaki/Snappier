package org.snappier.camera.capturer

import android.annotation.SuppressLint
import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.core.VideoCapture
import org.snappier.camera.Configuration
import org.snappier.camera.MainActivity
import kotlinx.android.synthetic.main.activity_main.*

class VideoCapturer(private val activity: MainActivity, private val lensFacing: Int) :
        Capturer(activity),
        VideoCapture.OnVideoSavedCallback {

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
                .setTargetRotation(activity.preview_view.display.rotation)
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
                activity.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build()

        // Set up video capture listener
        videoCapture.startRecording(
                outputFileOptions, executor, this
        )

        recording = true
        Log.d("Snappier", "Start video record")
    }

    @SuppressLint("RestrictedApi")
    fun stopVideo() {
        videoCapture.stopRecording()
        recording = false
        Log.d("Snappier", "Stop video record")
    }

    @SuppressLint("RestrictedApi")
    override fun onVideoSaved(output: VideoCapture.OutputFileResults) {
        Log.d(MainActivity.TAG, "Video capture succeeded: ${output.savedUri}")
        listener?.onFileSaved(output.savedUri)
    }

    @SuppressLint("RestrictedApi")
    override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
        Log.e("Snappier", "Video error: $message")
    }

    override fun getCapture(): UseCase {
        return videoCapture
    }
}