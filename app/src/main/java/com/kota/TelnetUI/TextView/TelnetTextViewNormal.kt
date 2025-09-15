package com.kota.TelnetUI.TextView

import android.content.Context
import android.util.AttributeSet

class TelnetTextViewNormal @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : TelnetTextView(context, attrs, defStyle) {
    
    init {
        reloadTextSize()
    }
    
    override fun reloadTextSize() {
        setTextModelSize(TEXT_VIEW_SIZE_NORMAL)
    }
}
