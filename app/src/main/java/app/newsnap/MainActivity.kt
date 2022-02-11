package app.newsnap

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import app.newsnap.camera.PhotoCamera
import app.newsnap.camera.VideoCamera
import app.newsnap.ui.OptionsBar.IOptionsBar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), IOptionsBar,
    SharedPreferences.OnSharedPreferenceChangeListener, TabLayout.OnTabSelectedListener {

    private lateinit var viewFinder: ViewFinder
    private lateinit var photoCamera: PhotoCamera
    private lateinit var videoCamera: VideoCamera
    private lateinit var activeCamera: app.newsnap.camera.Camera

    private var captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    private var cameraMode: Int = 0

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var viewsFaded: Boolean = false
    private val viewsToFade by lazy { hashMapOf(
        options_bar to 0.3f,
        camera_capture_button to 0.5f,
        camera_swap_button to 0.3f,
        tab_layout to 0.5f
    ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: try?
        // Build viewfinder
        viewFinder = ViewFinder(this, previewView)

        // Initialize supported camera modes
        for (mode: Int in Configuration.supportedCameraModes) {
            when (mode) {
                0 -> photoCamera = PhotoCamera(this, viewFinder)
                1 -> videoCamera = VideoCamera(this, viewFinder)
            }
        }

        activeCamera = photoCamera

        val cameraModes = resources.getStringArray(R.array.camera_modes)
        Configuration.supportedCameraModes.forEachIndexed { i, elem ->
            tab_layout.addTab(tab_layout.newTab()
                .setText(cameraModes[elem])
                .setId(i))
        }

        options_bar.setOptionsBarListener(this)
        tab_layout.setOnTabSelectedListener(this)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        // Request camera permissions
        if (allPermissionsGranted()) {
            activeCamera.startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener { capture() }
        camera_swap_button.setOnClickListener { swapCamera() }
        previewView.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    fadeControls()
                }
            }

            view.performClick()
            viewFinder.onTouch(view, motionEvent)
            return@OnTouchListener true
        })
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
                activeCamera.startCamera()
            } else {
                Toast.makeText(this, getString(R.string.permissions_not_granted),
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun capture() {
        unFadeControls()

        if (activeCamera.cameraModeId == 0) {
            photoCamera.takePhoto()
        } else if (activeCamera.cameraModeId == 1) {
            if (videoCamera.recording) {
                videoCamera.stopVideo()
                camera_capture_button.recording = false
            } else {
                videoCamera.startVideo()
                camera_capture_button.recording = true
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun swapCamera() {
        if (activeCamera.lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            camera_swap_button.setImageResource(R.drawable.ic_camera_front)
            videoCamera.lensFacingVideo = CameraSelector.LENS_FACING_BACK
            videoCamera.lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
            photoCamera.lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            camera_swap_button.setImageResource(R.drawable.ic_camera_rear)
            videoCamera.lensFacingVideo = CameraSelector.LENS_FACING_FRONT
            videoCamera.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
            photoCamera.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
        }

        activeCamera.startCamera()
    }

    private fun fadeControls() {
        if (viewsFaded) {
            return
        }

        // Fade views if they overlap with capture button or options bar.
        if (previewView.y + previewView.height < camera_capture_button.y ||
            previewView.y > options_bar.y + options_bar.height) {
            return
        }

        for ((key, value) in viewsToFade) {
            key.animate()
                .setDuration(350)
                .alpha(value)
                .start()
        }

        viewsFaded = true
    }

    private fun unFadeControls() {
        if (!viewsFaded) {
            return
        }

        for ((key, _) in viewsToFade) {
            key.animate()
                .setDuration(250)
                .alpha(1.0f)
                .start()
        }

        viewsFaded = false
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onFlashToggled(flashMode: Int) {
        photoCamera.imageCapturer.imageCapture.flashMode = flashMode
    }

    override fun onOptionsBarClick() {
        unFadeControls()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == Configuration.KEY_CAPTURE_MODE) {
            photoCamera.handleCaptureModeChange(sharedPreferences.getString(key, "0")!!.toInt())
            if (activeCamera.cameraModeId == Configuration.ID_PICTURE_CAMERA) {
                activeCamera.startCamera()
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (activeCamera.cameraModeId == tab?.id) {
            return
        }

        activeCamera = when (tab?.id) {
            Configuration.ID_PICTURE_CAMERA -> {
                Log.d(TAG, "Switch to photo mode")
                camera_capture_button.videoMode = false
                photoCamera
            }
            Configuration.ID_VIDEO_CAMERA -> {
                Log.d(TAG, "Switch to video mode")
                camera_capture_button.videoMode = true
                videoCamera
            }
            Configuration.ID_BOKEH -> {
                Log.d(TAG, "Switch to bokeh mode")
                camera_capture_button.videoMode = false
                videoCamera
            }
            Configuration.ID_NIGHT -> {
                Log.d(TAG, "Switch to night mode")
                camera_capture_button.videoMode = false
                videoCamera
            }
            else -> {
                Log.e(TAG, "Unknown tab. Using photo camera.")
                camera_capture_button.videoMode = false
                photoCamera
            }
        }

        camera_capture_button.refreshDrawableState()
        activeCamera.startCamera()
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
