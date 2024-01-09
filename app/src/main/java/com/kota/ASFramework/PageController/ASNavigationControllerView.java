package com.kota.ASFramework.PageController;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.kota.ASFramework.Model.ASSize;

public class ASNavigationControllerView extends ASPageView implements ASGestureViewDelegate {
    private ASPageView _background_view = null;
    private ASSize _content_size = new ASSize(0, 0);
    private ASPageView _content_view = null;
    private ASGestureView _gesture_view = null;
    private ASNavigationController _page_controller = null;

    public ASNavigationControllerView(Context context) {
        super(context);
        initial(context);
    }

    public ASNavigationControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial(context);
    }

    public ASNavigationControllerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this._page_controller != null) {
            this._page_controller.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public void onASGestureDispathTouchEvent(MotionEvent event) {
        this._content_view.dispatchTouchEvent(event);
    }

    public boolean onASGestureReceivedGestureUp() {
        if (this._page_controller != null) {
            return this._page_controller.onReceivedGestureUp();
        }
        return false;
    }

    public boolean onASGestureReceivedGestureDown() {
        if (this._page_controller != null) {
            return this._page_controller.onReceivedGestureDown();
        }
        return false;
    }

    public boolean onASGestureReceivedGestureLeft() {
        if (this._page_controller != null) {
            return this._page_controller.onReceivedGestureLeft();
        }
        return false;
    }

    public boolean onASGestureReceivedGestureRight() {
        if (this._page_controller != null) {
            return this._page_controller.onReceivedGestureRight();
        }
        return false;
    }
}
