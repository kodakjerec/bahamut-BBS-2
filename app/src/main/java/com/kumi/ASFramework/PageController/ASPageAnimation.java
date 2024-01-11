package com.kumi.ASFramework.PageController;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class ASPageAnimation {
  private static Animation _fade_in_from_left = null;
  
  private static Animation _fade_in_from_right = null;
  
  private static Animation _fade_out_to_left = null;
  
  private static Animation _fade_out_to_right = null;
  
  public static Animation getFadeInFromLeftAnimation() {
    if (_fade_in_from_left == null) {
      _fade_in_from_left = (Animation)new TranslateAnimation(2, -1.0F, 2, 0.0F, 2, 0.0F, 2, 0.0F);
      init(_fade_in_from_left);
    } 
    return _fade_in_from_left;
  }
  
  public static Animation getFadeInFromRightAnimation() {
    if (_fade_in_from_right == null) {
      _fade_in_from_right = (Animation)new TranslateAnimation(2, 1.0F, 2, 0.0F, 2, 0.0F, 2, 0.0F);
      init(_fade_in_from_right);
    } 
    return _fade_in_from_right;
  }
  
  public static Animation getFadeOutToLeftAnimation() {
    if (_fade_out_to_left == null) {
      _fade_out_to_left = (Animation)new TranslateAnimation(2, 0.0F, 2, -1.0F, 2, 0.0F, 2, 0.0F);
      init(_fade_out_to_left);
    } 
    return _fade_out_to_left;
  }
  
  public static Animation getFadeOutTtRightAnimation() {
    if (_fade_out_to_right == null) {
      _fade_out_to_right = (Animation)new TranslateAnimation(2, 0.0F, 2, 1.0F, 2, 0.0F, 2, 0.0F);
      init(_fade_out_to_right);
    } 
    return _fade_out_to_right;
  }
  
  private static void init(Animation paramAnimation) {
    paramAnimation.setFillBefore(true);
    paramAnimation.setFillAfter(true);
    paramAnimation.setFillEnabled(true);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASPageAnimation.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */