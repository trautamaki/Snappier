package app.newsnap.capturer

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.VideoCapture
import app.newsnap.MainActivity
import kotlinx.android.synthetic.main.activity_main.*

class VideoCapturer(private val activity: MainActivity, private val lensFacing: Int) :
        Capturer(activity),
        VideoCapture.OnVideoSavedCallback {

    lateinit var videoCapture: VideoCapture

    var recording: Boolean = false

    init {
        build()
    }

    @SuppressLint("RestrictedApi")
    private fun build() {
        val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing).build()

        videoCapture = VideoCapture.Builder()
                .setTargetRotation(activity.previewView.display.rotation)
                .setCameraSelector(cameraSelector)
                .build()
    }

    @SuppressLint("RestrictedApi")
    fun startVideo() {
        // Create time-stamped output file to hold the image
        lastFile = createFile(outputDirectory, fileNameFormat, ".mp4")

        // Create output options object which contains file + metadata
        val outputOptions = VideoCapture.OutputFileOptions.Builder(lastFile!!).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        videoCapture.startRecording(
                outputOptions, executor, this
        )

        recording = true
        Log.d("NewSnap", "Start video record")
    }

    @SuppressLint("RestrictedApi")
    fun stopVideo() {
        videoCapture.stopRecording()
        recording = false
        Log.d("NewSnap", "Stop video record")
    }

    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
        val savedUri = Uri.fromFile(lastFile)
        val msg = "Video capture succeeded: $savedUri"
        Toast.makeText(activity.baseContext, msg, Toast.LENGTH_SHORT).show()
        Log.d(MainActivity.TAG, msg)
    }

    override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
        Log.e("NewSnap", "Video error: $message")
    }
}