package app.newsnap

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.MotionEvent
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_main.*

class ViewFinder(private val activity: MainActivity, private val previewView: PreviewView,
        private val aspectRatio: Int) :
    View.OnTouchListener {

    var camera: Camera? = null

    lateinit var preview: Preview

    init {
        build()
    }

    private fun build() {
        setAspectRatio()
        preview = Preview.Builder()
            .setTargetAspectRatio(aspectRatio)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        previewView.setOnTouchListener(this)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                // Get the MeteringPointFactory from viewFinder
                val factory = previewView.meteringPointFactory

                // Get touch point
                val point = factory.createPoint(motionEvent.x, motionEvent.y)
                showFocusRing(motionEvent.x, motionEvent.y)
                fadeControls()
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

        if (getAspectInt(params.dimensionRatio) == aspectRatio) {
            return
        }

        if (aspectRatio == AspectRatio.RATIO_16_9) {
            // Place preview to top of screen
            params.topToBottom = ConstraintLayout.LayoutParams.UNSET
            params.topToTop = activity.main_layout.id
            params.dimensionRatio = getAspectString(aspectRatio)
        } else if (aspectRatio == AspectRatio.RATIO_4_3) {
            // Place preview to bottom of options bar
            params.topToTop = ConstraintLayout.LayoutParams.UNSET
            params.topToBottom = activity.options_bar.id
            params.dimensionRatio = getAspectString(aspectRatio)
        }

        previewView.layoutParams = params
    }

    private fun getAspectInt(ratio: String) : Int {
        return when (ratio) {
            "4:3" -> {
                0
            }
            "9:16" -> {
                1
            }
            else -> {
                0
            }
        }
    }

    private fun getAspectString(ratio: Int) : String {
        return when (ratio) {
            0 -> {
                "3:4"
            }
            1 -> {
                "9:16"
            }
            else -> {
                "4:3"
            }
        }
    }

    private fun showFocusRing(x: Float, y: Float) {
        //Get the focus ring
        val focusRing = activity.focusRing

        //Show the focus ring on touch position
        val width = focusRing.width.toFloat()
        focusRing.x = x - width / 2
        focusRing.y = y + previewView.y - width / 2
        focusRing.visibility = View.VISIBLE
        focusRing.alpha = 1f

        //Animate fade out
        focusRing.animate()
            .setStartDelay(350)
            .setDuration(350)
            .alpha(0f)
            .setListener(object : AnimatorListenerAdapter() {

                var isCancelled = false

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (!isCancelled)
                        focusRing.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    isCancelled = true
                }
            })
            .start()
    }

    private fun fadeControls() {
        if (previewView.y + previewView.height < activity.camera_capture_button.y ||
                previewView.y > activity.options_bar.y + activity.options_bar.height) {
            return
        }

        activity.options_bar.animate()
                .setDuration(350)
                .alpha(0.3f)
                .start()
        activity.camera_capture_button.animate()
                .setDuration(350)
                .alpha(0.5f)
                .start()
        activity.camera_swap_button.animate()
                .setDuration(350)
                .alpha(0.3f)
                .start()
    }

    fun unFadeControls() {
        activity.options_bar.animate()
                .setDuration(250)
                .alpha(1f)
                .start()
        activity.camera_capture_button.animate()
                .setDuration(250)
                .alpha(1f)
                .start()
        activity.camera_swap_button.animate()
                .setDuration(250)
                .alpha(1f)
                .start()
    }
}