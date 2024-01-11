package com.kota.ASFramework.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;

public class ASScrollView extends LinearLayout implements GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
  private int _content_size_height = 0;
  
  private int _content_size_width = 0;
  
  private View _content_view = null;
  
  private ScaleGestureDetector _scale_detector = null;
  
  private GestureDetector _scroll_detector = null;
  
  public ASScrollView(Context paramContext) {
    super(paramContext);
    initial(paramContext);
  }
  
  public ASScrollView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    initial(paramContext);
  }
  
  private void initial(Context paramContext) {
    this._scroll_detector = new GestureDetector(paramContext, this);
    this._scale_detector = new ScaleGestureDetector(paramContext, this);
  }
  
  public boolean onDown(MotionEvent paramMotionEvent) {
    return true;
  }
  
  public boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    return false;
  }
  
  public void onLongPress(MotionEvent paramMotionEvent) {}
  
  public boolean onScale(ScaleGestureDetector paramScaleGestureDetector) {
    return true;
  }
  
  public boolean onScaleBegin(ScaleGestureDetector paramScaleGestureDetector) {
    return true;
  }
  
  public void onScaleEnd(ScaleGestureDetector paramScaleGestureDetector) {}
  
  public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    if (this._content_view == null)
      reload(); 
    if (this._content_view != null) {
      this._content_size_width = this._content_view.getWidth();
      this._content_size_height = this._content_view.getHeight();
      int i = getScrollX();
      int m = getScrollY();
      int j = (int)(i + paramFloat1);
      int k = this._content_size_width - getWidth();
      i = j;
      if (j > k)
        i = k; 
      j = i;
      if (i < 0)
        j = 0; 
      m = (int)(m + paramFloat2);
      k = this._content_size_height - getHeight();
      i = m;
      if (m > k)
        i = k; 
      k = i;
      if (i < 0)
        k = 0; 
      scrollTo(j, k);
    } 
    return false;
  }
  
  public void onShowPress(MotionEvent paramMotionEvent) {}
  
  public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
    return false;
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent) {
    return (paramMotionEvent.getPointerCount() == 2) ? this._scale_detector.onTouchEvent(paramMotionEvent) : this._scroll_detector.onTouchEvent(paramMotionEvent);
  }
  
  public void reload() {
    if (getChildCount() > 0)
      this._content_view = getChildAt(0); 
  }
  
  public void setContentSize(int paramInt1, int paramInt2) {
    this._content_size_width = paramInt1;
    this._content_size_height = paramInt2;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\UI\ASScrollView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */