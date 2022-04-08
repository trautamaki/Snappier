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
import kotlinx.android.synthetic.main.activity_main.*
import org.snappier.camera.camera.PhotoCamera
import org.snappier.camera.camera.VideoCamera
import org.snappier.camera.capturer.Capturer
import org.snappier.camera.ui.UIAnimator
import org.snappier.camera.ui.GalleryButton
import org.snappier.camera.ui.OptionsBar.IOptionsBar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), IOptionsBar,
    SharedPreferences.OnSharedPreferenceChangeListener, TabLayout.OnTabSelectedListener,
    Capturer.ICapturerCallback, View.OnClickListener {

    private lateinit var photoCamera: PhotoCamera
    private lateinit var videoCamera: VideoCamera
    private lateinit var activeCamera: org.snappier.camera.camera.Camera
    private val tabTouchables by lazy { tab_layout.touchables }

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val fadeableViews : HashMap<View, Float> by lazy { hashMapOf(
        options_bar to 0.3f,
        camera_capture_button to 0.5f,
        camera_swap_button to 0.3f,
        tab_layout to 0.5f,
        gallery_button_wrapper to 0.3f,
    ) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            activeCamera.startCamera(this)
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        camera_capture_button.setOnClickListener(this)
        camera_swap_button.setOnClickListener(this)
        options_bar.setOnClickListener(this)
        gallery_button.setOnClickListener(this)
        preview_view.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    UIAnimator.fadeControls(fadeableViews, preview_view, options_bar,
                            gallery_button, gallery_button_wrapper)
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
                activeCamera.startCamera(this)
            } else {
                Toast.makeText(this, getString(R.string.permissions_not_granted),
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun capture() {
        UIAnimator.unFadeControls()

        if (activeCamera.cameraModeId == Configuration.ID_PICTURE_CAMERA) {
            camera_capture_button.isEnabled = false
            photoCamera.takePhoto()
            UIAnimator.shutterAnimation(shutter, preview_view)
        } else if (activeCamera.cameraModeId == Configuration.ID_VIDEO_CAMERA) {
            if (videoCamera.recording) {
                videoCamera.stopVideo()
                camera_capture_button.recording = false
                tabTouchables?.forEach { it.isEnabled = true }
                options_bar.enableOptionsForVideoRecord(true)
                UIAnimator.animateReveal(camera_swap_button, 100, View.VISIBLE)
                UIAnimator.animateReveal(gallery_button_wrapper, 100, View.VISIBLE)
                UIAnimator.animateReveal(gallery_button, 100, View.VISIBLE)
            } else {
                videoCamera.startVideo()
                camera_capture_button.recording = true
                tabTouchables?.forEach { it.isEnabled = false }
                options_bar.enableOptionsForVideoRecord(false)
                UIAnimator.animateHide(camera_swap_button, 0f, 100, View.INVISIBLE)
                UIAnimator.animateHide(gallery_button_wrapper, 0f, 100, View.INVISIBLE)
                UIAnimator.animateHide(gallery_button, 0f, 100, View.INVISIBLE)
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

        activeCamera.startCamera(this)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
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
        activeCamera.startCamera(this)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {}

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onFileSaved(fileUri: Uri?) {
        gallery_button.setNewFile(fileUri)
        camera_capture_button.isEnabled = true
    }

    override fun onClick(v: View?) {
        UIAnimator.unFadeControls()

        when (v) {
            camera_capture_button -> capture()
            camera_swap_button -> swapCamera()
            gallery_button -> (gallery_button as GalleryButton).onClick(v)
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
