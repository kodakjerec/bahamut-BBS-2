package com.kota.ASFramework.PageController;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/* loaded from: classes.dex */
public class ASLinearLayout extends LinearLayout {
  public ASLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ASLinearLayout(Context context) {
    super(context);
  }

  public void notifyDataSetChanged() {
  }

  @Override // android.view.View, android.view.ViewParent
  public void requestLayout() {
    super.requestLayout();
  }
}
