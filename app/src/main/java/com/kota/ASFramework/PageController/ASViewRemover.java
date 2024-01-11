package com.kota.ASFramework.PageController;

import android.view.View;
import android.view.ViewGroup;
import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public class ASViewRemover {
  private ViewGroup _parent_view;
  private View _target_view;

  public static void remove(ViewGroup parentView, View targetView) {
    ASViewRemover remover = new ASViewRemover(parentView, targetView);
    remover.start();
  }

  public ASViewRemover(ViewGroup parentView, View targetView) {
    this._parent_view = null;
    this._target_view = null;
    this._parent_view = parentView;
    this._target_view = targetView;
  }

  public void start() {
    new ASRunner() { // from class: com.kumi.ASFramework.PageController.ASViewRemover.1
      @Override // com.kumi.ASFramework.Thread.ASRunner
      public void run() {
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        ASViewRemover.this.remove();
      }
    }.runInNewThread();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void remove() {
    new ASRunner() { // from class: com.kumi.ASFramework.PageController.ASViewRemover.2
      @Override // com.kumi.ASFramework.Thread.ASRunner
      public void run() {
        if (ASViewRemover.this._parent_view != null && ASViewRemover.this._target_view != null) {
          ASViewRemover.this._parent_view.removeView(ASViewRemover.this._target_view);
        }
      }
    }.runInMainThread();
  }
}