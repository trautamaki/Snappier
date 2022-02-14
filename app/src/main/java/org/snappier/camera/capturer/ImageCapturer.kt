package org.snappier.camera.capturer

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.UseCase
import org.snappier.camera.Configuration
import org.snappier.camera.MainActivity
import java.util.concurrent.Executor

class ImageCapturer(
    private val contentResolver: ContentResolver, private val captureMode: Int, executor: Executor,
) :
    Capturer(executor), ImageCapture.OnImageSavedCallback {

    lateinit var imageCapture: ImageCapture

    init {
        build()
    }

    private fun build() {
        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(Configuration.ASPECT_RATIO)
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
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
                outputFileOptions, executor, this
        )

        listener?.onStartTakingPicture()
    }

    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
        Log.d(MainActivity.TAG, "Photo capture succeeded: ${output.savedUri}")
        listener?.onPictureTaken()
    }

    override fun onError(exc: ImageCaptureException) {
        Log.e(MainActivity.TAG, "Photo capture failed: ${exc.message}", exc)
        exc.message?.let { listener?.onError(it) }
    }

    override fun getCapture(): UseCase {
        return imageCapture
    }
}