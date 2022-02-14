package org.snappier.camera.camera

import androidx.camera.core.CameraSelector
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
        return VideoCapturer(activity, lensFacingVideo)
    }

    fun startVideo() {
        (capturer as VideoCapturer).startVideo()
    }

    fun stopVideo() {
        (capturer as VideoCapturer).stopVideo()
    }
}