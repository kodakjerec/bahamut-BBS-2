package com.kota.ASFramework.Dialog;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class ASLayoutParams {
  private static ASLayoutParams _instance = null;

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
    stateListDrawable.addState(new int[] { 16842919, 16842910 }, colorDrawable);
    colorDrawable = new ColorDrawable(-8388608);
    stateListDrawable.addState(new int[] { 16842910, 16842908 }, colorDrawable);
    colorDrawable = new ColorDrawable(-12582912);
    stateListDrawable.addState(new int[] { 16842910 }, colorDrawable);
    colorDrawable = new ColorDrawable(-14680064);
    stateListDrawable.addState(new int[0], colorDrawable);
    return stateListDrawable;
  }
  
  public ColorStateList getAlertItemTextColor() {
    return new ColorStateList(new int[][] { { 16842919, 16842910 }, { 16842910, 16842908 }, { 16842910 }, {} }, new int[] { -16777216, -16777216, -1, -8355712 });
  }
  
  public float getDefaultTouchBlockHeight() {
    return 60.0F;
  }
  
  public float getDefaultTouchBlockWidth() {
    return 60.0F;
  }
  
  public float getDialogWidthLarge() {
    return 320.0F;
  }
  
  public float getDialogWidthNormal() {
    return 270.0F;
  }
  
  public Drawable getListItemBackgroundDrawable() {
    StateListDrawable stateListDrawable = new StateListDrawable();
    ColorDrawable colorDrawable = new ColorDrawable(-14066);
    stateListDrawable.addState(new int[] { 16842919, 16842910 }, colorDrawable);
    colorDrawable = new ColorDrawable(-16777216);
    stateListDrawable.addState(new int[] { 16842910, 16842908 }, colorDrawable);
    colorDrawable = new ColorDrawable(-16777216);
    stateListDrawable.addState(new int[] { 16842910 }, colorDrawable);
    colorDrawable = new ColorDrawable(-16777216);
    stateListDrawable.addState(new int[0], colorDrawable);
    return stateListDrawable;
  }
  
  public ColorStateList getListItemTextColor() {
    return new ColorStateList(new int[][] { { 16842919, 16842910 }, { 16842910, 16842908 }, { 16842910 } }, new int[] { -16777216, -16777216, -1, -8355712 });
  }
  
  public float getPaddingLarge() {
    return 20.0F;
  }
  
  public float getPaddingNormal() {
    return 10.0F;
  }
  
  public float getPaddingSmall() {
    return 5.0F;
  }
  
  public float getTextSizeLarge() {
    return 24.0F;
  }
  
  public float getTextSizeNormal() {
    return 20.0F;
  }
  
  public float getTextSizeSmall() {
    return 16.0F;
  }
  
  public float getTextSizeUltraLarge() {
    return 28.0F;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASLayoutParams.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */