package com.kota.ASFramework.PageController;

import android.view.animation.Animation;
import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public class ASNavigationControllerPopAnimation {
  private ASViewController _source_controller;
  private ASViewController _target_controller;
  private int animation_duration = 250;
  private boolean _source_finished = false;
  private boolean _target_finished = false;
  private boolean _finished = false;
  private Animation _source_animation = null;
  private Animation _target_animation = null;

  public ASNavigationControllerPopAnimation(ASViewController aSourceController, ASViewController aTargetController) {
    this._source_controller = null;
    this._target_controller = null;
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
    if (this._source_controller != null && this._source_controller.getPageView() != null) {
      this._source_animation = ASPageAnimation.getFadeOutTtRightAnimation();
      this._source_animation.setDuration(this.animation_duration);this._source_animation.setAnimationListener(new Animation.AnimationListener() { // from class: com.kumi.ASFramework.PageController.ASNavigationControllerPopAnimation.1
        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
          synchronized (ASNavigationControllerPopAnimation.this) {
            ASNavigationControllerPopAnimation.this._source_controller.getPageView().onPageAnimationFinished();
            ASNavigationControllerPopAnimation.this._source_finished = true;
            ASNavigationControllerPopAnimation.this.checkFinished();
          }
        }
      });
      this._source_controller.getPageView().onPageAnimationStart();
      this._source_controller.getPageView().startAnimation(this._source_animation);
    } else {
      synchronized (this) {
        this._source_finished = true;
        checkFinished();
      }
    }
    if (this._target_controller != null && this._target_controller.getPageView() != null) {
      this._target_animation = ASPageAnimation.getFadeInFromLeftAnimation();
      this._target_animation.setDuration(this.animation_duration);
      this._target_animation.setAnimationListener(new Animation.AnimationListener() { // from class: com.kumi.ASFramework.PageController.ASNavigationControllerPopAnimation.2
        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
          synchronized (ASNavigationControllerPopAnimation.this) {
            ASNavigationControllerPopAnimation.this._target_controller.getPageView().onPageAnimationFinished();
            ASNavigationControllerPopAnimation.this._target_finished = true;
            ASNavigationControllerPopAnimation.this.checkFinished();
          }
        }
      });
      this._target_controller.getPageView().onPageAnimationStart();
      this._target_controller.getPageView().startAnimation(this._target_animation);
      return;
    }
    synchronized (this) {
      this._target_finished = true;
      checkFinished();
    }
  }

  /* JADX INFO: Access modifiers changed from: private */
  public void checkFinished() {
    if (this._source_finished && this._target_finished) {
      finish();
    }
  }

  private void finish() {
    if (!this._finished) {
      new ASRunner() { // from class: com.kumi.ASFramework.PageController.ASNavigationControllerPopAnimation.3
        @Override // com.kumi.ASFramework.Thread.ASRunner
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