package com.kota.ASFramework.PageController;

import android.view.View;
import android.view.animation.Animation;
import com.kota.ASFramework.Thread.ASRunner;

public abstract class ASAnimationRunner {
    Animation _animation = null;

    /* access modifiers changed from: package-private */
    public abstract View getTargetView();

    /* access modifiers changed from: package-private */
    public abstract void onAnimationStartFail();

    public ASAnimationRunner(Animation animation) {
        this._animation = animation;
    }

    public void start() {
        new ASRunner() {
            public void run() {
                int i = 0;
                boolean success = false;
                while (true) {
                    if (i >= 10) {
                        break;
                    } else if (ASAnimationRunner.this.getTargetView() != null) {
                        ASAnimationRunner.this.animate();
                        success = true;
                        break;
                    } else {
                        i++;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!success) {
                    ASAnimationRunner.this.fail();
                }
            }
        }.runInNewThread();
    }

    /* access modifiers changed from: private */
    public void fail() {
        new ASRunner() {
            public void run() {
                ASAnimationRunner.this.onAnimationStartFail();
            }
        }.runInMainThread();
    }

    /* access modifiers changed from: private */
    public void animate() {
        new ASRunner() {
            public void run() {
                View target_view = ASAnimationRunner.this.getTargetView();
                if (target_view != null) {
                    target_view.startAnimation(ASAnimationRunner.this._animation);
                }
            }
        }.runInMainThread();
    }
}
