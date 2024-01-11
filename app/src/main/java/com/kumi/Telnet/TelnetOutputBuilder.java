package com.kumi.Telnet;

import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.DataPool.MutableByteBuffer;
import com.kumi.Telnet.Reference.TelnetKeyboard;
import com.kumi.TextEncoder.U2BEncoder;

public class TelnetOutputBuilder {
  private MutableByteBuffer _buffer = MutableByteBuffer.createMutableByteBuffer();
  
  public static TelnetOutputBuilder create() {
    return new TelnetOutputBuilder();
  }
  
  private void sendToServer(int paramInt) {
    byte[] arrayOfByte = build();
    TelnetClient.getClient().sendDataToServer(arrayOfByte, paramInt);
  }
  
  public byte[] build() {
    this._buffer.close();
    byte[] arrayOfByte = this._buffer.toByteArray();
    MutableByteBuffer.recycleMutableByteBuffer(this._buffer);
    return arrayOfByte;
  }
  
  public TelnetOutputBuilder pushData(byte paramByte) {
    this._buffer.put(paramByte);
    return this;
  }
  
  public TelnetOutputBuilder pushData(byte[] paramArrayOfbyte) {
    for (byte b = 0; b < paramArrayOfbyte.length; b++)
      pushData(paramArrayOfbyte[b]); 
    return this;
  }
  
  public TelnetOutputBuilder pushKey(int paramInt) {
    pushData(TelnetKeyboard.getKeyData(paramInt));
    return this;
  }
  
  public TelnetOutputBuilder pushString(String paramString) {
    try {
      pushData(U2BEncoder.getInstance().encodeToBytes(paramString.getBytes("UTF-16LE"), 0));
    } catch (Exception exception) {
      exception.printStackTrace();
    } 
    return this;
  }
  
  public void sendToServer() {
    if (ASRunner.isMainThread()) {
      sendToServerInBackground(0);
      return;
    } 
    sendToServer(0);
  }
  
  public void sendToServerInBackground() {
    sendToServerInBackground(0);
  }
  
  public void sendToServerInBackground(int paramInt) {
    byte[] arrayOfByte = build();
    TelnetClient.getClient().sendDataToServerInBackground(arrayOfByte, paramInt);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetOutputBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */