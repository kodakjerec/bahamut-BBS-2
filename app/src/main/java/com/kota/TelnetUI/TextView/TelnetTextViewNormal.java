package com.kota.TelnetUI.TextView;

import android.content.Context;
import android.util.AttributeSet;

public class TelnetTextViewNormal extends TelnetTextView {
    public TelnetTextViewNormal(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        reloadTextSize();
    }

    public TelnetTextViewNormal(Context context, AttributeSet attrs) {
        super(context, attrs);
        reloadTextSize();
    }

    public TelnetTextViewNormal(Context context) {
        super(context);
        reloadTextSize();
    }

    /* access modifiers changed from: protected */
    public void reloadTextSize() {
        setTextModelSize(0);
    }
}
