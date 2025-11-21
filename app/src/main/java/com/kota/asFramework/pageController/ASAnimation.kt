package com.kota.asFramework.pageController

import android.R.anim
import android.content.Context
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

object ASAnimation {
    private const val ANIMATION_DURATION = 250

    val fadeInFromLeftAnimation: Animation
        get() {
            val translateAnimation =
                TranslateAnimation(1, -1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f)
            translateAnimation.duration = ANIMATION_DURATION.toLong()
            translateAnimation.setInterpolator(
                ASNavigationController.currentController as Context?,
                anim.accelerate_decelerate_interpolator
            )
            return translateAnimation as Animation
        }

    val fadeInFromRightAnimation: Animation
        get() {
            val translateAnimation =
                TranslateAnimation(1, 1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f)
            translateAnimation.duration = ANIMATION_DURATION.toLong()
            translateAnimation.setInterpolator(
                ASNavigationController.currentController as Context?,
                anim.accelerate_decelerate_interpolator
            )
            return translateAnimation as Animation
        }

    val fadeOutToLeftAnimation: Animation
        get() {
            val translateAnimation =
                TranslateAnimation(1, 0.0f, 2, -1.0f, 2, 0.0f, 2, 0.0f)
            translateAnimation.duration = ANIMATION_DURATION.toLong()
            translateAnimation.setInterpolator(
                ASNavigationController.currentController as Context?,
                anim.accelerate_decelerate_interpolator
            )
            return translateAnimation as Animation
        }

    val fadeOutToRightAnimation: Animation
        get() {
            val translateAnimation =
                TranslateAnimation(1, 0.0f, 2, 1.0f, 2, 0.0f, 2, 0.0f)
            translateAnimation.duration = ANIMATION_DURATION.toLong()
            translateAnimation.setInterpolator(
                ASNavigationController.currentController as Context?,
                anim.accelerate_decelerate_interpolator
            )
            return translateAnimation as Animation
        }
}


