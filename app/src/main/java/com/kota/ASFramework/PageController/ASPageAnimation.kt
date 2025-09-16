package com.kota.ASFramework.PageController

import android.view.animation.Animation
import android.view.animation.TranslateAnimation

/* loaded from: classes.dex */
object ASPageAnimation {
    private var _fade_in_from_left: Animation? = null
    private var _fade_in_from_right: Animation? = null
    private var _fade_out_to_left: Animation? = null
    private var _fade_out_to_right: Animation? = null

    private fun init(aAnimation: Animation) {
        aAnimation.setFillBefore(true)
        aAnimation.setFillAfter(true)
        aAnimation.setFillEnabled(true)
    }

    val fadeInFromLeftAnimation: Animation?
        get() {
            if (_fade_in_from_left == null) {
                _fade_in_from_left =
                    TranslateAnimation(2, -1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f)
                ASPageAnimation.init(_fade_in_from_left!!)
            }
            return _fade_in_from_left
        }

    val fadeInFromRightAnimation: Animation?
        get() {
            if (_fade_in_from_right == null) {
                _fade_in_from_right =
                    TranslateAnimation(2, 1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f)
                ASPageAnimation.init(_fade_in_from_right!!)
            }
            return _fade_in_from_right
        }

    val fadeOutToLeftAnimation: Animation?
        get() {
            if (_fade_out_to_left == null) {
                _fade_out_to_left =
                    TranslateAnimation(2, 0.0f, 2, -1.0f, 2, 0.0f, 2, 0.0f)
                ASPageAnimation.init(_fade_out_to_left!!)
            }
            return _fade_out_to_left
        }

    val fadeOutTtRightAnimation: Animation?
        get() {
            if (_fade_out_to_right == null) {
                _fade_out_to_right =
                    TranslateAnimation(2, 0.0f, 2, 1.0f, 2, 0.0f, 2, 0.0f)
                ASPageAnimation.init(_fade_out_to_right!!)
            }
            return _fade_out_to_right
        }
}
