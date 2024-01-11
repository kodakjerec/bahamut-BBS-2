package com.kumi.ASFramework.PageController;

import android.content.Context;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ASGestureView extends FrameLayout implements GestureDetector.OnGestureListener {
  public static int filter = 550;
  
  public static float range = 2.4F;
  
  private ASGestureViewDelegate _delegate = null;
  
  private boolean _event_locked = false;
  
  private GestureDetector _gesture_detector = null;
  
  public ASGestureView(Context paramContext) {
    super(paramContext);
    this._gesture_detector = new GestureDetector(paramContext, this);
    filter = (int)TypedValue.applyDimension(1, 550.0F, paramContext.getResources().getDisplayMetrics());
  }
  
  public boolean onDown(MotionEvent paramMotionEvent) {
    return true;
  }
  
  public boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (this._delegate != null) {
      float f2 = Math.abs(paramFloat1);
      float f1 = Math.abs(paramFloat2);
      if (f2 > filter && f2 > range * f1) {
        if (paramFloat1 > 0.0F) {
          bool1 = this._delegate.onASGestureReceivedGestureRight();
        } else {
          bool1 = this._delegate.onASGestureReceivedGestureLeft();
        } 
      } else {
        bool1 = bool2;
        if (f1 > filter) {
          bool1 = bool2;
          if (f1 > range * f2)
            if (paramFloat2 > 0.0F) {
              bool1 = this._delegate.onASGestureReceivedGestureDown();
            } else {
              bool1 = this._delegate.onASGestureReceivedGestureUp();
            }  
        } 
      } 
    } 
    this._event_locked = bool1;
    if (bool1 && this._delegate != null) {
      paramMotionEvent1 = MotionEvent.obtain(paramMotionEvent2);
      paramMotionEvent1.setAction(3);
      this._delegate.onASGestureDispathTouchEvent(paramMotionEvent1);
      paramMotionEvent1.recycle();
    } 
    return bool1;
  }
  
  public void onLongPress(MotionEvent paramMotionEvent) {}
  
  public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    return false;
  }
  
  public void onShowPress(MotionEvent paramMotionEvent) {}
  
  public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
    return true;
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent) {
    this._gesture_detector.onTouchEvent(paramMotionEvent);
    if (!this._event_locked && this._delegate != null)
      this._delegate.onASGestureDispathTouchEvent(paramMotionEvent); 
    if (paramMotionEvent.getAction() == 1)
      this._event_locked = false; 
    return true;
  }
  
  public void setDelegate(ASGestureViewDelegate paramASGestureViewDelegate) {
    this._delegate = paramASGestureViewDelegate;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASGestureView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */