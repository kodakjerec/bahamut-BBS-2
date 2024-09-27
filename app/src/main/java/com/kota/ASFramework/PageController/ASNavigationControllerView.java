package com.kota.ASFramework.PageController;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.kota.ASFramework.Model.ASSize;

/* loaded from: classes.dex */
public class ASNavigationControllerView extends ASPageView implements ASGestureViewDelegate {
  private ASPageView _background_view;
  private ASSize _content_size;
  private ASPageView _content_view;
  private ASGestureView _gesture_view;
  private ASNavigationController _page_controller;

  public ASNavigationControllerView(Context context) {
    super(context);
    this._background_view = null;
    this._content_view = null;
    this._gesture_view = null;
    this._content_size = new ASSize(0, 0);
    this._page_controller = null;
    initial(context);
  }

  public ASNavigationControllerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this._background_view = null;
    this._content_view = null;
    this._gesture_view = null;
    this._content_size = new ASSize(0, 0);
    this._page_controller = null;
    initial(context);
  }

  public ASNavigationControllerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    this._background_view = null;
    this._content_view = null;
    this._gesture_view = null;
    this._content_size = new ASSize(0, 0);
    this._page_controller = null;
    initial(context);
  }

  private void initial(Context context) {
    this._background_view = new ASPageView(context);
    this._background_view.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
    addView(this._background_view);
    this._content_view = new ASPageView(context);
    this._content_view.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
    addView(this._content_view);
    this._gesture_view = new ASGestureView(context);
    this._gesture_view.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
    this._gesture_view.setDelegate(this);
    addView(this._gesture_view);
  }

  public ASPageView getBackgroundView() {
    return this._background_view;
  }

  public ASPageView getContentView() {
    return this._content_view;
  }

  public ASSize getContentSize() {
    return this._content_size;
  }

  public void setPageController(ASNavigationController aController) {
    this._page_controller = aController;
  }

  @Override // android.view.View
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (this._page_controller != null) {
      this._page_controller.onSizeChanged(w, h, oldw, oldh);
    }
  }

  @Override // com.kota.ASFramework.PageController.ASGestureViewDelegate
  public void onASGestureDispathTouchEvent(MotionEvent event) {
    this._content_view.dispatchTouchEvent(event);
  }

  @Override // com.kota.ASFramework.PageController.ASGestureViewDelegate
  public boolean onASGestureReceivedGestureUp() {
    if (this._page_controller != null) {
      return this._page_controller.onReceivedGestureUp();
    }
    return false;
  }

  @Override // com.kota.ASFramework.PageController.ASGestureViewDelegate
  public boolean onASGestureReceivedGestureDown() {
    if (this._page_controller != null) {
      return this._page_controller.onReceivedGestureDown();
    }
    return false;
  }

  @Override // com.kota.ASFramework.PageController.ASGestureViewDelegate
  public boolean onASGestureReceivedGestureLeft() {
    if (this._page_controller != null) {
      return this._page_controller.onReceivedGestureLeft();
    }
    return false;
  }

  @Override // com.kota.ASFramework.PageController.ASGestureViewDelegate
  public boolean onASGestureReceivedGestureRight() {
    if (this._page_controller != null) {
      return this._page_controller.onReceivedGestureRight();
    }
    return false;
  }
}
