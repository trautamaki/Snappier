package org.snappier.camera.utils

import android.view.Surface
import org.snappier.camera.utils.OrientationManager.DeviceOrientation

class Util {
    companion object {
        /**
         * Convert DeviceOrientation to Surface orientation.
         *
         * @param orientation orientation to convert.
         */
        fun convertDeviceOrientation(orientation: DeviceOrientation): Int {
            // The values reported by OrientationManager are different than what imageCapture wants.
            return when (orientation) {
                DeviceOrientation.CLOCKWISE_0 -> Surface.ROTATION_0
                DeviceOrientation.CLOCKWISE_90 -> Surface.ROTATION_270
                DeviceOrientation.CLOCKWISE_180 -> Surface.ROTATION_180
                DeviceOrientation.CLOCKWISE_270 -> Surface.ROTATION_90
            }
        }
    }
}