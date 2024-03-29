package com.kota.ASFramework.PageController;

import android.view.View;
import android.view.ViewGroup;
import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public class ASViewRemover {
  private final ViewGroup _parent_view;
  private final View _target_view;

  public static void remove(ViewGroup parentView, View targetView) {
    ASViewRemover remover = new ASViewRemover(parentView, targetView);
    remover.start();
  }

  public ASViewRemover(ViewGroup parentView, View targetView) {
    this._parent_view = parentView;
    this._target_view = targetView;
  }

  public void start() {
    ASRunner.runInNewThread(()->{
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        ASViewRemover.this.remove();
    });
  }

  private void remove() {
    new ASRunner() { // from class: com.kota.ASFramework.PageController.ASViewRemover.2
      @Override // com.kota.ASFramework.Thread.ASRunner
      public void run() {
        if (ASViewRemover.this._parent_view != null && ASViewRemover.this._target_view != null) {
          ASViewRemover.this._parent_view.removeView(ASViewRemover.this._target_view);
        }
      }
    }.runInMainThread();
  }
}