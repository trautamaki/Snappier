package org.snappier.camera.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.snappier.camera.R
import kotlinx.android.synthetic.main.options_bar.view.*

import org.snappier.camera.SettingsActivity

import android.content.Intent

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

    var listener: IOptionsBar? = null

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.options_bar, this, true)

        button_flash_toggle.setOnClickListener(this)
        button_settings.setOnClickListener(this)
        button_aspect_ratio.setOnClickListener(this)
    }

    fun setOptionsBarListener(listener: IOptionsBar) {
        this.listener = listener
    }

    fun updateOptions(hasFlashUnit: Boolean) {
        if (!hasFlashUnit) {
            button_flash_toggle.visibility = View.GONE
        } else {
            button_flash_toggle.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        listener?.onOptionsBarClick()
        if (v == button_flash_toggle) {
            button_flash_toggle.toggleMode()
            listener?.onFlashToggled(button_flash_toggle.getFlashMode())
        } else if (v == button_settings) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        } else if (v == button_aspect_ratio) {
            button_aspect_ratio.toggleAspectRatio()
            listener?.onAspectRatioChanged(button_aspect_ratio.getAspectRatio())
        }
    }
}