package com.kota.ASFramework.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import com.kota.ASFramework.PageController.ASGestureView;

/* loaded from: classes.dex */
public class ASListView extends ListView implements GestureDetector.OnGestureListener {
  private GestureDetector _gesture_detector;
  private boolean _scroll_on_bottom;
  private boolean _scroll_on_top;
  public ASListViewExtentOptionalDelegate extendOptionalDelegate;
  public ASListViewOverscrollDelegate overscrollDelegate;

  public ASListView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    this._gesture_detector = null;
    this._scroll_on_top = false;
    this._scroll_on_bottom = false;
    this.extendOptionalDelegate = null;
    this.overscrollDelegate = null;
    init();
  }

  public ASListView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this._gesture_detector = null;
    this._scroll_on_top = false;
    this._scroll_on_bottom = false;
    this.extendOptionalDelegate = null;
    this.overscrollDelegate = null;
    init();
  }

  public ASListView(Context context) {
    super(context);
    this._gesture_detector = null;
    this._scroll_on_top = false;
    this._scroll_on_bottom = false;
    this.extendOptionalDelegate = null;
    this.overscrollDelegate = null;
    init();
  }

  private void init() {
    this._gesture_detector = new GestureDetector(getContext(), this);
  }

  @Override // android.widget.AbsListView, android.view.View
  public boolean onTouchEvent(MotionEvent event) {
    this._gesture_detector.onTouchEvent(event);
    if (event.getAction() == 0) {
      detectScrollPosition(event);
    }
    return super.onTouchEvent(event);
  }

  @Override // android.view.GestureDetector.OnGestureListener
  public boolean onDown(MotionEvent arg0) {
    return true;
  }

  @Override // android.view.GestureDetector.OnGestureListener
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    float distance_x = Math.abs(velocityX);
    float distance_y = Math.abs(velocityY);
    if (this.extendOptionalDelegate != null && distance_x > ASGestureView.filter && distance_x > ASGestureView.range * distance_y && getChildCount() > 0) {
      synchronized (this) {
        float position_y = e1.getY();
        int i = 0;
        while (true) {
          if (i >= getChildCount()) {
            break;
          }
          View child_view = getChildAt(i);
          if (child_view.getHeight() <= 0 || child_view.getTop() > position_y || child_view.getBottom() < position_y) {
            i++;
          } else {
            int index = getFirstVisiblePosition() + i;
            if (this.extendOptionalDelegate.onASListViewHandleExtentOptional(this, index)) {
              MotionEvent cancel_event = MotionEvent.obtain(e2);
              cancel_event.setAction(3);
              super.onTouchEvent(cancel_event);
            }
          }
        }
      }
    }
    if (this.overscrollDelegate != null && distance_y > ASGestureView.filter && distance_y > ASGestureView.range * distance_x && getChildCount() > 0) {
      synchronized (this) {
        if (velocityY < 0.0f) {
          if (this._scroll_on_bottom) {
            this.overscrollDelegate.onASListViewDelectedOverscrollTop(this);
            this._scroll_on_top = false;
            this._scroll_on_bottom = false;
          }
        }
        if (velocityY > 0.0f && this._scroll_on_top) {
          this.overscrollDelegate.onASListViewDelectedOverscrollBottom(this);
        }
        this._scroll_on_top = false;
        this._scroll_on_bottom = false;
      }
      return true;
    }
    return true;
  }

  @Override // android.view.GestureDetector.OnGestureListener
  public void onLongPress(MotionEvent e) {
  }

  @Override // android.view.GestureDetector.OnGestureListener
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    return false;
  }

  @Override // android.view.GestureDetector.OnGestureListener
  public void onShowPress(MotionEvent e) {
  }

  @Override // android.view.GestureDetector.OnGestureListener
  public boolean onSingleTapUp(MotionEvent e) {
    return false;
  }

  private void detectScrollPosition(MotionEvent event) {
    synchronized (this) {
      this._scroll_on_top = false;
      this._scroll_on_bottom = false;
      if (getChildCount() > 0) {
        if (getFirstVisiblePosition() == 0) {
          View first_child = getChildAt(0);
          if (first_child.getTop() >= 0) {
            this._scroll_on_top = true;
          }
        }
        if (getLastVisiblePosition() == getCount() - 1) {
          View last_child = getChildAt(getChildCount() - 1);
          if (last_child.getBottom() <= getHeight()) {
            this._scroll_on_bottom = true;
          }
        }
      } else {
        this._scroll_on_top = true;
        this._scroll_on_bottom = true;
      }
    }
  }
}