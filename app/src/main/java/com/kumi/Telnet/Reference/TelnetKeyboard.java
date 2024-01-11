package com.kumi.Telnet.Reference;

public class TelnetKeyboard {
  public static final int DELETE = 265;
  
  public static final int DOWN_ARROW = 259;
  
  public static final int END = 263;
  
  public static final int HOME = 262;
  
  public static final int INSERT = 264;
  
  public static final int LEFT_ARROW = 256;
  
  public static final int PAGE_DOWN = 261;
  
  public static final int PAGE_UP = 260;
  
  public static final int RIGHT_ARROW = 257;
  
  public static final int SPACE = 32;
  
  public static final int TAB = 9;
  
  public static final int UP_ARROW = 258;
  
  public static byte[] getKeyData(int paramInt) {
    switch (paramInt) {
      default:
        arrayOfByte = new byte[1];
        arrayOfByte[0] = (byte)paramInt;
        return arrayOfByte;
      case 9:
        arrayOfByte = new byte[1];
        arrayOfByte[0] = 9;
        return arrayOfByte;
      case 32:
        arrayOfByte = new byte[1];
        arrayOfByte[0] = 32;
        return arrayOfByte;
      case 256:
        arrayOfByte = new byte[3];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 68;
        return arrayOfByte;
      case 257:
        arrayOfByte = new byte[3];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 67;
        return arrayOfByte;
      case 258:
        arrayOfByte = new byte[3];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 65;
        return arrayOfByte;
      case 259:
        arrayOfByte = new byte[3];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 66;
        return arrayOfByte;
      case 260:
        arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 53;
        arrayOfByte[3] = 126;
        return arrayOfByte;
      case 261:
        arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 54;
        arrayOfByte[3] = 126;
        return arrayOfByte;
      case 262:
        arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 49;
        arrayOfByte[3] = 126;
        return arrayOfByte;
      case 263:
        arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 52;
        arrayOfByte[3] = 126;
        return arrayOfByte;
      case 264:
        arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 50;
        arrayOfByte[3] = 126;
        return arrayOfByte;
      case 265:
        break;
    } 
    byte[] arrayOfByte = new byte[4];
    arrayOfByte[0] = 27;
    arrayOfByte[1] = 91;
    arrayOfByte[2] = 51;
    arrayOfByte[3] = 126;
    return arrayOfByte;
  }
  
  public static byte[] getKeyDataWithTimes(int paramInt1, int paramInt2) {
    byte[] arrayOfByte1 = getKeyData(paramInt1);
    byte[] arrayOfByte2 = new byte[arrayOfByte1.length * paramInt2];
    for (paramInt1 = 0; paramInt1 < arrayOfByte2.length; paramInt1++)
      arrayOfByte2[paramInt1] = arrayOfByte1[paramInt1 % arrayOfByte1.length]; 
    return arrayOfByte2;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\Reference\TelnetKeyboard.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */