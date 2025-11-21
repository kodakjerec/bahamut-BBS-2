package com.kota.telnetUI.textView

import android.content.Context
import android.util.AttributeSet

class TelnetTextViewUltraLarge : TelnetTextView {
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
        reloadTextSize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        reloadTextSize()
    }

    constructor(context: Context?) : super(context!!) {
        reloadTextSize()
    }

    public override fun reloadTextSize() {
        setTextModelSize(TEXT_VIEW_SIZE_ULTRA_LARGE)
    }
}
