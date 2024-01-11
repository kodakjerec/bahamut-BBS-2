package com.kota.ASFramework.Dialog;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class ASLayoutParams {
  private static ASLayoutParams _instance = null;
  
  private float _default_touch_block_height = 60.0F;
  
  private float _default_touch_block_width = 60.0F;
  
  private float _dialog_width_large = 320.0F;
  
  private float _dialog_width_normal = 270.0F;
  
  private float _padding_large = 20.0F;
  
  private float _padding_normal = 10.0F;
  
  private float _padding_small = 5.0F;
  
  private float _text_size_large = 24.0F;
  
  private float _text_size_normal = 20.0F;
  
  private float _text_size_small = 16.0F;
  
  private float _text_size_ultra_large = 28.0F;
  
  private ASLayoutParams() {
    initial();
  }
  
  public static ASLayoutParams getInstance() {
    if (_instance == null)
      _instance = new ASLayoutParams(); 
    return _instance;
  }
  
  private void initial() {}
  
  public Drawable getAlertItemBackgroundDrawable() {
    StateListDrawable stateListDrawable = new StateListDrawable();
    ColorDrawable colorDrawable = new ColorDrawable(-14066);
    stateListDrawable.addState(new int[] { 16842919, 16842910 }, (Drawable)colorDrawable);
    colorDrawable = new ColorDrawable(-8388608);
    stateListDrawable.addState(new int[] { 16842910, 16842908 }, (Drawable)colorDrawable);
    colorDrawable = new ColorDrawable(-12582912);
    stateListDrawable.addState(new int[] { 16842910 }, (Drawable)colorDrawable);
    colorDrawable = new ColorDrawable(-14680064);
    stateListDrawable.addState(new int[0], (Drawable)colorDrawable);
    return (Drawable)stateListDrawable;
  }
  
  public ColorStateList getAlertItemTextColor() {
    return new ColorStateList(new int[][] { { 16842919, 16842910 }, {}, { 16842910, 16842908 }, {}, { 16842910 }, {}, {} }, new int[] { -16777216, -16777216, -1, -8355712 });
  }
  
  public float getDefaultTouchBlockHeight() {
    return this._default_touch_block_height;
  }
  
  public float getDefaultTouchBlockWidth() {
    return this._default_touch_block_width;
  }
  
  public float getDialogWidthLarge() {
    return this._dialog_width_large;
  }
  
  public float getDialogWidthNormal() {
    return this._dialog_width_normal;
  }
  
  public Drawable getListItemBackgroundDrawable() {
    StateListDrawable stateListDrawable = new StateListDrawable();
    ColorDrawable colorDrawable = new ColorDrawable(-14066);
    stateListDrawable.addState(new int[] { 16842919, 16842910 }, (Drawable)colorDrawable);
    colorDrawable = new ColorDrawable(-16777216);
    stateListDrawable.addState(new int[] { 16842910, 16842908 }, (Drawable)colorDrawable);
    colorDrawable = new ColorDrawable(-16777216);
    stateListDrawable.addState(new int[] { 16842910 }, (Drawable)colorDrawable);
    colorDrawable = new ColorDrawable(-16777216);
    stateListDrawable.addState(new int[0], (Drawable)colorDrawable);
    return (Drawable)stateListDrawable;
  }
  
  public ColorStateList getListItemTextColor() {
    return new ColorStateList(new int[][] { { 16842919, 16842910 }, {}, { 16842910, 16842908 }, {}, { 16842910 }, {}, {} }, new int[] { -16777216, -16777216, -1, -8355712 });
  }
  
  public float getPaddingLarge() {
    return this._padding_large;
  }
  
  public float getPaddingNormal() {
    return this._padding_normal;
  }
  
  public float getPaddingSmall() {
    return this._padding_small;
  }
  
  public float getTextSizeLarge() {
    return this._text_size_large;
  }
  
  public float getTextSizeNormal() {
    return this._text_size_normal;
  }
  
  public float getTextSizeSmall() {
    return this._text_size_small;
  }
  
  public float getTextSizeUltraLarge() {
    return this._text_size_ultra_large;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASLayoutParams.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */