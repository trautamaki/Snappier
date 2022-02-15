package org.snappier.camera.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.camera.core.ImageCapture
import org.snappier.camera.R

class FlashButton(context: Context, attrs: AttributeSet) : AppCompatImageButton(context, attrs) {
    private val triState = arrayOf(
        ImageCapture.FLASH_MODE_OFF,
        ImageCapture.FLASH_MODE_ON,
        ImageCapture.FLASH_MODE_AUTO
    )
    private val onOff = arrayOf(ImageCapture.FLASH_MODE_OFF, ImageCapture.FLASH_MODE_ON)
    private var activeFlashOptions = triState
    var status = 0

    fun getFlashMode(): Int {
        return activeFlashOptions[status]
    }

    fun toggleMode() {
        status++
        if (status == activeFlashOptions.size) {
            status = 0
        }

        when (status) {
            0 -> {
                setImageResource(R.drawable.ic_flash_off)
            }
            1 -> {
                setImageResource(R.drawable.ic_flash_on)
            }
            2 -> {
                setImageResource(R.drawable.ic_flash_auto)
            }
            else -> {
                setImageResource(R.drawable.ic_flash_off)
            }
        }
    }

    fun setFlashOptions(useTriState: Boolean) {
        activeFlashOptions = if (useTriState) {
            triState
        } else {
            onOff
        }
    }
}
