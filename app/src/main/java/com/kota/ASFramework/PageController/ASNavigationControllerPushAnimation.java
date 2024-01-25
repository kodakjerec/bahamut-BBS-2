package com.kota.ASFramework.PageController;

import android.view.animation.Animation;

import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public class ASNavigationControllerPushAnimation {
  private final ASViewController _source_controller;
  private final ASViewController _target_controller;
  private boolean _source_finished = false;
  private boolean _target_finished = false;
  private boolean _finished = false;

  public ASNavigationControllerPushAnimation(ASViewController aSourceController, ASViewController aTargetController) {
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
    int animation_duration = 250;
    if (this._source_controller != null && this._source_controller.getPageView() != null) {
      Animation _source_animation = ASPageAnimation.getFadeOutToLeftAnimation();
      _source_animation.setDuration(animation_duration);
      _source_animation.setAnimationListener(new Animation.AnimationListener() { // from class: com.kumi.ASFramework.PageController.ASNavigationControllerPushAnimation.1
        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
          synchronized (ASNavigationControllerPushAnimation.this) {
            ASNavigationControllerPushAnimation.this._source_controller.getPageView().onPageAnimationFinished();
            ASNavigationControllerPushAnimation.this._source_finished = true;
            ASNavigationControllerPushAnimation.this.checkFinished();
          }
        }
      });
      this._source_controller.getPageView().onPageAnimationStart();
      this._source_controller.getPageView().startAnimation(_source_animation);
    } else {
      synchronized (this) {
        this._source_finished = true;
        checkFinished();
      }
    }
    if (this._target_controller != null && this._target_controller.getPageView() != null) {
      Animation _target_animation = ASPageAnimation.getFadeInFromRightAnimation();
      _target_animation.setDuration(animation_duration);
      _target_animation.setAnimationListener(new Animation.AnimationListener() { // from class: com.kumi.ASFramework.PageController.ASNavigationControllerPushAnimation.2
        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
          synchronized (ASNavigationControllerPushAnimation.this) {
            ASNavigationControllerPushAnimation.this._target_controller.getPageView().onPageAnimationFinished();
            ASNavigationControllerPushAnimation.this._target_finished = true;
            ASNavigationControllerPushAnimation.this.checkFinished();
          }
        }
      });
      this._target_controller.getPageView().onPageAnimationStart();
      this._target_controller.getPageView().startAnimation(_target_animation);
      return;
    }
    synchronized (this) {
      this._target_finished = true;
      checkFinished();
    }
  }

  private void checkFinished() {
    if (this._source_finished && this._target_finished) {
      finish();
    }
  }

  private void finish() {
    if (!this._finished) {
      new ASRunner() { // from class: com.kumi.ASFramework.PageController.ASNavigationControllerPushAnimation.3
        @Override // com.kumi.ASFramework.Thread.ASRunner
        public void run() {
          ASNavigationControllerPushAnimation.this.onAnimationFinished();
        }
      }.runInMainThread();
      this._finished = true;
    }
  }

  public void onAnimationFinished() {
  }
}