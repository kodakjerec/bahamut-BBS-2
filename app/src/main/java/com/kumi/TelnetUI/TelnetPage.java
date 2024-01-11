package com.kumi.TelnetUI;

import com.kumi.ASFramework.PageController.ASViewController;

public abstract class TelnetPage extends ASViewController {
  public boolean isKeepOnOffline() {
    return false;
  }
  
  public boolean isPopupPage() {
    return false;
  }
  
  public void onPageDidUnload() {
    clear();
    super.onPageDidUnload();
  }
  
  public boolean onPagePreload() {
    return true;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TelnetUI\TelnetPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */