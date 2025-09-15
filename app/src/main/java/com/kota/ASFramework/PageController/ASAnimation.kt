package com.kota.ASFramework.PageController

import android.content.Context
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

class ASAnimation {
    companion object {
        private const val ANIMATION_DURATION = 250

        fun getFadeInFromLeftAnimation(): Animation {
            val translateAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f
            )
            translateAnimation.duration = ANIMATION_DURATION.toLong()
            translateAnimation.setInterpolator(
                ASNavigationController.getCurrentController() as Context,
                android.R.anim.accelerate_decelerate_interpolator
            )
            return translateAnimation
        }

        fun getFadeInFromRightAnimation(): Animation {
            val translateAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f
            )
            translateAnimation.duration = ANIMATION_DURATION.toLong()
            translateAnimation.setInterpolator(
                ASNavigationController.getCurrentController() as Context,
                android.R.anim.accelerate_decelerate_interpolator
            )
            return translateAnimation
        }

        fun getFadeOutToLeftAnimation(): Animation {
            val translateAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f
            )
            translateAnimation.duration = ANIMATION_DURATION.toLong()
            translateAnimation.setInterpolator(
                ASNavigationController.getCurrentController() as Context,
                android.R.anim.accelerate_decelerate_interpolator
            )
            return translateAnimation
        }

        fun getFadeOutToRightAnimation(): Animation {
            val translateAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f
            )
            translateAnimation.duration = ANIMATION_DURATION.toLong()
            translateAnimation.setInterpolator(
                ASNavigationController.getCurrentController() as Context,
                android.R.anim.accelerate_decelerate_interpolator
            )
            return translateAnimation
        }
    }
}
