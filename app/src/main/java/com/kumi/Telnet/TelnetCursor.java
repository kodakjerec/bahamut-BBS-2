package com.kumi.Telnet;

public class TelnetCursor {
  public int column = 0;
  
  public int row = 0;
  
  public TelnetCursor() {}
  
  public TelnetCursor(int paramInt1, int paramInt2) {
    set(paramInt1, paramInt2);
  }
  
  public TelnetCursor clone() {
    return new TelnetCursor(this.row, this.column);
  }
  
  public boolean equals(int paramInt1, int paramInt2) {
    return (paramInt1 == this.row && paramInt2 == this.column);
  }
  
  public boolean equals(TelnetCursor paramTelnetCursor) {
    return (paramTelnetCursor.row == this.row && paramTelnetCursor.column == this.column);
  }
  
  public void set(int paramInt1, int paramInt2) {
    this.row = paramInt1;
    this.column = paramInt2;
  }
  
  public void set(TelnetCursor paramTelnetCursor) {
    this.row = paramTelnetCursor.row;
    this.column = paramTelnetCursor.column;
  }
  
  public String toString() {
    return "( " + this.row + " , " + this.column + " )";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetCursor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */