package com.kumi.Telnet;

public class TelnetCommand {
  public static final byte AUTH = 37;
  
  public static final byte DO = -3;
  
  public static final byte DONT = -2;
  
  public static final byte ECHO = 1;
  
  public static final byte IAC = -1;
  
  public static final byte IS = 0;
  
  public static final byte NAWS = 31;
  
  public static final byte NEW_ENV = 39;
  
  public static final byte SB = -6;
  
  public static final byte SE = -16;
  
  public static final byte SG = 3;
  
  public static final byte TERMINAL_TYPE = 24;
  
  public static final byte WILL = -5;
  
  public static final byte WONT = -4;
  
  public byte action = 0;
  
  public byte header = 0;
  
  public byte option = 0;
  
  public TelnetCommand() {}
  
  public TelnetCommand(byte paramByte1, byte paramByte2, byte paramByte3) {
    this.header = paramByte1;
    this.action = paramByte2;
    this.option = paramByte3;
  }
  
  public String getCommandNameString(int paramInt) {
    switch (paramInt) {
      default:
        return "UNKNOW(" + paramInt + ")";
      case 0:
        return "IS";
      case 1:
        return "ECHO";
      case 3:
        return "SG";
      case 24:
        return "TERMINAL_TYPE";
      case 31:
        return "NAWS";
      case 37:
        return "AUTH";
      case 39:
        return "NEW_ENV";
      case -16:
        return "SE";
      case -6:
        return "SB";
      case -5:
        return "WILL";
      case -4:
        return "WONT";
      case -3:
        return "DO";
      case -2:
        return "DONT";
      case -1:
        break;
    } 
    return "IAC";
  }
  
  public boolean isEqualTo(int paramInt1, int paramInt2, int paramInt3) {
    return (paramInt1 == this.header && paramInt2 == this.action && paramInt3 == this.option);
  }
  
  public boolean isEqualTo(TelnetCommand paramTelnetCommand) {
    return (paramTelnetCommand.header == this.header && paramTelnetCommand.action == this.action && paramTelnetCommand.option == this.option);
  }
  
  public String toString() {
    return getCommandNameString(this.header) + "," + getCommandNameString(this.action) + "," + getCommandNameString(this.option);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetCommand.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */