package com.kumi.Telnet;

import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.Telnet.Model.TelnetModel;
import com.kumi.Telnet.Reference.TelnetKeyboard;
import com.kumi.TextEncoder.U2BEncoder;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelnetClient implements TelnetConnectorListener {
  private static TelnetClient _instance = null;
  
  private TelnetConnector _connector = null;
  
  private TelnetClientListener _listener = null;
  
  private TelnetModel _model = null;
  
  private TelnetReceiver _receiver = null;
  
  ExecutorService _send_executor = Executors.newSingleThreadExecutor();
  
  private TelnetStateHandler _state_handler = null;
  
  private String _username;
  
  private TelnetClient(TelnetStateHandler paramTelnetStateHandler) {
    this._state_handler = paramTelnetStateHandler;
    this._model = new TelnetModel();
    this._connector = new TelnetConnector();
    this._connector.setListener(this);
    this._receiver = new TelnetReceiver(this._connector, this._model);
  }
  
  public static void construct(TelnetStateHandler paramTelnetStateHandler) {
    _instance = new TelnetClient(paramTelnetStateHandler);
  }
  
  public static TelnetClient getClient() {
    return _instance;
  }
  
  public static TelnetConnector getConnector() {
    return (getClient())._connector;
  }
  
  public static TelnetModel getModel() {
    return (getClient())._model;
  }
  
  private void sendKeyboardInputToServer(int paramInt1, int paramInt2) {
    sendDataToServer(TelnetKeyboard.getKeyData(paramInt1), paramInt2);
  }
  
  private void sendStringToServer(String paramString, int paramInt) {
    byte[] arrayOfByte;
    String str1 = null;
    boolean bool = false;
    String str2 = paramString + "\n";
    paramString = str1;
    try {
      byte[] arrayOfByte1 = str2.getBytes("UTF-16LE");
      arrayOfByte = arrayOfByte1;
      arrayOfByte1 = U2BEncoder.getInstance().encodeToBytes(arrayOfByte1, 0);
      arrayOfByte = arrayOfByte1;
      bool = true;
    } catch (UnsupportedEncodingException unsupportedEncodingException) {
      unsupportedEncodingException.printStackTrace();
    } 
    if (bool)
      sendDataToServer(arrayOfByte, paramInt); 
  }
  
  public void clear() {
    this._state_handler.clear();
    this._connector.clear();
    this._model.clear();
    this._receiver.stopReceiver();
  }
  
  public void close() {
    try {
      this._connector.close();
    } catch (Exception exception) {
      exception.printStackTrace();
    } 
  }
  
  public void connect(String paramString, int paramInt) {
    this._connector.connect(paramString, paramInt);
  }
  
  public String getUsername() {
    if (this._username == null)
      this._username = ""; 
    return this._username;
  }
  
  public void onTelnetConnectorClosed(TelnetConnector paramTelnetConnector) {
    clear();
    if (this._listener != null)
      this._listener.onTelnetClientConnectionClosed(this); 
  }
  
  public void onTelnetConnectorConnectFail(TelnetConnector paramTelnetConnector) {
    clear();
    if (this._listener != null)
      this._listener.onTelnetClientConnectionFail(this); 
  }
  
  public void onTelnetConnectorConnectStart(TelnetConnector paramTelnetConnector) {
    if (this._listener != null)
      this._listener.onTelnetClientConnectionStart(this); 
  }
  
  public void onTelnetConnectorConnectSuccess(TelnetConnector paramTelnetConnector) {
    this._receiver.startReceiver();
    if (this._listener != null)
      this._listener.onTelnetClientConnectionSuccess(this); 
  }
  
  public void onTelnetConnectorReceiveDataFinished(TelnetConnector paramTelnetConnector) {
    this._connector.cleanReadDataSize();
  }
  
  public void onTelnetConnectorReceiveDataStart(TelnetConnector paramTelnetConnector) {
    if (this._state_handler != null) {
      this._model.cleanCahcedData();
      this._state_handler.handleState();
    } 
  }
  
  public void sendDataToServer(byte[] paramArrayOfbyte) {
    if (ASRunner.isMainThread()) {
      sendDataToServerInBackground(paramArrayOfbyte, 0);
      return;
    } 
    sendDataToServer(paramArrayOfbyte, 0);
  }
  
  protected void sendDataToServer(byte[] paramArrayOfbyte, int paramInt) {
    if (paramArrayOfbyte != null && this._connector.isConnecting()) {
      this._connector.writeData(paramArrayOfbyte, paramInt);
      this._connector.sendData(paramInt);
    } 
  }
  
  public void sendDataToServerInBackground(byte[] paramArrayOfbyte) {
    sendDataToServerInBackground(paramArrayOfbyte, 0);
  }
  
  public void sendDataToServerInBackground(final byte[] data, final int channel) {
    if (data != null && this._connector.isConnecting())
      this._send_executor.submit(new Runnable() {
            final TelnetClient this$0;
            
            final int val$channel;
            
            final byte[] val$data;
            
            public void run() {
              TelnetClient.this._connector.writeData(data, channel);
              TelnetClient.this._connector.sendData(channel);
            }
          }); 
  }
  
  public void sendKeyboardInputToServer(int paramInt) {
    if (ASRunner.isMainThread()) {
      sendKeyboardInputToServerInBackground(paramInt, 0);
      return;
    } 
    sendKeyboardInputToServer(paramInt, 0);
  }
  
  public void sendKeyboardInputToServerInBackground(int paramInt) {
    sendKeyboardInputToServerInBackground(paramInt, 0);
  }
  
  public void sendKeyboardInputToServerInBackground(int paramInt1, int paramInt2) {
    sendDataToServerInBackground(TelnetKeyboard.getKeyData(paramInt1), paramInt2);
  }
  
  public void sendStringToServer(String paramString) {
    if (ASRunner.isMainThread()) {
      sendStringToServerInBackground(paramString, 0);
      return;
    } 
    sendStringToServer(paramString, 0);
  }
  
  public void sendStringToServerInBackground(String paramString) {
    sendStringToServerInBackground(paramString, 0);
  }
  
  public void sendStringToServerInBackground(String paramString, int paramInt) {
    byte[] arrayOfByte;
    String str1 = null;
    boolean bool = false;
    String str2 = paramString + "\n";
    paramString = str1;
    try {
      byte[] arrayOfByte1 = str2.getBytes("UTF-16LE");
      arrayOfByte = arrayOfByte1;
      arrayOfByte1 = U2BEncoder.getInstance().encodeToBytes(arrayOfByte1, 0);
      arrayOfByte = arrayOfByte1;
      bool = true;
    } catch (UnsupportedEncodingException unsupportedEncodingException) {
      unsupportedEncodingException.printStackTrace();
    } 
    if (bool)
      sendDataToServerInBackground(arrayOfByte, paramInt); 
  }
  
  public void setListener(TelnetClientListener paramTelnetClientListener) {
    this._listener = paramTelnetClientListener;
  }
  
  public void setUsername(String paramString) {
    this._username = paramString;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetClient.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */