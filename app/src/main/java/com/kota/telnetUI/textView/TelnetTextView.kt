package com.kota.telnetUI.textView

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
        val textScale = 1.0f
        val textScaleWeight = 0.0f
        when (size) {
            TEXT_VIEW_SIZE_SMALL -> {
                setTextSize(2, (TEXT_SIZE_SMALL * textScale) + textScaleWeight)
            }
            TEXT_VIEW_SIZE_LARGE -> {
                setTextSize(2, (TEXT_SIZE_LARGE * textScale) + textScaleWeight)
            }
            TEXT_VIEW_SIZE_ULTRA_LARGE -> {
                setTextSize(2, (TEXT_SIZE_ULTRA_LARGE * textScale) + textScaleWeight)
            }
            else -> {
                setTextSize(2, (TEXT_SIZE_NORMAL * textScale) + textScaleWeight)
            }
        }
    }

    companion object {
        const val TEXT_VIEW_SIZE_LARGE: Int = 1
        const val TEXT_VIEW_SIZE_NORMAL: Int = 0
        const val TEXT_VIEW_SIZE_SMALL: Int = -1
        const val TEXT_VIEW_SIZE_ULTRA_LARGE: Int = 2
        private const val TEXT_SIZE_LARGE = 24.0f
        private const val TEXT_SIZE_NORMAL = 20.0f
        private const val TEXT_SIZE_SMALL = 16.0f
        private const val TEXT_SIZE_ULTRA_LARGE = 28.0f
    }
}
