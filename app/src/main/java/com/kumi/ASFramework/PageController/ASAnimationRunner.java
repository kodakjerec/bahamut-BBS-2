package com.kumi.ASFramework.PageController;

import android.view.View;
import android.view.animation.Animation;
import com.kumi.ASFramework.Thread.ASRunner;

public abstract class ASAnimationRunner {
  Animation _animation = null;
  
  public ASAnimationRunner(Animation paramAnimation) {
    this._animation = paramAnimation;
  }
  
  private void animate() {
    (new ASRunner() {
        final ASAnimationRunner this$0;
        
        public void run() {
          View view = ASAnimationRunner.this.getTargetView();
          if (view != null)
            view.startAnimation(ASAnimationRunner.this._animation); 
        }
      }).runInMainThread();
  }
  
  private void fail() {
    (new ASRunner() {
        final ASAnimationRunner this$0;
        
        public void run() {
          ASAnimationRunner.this.onAnimationStartFail();
        }
      }).runInMainThread();
  }
  
  abstract View getTargetView();
  
  abstract void onAnimationStartFail();
  
  public void start() {
    (new ASRunner() {
        final ASAnimationRunner this$0;
        
        public void run() {
          byte b = 0;
          boolean bool = false;
          while (true) {
            boolean bool1 = bool;
            if (b < 10)
              if (ASAnimationRunner.this.getTargetView() != null) {
                ASAnimationRunner.this.animate();
                bool1 = true;
              } else {
                b++;
                try {
                  Thread.sleep(10L);
                } catch (InterruptedException interruptedException) {
                  interruptedException.printStackTrace();
                } 
                continue;
              }  
            if (!bool1)
              ASAnimationRunner.this.fail(); 
            return;
          } 
        }
      }).runInNewThread();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASAnimationRunner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */