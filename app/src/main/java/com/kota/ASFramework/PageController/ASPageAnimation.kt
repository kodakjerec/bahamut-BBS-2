package com.kota.ASFramework.PageController

import android.view.animation.Animation
import android.view.animation.TranslateAnimation

/* loaded from: classes.dex */
class ASPageAnimation {
    companion object {
        private var _fadeInFromLeft: Animation? = null
        private var _fadeInFromRight: Animation? = null
        private var _fadeOutToLeft: Animation? = null
        private var _fadeOutToRight: Animation? = null

        private fun init(aAnimation: Animation) {
            aAnimation.fillBefore = true
            aAnimation.fillAfter = true
            aAnimation.isFillEnabled = true
        }

        fun getFadeInFromLeftAnimation(): Animation {
            if (_fadeInFromLeft == null) {
                _fadeInFromLeft = TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, -1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f
                )
                init(_fadeInFromLeft!!)
            }
            return _fadeInFromLeft!!
        }

        fun getFadeInFromRightAnimation(): Animation {
            if (_fadeInFromRight == null) {
                _fadeInFromRight = TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f
                )
                init(_fadeInFromRight!!)
            }
            return _fadeInFromRight!!
        }

        fun getFadeOutToLeftAnimation(): Animation {
            if (_fadeOutToLeft == null) {
                _fadeOutToLeft = TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, -1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f
                )
                init(_fadeOutToLeft!!)
            }
            return _fadeOutToLeft!!
        }

        fun getFadeOutTtRightAnimation(): Animation {
            if (_fadeOutToRight == null) {
                _fadeOutToRight = TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 1.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f
                )
                init(_fadeOutToRight!!)
            }
            return _fadeOutToRight!!
        }
    }
}
