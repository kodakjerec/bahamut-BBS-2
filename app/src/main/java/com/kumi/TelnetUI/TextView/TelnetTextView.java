package com.kumi.TelnetUI.TextView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class TelnetTextView extends TextView {
  public static final int TEXT_VIEW_SIZE_LARGE = 1;
  
  public static final int TEXT_VIEW_SIZE_NORMAL = 0;
  
  public static final int TEXT_VIEW_SIZE_SMALL = -1;
  
  public static final int TEXT_VIEW_SIZE_ULTRA_LARGE = 2;
  
  private static final float text_size_large = 24.0F;
  
  private static final float text_size_normal = 20.0F;
  
  private static final float text_size_small = 16.0F;
  
  private static final float text_size_ultra_large = 28.0F;
  
  private float text_scale = 1.0F;
  
  private float text_scale_weight = 0.0F;
  
  public TelnetTextView(Context paramContext) {
    super(paramContext);
  }
  
  public TelnetTextView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
  }
  
  public TelnetTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
  }
  
  protected void reloadTextSize() {}
  
  protected void setTextModelSize(int paramInt) {
    if (paramInt == -1) {
      setTextSize(2, 16.0F * this.text_scale + this.text_scale_weight);
      return;
    } 
    if (paramInt == 1) {
      setTextSize(2, 24.0F * this.text_scale + this.text_scale_weight);
      return;
    } 
    if (paramInt == 2) {
      setTextSize(2, 28.0F * this.text_scale + this.text_scale_weight);
      return;
    } 
    setTextSize(2, 20.0F * this.text_scale + this.text_scale_weight);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TelnetUI\TextView\TelnetTextView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */