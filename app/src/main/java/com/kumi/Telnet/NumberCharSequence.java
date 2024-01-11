package com.kumi.Telnet;

public class NumberCharSequence implements CharSequence {
  private char[] _data = new char[5];
  
  private String _string = null;
  
  public NumberCharSequence() {
    clear();
    this._string = new String(this._data);
  }
  
  public char charAt(int paramInt) {
    return this._data[paramInt];
  }
  
  public void clear() {
    for (byte b = 0; b < 5; b++)
      this._data[b] = '0'; 
  }
  
  public int length() {
    return 5;
  }
  
  public void setInt(int paramInt) {
    this._data[4] = (char)(paramInt % 10 + 48);
    paramInt /= 10;
    this._data[3] = (char)(paramInt % 10 + 48);
    paramInt /= 10;
    this._data[2] = (char)(paramInt % 10 + 48);
    paramInt /= 10;
    this._data[1] = (char)(paramInt % 10 + 48);
    paramInt /= 10;
    this._data[0] = (char)(paramInt % 10 + 48);
    paramInt /= 10;
  }
  
  public CharSequence subSequence(int paramInt1, int paramInt2) {
    return null;
  }
  
  public String toString() {
    return this._string;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\NumberCharSequence.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */