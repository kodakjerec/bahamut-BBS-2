package com.kumi.Telnet;

import com.kumi.Telnet.Model.TelnetFrame;
import com.kumi.Telnet.Model.TelnetModel;
import com.kumi.Telnet.Model.TelnetRow;
import java.util.Vector;

public class TelnetArticleItem {
  public static final int TYPE_HEADER = 2;
  
  public static final int TYPE_TELNET = 1;
  
  public static final int TYPE_TEXT = 0;
  
  public static final int TYPE_TIME = 3;
  
  private String _author = "";
  
  private String _content = "";
  
  private TelnetFrame _frame = null;
  
  private String _nickname = "";
  
  private int _quote_level = 0;
  
  private Vector<TelnetRow> _rows = new Vector<TelnetRow>();
  
  private int _type = 0;
  
  public void addRow(TelnetRow paramTelnetRow) {
    this._rows.add(paramTelnetRow);
  }
  
  public void build() {
    StringBuffer stringBuffer = new StringBuffer();
    for (TelnetRow telnetRow : this._rows) {
      if (stringBuffer.length() > 0)
        stringBuffer.append("\n"); 
      stringBuffer.append(telnetRow.toContentString());
    } 
    this._content = stringBuffer.toString();
  }
  
  public void buildFrame() {
    this._frame = new TelnetFrame(this._rows.size());
    for (byte b = 0; b < this._rows.size(); b++)
      this._frame.setRow(b, this._rows.get(b)); 
  }
  
  public void cleanRows() {
    this._rows.clear();
  }
  
  public void clear() {
    this._author = "";
    this._nickname = "";
    this._content = "";
    this._quote_level = 0;
  }
  
  public String getAuthor() {
    return this._author;
  }
  
  public String getContent() {
    return this._content;
  }
  
  public TelnetFrame getFrame() {
    if (this._frame == null)
      buildFrame(); 
    return this._frame;
  }
  
  public TelnetModel getModel() {
    return new TelnetModel(this._rows.size());
  }
  
  public String getNickname() {
    return this._nickname;
  }
  
  public int getQuoteLevel() {
    return this._quote_level;
  }
  
  public TelnetRow getRow(int paramInt) {
    return this._rows.get(paramInt);
  }
  
  public int getRowSize() {
    return this._rows.size();
  }
  
  public int getType() {
    return this._type;
  }
  
  public boolean isEmpty() {
    return (this._content.length() == 0);
  }
  
  public TelnetRow removeRow(int paramInt) {
    return this._rows.remove(paramInt);
  }
  
  public void setAuthor(String paramString) {
    this._author = paramString;
  }
  
  public void setNickname(String paramString) {
    this._nickname = paramString;
  }
  
  public void setQuoteLevel(int paramInt) {
    this._quote_level = paramInt;
  }
  
  public void setType(int paramInt) {
    this._type = paramInt;
  }
  
  public String toString() {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("QuoteLevel:" + this._quote_level + "\n");
    stringBuffer.append("Author:" + this._author + "\n");
    stringBuffer.append("Nickname:" + this._nickname + "\n");
    stringBuffer.append("Content:" + this._content + "\n");
    return stringBuffer.toString();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetArticleItem.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */