package org.snappier.camera.capturer

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.snappier.camera.Configuration
import org.snappier.camera.MainActivity
import org.snappier.camera.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageCapturer(private val activity: MainActivity, private val captureMode: Int) :
    ImageCapture.OnImageSavedCallback {
    lateinit var imageCapture: ImageCapture
        private set
    private var lastPhotoFile: File? = null
    private var outputDirectory: File

    init {
        build()

        // Set output directory
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
            File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        outputDirectory = if (mediaDir != null && mediaDir.exists())
            mediaDir else activity.filesDir
    }

    private fun build() {
        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(Configuration.ASPECT_RATIO)
            .setCaptureMode(captureMode)
            .build()
    }

    fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        lastPhotoFile = File(
            outputDirectory,
            SimpleDateFormat(
                MainActivity.FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(lastPhotoFile!!).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(activity), this
        )
        activity.camera_capture_button.isEnabled = false
    }

    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
        val savedUri = Uri.fromFile(lastPhotoFile)
        val msg = "Photo capture succeeded: $savedUri"
        Toast.makeText(activity.baseContext, msg, Toast.LENGTH_SHORT).show()
        Log.d(MainActivity.TAG, msg)

        activity.camera_capture_button.isEnabled = true
    }

    override fun onError(exc: ImageCaptureException) {
        Log.e(MainActivity.TAG, "Photo capture failed: ${exc.message}", exc)
        activity.camera_capture_button.isEnabled = true
    }
}