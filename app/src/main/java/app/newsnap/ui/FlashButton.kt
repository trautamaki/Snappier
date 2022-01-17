package app.newsnap.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.camera.core.ImageCapture
import app.newsnap.R

class FlashButton(context: Context, attrs: AttributeSet) : AppCompatImageButton(context, attrs) {
    private var status = ImageCapture.FLASH_MODE_OFF

    fun getFlashMode(): Int {
        return status
    }

    fun toggleMode() {
        val previous = status
        status = when (previous) {
            ImageCapture.FLASH_MODE_ON -> {
                setImageResource(R.drawable.ic_flash_off)
                ImageCapture.FLASH_MODE_OFF
            }
            ImageCapture.FLASH_MODE_OFF -> {
                setImageResource(R.drawable.ic_flash_auto)
                ImageCapture.FLASH_MODE_AUTO
            }
            ImageCapture.FLASH_MODE_AUTO -> {
                setImageResource(R.drawable.ic_flash_on)
                ImageCapture.FLASH_MODE_ON
            }
            else -> {
                setImageResource(R.drawable.ic_flash_off)
                ImageCapture.FLASH_MODE_OFF
            }
        }
    }
}
