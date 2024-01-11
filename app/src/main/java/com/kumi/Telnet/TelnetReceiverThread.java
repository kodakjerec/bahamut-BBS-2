package com.kumi.Telnet;

import com.kumi.Telnet.Model.TelnetModel;
import java.io.IOException;

public class TelnetReceiverThread extends Thread {
  public static final int UNSET = -1;
  
  private TelnetCommand _command = new TelnetCommand();
  
  private TelnetConnector _connector = null;
  
  private TelnetModel _model = null;
  
  private boolean _receiving = true;
  
  public TelnetReceiverThread(TelnetConnector paramTelnetConnector, TelnetModel paramTelnetModel) {
    this._connector = paramTelnetConnector;
    this._model = paramTelnetModel;
  }
  
  private byte readData() throws TelnetConnectionClosedException, IOException {
    return this._connector.readData(0);
  }
  
  private boolean receiveData() {
    byte b;
    boolean bool = true;
    try {
      b = readData();
      if (b == -1) {
        byte b1 = readData();
        byte b2 = readData();
        this._command.header = b;
        this._command.action = b1;
        this._command.option = b2;
        handleCommand();
        return bool;
      } 
      if (b == 13) {
        this._model.moveCursorColumnToBegin();
        return bool;
      } 
    } catch (Exception exception) {
      exception.printStackTrace();
      bool = false;
      boolean bool1 = bool;
      if (this._connector != null) {
        bool1 = bool;
        if (this._connector.isConnecting()) {
          this._connector.close();
          bool1 = bool;
        } 
      } 
      return bool1;
    } 
    if (b == 10) {
      this._model.moveCursorToNextLine();
      return bool;
    } 
    if (b == 7) {
      System.out.println("get BEL");
      return bool;
    } 
    if (b == 8) {
      this._model.moveCursorColumnLeft();
      return bool;
    } 
    if (b == 27) {
      boolean bool1 = bool;
      if (readData() == 91) {
        this._model.cleanAnsiBuffer();
        byte b1 = readData();
        this._model.pushAnsiBuffer(b1);
        byte b2 = b1;
        while (true) {
          if ((b2 >= 48 && b2 <= 57) || b2 == 59) {
            b1 = readData();
            this._model.pushAnsiBuffer(b1);
            b2 = b1;
            continue;
          } 
          this._model.parseAnsiBuffer();
          bool1 = bool;
          // Byte code: goto -> 55
        } 
      } 
      return bool1;
    } 
    this._model.pushData(b);
    return bool;
  }
  
  public void close() {
    this._receiving = false;
    this._connector = null;
    this._model = null;
  }
  
  public void handleCommand() throws TelnetConnectionClosedException, IOException {
    if (this._command.isEqualTo(-1, -3, 37)) {
      sendCommandToServer(-1, -4, 37);
      return;
    } 
    if (this._command.isEqualTo(-1, -5, 1)) {
      sendCommandToServer(-1, -2, 1);
      return;
    } 
    if (this._command.isEqualTo(-1, -3, 1)) {
      sendCommandToServer(-1, -4, 1);
      return;
    } 
    if (this._command.isEqualTo(-1, -5, 3)) {
      sendCommandToServer(-1, -2, 3);
      return;
    } 
    if (this._command.isEqualTo(-1, -3, 39)) {
      sendCommandToServer(-1, -4, 39);
      return;
    } 
    if (this._command.isEqualTo(-1, -3, 31)) {
      sendCommandToServer(-1, -4, 31);
      return;
    } 
    if (this._command.isEqualTo(-1, -3, 0)) {
      sendCommandToServer(-1, -4, 0);
      return;
    } 
    if (this._command.isEqualTo(-1, -5, 0)) {
      sendCommandToServer(-1, -2, 0);
      return;
    } 
    if (this._command.isEqualTo(-1, -3, 24)) {
      sendCommandToServer(-1, -5, 24);
      return;
    } 
    if (this._command.isEqualTo(-1, -3, 0)) {
      sendCommandToServer(-1, -4, 0);
      return;
    } 
    if (this._command.isEqualTo(-1, -6, 24)) {
      TelnetClient.getConnector().readData(0);
      TelnetClient.getConnector().readData(0);
      TelnetClient.getConnector().readData(0);
      TelnetOutputBuilder.create().pushData((byte)-1).pushData((byte)-6).pushData((byte)24).pushData((byte)0).pushData((byte)65).pushData((byte)78).pushData((byte)83).pushData((byte)73).pushData((byte)-1).pushData((byte)-16).sendToServer();
      return;
    } 
    System.out.println("[MuTelnetCommandHandler]Unimplement command : " + this._command.toString());
  }
  
  public void run() {
    do {
    
    } while (this._receiving && receiveData());
  }
  
  void sendCommandToServer(int paramInt1, int paramInt2, int paramInt3) {
    TelnetOutputBuilder.create().pushData((byte)paramInt1).pushData((byte)paramInt2).pushData((byte)paramInt3).sendToServer();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetReceiverThread.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */