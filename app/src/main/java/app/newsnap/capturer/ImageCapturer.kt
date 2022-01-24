package app.newsnap.capturer

import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import app.newsnap.Configuration
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

        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileFormat)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, createFileName(fileFormat))
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Configuration.IMAGE_RELATIVE_PATH)
        }

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                activity.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
                outputFileOptions, executor, this
        )
    }

    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
        Log.d(MainActivity.TAG, "Photo capture succeeded: ${output.savedUri}")
    }

    override fun onError(exc: ImageCaptureException) {
        Log.e(MainActivity.TAG, "Photo capture failed: ${exc.message}", exc)
    }
}