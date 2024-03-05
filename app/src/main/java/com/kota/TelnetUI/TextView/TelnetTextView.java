package com.kota.TelnetUI.TextView;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class TelnetTextView extends AppCompatTextView {
    public static final int TEXT_VIEW_SIZE_LARGE = 1;
    public static final int TEXT_VIEW_SIZE_NORMAL = 0;
    public static final int TEXT_VIEW_SIZE_SMALL = -1;
    public static final int TEXT_VIEW_SIZE_ULTRA_LARGE = 2;
    private static final float text_size_large = 24.0f;
    private static final float text_size_normal = 20.0f;
    private static final float text_size_small = 16.0f;
    private static final float text_size_ultra_large = 28.0f;

    public TelnetTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TelnetTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TelnetTextView(Context context) {
        super(context);
    }

    protected void reloadTextSize() {
    }

    protected void setTextModelSize(int size) {
        float text_scale = 1.0f;
        float text_scale_weight = 0.0f;
        if (size == TEXT_VIEW_SIZE_SMALL) {
            setTextSize(2, (text_size_small * text_scale) + text_scale_weight);
        } else if (size == TEXT_VIEW_SIZE_LARGE) {
            setTextSize(2, (text_size_large * text_scale) + text_scale_weight);
        } else if (size == TEXT_VIEW_SIZE_ULTRA_LARGE) {
            setTextSize(2, (text_size_ultra_large * text_scale) + text_scale_weight);
        } else {
            setTextSize(2, (text_size_normal * text_scale) + text_scale_weight);
        }
    }
}
