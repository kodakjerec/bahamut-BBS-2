package com.kumi.Telnet.Model;

public class TelnetData {
  public static final byte BIT_SPACE_1 = 0;
  
  public static final byte DOUBLE_BIT_SPACE_2_1 = 3;
  
  public static final byte DOUBLE_BIT_SPACE_2_2 = 4;
  
  public static final byte SINGLE_BIT_SPACE_2_1 = 1;
  
  public static final byte SINGLE_BIT_SPACE_2_2 = 2;
  
  private static int _count = 0;
  
  public byte backgroundColor = 0;
  
  public byte bitSpace = 0;
  
  public boolean blink = false;
  
  public byte data = 0;
  
  public boolean italic = false;
  
  public byte textColor = 0;
  
  public TelnetData() {}
  
  public TelnetData(TelnetData paramTelnetData) {
    set(paramTelnetData);
  }
  
  public void clear() {
    this.data = 0;
    this.textColor = 0;
    this.backgroundColor = 0;
    this.blink = false;
    this.italic = false;
  }
  
  public TelnetData clone() {
    return new TelnetData(this);
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public boolean isEmpty() {
    return (this.data == 0);
  }
  
  public void set(TelnetData paramTelnetData) {
    this.data = paramTelnetData.data;
    this.textColor = paramTelnetData.textColor;
    this.backgroundColor = paramTelnetData.backgroundColor;
    this.blink = paramTelnetData.blink;
    this.italic = paramTelnetData.italic;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\Model\TelnetData.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */