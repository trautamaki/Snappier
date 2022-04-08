package org.snappier.camera

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*

class ViewFinder(private val activity: MainActivity, private val previewView: PreviewView) {

    var camera: Camera? = null

    lateinit var preview: Preview

    private var handler: Handler = Handler()
    private var zoomInProgress: Boolean = false

    private val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = camera?.cameraInfo?.zoomState?.value?.zoomRatio?.times(detector.scaleFactor)
            if (scale != null) {
                camera?.cameraControl?.setZoomRatio(scale)
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            zoomInProgress = true
            return super.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            handler.postDelayed(Runnable { zoomInProgress = false }, 300)
            super.onScaleEnd(detector)
        }
    }

    private val scaleGestureDetector = ScaleGestureDetector(activity.applicationContext, listener)

    init {
        build()
    }

    private fun build() {
        setAspectRatio()
        preview = Preview.Builder()
            .setTargetAspectRatio(Configuration.ASPECT_RATIO)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
    }

    fun onTouch(motionEvent: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(motionEvent)

        if (zoomInProgress) {
            return true
        }

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                // Get the MeteringPointFactory from viewFinder
                val factory = previewView.meteringPointFactory

                // Get touch point
                val point = factory.createPoint(motionEvent.x, motionEvent.y)
                showFocusRing(motionEvent.x, motionEvent.y)
                // Create a MeteringAction
                val action = FocusMeteringAction.Builder(point).build()

                // Start metering
                camera?.cameraControl?.startFocusAndMetering(action)
                return true
            }
            else -> return false
        }
    }

    private fun setAspectRatio() {
        val params = previewView.layoutParams as ConstraintLayout.LayoutParams

        if (Configuration.getAspectInt(params.dimensionRatio) == Configuration.ASPECT_RATIO) {
            return
        }

        if (Configuration.ASPECT_RATIO == AspectRatio.RATIO_16_9) {
            // Place preview to top of screen
            params.topToBottom = ConstraintLayout.LayoutParams.UNSET
            params.topToTop = activity.main_layout.id
            params.dimensionRatio = Configuration.getAspectString(Configuration.ASPECT_RATIO)
        } else if (Configuration.ASPECT_RATIO == AspectRatio.RATIO_4_3) {
            // Place preview to bottom of options bar
            params.topToTop = ConstraintLayout.LayoutParams.UNSET
            params.topToBottom = activity.options_bar.id
            params.dimensionRatio = Configuration.getAspectString(Configuration.ASPECT_RATIO)
        }

        previewView.layoutParams = params
    }

    private fun showFocusRing(x: Float, y: Float) {
        // Get the focus ring
        val focusRing = activity.focusRing

        // Show the focus ring on touch position
        val width = focusRing.width.toFloat()
        focusRing.x = x - width / 2
        focusRing.y = y + previewView.y - width / 2
        focusRing.visibility = View.VISIBLE
        focusRing.alpha = 1f

        // Animate fade out
        focusRing.animate()
            .setStartDelay(350)
            .setDuration(350)
            .alpha(0f)
            .setListener(object : AnimatorListenerAdapter() {
                var isCancelled = false

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (!isCancelled) {
                        focusRing.visibility = View.INVISIBLE
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    isCancelled = true
                }
            })
            .start()
    }
}