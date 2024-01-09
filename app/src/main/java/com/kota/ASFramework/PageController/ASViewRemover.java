package com.kota.ASFramework.PageController;

import android.view.View;
import android.view.ViewGroup;
import com.kota.ASFramework.Thread.ASRunner;

public class ASViewRemover {
    /* access modifiers changed from: private */
    public ViewGroup _parent_view = null;
    /* access modifiers changed from: private */
    public View _target_view = null;

    public static void remove(ViewGroup parentView, View targetView) {
        new ASViewRemover(parentView, targetView).start();
    }

    public ASViewRemover(ViewGroup parentView, View targetView) {
        this._parent_view = parentView;
        this._target_view = targetView;
    }

    public void start() {
        new ASRunner() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ASViewRemover.this.remove();
            }
        }.runInNewThread();
    }

    /* access modifiers changed from: private */
    public void remove() {
        new ASRunner() {
            public void run() {
                if (ASViewRemover.this._parent_view != null && ASViewRemover.this._target_view != null) {
                    ASViewRemover.this._parent_view.removeView(ASViewRemover.this._target_view);
                }
            }
        }.runInMainThread();
    }
}
