package app.newsnap

import android.app.Activity
import androidx.camera.core.AspectRatio

class Configuration {
    companion object {
        const val KEY_CAPTURE_MODE = "capture_mode"
        const val DEFAULT_ASPECT_RATIO = AspectRatio.RATIO_4_3
    }
}