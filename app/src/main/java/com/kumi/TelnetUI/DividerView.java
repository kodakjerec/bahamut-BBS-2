package com.kumi.TelnetUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DividerView extends View {
  Paint _paint = new Paint();
  
  public DividerView(Context paramContext) {
    super(paramContext);
  }
  
  public DividerView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
  }
  
  public DividerView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
  }
  
  protected void onDraw(Canvas paramCanvas) {
    super.onDraw(paramCanvas);
    float f1 = (float)Math.ceil((getWidth() / 80));
    float f2 = (float)Math.floor(((getWidth() - 79 * f1) / 2.0F));
    for (byte b = 0; b < 79; b++) {
      float f3 = b;
      float f4 = b;
      this._paint.setColor(-12566464);
      paramCanvas.drawLine(f2 + f3 * f1, 0.0F, f4 * f1 + f2 + f1, 0.0F, this._paint);
      b++;
      this._paint.setColor(0);
      paramCanvas.drawLine(f2 + b * f1, 0.0F, b * f1 + f2 + f1, 0.0F, this._paint);
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TelnetUI\DividerView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */