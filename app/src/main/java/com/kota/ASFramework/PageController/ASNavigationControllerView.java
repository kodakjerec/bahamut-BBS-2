package com.kota.ASFramework.PageController;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.kota.ASFramework.Model.ASSize;

public class ASNavigationControllerView extends ASPageView implements ASGestureViewDelegate {
  private ASPageView _background_view = null;
  
  private final ASSize _content_size = new ASSize(0, 0);
  
  private ASPageView _content_view = null;

  private ASNavigationController _page_controller = null;
  
  public ASNavigationControllerView(Context paramContext) {
    super(paramContext);
    initial(paramContext);
  }
  
  public ASNavigationControllerView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    initial(paramContext);
  }
  
  public ASNavigationControllerView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
    initial(paramContext);
  }
  
  private void initial(Context paramContext) {
    this._background_view = new ASPageView(paramContext);
    this._background_view.setLayoutParams(new LayoutParams(-1, -1));
    addView(this._background_view);
    this._content_view = new ASPageView(paramContext);
    this._content_view.setLayoutParams(new LayoutParams(-1, -1));
    addView(this._content_view);
    ASGestureView _gesture_view = new ASGestureView(paramContext);
    _gesture_view.setLayoutParams(new LayoutParams(-1, -1));
    _gesture_view.setDelegate(this);
    addView(_gesture_view);
  }
  
  public ASPageView getBackgroundView() {
    return this._background_view;
  }
  
  public ASSize getContentSize() {
    return this._content_size;
  }
  
  public ASPageView getContentView() {
    return this._content_view;
  }
  
  public void onASGestureDispathTouchEvent(MotionEvent paramMotionEvent) {
    this._content_view.dispatchTouchEvent(paramMotionEvent);
  }
  
  public boolean onASGestureReceivedGestureDown() {
    return this._page_controller != null && this._page_controller.onReceivedGestureDown();
  }
  
  public boolean onASGestureReceivedGestureLeft() {
    return this._page_controller != null && this._page_controller.onReceivedGestureLeft();
  }
  
  public boolean onASGestureReceivedGestureRight() {
    return this._page_controller != null && this._page_controller.onReceivedGestureRight();
  }
  
  public boolean onASGestureReceivedGestureUp() {
    return this._page_controller != null && this._page_controller.onReceivedGestureUp();
  }
  
  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    if (this._page_controller != null)
      this._page_controller.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4); 
  }
  
  public void setPageController(ASNavigationController paramASNavigationController) {
    this._page_controller = paramASNavigationController;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASNavigationControllerView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */