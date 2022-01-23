package app.newsnap.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import app.newsnap.Configuration
import app.newsnap.MainActivity
import app.newsnap.ViewFinder
import app.newsnap.capturer.VideoCapturer

class VideoCamera(
        private val activity: MainActivity, private val viewFinder: ViewFinder
) : Camera() {
    override var cameraModeId = Configuration.ID_VIDEO_CAMERA
    var lensFacingVideo: Int = CameraSelector.LENS_FACING_BACK

    var recording: Boolean = false
        get() = videoCapturer.recording

    lateinit var videoCapturer: VideoCapturer

    override fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity.applicationContext)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            videoCapturer = VideoCapturer(activity, lensFacingVideo)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                        activity, lensFacing, viewFinder.preview, videoCapturer.videoCapture
                )

                viewFinder.camera = camera

            } catch (exc: Exception) {
                Log.e(MainActivity.TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(activity.applicationContext))
    }

    fun startVideo() {
        videoCapturer.startVideo()
    }

    fun stopVideo() {
        videoCapturer.stopVideo()
    }
}