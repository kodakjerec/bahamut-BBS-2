package com.kumi.ASFramework.PageController;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ASPageView extends FrameLayout {
  private static int _count = 0;
  
  private ASViewController _owner_controller = null;
  
  public ASPageView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public ASPageView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  public ASPageView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
    init();
  }
  
  private void init() {}
  
  public void draw(Canvas paramCanvas) {
    int i = paramCanvas.getSaveCount();
    dispatchDraw(paramCanvas);
    paramCanvas.restoreToCount(i);
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public ASViewController getOwnerController() {
    return this._owner_controller;
  }
  
  protected void onDraw(Canvas paramCanvas) {}
  
  public void onPageAnimationFinished() {}
  
  public void onPageAnimationStart() {}
  
  public void setOwnerController(ASViewController paramASViewController) {
    this._owner_controller = paramASViewController;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASPageView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */