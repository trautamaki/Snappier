package app.newsnap

class Configuration {
    companion object {
        const val KEY_CAPTURE_MODE = "capture_mode"

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
    }
}