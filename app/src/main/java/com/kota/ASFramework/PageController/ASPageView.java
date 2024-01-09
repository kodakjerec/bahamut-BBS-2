package com.kota.ASFramework.PageController;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ASPageView extends FrameLayout {
    private static final int _count = 0;
    private ASViewController _owner_controller = null;

    /* access modifiers changed from: protected */
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public ASPageView(Context context) {
        super(context);
        init();
    }

    public ASPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ASPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
    }

    public void setOwnerController(ASViewController aController) {
        this._owner_controller = aController;
    }

    public ASViewController getOwnerController() {
        return this._owner_controller;
    }

    public void draw(Canvas canvas) {
        int save_count = canvas.getSaveCount();
        dispatchDraw(canvas);
        canvas.restoreToCount(save_count);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas aCanvas) {
    }

    public void onPageAnimationStart() {
    }

    public void onPageAnimationFinished() {
    }
}
