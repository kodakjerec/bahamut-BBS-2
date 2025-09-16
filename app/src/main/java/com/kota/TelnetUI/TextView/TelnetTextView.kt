package com.kota.TelnetUI.TextView

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

open class TelnetTextView : AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)

    protected open fun reloadTextSize() {
    }

    protected fun setTextModelSize(size: Int) {
        val text_scale = 1.0f
        val text_scale_weight = 0.0f
        if (size == TEXT_VIEW_SIZE_SMALL) {
            setTextSize(2, (text_size_small * text_scale) + text_scale_weight)
        } else if (size == TEXT_VIEW_SIZE_LARGE) {
            setTextSize(2, (text_size_large * text_scale) + text_scale_weight)
        } else if (size == TEXT_VIEW_SIZE_ULTRA_LARGE) {
            setTextSize(2, (text_size_ultra_large * text_scale) + text_scale_weight)
        } else {
            setTextSize(2, (text_size_normal * text_scale) + text_scale_weight)
        }
    }

    companion object {
        const val TEXT_VIEW_SIZE_LARGE: Int = 1
        const val TEXT_VIEW_SIZE_NORMAL: Int = 0
        val TEXT_VIEW_SIZE_SMALL: Int = -1
        const val TEXT_VIEW_SIZE_ULTRA_LARGE: Int = 2
        private const val text_size_large = 24.0f
        private const val text_size_normal = 20.0f
        private const val text_size_small = 16.0f
        private const val text_size_ultra_large = 28.0f
    }
}
