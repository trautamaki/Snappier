package app.newsnap

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.MotionEvent
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import kotlinx.android.synthetic.main.activity_main.*

class ViewFinder(private val activity: MainActivity, private val previewView: PreviewView) :
    View.OnTouchListener {

    private var camera: Camera? = null

    fun build(cameraProvider: ProcessCameraProvider, cameraSelector: CameraSelector) {
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        camera = cameraProvider.bindToLifecycle(
            activity, cameraSelector, preview
        )
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
                // Create a MeteringAction
                val action = FocusMeteringAction.Builder(point).build()

                // Start metering
                camera?.cameraControl?.startFocusAndMetering(action)
                return true
            }
            else -> return false
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
}