package org.snappier.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayout
import org.snappier.camera.camera.PhotoCamera
import org.snappier.camera.camera.VideoCamera
import org.snappier.camera.capturer.Capturer
import org.snappier.camera.ui.UIAnimator
import org.snappier.camera.databinding.ActivityMainBinding
import org.snappier.camera.ui.OptionsBar.IOptionsBar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), IOptionsBar,
        SharedPreferences.OnSharedPreferenceChangeListener, TabLayout.OnTabSelectedListener,
        Capturer.ICapturerCallback, View.OnClickListener {

    lateinit var binding: ActivityMainBinding

    private lateinit var photoCamera: PhotoCamera
    private lateinit var videoCamera: VideoCamera
    private lateinit var activeCamera: org.snappier.camera.camera.Camera

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val tabTouchables by lazy { binding.tabLayout.touchables }
    private val fadeableViews: HashMap<View, Float> by lazy {
        hashMapOf(
                binding.optionsBar to 0.3f,
                binding.cameraCaptureButton to 0.5f,
                binding.cameraSwapButton to 0.3f,
                binding.tabLayout to 0.5f,
                binding.galleryButtonWrapper to 0.3f,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize supported camera modes
        for (mode: Int in Configuration.supportedCameraModes) {
            when (mode) {
                Configuration.ID_PICTURE_CAMERA -> photoCamera = PhotoCamera(this)
                Configuration.ID_VIDEO_CAMERA -> videoCamera = VideoCamera(this)
            }
        }

        activeCamera = photoCamera

        val cameraModes = resources.getStringArray(R.array.camera_modes)
        Configuration.supportedCameraModes.forEachIndexed { i, elem ->
            binding.tabLayout.addTab(
                    binding.tabLayout.newTab()
                            .setText(cameraModes[elem])
                            .setId(i)
            )
        }

        binding.optionsBar.setOptionsBarListener(this)
        binding.tabLayout.addOnTabSelectedListener(this)
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)

        // Request camera permissions
        if (allPermissionsGranted()) {
            activeCamera.startCamera(this)
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        binding.cameraCaptureButton.setOnClickListener(this)
        binding.cameraSwapButton.setOnClickListener(this)
        binding.optionsBar.setOnClickListener(this)
        binding.galleryButton.setOnClickListener(this)
        binding.previewView.setOnTouchListener(View.OnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    UIAnimator.fadeControls(
                            fadeableViews, binding.previewView, binding.optionsBar,
                            binding.galleryButton, binding.galleryButtonWrapper
                    )
                }
            }

            view.performClick()
            activeCamera.viewFinder.onTouch(motionEvent)
            return@OnTouchListener true
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                activeCamera.startCamera(this)
            } else {
                Toast.makeText(
                        this, getString(R.string.permissions_not_granted),
                        Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun capture() {
        UIAnimator.unFadeControls()

        if (activeCamera.cameraModeId == Configuration.ID_PICTURE_CAMERA) {
            binding.cameraCaptureButton.isClickable = false
            photoCamera.takePhoto()
            UIAnimator.shutterAnimation(binding.shutter, binding.previewView)
        } else if (activeCamera.cameraModeId == Configuration.ID_VIDEO_CAMERA) {
            if (videoCamera.recording) {
                videoCamera.stopVideo()
                binding.cameraCaptureButton.recording = false
                tabTouchables?.forEach { it.isEnabled = true }
                binding.optionsBar.enableOptionsForVideoRecord(true)
                UIAnimator.animateReveal(binding.cameraSwapButton, 100, View.VISIBLE)
                UIAnimator.animateReveal(binding.galleryButtonWrapper, 100, View.VISIBLE)
                UIAnimator.animateReveal(binding.galleryButton, 100, View.VISIBLE)
            } else {
                videoCamera.startVideo()
                binding.cameraCaptureButton.recording = true
                tabTouchables?.forEach { it.isEnabled = false }
                binding.optionsBar.enableOptionsForVideoRecord(false)
                UIAnimator.animateHide(binding.cameraSwapButton, 0f, 100, View.INVISIBLE)
                UIAnimator.animateHide(binding.galleryButtonWrapper, 0f, 100, View.INVISIBLE)
                UIAnimator.animateHide(binding.galleryButton, 0f, 100, View.INVISIBLE)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun swapCamera() {
        if (activeCamera.lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            binding.cameraSwapButton.setImageResource(R.drawable.ic_camera_front)
            videoCamera.lensFacingVideo = CameraSelector.LENS_FACING_BACK
            videoCamera.lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
            photoCamera.lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            binding.cameraSwapButton.setImageResource(R.drawable.ic_camera_rear)
            videoCamera.lensFacingVideo = CameraSelector.LENS_FACING_FRONT
            videoCamera.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
            photoCamera.lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
        }

        activeCamera.startCamera(this)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onFlashToggled(flashMode: Int) {
        (photoCamera.capturer.getCapture() as ImageCapture).flashMode = flashMode
    }

    override fun onOptionsBarClick() {
        UIAnimator.unFadeControls()
    }

    override fun onAspectRatioChanged(aspectRatio: Int) {
        Configuration.ASPECT_RATIO = aspectRatio
        activeCamera.startCamera(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == Configuration.KEY_CAPTURE_MODE) {
            photoCamera.handleCaptureModeChange(sharedPreferences.getString(key, "0")!!.toInt())
            if (activeCamera.cameraModeId == Configuration.ID_PICTURE_CAMERA) {
                activeCamera.startCamera(this)
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
                binding.cameraCaptureButton.videoMode = false
                photoCamera
            }
            Configuration.ID_VIDEO_CAMERA -> {
                Log.d(TAG, "Switch to video mode")
                binding.cameraCaptureButton.videoMode = true
                videoCamera
            }
            Configuration.ID_BOKEH -> {
                Log.d(TAG, "Switch to bokeh mode")
                binding.cameraCaptureButton.videoMode = false
                videoCamera
            }
            Configuration.ID_NIGHT -> {
                Log.d(TAG, "Switch to night mode")
                binding.cameraCaptureButton.videoMode = false
                videoCamera
            }
            else -> {
                Log.e(TAG, "Unknown tab. Using photo camera.")
                binding.cameraCaptureButton.videoMode = false
                photoCamera
            }
        }

        binding.cameraCaptureButton.refreshDrawableState()
        activeCamera.startCamera(this)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onFileSaved(fileUri: Uri?) {
        binding.galleryButton.setNewFile(fileUri)
        binding.cameraCaptureButton.isClickable = true
    }

    override fun onClick(v: View?) {
        UIAnimator.unFadeControls()

        when (v) {
            binding.cameraCaptureButton -> capture()
            binding.cameraSwapButton -> swapCamera()
            binding.galleryButton -> binding.galleryButton.onClick(v)
        }
    }

    companion object {
        const val TAG = "Snappier"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        )
    }
}
