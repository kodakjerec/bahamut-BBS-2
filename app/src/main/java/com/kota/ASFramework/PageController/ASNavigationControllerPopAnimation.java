package com.kota.ASFramework.PageController;

import android.view.animation.Animation;
import com.kota.ASFramework.Thread.ASRunner;

public class ASNavigationControllerPopAnimation {
    private boolean _finished = false;
    private Animation _source_animation = null;
    /* access modifiers changed from: private */
    public ASViewController _source_controller = null;
    /* access modifiers changed from: private */
    public boolean _source_finished = false;
    private Animation _target_animation = null;
    /* access modifiers changed from: private */
    public ASViewController _target_controller = null;
    /* access modifiers changed from: private */
    public boolean _target_finished = false;
    private final int animation_duration = 250;

    public ASNavigationControllerPopAnimation(ASViewController aSourceController, ASViewController aTargetController) {
        this._source_controller = aSourceController;
        this._target_controller = aTargetController;
    }

    public void start(boolean animated) {
        if (!animated) {
            finish();
        } else {
            animate();
        }
    }

    private void animate() {
        if (this._source_controller == null || this._source_controller.getPageView() == null) {
            synchronized (this) {
                this._source_finished = true;
                checkFinished();
            }
        } else {
            this._source_animation = ASPageAnimation.getFadeOutTtRightAnimation();
            this._source_animation.setDuration((long) this.animation_duration);
            this._source_animation.setInterpolator(ASNavigationController.getCurrentController(), 17432580);
            this._source_animation.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    synchronized (ASNavigationControllerPopAnimation.this) {
                        ASNavigationControllerPopAnimation.this._source_controller.getPageView().onPageAnimationFinished();
                        boolean unused = ASNavigationControllerPopAnimation.this._source_finished = true;
                        ASNavigationControllerPopAnimation.this.checkFinished();
                    }
                }
            });
            this._source_controller.getPageView().onPageAnimationStart();
            this._source_controller.getPageView().startAnimation(this._source_animation);
        }
        if (this._target_controller == null || this._target_controller.getPageView() == null) {
            synchronized (this) {
                this._target_finished = true;
                checkFinished();
            }
            return;
        }
        this._target_animation = ASPageAnimation.getFadeInFromLeftAnimation();
        this._target_animation.setInterpolator(ASNavigationController.getCurrentController(), 17432580);
        this._target_animation.setDuration((long) this.animation_duration);
        this._target_animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                synchronized (ASNavigationControllerPopAnimation.this) {
                    ASNavigationControllerPopAnimation.this._target_controller.getPageView().onPageAnimationFinished();
                    boolean unused = ASNavigationControllerPopAnimation.this._target_finished = true;
                    ASNavigationControllerPopAnimation.this.checkFinished();
                }
            }
        });
        this._target_controller.getPageView().onPageAnimationStart();
        this._target_controller.getPageView().startAnimation(this._target_animation);
    }

    /* access modifiers changed from: private */
    public void checkFinished() {
        if (this._source_finished && this._target_finished) {
            finish();
        }
    }

    private void finish() {
        if (!this._finished) {
            new ASRunner() {
                public void run() {
                    ASNavigationControllerPopAnimation.this.onAnimationFinished();
                }
            }.runInMainThread();
            this._finished = true;
        }
    }

    public void onAnimationFinished() {
    }
}
