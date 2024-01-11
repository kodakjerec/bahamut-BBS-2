package com.kumi.Telnet;

import com.kumi.Telnet.Model.TelnetModel;

public class TelnetReceiver {
  private TelnetConnector _connector = null;
  
  private TelnetModel _model = null;
  
  private TelnetReceiverThread _receiver_thread = null;
  
  public TelnetReceiver() {}
  
  public TelnetReceiver(TelnetConnector paramTelnetConnector, TelnetModel paramTelnetModel) {
    this._connector = paramTelnetConnector;
    this._model = paramTelnetModel;
  }
  
  public boolean isReceiving() {
    boolean bool = false;
    if (this._receiver_thread != null)
      bool = true; 
    return bool;
  }
  
  public void startReceiver() {
    this._receiver_thread = new TelnetReceiverThread(this._connector, this._model);
    this._receiver_thread.setDaemon(true);
    this._receiver_thread.start();
  }
  
  public void stopReceiver() {
    if (this._receiver_thread != null) {
      this._receiver_thread.close();
      this._receiver_thread = null;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */