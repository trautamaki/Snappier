package app.newsnap.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import app.newsnap.R
import kotlinx.android.synthetic.main.options_bar.view.*

class OptionsBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes), View.OnClickListener {

    interface IOptionsBar {
        fun onFlashToggled(flashMode: Int)
        fun onOptionsBarClick()
    }

    var listener: IOptionsBar? = null

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.options_bar, this, true)

        button_flash_toggle.setOnClickListener(this)
    }

    fun setOptionsBarListener(listener: IOptionsBar) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        listener?.onOptionsBarClick()
        if (v == button_flash_toggle) {
            button_flash_toggle.toggleMode()
            listener?.onFlashToggled(button_flash_toggle.getFlashMode())
        }
    }
}