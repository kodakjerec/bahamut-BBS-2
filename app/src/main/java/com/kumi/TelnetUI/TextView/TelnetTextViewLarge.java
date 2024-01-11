package com.kumi.TelnetUI.TextView;

import android.content.Context;
import android.util.AttributeSet;

public class TelnetTextViewLarge extends TelnetTextView {
  public TelnetTextViewLarge(Context paramContext) {
    super(paramContext);
    reloadTextSize();
  }
  
  public TelnetTextViewLarge(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    reloadTextSize();
  }
  
  public TelnetTextViewLarge(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
    reloadTextSize();
  }
  
  protected void reloadTextSize() {
    setTextModelSize(1);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TelnetUI\TextView\TelnetTextViewLarge.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */