package com.kumi.Telnet;

import com.kumi.Telnet.Model.TelnetRow;
import java.util.Vector;

public class TelnetArticlePage {
  private Vector<TelnetRow> rows = new Vector<TelnetRow>();
  
  public void addRow(TelnetRow paramTelnetRow) {
    this.rows.add(paramTelnetRow.clone());
  }
  
  public void clear() {
    this.rows.clear();
  }
  
  public TelnetRow getRow(int paramInt) {
    return this.rows.get(paramInt);
  }
  
  public int getRowCount() {
    return this.rows.size();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetArticlePage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */