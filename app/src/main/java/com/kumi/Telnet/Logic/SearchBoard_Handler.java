package com.kumi.Telnet.Logic;

import com.kumi.Telnet.TelnetClient;
import java.util.Vector;

public class SearchBoard_Handler {
  private static SearchBoard_Handler _instance = null;
  
  private Vector<String> _boards = new Vector<String>();
  
  public static SearchBoard_Handler getInstance() {
    if (_instance == null)
      _instance = new SearchBoard_Handler(); 
    return _instance;
  }
  
  public void clear() {
    this._boards.clear();
  }
  
  public String getBoard(int paramInt) {
    return this._boards.get(paramInt);
  }
  
  public String[] getBoards() {
    return this._boards.<String>toArray(new String[this._boards.size()]);
  }
  
  public int getBoardsSize() {
    return this._boards.size();
  }
  
  public void read() {
    byte b = 3;
    while (true) {
      if (b < 23) {
        String str = TelnetClient.getModel().getRowString(b).trim();
        if (str.length() != 0) {
          for (String str1 : str.split(" +")) {
            if (str1.length() > 0)
              this._boards.add(str1); 
          } 
          b++;
          continue;
        } 
      } 
      return;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\Logic\SearchBoard_Handler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */