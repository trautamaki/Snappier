package app.newsnap

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import app.newsnap.capturer.ImageCapturer
import app.newsnap.capturer.VideoCapturer
import app.newsnap.ui.OptionsBar.IOptionsBar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), IOptionsBar,
    SharedPreferences.OnSharedPreferenceChangeListener, TabLayout.OnTabSelectedListener {
    private var lensFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var lensFacing2: Int = CameraSelector.LENS_FACING_BACK

    private lateinit var viewFinder: ViewFinder
    private lateinit var imageCapturer: ImageCapturer
    private lateinit var videoCapturer: VideoCapturer
    private lateinit var cameraExecutor: ExecutorService

    private var captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    private var cameraMode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cameraModes = resources.getStringArray(R.array.camera_modes)
        cameraModes.forEach { tab_layout.addTab(tab_layout.newTab().setText(it)) }

        options_bar.setOptionsBarListener(this)
        tab_layout.setOnTabSelectedListener(this)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startPhotoCamera()
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener { capture() }
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
                startPhotoCamera()
            } else {
                Toast.makeText(this, getString(R.string.permissions_not_granted),
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun capture() {
        viewFinder.unFadeControls()

        if (cameraMode == 0) {
            imageCapturer.takePhoto()
        } else {
            if (videoCapturer.recording) {
                videoCapturer.stopVideo()
                camera_capture_button.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.accent))
            } else {
                camera_capture_button.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.capture_button_capturing))
                videoCapturer.startVideo()
            }
        }
    }

    private fun swapCamera() {
        lensFacing = if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            camera_swap_button.setImageResource(R.drawable.ic_camera_front)
            lensFacing2 = CameraSelector.LENS_FACING_BACK
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            camera_swap_button.setImageResource(R.drawable.ic_camera_rear)
            lensFacing2 = CameraSelector.LENS_FACING_FRONT
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        startCamera()
    }

    private fun startPhotoCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageCapturer = ImageCapturer(this, captureMode)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Build viewfinder
                viewFinder = ViewFinder(this, previewView)

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

    private fun startVideoCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            videoCapturer = VideoCapturer(this, lensFacing2)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Build viewfinder
                viewFinder = ViewFinder(this, previewView)

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                        this, lensFacing, viewFinder.preview, videoCapturer.videoCapture
                )

                viewFinder.camera = camera

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startCamera() {
        when (cameraMode) {
            0 -> startPhotoCamera()
            1 -> startVideoCamera()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onFlashToggled(flashMode: Int) {
        imageCapturer.imageCapture.flashMode = flashMode
    }

    override fun onOptionsBarClick() {
        viewFinder.unFadeControls()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == Configuration.KEY_CAPTURE_MODE) {
            captureMode = sharedPreferences.getString(key, "0")!!.toInt()
            startCamera()
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (cameraMode == tab?.position) {
            return
        }

        when (tab?.position) {
            0 -> {
                Log.d(TAG, "Switch to photo mode")
                cameraMode = 0
                startPhotoCamera()
            }
            1 -> {
                Log.d(TAG, "Switch to video mode")
                cameraMode = 1
                startVideoCamera()
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    companion object {
        const val TAG = "NewSnap"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}
