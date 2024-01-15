package com.kota.ASFramework.PageController;

import android.view.View;
import android.view.animation.Animation;
import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public abstract class ASAnimationRunner {
  Animation _animation;

  abstract View getTargetView();

  abstract void onAnimationStartFail();

  public ASAnimationRunner(Animation animation) {
    this._animation = null;
    this._animation = animation;
  }

  public void start() {
    ASRunner.runInNewThread(()->{
        int i = 0;
        boolean success = false;
        while (true) {
          if (i >= 10) {
            break;
          }
          View target_view = ASAnimationRunner.this.getTargetView();
          if (target_view != null) {
            ASAnimationRunner.this.animate();
            success = true;
            break;
          }
          i++;
          try {
            Thread.sleep(10L);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if (!success) {
          ASAnimationRunner.this.fail();
        }
    });
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void fail() {
    new ASRunner() { // from class: com.kumi.ASFramework.PageController.ASAnimationRunner.2
      @Override // com.kumi.ASFramework.Thread.ASRunner
      public void run() {
        ASAnimationRunner.this.onAnimationStartFail();
      }
    }.runInMainThread();
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void animate() {
    new ASRunner() { // from class: com.kumi.ASFramework.PageController.ASAnimationRunner.3
      @Override // com.kumi.ASFramework.Thread.ASRunner
      public void run() {
        View target_view = ASAnimationRunner.this.getTargetView();
        if (target_view != null) {
          target_view.startAnimation(ASAnimationRunner.this._animation);
        }
      }
    }.runInMainThread();
  }
}