package com.kumi.Telnet;

public interface TelnetConnectorListener {
  void onTelnetConnectorClosed(TelnetConnector paramTelnetConnector);
  
  void onTelnetConnectorConnectFail(TelnetConnector paramTelnetConnector);
  
  void onTelnetConnectorConnectStart(TelnetConnector paramTelnetConnector);
  
  void onTelnetConnectorConnectSuccess(TelnetConnector paramTelnetConnector);
  
  void onTelnetConnectorReceiveDataFinished(TelnetConnector paramTelnetConnector);
  
  void onTelnetConnectorReceiveDataStart(TelnetConnector paramTelnetConnector);
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetConnectorListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */