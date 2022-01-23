package app.newsnap.camera

import androidx.camera.core.CameraSelector
import app.newsnap.Configuration

open class Camera {
    open var cameraModeId = Configuration.ID_PICTURE_CAMERA

    var lensFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    open fun startCamera() {}
}