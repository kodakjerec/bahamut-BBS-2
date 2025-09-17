package com.kota.asFramework.pageController

import android.view.animation.Animation
import android.view.animation.TranslateAnimation

object ASPageAnimation {
    private var fadeInFromLeft: Animation? = null
    private var fadeInFromRight: Animation? = null
    private var fadeOutToLeft: Animation? = null
    private var fadeOutToRight: Animation? = null

    private fun init(aAnimation: Animation) {
        aAnimation.fillBefore = true
        aAnimation.fillAfter = true
        aAnimation.isFillEnabled = true
    }

    val fadeInFromLeftAnimation: Animation?
        get() {
            if (fadeInFromLeft == null) {
                fadeInFromLeft =
                    TranslateAnimation(2, -1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f)
                init(fadeInFromLeft!!)
            }
            return fadeInFromLeft
        }

    val fadeInFromRightAnimation: Animation?
        get() {
            if (fadeInFromRight == null) {
                fadeInFromRight =
                    TranslateAnimation(2, 1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f)
                init(fadeInFromRight!!)
            }
            return fadeInFromRight
        }

    val fadeOutToLeftAnimation: Animation?
        get() {
            if (fadeOutToLeft == null) {
                fadeOutToLeft =
                    TranslateAnimation(2, 0.0f, 2, -1.0f, 2, 0.0f, 2, 0.0f)
                init(fadeOutToLeft!!)
            }
            return fadeOutToLeft
        }

    val fadeOutTtRightAnimation: Animation?
        get() {
            if (fadeOutToRight == null) {
                fadeOutToRight =
                    TranslateAnimation(2, 0.0f, 2, 1.0f, 2, 0.0f, 2, 0.0f)
                init(fadeOutToRight!!)
            }
            return fadeOutToRight
        }
}
