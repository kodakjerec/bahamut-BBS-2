package com.kota.ASFramework.PageController;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/* loaded from: classes.dex */
public class ASPageView extends FrameLayout {
  private static int _count = 0;
  private ASViewController _owner_controller;

  protected void finalize() throws Throwable {
    super.finalize();
  }

  public ASPageView(Context context) {
    super(context);
    this._owner_controller = null;
    init();
  }

  public ASPageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    this._owner_controller = null;
    init();
  }

  public ASPageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this._owner_controller = null;
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

  @Override // android.view.View
  public void draw(Canvas canvas) {
    super.draw(canvas);
    int save_count = canvas.getSaveCount();
    dispatchDraw(canvas);
    canvas.restoreToCount(save_count);
  }

  @Override // android.view.View
  protected void onDraw(Canvas aCanvas) {
  }

  public void onPageAnimationStart() {
  }

  public void onPageAnimationFinished() {
  }
}
