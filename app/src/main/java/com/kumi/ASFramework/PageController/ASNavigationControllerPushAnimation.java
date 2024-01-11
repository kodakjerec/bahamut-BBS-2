package com.kumi.ASFramework.PageController;

import android.view.animation.Animation;
import com.kumi.ASFramework.Thread.ASRunner;

public class ASNavigationControllerPushAnimation {
  private boolean _finished = false;
  
  private Animation _source_animation = null;
  
  private ASViewController _source_controller = null;
  
  private boolean _source_finished = false;
  
  private Animation _target_animation = null;
  
  private ASViewController _target_controller = null;
  
  private boolean _target_finished = false;
  
  private int animation_duration = 250;
  
  public ASNavigationControllerPushAnimation(ASViewController paramASViewController1, ASViewController paramASViewController2) {
    this._source_controller = paramASViewController1;
    this._target_controller = paramASViewController2;
  }
  
  private void animate() {
    // Byte code:
    //   0: aload_0
    //   1: getfield _source_controller : Lcom/kumi/ASFramework/PageController/ASViewController;
    //   4: ifnull -> 175
    //   7: aload_0
    //   8: getfield _source_controller : Lcom/kumi/ASFramework/PageController/ASViewController;
    //   11: invokevirtual getPageView : ()Lcom/kumi/ASFramework/PageController/ASPageView;
    //   14: ifnull -> 175
    //   17: aload_0
    //   18: invokestatic getFadeOutToLeftAnimation : ()Landroid/view/animation/Animation;
    //   21: putfield _source_animation : Landroid/view/animation/Animation;
    //   24: aload_0
    //   25: getfield _source_animation : Landroid/view/animation/Animation;
    //   28: invokestatic getCurrentController : ()Lcom/kumi/ASFramework/PageController/ASNavigationController;
    //   31: ldc 17432580
    //   33: invokevirtual setInterpolator : (Landroid/content/Context;I)V
    //   36: aload_0
    //   37: getfield _source_animation : Landroid/view/animation/Animation;
    //   40: aload_0
    //   41: getfield animation_duration : I
    //   44: i2l
    //   45: invokevirtual setDuration : (J)V
    //   48: aload_0
    //   49: getfield _source_animation : Landroid/view/animation/Animation;
    //   52: new com/kumi/ASFramework/PageController/ASNavigationControllerPushAnimation$1
    //   55: dup
    //   56: aload_0
    //   57: invokespecial <init> : (Lcom/kumi/ASFramework/PageController/ASNavigationControllerPushAnimation;)V
    //   60: invokevirtual setAnimationListener : (Landroid/view/animation/Animation$AnimationListener;)V
    //   63: aload_0
    //   64: getfield _source_controller : Lcom/kumi/ASFramework/PageController/ASViewController;
    //   67: invokevirtual getPageView : ()Lcom/kumi/ASFramework/PageController/ASPageView;
    //   70: invokevirtual onPageAnimationStart : ()V
    //   73: aload_0
    //   74: getfield _source_controller : Lcom/kumi/ASFramework/PageController/ASViewController;
    //   77: invokevirtual getPageView : ()Lcom/kumi/ASFramework/PageController/ASPageView;
    //   80: aload_0
    //   81: getfield _source_animation : Landroid/view/animation/Animation;
    //   84: invokevirtual startAnimation : (Landroid/view/animation/Animation;)V
    //   87: aload_0
    //   88: getfield _target_controller : Lcom/kumi/ASFramework/PageController/ASViewController;
    //   91: ifnull -> 196
    //   94: aload_0
    //   95: getfield _target_controller : Lcom/kumi/ASFramework/PageController/ASViewController;
    //   98: invokevirtual getPageView : ()Lcom/kumi/ASFramework/PageController/ASPageView;
    //   101: ifnull -> 196
    //   104: aload_0
    //   105: invokestatic getFadeInFromRightAnimation : ()Landroid/view/animation/Animation;
    //   108: putfield _target_animation : Landroid/view/animation/Animation;
    //   111: aload_0
    //   112: getfield _target_animation : Landroid/view/animation/Animation;
    //   115: invokestatic getCurrentController : ()Lcom/kumi/ASFramework/PageController/ASNavigationController;
    //   118: ldc 17432580
    //   120: invokevirtual setInterpolator : (Landroid/content/Context;I)V
    //   123: aload_0
    //   124: getfield _target_animation : Landroid/view/animation/Animation;
    //   127: aload_0
    //   128: getfield animation_duration : I
    //   131: i2l
    //   132: invokevirtual setDuration : (J)V
    //   135: aload_0
    //   136: getfield _target_animation : Landroid/view/animation/Animation;
    //   139: new com/kumi/ASFramework/PageController/ASNavigationControllerPushAnimation$2
    //   142: dup
    //   143: aload_0
    //   144: invokespecial <init> : (Lcom/kumi/ASFramework/PageController/ASNavigationControllerPushAnimation;)V
    //   147: invokevirtual setAnimationListener : (Landroid/view/animation/Animation$AnimationListener;)V
    //   150: aload_0
    //   151: getfield _target_controller : Lcom/kumi/ASFramework/PageController/ASViewController;
    //   154: invokevirtual getPageView : ()Lcom/kumi/ASFramework/PageController/ASPageView;
    //   157: invokevirtual onPageAnimationStart : ()V
    //   160: aload_0
    //   161: getfield _target_controller : Lcom/kumi/ASFramework/PageController/ASViewController;
    //   164: invokevirtual getPageView : ()Lcom/kumi/ASFramework/PageController/ASPageView;
    //   167: aload_0
    //   168: getfield _target_animation : Landroid/view/animation/Animation;
    //   171: invokevirtual startAnimation : (Landroid/view/animation/Animation;)V
    //   174: return
    //   175: aload_0
    //   176: monitorenter
    //   177: aload_0
    //   178: iconst_1
    //   179: putfield _source_finished : Z
    //   182: aload_0
    //   183: invokespecial checkFinished : ()V
    //   186: aload_0
    //   187: monitorexit
    //   188: goto -> 87
    //   191: astore_1
    //   192: aload_0
    //   193: monitorexit
    //   194: aload_1
    //   195: athrow
    //   196: aload_0
    //   197: monitorenter
    //   198: aload_0
    //   199: iconst_1
    //   200: putfield _target_finished : Z
    //   203: aload_0
    //   204: invokespecial checkFinished : ()V
    //   207: aload_0
    //   208: monitorexit
    //   209: goto -> 174
    //   212: astore_1
    //   213: aload_0
    //   214: monitorexit
    //   215: aload_1
    //   216: athrow
    // Exception table:
    //   from	to	target	type
    //   177	188	191	finally
    //   192	194	191	finally
    //   198	209	212	finally
    //   213	215	212	finally
  }
  
  private void checkFinished() {
    if (this._source_finished && this._target_finished)
      finish(); 
  }
  
  private void finish() {
    if (!this._finished) {
      (new ASRunner() {
          final ASNavigationControllerPushAnimation this$0;
          
          public void run() {
            ASNavigationControllerPushAnimation.this.onAnimationFinished();
          }
        }).runInMainThread();
      this._finished = true;
    } 
  }
  
  public void onAnimationFinished() {}
  
  public void start(boolean paramBoolean) {
    if (!paramBoolean) {
      finish();
      return;
    } 
    animate();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASNavigationControllerPushAnimation.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */