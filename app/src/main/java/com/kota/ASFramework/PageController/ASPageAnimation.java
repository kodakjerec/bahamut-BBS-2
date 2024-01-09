package com.kota.ASFramework.PageController;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class ASPageAnimation {
    private static Animation _fade_in_from_left = null;
    private static Animation _fade_in_from_right = null;
    private static Animation _fade_out_to_left = null;
    private static Animation _fade_out_to_right = null;

    private static void init(Animation aAnimation) {
        aAnimation.setFillBefore(true);
        aAnimation.setFillAfter(true);
        aAnimation.setFillEnabled(true);
    }

    public static Animation getFadeInFromLeftAnimation() {
        if (_fade_in_from_left == null) {
            _fade_in_from_left = new TranslateAnimation(2, -1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f);
            init(_fade_in_from_left);
        }
        return _fade_in_from_left;
    }

    public static Animation getFadeInFromRightAnimation() {
        if (_fade_in_from_right == null) {
            _fade_in_from_right = new TranslateAnimation(2, 1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f);
            init(_fade_in_from_right);
        }
        return _fade_in_from_right;
    }

    public static Animation getFadeOutToLeftAnimation() {
        if (_fade_out_to_left == null) {
            _fade_out_to_left = new TranslateAnimation(2, 0.0f, 2, -1.0f, 2, 0.0f, 2, 0.0f);
            init(_fade_out_to_left);
        }
        return _fade_out_to_left;
    }

    public static Animation getFadeOutTtRightAnimation() {
        if (_fade_out_to_right == null) {
            _fade_out_to_right = new TranslateAnimation(2, 0.0f, 2, 1.0f, 2, 0.0f, 2, 0.0f);
            init(_fade_out_to_right);
        }
        return _fade_out_to_right;
    }
}
