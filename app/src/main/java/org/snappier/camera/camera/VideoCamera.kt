package org.snappier.camera.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import kotlinx.android.synthetic.main.activity_main.*
import org.snappier.camera.Configuration
import org.snappier.camera.MainActivity
import org.snappier.camera.capturer.Capturer
import org.snappier.camera.capturer.ImageCapturer
import org.snappier.camera.capturer.VideoCapturer

class VideoCamera(
        private val activity: MainActivity) : Camera(activity) {
    override var cameraModeId = Configuration.ID_VIDEO_CAMERA
    var lensFacingVideo: Int = CameraSelector.LENS_FACING_BACK

    var recording: Boolean = false
        get() = (capturer as VideoCapturer).recording

    override fun buildCapturer(): Capturer {
        return VideoCapturer(activity.contentResolver, lensFacingVideo,
            activity.preview_view.display.rotation, activity.mainExecutor)
    }

    fun turnFlashOn(flashMode: Int) {
        camera?.cameraControl?.enableTorch(flashMode == ImageCapture.FLASH_MODE_ON)
    }

    fun startVideo() {
        (capturer as VideoCapturer).startVideo()
    }

    fun stopVideo() {
        (capturer as VideoCapturer).stopVideo()
    }
}