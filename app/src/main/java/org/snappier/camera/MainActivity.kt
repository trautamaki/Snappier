package org.snappier.camera

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.options_bar.view.*
import org.snappier.camera.camera.PhotoCamera
import org.snappier.camera.camera.VideoCamera
import org.snappier.camera.capturer.Capturer
import org.snappier.camera.ui.OptionsBar.IOptionsBar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), IOptionsBar,
    SharedPreferences.OnSharedPreferenceChangeListener, TabLayout.OnTabSelectedListener,
    Capturer.ICapturer {

    private lateinit var photoCamera: PhotoCamera
    private lateinit var videoCamera: VideoCamera
    private lateinit var activeCamera: org.snappier.camera.camera.Camera

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

        // Initialize supported camera modes
        for (mode: Int in Configuration.supportedCameraModes) {
            when (mode) {
                0 -> photoCamera = PhotoCamera(this)
                1 -> videoCamera = VideoCamera(this)
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
        preview_view.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    fadeControls()
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
            shutterAnimation()
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
        if (preview_view.y + preview_view.height < camera_capture_button.y ||
            preview_view.y > options_bar.y + options_bar.height) {
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

    private fun shutterAnimation() {
        // Make sure shutter view size is same as preview view
        val previewParams = preview_view.layoutParams as ConstraintLayout.LayoutParams
        val shutterParams = shutter.layoutParams as ConstraintLayout.LayoutParams
        shutterParams.topToBottom = previewParams.topToBottom
        shutterParams.topToTop = previewParams.topToTop
        shutterParams.dimensionRatio = previewParams.dimensionRatio
        shutter.layoutParams = shutterParams

        shutter.visibility = View.VISIBLE
        shutter.animate()
            .setDuration(100)
            .alpha(0f)
            .setStartDelay(50)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    shutter.visibility = View.INVISIBLE
                    shutter.alpha = 1f
                }
            })
            .start()
    }

    override fun onFlashToggled(flashMode: Int) {
        (photoCamera.capturer.getCapture() as ImageCapture).flashMode = flashMode
        if (activeCamera is VideoCamera) {
            videoCamera.setFlash(flashMode)
        }
    }

    override fun onOptionsBarClick() {
        unFadeControls()
    }

    override fun onAspectRatioChanged(aspectRatio: Int) {
        Configuration.ASPECT_RATIO = aspectRatio
        activeCamera.startCamera()
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
            Configuration.ID_PICTURE_CAMERA,
            Configuration.ID_BOKEH,
            Configuration.ID_NIGHT -> {
                Log.d(TAG, "Switch to photo mode")
                camera_capture_button.videoMode = false
                options_bar.button_flash_toggle.setFlashOptions(true /* triState */)
                photoCamera
            }
            Configuration.ID_VIDEO_CAMERA -> {
                Log.d(TAG, "Switch to video mode")
                camera_capture_button.videoMode = true
                // Switch the flash to off-mode if it's in auto-mode
                val flashMode = options_bar.button_flash_toggle.getFlashMode()
                if (flashMode == ImageCapture.FLASH_MODE_AUTO) {
                    options_bar.button_flash_toggle.toggleMode()
                }

                options_bar.button_flash_toggle.setFlashOptions(false /* triState */)
                videoCamera.setFlash(flashMode)
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

    override fun onStartTakingPicture() {
        camera_capture_button.isEnabled = false
    }

    override fun onPictureTaken() {
        camera_capture_button.isEnabled = true
    }

    override fun onVideoStart() {
        Log.d("Snappier", "Start video record")
        camera_swap_button.visibility = View.INVISIBLE
    }

    override fun onVideoStop() {
        Log.d("Snappier", "Stop video record")
        camera_swap_button.visibility = View.VISIBLE
    }

    override fun onVideoSaved() {
        Log.d(TAG, "Video capture succeeded")
    }

    override fun onError(msg: String) {
        Log.e(TAG, msg)
        camera_capture_button.isEnabled = true
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
