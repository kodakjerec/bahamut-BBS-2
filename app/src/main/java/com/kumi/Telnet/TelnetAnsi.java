package com.kumi.Telnet;

public class TelnetAnsi {
  public static byte DEFAULT_BACKGROUND_COLOR;
  
  private static boolean DEFAULT_TEXT_BLINK;
  
  private static boolean DEFAULT_TEXT_BRIGHT;
  
  public static byte DEFAULT_TEXT_COLOR = 7;
  
  private static boolean DEFAULT_TEXT_ITALIC;
  
  public byte backgroundColor = 0;
  
  public boolean textBlink = false;
  
  public boolean textBright = false;
  
  public byte textColor = 0;
  
  public boolean textItalic = false;
  
  static {
    DEFAULT_TEXT_BLINK = false;
    DEFAULT_TEXT_BRIGHT = false;
    DEFAULT_TEXT_ITALIC = false;
    DEFAULT_BACKGROUND_COLOR = 0;
  }
  
  public TelnetAnsi() {
    resetToDefaultState();
  }
  
  public static byte getDefaultBackgroundColor() {
    return DEFAULT_BACKGROUND_COLOR;
  }
  
  public static boolean getDefaultTextBlink() {
    return DEFAULT_TEXT_BLINK;
  }
  
  public static byte getDefaultTextColor() {
    return DEFAULT_TEXT_COLOR;
  }
  
  public static boolean getDefaultTextItalic() {
    return DEFAULT_TEXT_ITALIC;
  }
  
  public void resetToDefaultState() {
    this.textColor = DEFAULT_TEXT_COLOR;
    this.textBlink = DEFAULT_TEXT_BLINK;
    this.textBright = DEFAULT_TEXT_BRIGHT;
    this.textItalic = DEFAULT_TEXT_ITALIC;
    this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetAnsi.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */