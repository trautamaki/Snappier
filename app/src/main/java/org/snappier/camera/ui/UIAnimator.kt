package org.snappier.camera.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class UIAnimator {
    companion object {
        private val viewsToFade : HashMap<View, Float> by lazy { hashMapOf()}
        private var viewsFaded: Boolean = false

        /**
         * Fade controls that overlap camera preview.
         * Options bar requires seperate handling because it is placed in the top of the screen.
         * Gallery button requires seperate handling because it's coordinates are inside the
         * @param gallery_button_wrapper .
         *
         * @param fadeableViews views that could be faded and their end opacity
         * @param preview_view the camera preview
         * @param options_bar options bar view
         * @param gallery_button the gallery button
         * @param gallery_button_wrapper the outer view containing gallery button
         */
        fun fadeControls(fadeableViews: HashMap<View, Float>, preview_view: View,
                         options_bar: View, gallery_button: View, gallery_button_wrapper: View) {
            if (viewsFaded) {
                return
            }

            viewsToFade.clear()

            // Fade views if they overlap with the camera preview.
            for ((view, fadeAmount) in fadeableViews) {
                if (view == options_bar) {
                    if (preview_view.y < options_bar.y + view.height) {
                        viewsToFade[view] = fadeAmount
                    }
                } else if (preview_view.y + preview_view.height > view.y) {
                    if (view == gallery_button_wrapper) {
                        viewsToFade[gallery_button] = fadeAmount
                    }

                    viewsToFade[view] = fadeAmount
                }
            }

            for ((key, value) in viewsToFade) {
                animateHide(key, value, 350, View.VISIBLE)
            }

            viewsFaded = true
        }

        /**
         * Unfade all controls that overlap with camera preview.
         */
        fun unFadeControls() {
            if (!viewsFaded) {
                return
            }

            for ((key, _) in viewsToFade) {
                animateReveal(key, 100, View.VISIBLE)
            }

            viewsFaded = false
        }

        /**
         * Play the shutter animation. Scales the shutter view to same size as @param preview_view.
         *
         * @param shutter the shutter view
         * @param preview_view camera preview
         */
        fun shutterAnimation(shutter: View, preview_view: View) {
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

        /**
         * Animate a fade out of @param view
         *
         * @param view view to hide
         * @param amount end alpha for the view
         * @param duration animation duration
         * @paran endVisibility visibility to set for the view at onAnimationEnd
         */
        fun animateHide(view: View, amount: Float, duration: Long, endVisibility: Int) {
            view.animate()
                .setDuration(duration)
                .alpha(amount)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        view.visibility = endVisibility
                    }
                })
                .start()
        }

        /**
         * Animate a fade in of @param view
         *
         * @param view view to reveal
         * @param duration animation duration
         * @paran endVisibility visibility to set for the view at onAnimationEnd
         */
        fun animateReveal(view: View, duration: Long, endVisibility: Int) {
            view.animate()
                .setDuration(duration)
                .alpha(1.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        view.visibility = endVisibility
                    }
                })
                .start()
        }
    }
}