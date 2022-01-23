package app.newsnap.camera

import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import app.newsnap.MainActivity
import app.newsnap.ViewFinder
import app.newsnap.capturer.ImageCapturer
import kotlinx.android.synthetic.main.activity_main.*

class PhotoCamera(
        private val activity: MainActivity, private val viewFinder: ViewFinder
) : Camera() {
    private var captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    lateinit var imageCapturer: ImageCapturer

    override fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity.applicationContext)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageCapturer = ImageCapturer(activity, captureMode)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                        activity, lensFacing, viewFinder.preview, imageCapturer.imageCapture
                )

                activity.options_bar.updateOptions(camera.cameraInfo.hasFlashUnit())
                viewFinder.camera = camera

            } catch (exc: Exception) {
                Log.e(MainActivity.TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(activity.applicationContext))
    }

    fun handleCaptureModeChange(captureMode: Int) {
        this.captureMode = captureMode
    }

    fun takePhoto() {
        imageCapturer.takePhoto()
    }
}