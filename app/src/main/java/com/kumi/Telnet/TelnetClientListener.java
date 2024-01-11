package com.kumi.Telnet;

public interface TelnetClientListener {
  void onTelnetClientConnectionClosed(TelnetClient paramTelnetClient);
  
  void onTelnetClientConnectionFail(TelnetClient paramTelnetClient);
  
  void onTelnetClientConnectionStart(TelnetClient paramTelnetClient);
  
  void onTelnetClientConnectionSuccess(TelnetClient paramTelnetClient);
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetClientListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */