package com.kota.TelnetUI.TextView

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

open class TelnetTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {
    
    companion object {
        const val TEXT_VIEW_SIZE_LARGE = 1
        const val TEXT_VIEW_SIZE_NORMAL = 0
        const val TEXT_VIEW_SIZE_SMALL = -1
        const val TEXT_VIEW_SIZE_ULTRA_LARGE = 2
        
        private const val TEXT_SIZE_LARGE = 24.0f
        private const val TEXT_SIZE_NORMAL = 20.0f
        private const val TEXT_SIZE_SMALL = 16.0f
        private const val TEXT_SIZE_ULTRA_LARGE = 28.0f
    }
    
    protected open fun reloadTextSize() {
        // 子類別可以覆寫此方法
    }
    
    protected fun setTextModelSize(size: Int) {
        val textScale = 1.0f
        val textScaleWeight = 0.0f
        
        val finalTextSize = when (size) {
            TEXT_VIEW_SIZE_SMALL -> TEXT_SIZE_SMALL * textScale + textScaleWeight
            TEXT_VIEW_SIZE_LARGE -> TEXT_SIZE_LARGE * textScale + textScaleWeight
            TEXT_VIEW_SIZE_ULTRA_LARGE -> TEXT_SIZE_ULTRA_LARGE * textScale + textScaleWeight
            else -> TEXT_SIZE_NORMAL * textScale + textScaleWeight
        }
        
        setTextSize(2, finalTextSize)
    }
}
