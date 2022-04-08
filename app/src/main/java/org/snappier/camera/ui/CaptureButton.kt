package org.snappier.camera.ui

import android.content.Context
import android.util.AttributeSet
import org.snappier.camera.R

class CaptureButton(context: Context, attrs: AttributeSet) :
    com.google.android.material.floatingactionbutton.FloatingActionButton(context, attrs) {

    var recording = false
    var videoMode = false

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        if (recording) {
            mergeDrawableStates(drawableState, STATE_RECORDING)
        }

        if (videoMode) {
            mergeDrawableStates(drawableState, STATE_VIDEO_MODE)
        }

        return drawableState
    }

    companion object {
        private val STATE_RECORDING = intArrayOf(R.attr.recording)
        private val STATE_VIDEO_MODE = intArrayOf(R.attr.video_mode)
    }
}