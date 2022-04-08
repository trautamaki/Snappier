package org.snappier.camera.camera

import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import org.snappier.camera.MainActivity
import org.snappier.camera.capturer.Capturer
import org.snappier.camera.capturer.ImageCapturer

class PhotoCamera(
        private val activity: MainActivity
) : Camera(activity) {
    private var captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY

    override fun buildCapturer(): Capturer {
        return ImageCapturer(
                ContextCompat.getMainExecutor(activity),
                activity.contentResolver,
                captureMode
        )
    }

    fun handleCaptureModeChange(captureMode: Int) {
        this.captureMode = captureMode
    }

    fun takePhoto() {
        (capturer as ImageCapturer).takePhoto()
    }
}