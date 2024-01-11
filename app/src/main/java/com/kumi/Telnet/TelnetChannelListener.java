package com.kumi.Telnet;

public interface TelnetChannelListener {
  void onTelnetChannelReceiveDataFinished(TelnetChannel paramTelnetChannel);
  
  void onTelnetChannelReceiveDataStart(TelnetChannel paramTelnetChannel);
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetChannelListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */