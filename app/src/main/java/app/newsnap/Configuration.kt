package app.newsnap

import android.app.Activity
import androidx.camera.core.AspectRatio

class Configuration {
    companion object {
        const val KEY_CAPTURE_MODE = "capture_mode"
        const val DEFAULT_ASPECT_RATIO = AspectRatio.RATIO_4_3

        var ASPECT_RATIO = DEFAULT_ASPECT_RATIO

        fun getAspectInt(ratio: String) : Int {
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

        fun getAspectString(ratio: Int) : String {
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
    }
}