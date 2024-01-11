package com.kumi.ASFramework.PageController;

import android.view.MotionEvent;

public interface ASGestureViewDelegate {
  void onASGestureDispathTouchEvent(MotionEvent paramMotionEvent);
  
  boolean onASGestureReceivedGestureDown();
  
  boolean onASGestureReceivedGestureLeft();
  
  boolean onASGestureReceivedGestureRight();
  
  boolean onASGestureReceivedGestureUp();
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASGestureViewDelegate.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */