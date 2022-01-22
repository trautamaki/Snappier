package app.newsnap.capturer

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import app.newsnap.MainActivity

class ImageCapturer(private val activity: MainActivity, captureMode: Int) : Capturer(activity),
        ImageCapture.OnImageSavedCallback {
    lateinit var imageCapture: ImageCapture

    init {
        build(captureMode)
    }

    private fun build(captureMode: Int) {
        imageCapture = ImageCapture.Builder()
                .setCaptureMode(captureMode)
                .build()
    }

    fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        lastFile = createFile(outputDirectory, fileNameFormat, ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(lastFile!!).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions, executor, this
        )
    }

    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
        val savedUri = Uri.fromFile(lastFile)
        val msg = "Photo capture succeeded: $savedUri"
        Toast.makeText(activity.baseContext, msg, Toast.LENGTH_SHORT).show()
        Log.d(MainActivity.TAG, msg)
    }

    override fun onError(exc: ImageCaptureException) {
        Log.e(MainActivity.TAG, "Photo capture failed: ${exc.message}", exc)
    }
}