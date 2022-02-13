package app.newsnap.camera

import androidx.camera.core.CameraSelector
import app.newsnap.Configuration
import app.newsnap.MainActivity
import app.newsnap.ViewFinder
import app.newsnap.capturer.Capturer
import app.newsnap.capturer.VideoCapturer

class VideoCamera(
        private val activity: MainActivity) : Camera(activity) {
    override var cameraModeId = Configuration.ID_VIDEO_CAMERA
    var lensFacingVideo: Int = CameraSelector.LENS_FACING_BACK

    var recording: Boolean = false
        get() = (capturer as VideoCapturer).recording


    override fun buildCapturer(): Capturer {
        return VideoCapturer(activity, lensFacingVideo)
    }

    fun startVideo() {
        (capturer as VideoCapturer).startVideo()
    }

    fun stopVideo() {
        (capturer as VideoCapturer).stopVideo()
    }
}