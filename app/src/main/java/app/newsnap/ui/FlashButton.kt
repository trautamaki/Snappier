package app.newsnap.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import app.newsnap.R

class FlashButton(context: Context, attrs: AttributeSet) : AppCompatImageButton(context, attrs) {
    enum class FlashMode {
        AUTO,
        ON,
        OFF
    }

    private var status = FlashMode.OFF

    fun getFlashMode(): FlashMode {
        return status
    }

    fun toggleMode() {
        status = when (status) {
            FlashMode.ON -> {
                setImageResource(R.drawable.ic_flash_off)
                FlashMode.OFF
            }
            FlashMode.OFF -> {
                setImageResource(R.drawable.ic_flash_auto)
                FlashMode.AUTO
            }
            FlashMode.AUTO -> {
                setImageResource(R.drawable.ic_flash_on)
                FlashMode.ON
            }
        }
    }
}
