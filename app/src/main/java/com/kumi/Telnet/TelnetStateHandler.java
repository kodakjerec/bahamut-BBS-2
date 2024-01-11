package com.kumi.Telnet;

public abstract class TelnetStateHandler {
  int _current_page = 0;
  
  public void cleanFrame() {
    if (TelnetClient.getModel() != null)
      TelnetClient.getModel().cleanFrame(); 
  }
  
  public void cleanRow(int paramInt) {
    if (TelnetClient.getModel() != null)
      TelnetClient.getModel().cleanRow(paramInt); 
  }
  
  public void clear() {}
  
  public int getCurrentPage() {
    return this._current_page;
  }
  
  public String getRowString(int paramInt) {
    return (TelnetClient.getModel() != null) ? TelnetClient.getModel().getRowString(paramInt) : "";
  }
  
  public abstract void handleState();
  
  public void setCurrentPage(int paramInt) {
    this._current_page = paramInt;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetStateHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */