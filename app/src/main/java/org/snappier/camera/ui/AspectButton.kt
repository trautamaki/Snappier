package org.snappier.camera.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.camera.core.AspectRatio
import org.snappier.camera.Configuration
import org.snappier.camera.R

class AspectButton(context: Context, attrs: AttributeSet) : AppCompatImageButton(context, attrs) {
    private var status = Configuration.DEFAULT_ASPECT_RATIO

    fun getAspectRatio(): Int {
        return status
    }

    fun toggleAspectRatio() {
        val previous = status
        status = when (previous) {
            AspectRatio.RATIO_4_3 -> {
                setImageResource(R.drawable.ic_16_9)
                AspectRatio.RATIO_16_9
            }
            AspectRatio.RATIO_16_9 -> {
                setImageResource(R.drawable.ic_3_4)
                AspectRatio.RATIO_4_3
            }
            else -> {
                setImageResource(R.drawable.ic_16_9)
                AspectRatio.RATIO_16_9
            }
        }
    }
}
