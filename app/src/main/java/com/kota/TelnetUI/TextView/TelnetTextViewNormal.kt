package com.kota.TelnetUI.TextView

import android.content.Context
import android.util.AttributeSet

class TelnetTextViewNormal : TelnetTextView {
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        reloadTextSize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        reloadTextSize()
    }

    constructor(context: Context?) : super(context) {
        reloadTextSize()
    }

    /* access modifiers changed from: protected */
    public override fun reloadTextSize() {
        setTextModelSize(TelnetTextView.Companion.TEXT_VIEW_SIZE_NORMAL)
    }
}
