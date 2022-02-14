package org.snappier.camera

import androidx.camera.core.AspectRatio

class Configuration {
    companion object {
        const val KEY_CAPTURE_MODE = "capture_mode"

        const val DEFAULT_ASPECT_RATIO = AspectRatio.RATIO_4_3
        var ASPECT_RATIO = DEFAULT_ASPECT_RATIO

        const val ID_PICTURE_CAMERA = 0
        const val ID_VIDEO_CAMERA = 1
        const val ID_NIGHT = 2
        const val ID_BOKEH = 3

        const val FILE_NAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val IMAGE_RELATIVE_PATH = "DCIM/Camera"

        var supportedCameraModes: Array<Int> = arrayOf(
            ID_PICTURE_CAMERA,
            ID_VIDEO_CAMERA,
        )

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