package com.kumi.ASFramework.PageController;

import android.view.View;
import android.view.ViewGroup;
import com.kumi.ASFramework.Thread.ASRunner;

public class ASViewRemover {
  private ViewGroup _parent_view = null;
  
  private View _target_view = null;
  
  public ASViewRemover(ViewGroup paramViewGroup, View paramView) {
    this._parent_view = paramViewGroup;
    this._target_view = paramView;
  }
  
  private void remove() {
    (new ASRunner() {
        final ASViewRemover this$0;
        
        public void run() {
          if (ASViewRemover.this._parent_view != null && ASViewRemover.this._target_view != null)
            ASViewRemover.this._parent_view.removeView(ASViewRemover.this._target_view); 
        }
      }).runInMainThread();
  }
  
  public static void remove(ViewGroup paramViewGroup, View paramView) {
    (new ASViewRemover(paramViewGroup, paramView)).start();
  }
  
  public void start() {
    (new ASRunner() {
        final ASViewRemover this$0;
        
        public void run() {
          try {
            Thread.sleep(1000L);
          } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
          } 
          ASViewRemover.this.remove();
        }
      }).runInNewThread();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASViewRemover.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */