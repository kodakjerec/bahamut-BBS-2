package com.kota.ASFramework.Dialog;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import androidx.core.view.ViewCompat;

public class ASLayoutParams {
    private static ASLayoutParams _instance = null;
    private final float _default_touch_block_height = 60.0f;
    private final float _default_touch_block_width = 60.0f;
    private final float _dialog_width_large = 320.0f;
    private final float _dialog_width_normal = 270.0f;
    private final float _padding_large = 20.0f;
    private final float _padding_normal = 10.0f;
    private final float _padding_small = 5.0f;
    private final float _text_size_large = 24.0f;
    private final float _text_size_normal = 20.0f;
    private final float _text_size_small = 16.0f;
    private final float _text_size_ultra_large = 28.0f;

    private ASLayoutParams() {
        initial();
    }

    public static ASLayoutParams getInstance() {
        if (_instance == null) {
            _instance = new ASLayoutParams();
        }
        return _instance;
    }

    private void initial() {
    }

    public float getTextSizeSmall() {
        return this._text_size_small;
    }

    public float getTextSizeNormal() {
        return this._text_size_normal;
    }

    public float getTextSizeLarge() {
        return this._text_size_large;
    }

    public float getTextSizeUltraLarge() {
        return this._text_size_ultra_large;
    }

    public float getDialogWidthNormal() {
        return this._dialog_width_normal;
    }

    public float getDialogWidthLarge() {
        return this._dialog_width_large;
    }

    public float getDefaultTouchBlockWidth() {
        return this._default_touch_block_width;
    }

    public float getDefaultTouchBlockHeight() {
        return this._default_touch_block_height;
    }

    public float getPaddingSmall() {
        return this._padding_small;
    }

    public float getPaddingNormal() {
        return this._padding_normal;
    }

    public float getPaddingLarge() {
        return this._padding_large;
    }

    public Drawable getAlertItemBackgroundDrawable() {
        StateListDrawable alert_item_background = new StateListDrawable();
        alert_item_background.addState(new int[]{16842919, 16842910}, new ColorDrawable(-14066));
        alert_item_background.addState(new int[]{16842910, 16842908}, new ColorDrawable(-8388608));
        alert_item_background.addState(new int[]{16842910}, new ColorDrawable(-12582912));
        alert_item_background.addState(new int[0], new ColorDrawable(-14680064));
        return alert_item_background;
    }

    public ColorStateList getAlertItemTextColor() {
        return new ColorStateList(new int[][]{new int[]{16842919, 16842910}, new int[]{16842910, 16842908}, new int[]{16842910}, new int[0]}, new int[]{ViewCompat.MEASURED_STATE_MASK, ViewCompat.MEASURED_STATE_MASK, -1, -8355712});
    }

    public Drawable getListItemBackgroundDrawable() {
        StateListDrawable alert_item_background = new StateListDrawable();
        alert_item_background.addState(new int[]{16842919, 16842910}, new ColorDrawable(-14066));
        alert_item_background.addState(new int[]{16842910, 16842908}, new ColorDrawable(ViewCompat.MEASURED_STATE_MASK));
        alert_item_background.addState(new int[]{16842910}, new ColorDrawable(ViewCompat.MEASURED_STATE_MASK));
        alert_item_background.addState(new int[0], new ColorDrawable(ViewCompat.MEASURED_STATE_MASK));
        return alert_item_background;
    }

    public ColorStateList getListItemTextColor() {
        return new ColorStateList(new int[][]{new int[]{16842919, 16842910}, new int[]{16842910, 16842908}, new int[]{16842910}, new int[0]}, new int[]{ViewCompat.MEASURED_STATE_MASK, ViewCompat.MEASURED_STATE_MASK, -1, -8355712});
    }
}
