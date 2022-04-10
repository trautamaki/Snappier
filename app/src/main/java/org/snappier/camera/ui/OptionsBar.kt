package org.snappier.camera.ui

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.snappier.camera.SettingsActivity
import org.snappier.camera.databinding.OptionsBarBinding

class OptionsBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes), View.OnClickListener {

    interface IOptionsBar {
        fun onFlashToggled(flashMode: Int)
        fun onOptionsBarClick()
        fun onAspectRatioChanged(aspectRatio: Int)
    }

    private var listener: IOptionsBar? = null
    private var binding: OptionsBarBinding =
        OptionsBarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.buttonFlashToggle.setOnClickListener(this)
        binding.buttonSettings.setOnClickListener(this)
        binding.buttonAspectRatio.setOnClickListener(this)
    }

    fun setOptionsBarListener(listener: IOptionsBar) {
        this.listener = listener
    }

    fun updateOptions(hasFlashUnit: Boolean) {
        if (!hasFlashUnit) {
            binding.buttonFlashToggle.visibility = View.GONE
        } else {
            binding.buttonFlashToggle.visibility = View.VISIBLE
        }
    }

    /**
     * Get the flash toggle button.
     *
     * @return the flash button.
     */
    fun getFlashButton(): MultiStateButton {
        return binding.buttonFlashToggle
    }

    /**
     * Get the aspect ratio toggle button.
     *
     * @return the aspect ratio button.
     */
    fun getAspectRatioButton(): MultiStateButton {
        return binding.buttonAspectRatio
    }

    override fun onClick(v: View?) {
        listener?.onOptionsBarClick()
        when (v) {
            binding.buttonFlashToggle -> {
                binding.buttonFlashToggle.onClick(v)
                listener?.onFlashToggled(binding.buttonFlashToggle.buttonState)
            }
            binding.buttonFlashToggle -> {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
            binding.buttonAspectRatio -> {
                binding.buttonAspectRatio.onClick(v)
                listener?.onAspectRatioChanged(binding.buttonAspectRatio.buttonState)
            }
        }
    }

    /**
     * Enable/disable buttons in the options bar during video record.
     *
     * @param enable whether to enable or disable the buttons
     */
    fun enableOptionsForVideoRecord(enable: Boolean) {
        binding.buttonAspectRatio.isEnabled = enable
        binding.buttonSettings.isEnabled = enable
    }
}