package com.kumi.TelnetUI.TextView;

import android.content.Context;
import android.util.AttributeSet;

public class TelnetTextViewSmall extends TelnetTextView {
  public TelnetTextViewSmall(Context paramContext) {
    super(paramContext);
    reloadTextSize();
  }
  
  public TelnetTextViewSmall(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    reloadTextSize();
  }
  
  public TelnetTextViewSmall(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
    reloadTextSize();
  }
  
  protected void reloadTextSize() {
    setTextModelSize(-1);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TelnetUI\TextView\TelnetTextViewSmall.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */