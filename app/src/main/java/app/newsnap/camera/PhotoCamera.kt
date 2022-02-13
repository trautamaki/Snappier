package app.newsnap.camera

import androidx.camera.core.ImageCapture
import app.newsnap.MainActivity
import app.newsnap.ViewFinder
import app.newsnap.capturer.Capturer
import app.newsnap.capturer.ImageCapturer

class PhotoCamera(
        private val activity: MainActivity) : Camera(activity) {
    private var captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY

    override fun buildCapturer(): Capturer {
        return ImageCapturer(activity, captureMode)
    }

    fun handleCaptureModeChange(captureMode: Int) {
        this.captureMode = captureMode
    }

    fun takePhoto() {
        (capturer as ImageCapturer).takePhoto()
    }
}