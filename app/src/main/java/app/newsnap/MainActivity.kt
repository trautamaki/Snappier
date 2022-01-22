package app.newsnap

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import app.newsnap.capturer.ImageCapturer
import app.newsnap.ui.OptionsBar.IOptionsBar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), IOptionsBar,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private var lensFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private lateinit var viewFinder: ViewFinder
    private lateinit var imageCapturer: ImageCapturer
    private lateinit var cameraExecutor: ExecutorService

    private var captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    private var aspectRatio: Int = Configuration.DEFAULT_ASPECT_RATIO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        options_bar.setOptionsBarListener(this)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener { takePhoto() }
        camera_swap_button.setOnClickListener { swapCamera() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, getString(R.string.permissions_not_granted),
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun takePhoto() {
        viewFinder.unFadeControls()
        imageCapturer.takePhoto()
    }

    private fun swapCamera() {
        lensFacing = if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            camera_swap_button.setImageResource(R.drawable.ic_camera_front)
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            camera_swap_button.setImageResource(R.drawable.ic_camera_rear)
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageCapturer = ImageCapturer(this, captureMode, aspectRatio)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Build viewfinder
                viewFinder = ViewFinder(this, preview_view, aspectRatio)

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    this, lensFacing, viewFinder.preview, imageCapturer.imageCapture
                )

                viewFinder.camera = camera

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val TAG = "NewSnap"
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        val SAVEFILE_LOCATION = Environment.DIRECTORY_DCIM
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onFlashToggled(flashMode: Int) {
        imageCapturer.imageCapture.flashMode = flashMode
    }

    override fun onOptionsBarClick() {
        viewFinder.unFadeControls()
    }

    override fun onAspectRatioChanged(aspectRatio: Int) {
        this.aspectRatio = aspectRatio
        startCamera()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == Configuration.KEY_CAPTURE_MODE) {
            captureMode = sharedPreferences.getString(key, "0")!!.toInt()
            startCamera()
        }
    }
}
