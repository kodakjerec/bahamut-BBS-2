package com.kota.ASFramework.PageController;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class ASAnimation {
  private static int animation_duration = 250;
  
  public static Animation getFadeInFromLeftAnimation() {
    TranslateAnimation translateAnimation = new TranslateAnimation(1, -1.0F, 2, 0.0F, 2, 0.0F, 2, 0.0F);
    translateAnimation.setDuration(animation_duration);
    translateAnimation.setInterpolator((Context)ASNavigationController.getCurrentController(), 17432580);
    return (Animation)translateAnimation;
  }
  
  public static Animation getFadeInFromRightAnimation() {
    TranslateAnimation translateAnimation = new TranslateAnimation(1, 1.0F, 2, 0.0F, 2, 0.0F, 2, 0.0F);
    translateAnimation.setDuration(animation_duration);
    translateAnimation.setInterpolator((Context)ASNavigationController.getCurrentController(), 17432580);
    return (Animation)translateAnimation;
  }
  
  public static Animation getFadeOutToLeftAnimation() {
    TranslateAnimation translateAnimation = new TranslateAnimation(1, 0.0F, 2, -1.0F, 2, 0.0F, 2, 0.0F);
    translateAnimation.setDuration(animation_duration);
    translateAnimation.setInterpolator((Context)ASNavigationController.getCurrentController(), 17432580);
    return (Animation)translateAnimation;
  }
  
  public static Animation getFadeOutToRightAnimation() {
    TranslateAnimation translateAnimation = new TranslateAnimation(1, 0.0F, 2, 1.0F, 2, 0.0F, 2, 0.0F);
    translateAnimation.setDuration(animation_duration);
    translateAnimation.setInterpolator((Context)ASNavigationController.getCurrentController(), 17432580);
    return (Animation)translateAnimation;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASAnimation.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */