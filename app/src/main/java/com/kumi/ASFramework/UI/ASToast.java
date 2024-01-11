package com.kumi.ASFramework.UI;

import android.content.Context;
import android.widget.Toast;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.Thread.ASRunner;

public class ASToast {
  public static void showLongToast(final String aToastMessage) {
    (new ASRunner() {
        final String val$aToastMessage;
        
        public void run() {
          Toast.makeText((Context)ASNavigationController.getCurrentController(), aToastMessage, 1).show();
        }
      }).runInMainThread();
  }
  
  public static void showShortToast(final String aToastMessage) {
    (new ASRunner() {
        final String val$aToastMessage;
        
        public void run() {
          Toast.makeText((Context)ASNavigationController.getCurrentController(), aToastMessage, 0).show();
        }
      }).runInMainThread();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\UI\ASToast.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */