package com.kumi.Telnet.Model;

import com.kumi.Telnet.Reference.TelnetAnsiCode;
import java.util.Iterator;
import java.util.Vector;

public class TelnetFrame {
  public static final int DEFAULT_COLUMN = 80;
  
  public static final int DEFAULT_ROW = 24;
  
  private static int _count = 0;
  
  public Vector<TelnetRow> rows = new Vector<TelnetRow>();
  
  public TelnetFrame() {
    initialData(24);
    clear();
  }
  
  public TelnetFrame(int paramInt) {
    initialData(paramInt);
    clear();
  }
  
  public TelnetFrame(TelnetFrame paramTelnetFrame) {
    set(paramTelnetFrame);
  }
  
  public void cleanCachedData() {
    Iterator<TelnetRow> iterator = this.rows.iterator();
    while (iterator.hasNext())
      ((TelnetRow)iterator.next()).cleanCachedData(); 
  }
  
  public void cleanPositionData(int paramInt1, int paramInt2) {
    getRow(paramInt1).cleanColumn(paramInt2);
  }
  
  public void clear() {
    for (byte b = 0; b < this.rows.size(); b++)
      getRow(b).clear(); 
  }
  
  public TelnetFrame clone() {
    return new TelnetFrame(this);
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
  }
  
  public TelnetRow getFirstRow() {
    return this.rows.firstElement();
  }
  
  public TelnetRow getLastestRow() {
    return this.rows.lastElement();
  }
  
  public int getPositionBackgroundColor(int paramInt1, int paramInt2) {
    return TelnetAnsiCode.getBackgroundColor((getRow(paramInt1)).backgroundColor[paramInt2]);
  }
  
  public byte getPositionBitSpace(int paramInt1, int paramInt2) {
    return (getRow(paramInt1)).bitSpace[paramInt2];
  }
  
  public boolean getPositionBlink(int paramInt1, int paramInt2) {
    return (getRow(paramInt1)).blink[paramInt2];
  }
  
  public int getPositionData(int paramInt1, int paramInt2) {
    return (getRow(paramInt1)).data[paramInt2] & 0xFF;
  }
  
  public int getPositionTextColor(int paramInt1, int paramInt2) {
    return TelnetAnsiCode.getTextColor((getRow(paramInt1)).textColor[paramInt2]);
  }
  
  public TelnetRow getRow(int paramInt) {
    return this.rows.get(paramInt);
  }
  
  public int getRowSize() {
    return this.rows.size();
  }
  
  public void initialData(int paramInt) {
    this.rows.clear();
    for (byte b = 0; b < paramInt; b++)
      this.rows.add(new TelnetRow()); 
    clear();
  }
  
  public boolean isEmpty() {
    boolean bool = true;
    for (byte b = 0;; b++) {
      boolean bool1 = bool;
      if (b < this.rows.size()) {
        if (!((TelnetRow)this.rows.get(b)).isEmpty())
          return false; 
      } else {
        return bool1;
      } 
    } 
  }
  
  public void printBackgroundColor() {
    for (byte b = 0; b < this.rows.size(); b++) {
      StringBuffer stringBuffer = new StringBuffer();
      TelnetRow telnetRow = this.rows.get(b);
      for (byte b1 = 0; b1 < 80; b1++) {
        stringBuffer.append(String.format("%1$02d ", new Object[] { Byte.valueOf(telnetRow.backgroundColor[b1]) }));
      } 
      System.out.println(stringBuffer.toString());
    } 
  }
  
  public void reloadSpace() {
    for (byte b = 0; b < getRowSize(); b++) {
      for (byte b1 = 0; b1 < 80; b1++) {
        if (getPositionData(b, b1) > 127 && b1 < 79) {
          boolean bool1;
          boolean bool2;
          if (getPositionTextColor(b, b1) != getPositionTextColor(b, b1 + 1)) {
            bool1 = true;
          } else {
            bool1 = false;
          } 
          if (getPositionBackgroundColor(b, b1) != getPositionBackgroundColor(b, b1 + 1)) {
            bool2 = true;
          } else {
            bool2 = false;
          } 
          if (bool1 || bool2) {
            setPositionBitSpace(b, b1, (byte)3);
            setPositionBitSpace(b, b1 + 1, (byte)4);
          } else {
            setPositionBitSpace(b, b1, (byte)1);
            setPositionBitSpace(b, b1 + 1, (byte)2);
          } 
          b1++;
        } else {
          setPositionBitSpace(b, b1, (byte)0);
        } 
      } 
    } 
  }
  
  public TelnetRow removeRow(int paramInt) {
    return this.rows.remove(paramInt);
  }
  
  public void set(TelnetFrame paramTelnetFrame) {
    if (this.rows.size() != paramTelnetFrame.getRowSize())
      initialData(paramTelnetFrame.getRowSize()); 
    for (byte b = 0; b < this.rows.size(); b++)
      ((TelnetRow)this.rows.get(b)).set(paramTelnetFrame.getRow(b)); 
  }
  
  public void setPositionBitSpace(int paramInt1, int paramInt2, byte paramByte) {
    (getRow(paramInt1)).bitSpace[paramInt2] = paramByte;
  }
  
  public void setRow(int paramInt, TelnetRow paramTelnetRow) {
    if (paramInt >= 0 && paramInt < this.rows.size())
      this.rows.set(paramInt, paramTelnetRow); 
  }
  
  public void switchRow(int paramInt1, int paramInt2) {
    TelnetRow telnetRow = this.rows.get(paramInt1);
    this.rows.set(paramInt1, this.rows.get(paramInt2));
    this.rows.set(paramInt2, telnetRow);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\Model\TelnetFrame.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */