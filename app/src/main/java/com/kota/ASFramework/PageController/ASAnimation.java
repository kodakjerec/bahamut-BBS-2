package com.kota.ASFramework.PageController;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class ASAnimation {
    private static int animation_duration = 250;

    public static Animation getFadeOutToLeftAnimation() {
        Animation animation = new TranslateAnimation(1, 0.0f, 2, -1.0f, 2, 0.0f, 2, 0.0f);
        animation.setDuration((long) animation_duration);
        animation.setInterpolator(ASNavigationController.getCurrentController(), 17432580);
        return animation;
    }

    public static Animation getFadeOutToRightAnimation() {
        Animation animation = new TranslateAnimation(1, 0.0f, 2, 1.0f, 2, 0.0f, 2, 0.0f);
        animation.setDuration((long) animation_duration);
        animation.setInterpolator(ASNavigationController.getCurrentController(), 17432580);
        return animation;
    }

    public static Animation getFadeInFromLeftAnimation() {
        Animation animation = new TranslateAnimation(1, -1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f);
        animation.setDuration((long) animation_duration);
        animation.setInterpolator(ASNavigationController.getCurrentController(), 17432580);
        return animation;
    }

    public static Animation getFadeInFromRightAnimation() {
        Animation animation = new TranslateAnimation(1, 1.0f, 2, 0.0f, 2, 0.0f, 2, 0.0f);
        animation.setDuration((long) animation_duration);
        animation.setInterpolator(ASNavigationController.getCurrentController(), 17432580);
        return animation;
    }
}
