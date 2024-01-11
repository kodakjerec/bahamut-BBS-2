package com.kumi.Telnet.Reference;

public class TelnetAnsiCode {
  public static final int[] BACKGROUND_COLOR_NORMAL;
  
  public static final int[] COLOR_BRIGHT;
  
  public static final int[] TEXT_COLOR_NORMAL = new int[] { -16777216, -8388608, -16744448, -8355840, -16777088, -8388480, -16744320, -4144960 };
  
  static {
    BACKGROUND_COLOR_NORMAL = new int[] { -16777216, -8388608, -16744448, -8355840, -16777088, -8388480, -16744320, -4144960 };
    COLOR_BRIGHT = new int[] { -8355712, -65536, -16711936, -256, -16776961, -65281, -16711681, -1 };
  }
  
  public static int getBackgroundColor(byte paramByte) {
    int i = -4144960;
    int j = paramByte & 0xFF;
    if (paramByte >= 8) {
      int k;
      try {
        k = COLOR_BRIGHT[j - 8];
      } catch (Exception exception) {
        exception.printStackTrace();
        k = i;
      } 
      return k;
    } 
    return BACKGROUND_COLOR_NORMAL[j];
  }
  
  public static int getTextColor(byte paramByte) {
    int i = -4144960;
    int j = paramByte & 0xFF;
    if (paramByte >= 8) {
      int k;
      try {
        k = COLOR_BRIGHT[j - 8];
      } catch (Exception exception) {
        exception.printStackTrace();
        k = i;
      } 
      return k;
    } 
    return TEXT_COLOR_NORMAL[j];
  }
  
  public static class Code {
    public static final int CHA = 6;
    
    public static final int CNL = 4;
    
    public static final int CPL = 5;
    
    public static final int CUB = 3;
    
    public static final int CUD = 1;
    
    public static final int CUF = 2;
    
    public static final int CUP = 7;
    
    public static final int CUU = 0;
    
    public static final int DSR = 14;
    
    public static final int ED = 8;
    
    public static final int EL = 9;
    
    public static final int HC = 17;
    
    public static final int HVP = 12;
    
    public static final int RCP = 16;
    
    public static final int SC = 18;
    
    public static final int SCP = 15;
    
    public static final int SD = 11;
    
    public static final int SGR = 13;
    
    public static final int SU = 10;
  }
  
  public static class Color {
    public static final byte BLACK = 0;
    
    public static final byte BLUE = 4;
    
    public static final byte CYAN = 6;
    
    public static final byte GRAY = 7;
    
    public static final byte GREEN = 2;
    
    public static final byte MAGENTA = 5;
    
    public static final byte RED = 1;
    
    public static final byte YELLOW = 3;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\Reference\TelnetAnsiCode.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */