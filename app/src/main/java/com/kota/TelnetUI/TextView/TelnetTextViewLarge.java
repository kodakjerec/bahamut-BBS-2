package com.kota.TelnetUI.TextView;

import android.content.Context;
import android.util.AttributeSet;

public class TelnetTextViewLarge extends TelnetTextView {
    public TelnetTextViewLarge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        reloadTextSize();
    }

    public TelnetTextViewLarge(Context context, AttributeSet attrs) {
        super(context, attrs);
        reloadTextSize();
    }

    public TelnetTextViewLarge(Context context) {
        super(context);
        reloadTextSize();
    }

    /* access modifiers changed from: protected */
    public void reloadTextSize() {
        setTextModelSize(1);
    }
}
