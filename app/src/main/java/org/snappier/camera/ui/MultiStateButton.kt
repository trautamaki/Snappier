package org.snappier.camera.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import org.snappier.camera.R

open class MultiStateButton(context: Context, attrs: AttributeSet) :
        AppCompatImageButton(context, attrs),
        View.OnClickListener {

    var buttonState = 0
    private lateinit var imageIds: IntArray

    init {
        initializeOnClick()
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.MultiStateButton,
                0, 0
        )

        setImageIds(a.getResourceId(R.styleable.MultiStateButton_imageIds, 0))
    }

    /**
     * Set the image array.
     *
     * @param res the resource ID of the array (R.array.example_array).
     */
    fun setImageIds(res: Int) {
        val ids = resources.obtainTypedArray(res)

        imageIds = IntArray(ids.length())
        for (i in 0 until ids.length()) {
            imageIds[i] = ids.getResourceId(i, 0)
        }
        ids.recycle()
    }

    /**
     * Set the button state and update the image.
     *
     * @param state new state. Must be between 0 and imageIds.size.
     */
    fun setState(state: Int) {
        if (state < 0 || state > imageIds.size) {
            return
        }

        buttonState = state
        setImageDrawable(ContextCompat.getDrawable(context, imageIds[state]))
    }

    private fun initializeOnClick() {
        setOnClickListener(this)
    }

    private fun nextState() {
        buttonState += +1
        if (buttonState >= imageIds.size) {
            buttonState = 0
        }
        setState(buttonState)
    }

    override fun onClick(v: View?) {
        nextState()
    }
}