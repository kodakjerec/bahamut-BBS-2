package com.kota.ASFramework.PageController

import android.R
import android.content.Context
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

object ASAnimation {
    private const val animation_duration = 250

    val fadeInFromLeftAnimation: Animation
        get() {
            val translateAnimation =
                TranslateAnimation(1, -1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f)
            translateAnimation.setDuration(animation_duration.toLong())
            translateAnimation.setInterpolator(
                ASNavigationController.Companion.getCurrentController() as Context?,
                R.anim.accelerate_decelerate_interpolator
            )
            return translateAnimation as Animation
        }

    val fadeInFromRightAnimation: Animation
        get() {
            val translateAnimation =
                TranslateAnimation(1, 1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f)
            translateAnimation.setDuration(animation_duration.toLong())
            translateAnimation.setInterpolator(
                ASNavigationController.Companion.getCurrentController() as Context?,
                R.anim.accelerate_decelerate_interpolator
            )
            return translateAnimation as Animation
        }

    val fadeOutToLeftAnimation: Animation
        get() {
            val translateAnimation =
                TranslateAnimation(1, 0.0f, 2, -1.0f, 2, 0.0f, 2, 0.0f)
            translateAnimation.setDuration(animation_duration.toLong())
            translateAnimation.setInterpolator(
                ASNavigationController.Companion.getCurrentController() as Context?,
                R.anim.accelerate_decelerate_interpolator
            )
            return translateAnimation as Animation
        }

    val fadeOutToRightAnimation: Animation
        get() {
            val translateAnimation =
                TranslateAnimation(1, 0.0f, 2, 1.0f, 2, 0.0f, 2, 0.0f)
            translateAnimation.setDuration(animation_duration.toLong())
            translateAnimation.setInterpolator(
                ASNavigationController.Companion.getCurrentController() as Context?,
                R.anim.accelerate_decelerate_interpolator
            )
            return translateAnimation as Animation
        }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASAnimation.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


