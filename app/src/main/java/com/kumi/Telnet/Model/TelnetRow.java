package com.kumi.Telnet.Model;

import com.kumi.TextEncoder.B2UEncoder;

public class TelnetRow {
  private static int _count = 0;
  
  private TelnetRow _append_row = null;
  
  private String _cached_string = null;
  
  private int _empty_space = 0;
  
  private int _quote_level = -1;
  
  private int _quote_space = 0;
  
  public byte[] backgroundColor = new byte[80];
  
  public byte[] bitSpace = new byte[80];
  
  public boolean[] blink = new boolean[80];
  
  public byte[] data = new byte[80];
  
  public boolean[] italic = new boolean[80];
  
  public byte[] textColor = new byte[80];
  
  public TelnetRow() {
    clear();
  }
  
  public TelnetRow(TelnetRow paramTelnetRow) {
    clear();
    set(paramTelnetRow);
  }
  
  private void reloadQuoteSpace() {
    this._quote_level = 0;
    this._quote_space = 0;
    int i = 0;
    this._quote_space = 0;
    while (this._quote_space < this.data.length) {
      if (this.data[this._quote_space] == 62) {
        this._quote_level++;
        i = 0;
      } else if (this.data[this._quote_space] == 32) {
        int j = i + 1;
        i = j;
        if (j > 1)
          break; 
      } else {
        break;
      } 
      this._quote_space++;
    } 
    this._empty_space = 0;
    for (i = this.data.length - 1; i >= 0 && this.data[i] == 0; i--)
      this._empty_space++; 
  }
  
  public void append(TelnetRow paramTelnetRow) {
    this._append_row = paramTelnetRow;
    cleanCachedData();
    System.out.println("self become:" + getRawString());
  }
  
  public void cleanCachedData() {
    this._cached_string = null;
    this._quote_level = -1;
  }
  
  public void cleanColumn(int paramInt) {
    this.data[paramInt] = 0;
    this.textColor[paramInt] = 0;
    this.backgroundColor[paramInt] = 0;
    this.bitSpace[paramInt] = 0;
    this.blink[paramInt] = false;
    this.italic[paramInt] = false;
  }
  
  public void clear() {
    for (byte b = 0; b < 80; b++)
      cleanColumn(b); 
    cleanCachedData();
  }
  
  public TelnetRow clone() {
    return new TelnetRow(this);
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public int getDataSpace() {
    return this.data.length - getEmptySpace();
  }
  
  public int getEmptySpace() {
    if (this._quote_level == -1)
      reloadQuoteSpace(); 
    return this._empty_space;
  }
  
  public int getQuoteLevel() {
    if (this._quote_level == -1)
      reloadQuoteSpace(); 
    return this._quote_level;
  }
  
  public int getQuoteSpace() {
    if (this._quote_level == -1)
      reloadQuoteSpace(); 
    return this._quote_space;
  }
  
  public String getRawString() {
    if (this._cached_string == null) {
      this._cached_string = B2UEncoder.getInstance().encodeToString(this.data);
      if (this._append_row != null)
        this._cached_string += this._append_row.getRawString(); 
    } 
    return this._cached_string;
  }
  
  public String getSpaceString(int paramInt1, int paramInt2) {
    int k = 0;
    int i = 0;
    int j = 0;
    while (j <= paramInt2) {
      if (j == paramInt1)
        k = i; 
      int m = i + 1;
      i = j;
      if ((this.data[j] & 0xFF) > 127) {
        i = j;
        if (j < paramInt2)
          i = j + 1; 
      } 
      j = i + 1;
      i = m;
    } 
    null = getRawString();
    paramInt1 = i;
    if (i > null.length())
      paramInt1 = null.length(); 
    return (paramInt1 < k) ? "" : null.substring(k, paramInt1);
  }
  
  public boolean isEmpty() {
    boolean bool = true;
    for (byte b = 0;; b++) {
      boolean bool1 = bool;
      if (b < 80) {
        if (this.data[b] != 0)
          return false; 
      } else {
        return bool1;
      } 
    } 
  }
  
  public boolean isHttpUrl() {
    return false;
  }
  
  public TelnetRow set(TelnetRow paramTelnetRow) {
    for (byte b = 0; b < 80; b++) {
      this.data[b] = paramTelnetRow.data[b];
      this.textColor[b] = paramTelnetRow.textColor[b];
      this.backgroundColor[b] = paramTelnetRow.backgroundColor[b];
      this.bitSpace[b] = paramTelnetRow.bitSpace[b];
      this.blink[b] = paramTelnetRow.blink[b];
      this.italic[b] = paramTelnetRow.italic[b];
    } 
    cleanCachedData();
    return this;
  }
  
  public String toContentString() {
    return getRawString().substring(getQuoteSpace());
  }
  
  public String toString() {
    return getRawString().substring(getQuoteSpace()).trim();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\Model\TelnetRow.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */