package com.kota.TelnetUI.TextView;

import android.content.Context;
import android.util.AttributeSet;

public class TelnetTextViewSmall extends TelnetTextView {
    public TelnetTextViewSmall(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        reloadTextSize();
    }

    public TelnetTextViewSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
        reloadTextSize();
    }

    public TelnetTextViewSmall(Context context) {
        super(context);
        reloadTextSize();
    }

    /* access modifiers changed from: protected */
    public void reloadTextSize() {
        setTextModelSize(-1);
    }
}
