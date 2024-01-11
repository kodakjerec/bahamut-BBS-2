package com.kumi.Bahamut.Pages.Model;

import com.kumi.Telnet.Model.TelnetRow;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetUtils;

public class BoardPageHandler {
  private static BoardPageHandler _instance = null;
  
  public static BoardPageHandler getInstance() {
    if (_instance == null)
      _instance = new BoardPageHandler(); 
    return _instance;
  }
  
  public BoardPageBlock load() {
    BoardPageBlock boardPageBlock = BoardPageBlock.create();
    String str = TelnetClient.getModel().getRowString(0);
    char[] arrayOfChar = str.toCharArray();
    int j = 0;
    int k = 0;
    int i = 0;
    while (true) {
      int m = j;
      if (i < arrayOfChar.length)
        if (arrayOfChar[i] == '【') {
          m = i + 1;
        } else {
          i++;
          continue;
        }  
      i = m;
      while (true) {
        j = k;
        if (i < arrayOfChar.length)
          if (arrayOfChar[i] == '】') {
            j = i;
          } else {
            i++;
            continue;
          }  
        if (j > m) {
          String str2 = str.substring(m, j).trim();
          String str1 = str2;
          if (str2.length() > 3) {
            str1 = str2;
            if (str2.startsWith("板主："))
              str1 = str2.substring(3); 
          } 
          boardPageBlock.BoardManager = str1;
        } 
        int n = arrayOfChar.length - 1;
        k = arrayOfChar.length - 1;
        i = arrayOfChar.length - 1;
        while (true) {
          m = k;
          if (i >= 0)
            if (arrayOfChar[i] == '》') {
              m = i;
            } else {
              i--;
              continue;
            }  
          k = m;
          while (true) {
            i = n;
            if (k >= 0)
              if (arrayOfChar[k] == '《') {
                i = k + 1;
              } else {
                k--;
                continue;
              }  
            if (m > i)
              boardPageBlock.BoardName = str.substring(i, m).trim(); 
            k = j + 1;
            j = arrayOfChar.length - 1;
            m = i;
            while (true) {
              i = j;
              if (m > k)
                if (arrayOfChar[m] == '看') {
                  i = m;
                } else {
                  m--;
                  continue;
                }  
              if (i > k)
                boardPageBlock.BoardTitle = str.substring(k, i).trim(); 
              if (boardPageBlock.BoardManager != null) {
                if (boardPageBlock.BoardManager.equals("主題串列")) {
                  boardPageBlock.Type = 1;
                } else {
                  continue;
                } 
              } else {
                boardPageBlock.Type = 0;
              } 
              m = 3;
              while (true) {
                if (m < 3 + 20) {
                  TelnetRow telnetRow = TelnetClient.getModel().getRow(m);
                  if (telnetRow != null && telnetRow.toString().trim().length() != 0) {
                    String str1 = telnetRow.getSpaceString(0, 0).trim();
                    k = TelnetUtils.getIntegerFromData(telnetRow, 1, 5);
                    if (k != 0) {
                      boolean bool;
                      j = 0;
                      i = j;
                      if (str1.length() > 0) {
                        i = j;
                        if (str1.charAt(0) == '>') {
                          boardPageBlock.selectedItemNumber = k;
                          i = 1;
                        } 
                      } 
                      n = telnetRow.data[7];
                      j = TelnetUtils.getIntegerFromData(telnetRow, 8, 9);
                      str1 = telnetRow.getSpaceString(10, 14).trim();
                      String str3 = telnetRow.getSpaceString(16, 27).trim();
                      String str2 = telnetRow.getSpaceString(29, 30).trim();
                      String str4 = telnetRow.getSpaceString(31, 79).trim();
                      BoardPageItem boardPageItem = BoardPageItem.create();
                      if (m == 3)
                        boardPageBlock.minimumItemNumber = k; 
                      boardPageBlock.maximumItemNumber = k;
                      boardPageItem.Number = k;
                      boardPageItem.Date = str1;
                      boardPageItem.Author = str3;
                      if (n != 43 && n != 77) {
                        bool = true;
                      } else {
                        bool = false;
                      } 
                      boardPageItem.isRead = bool;
                      if (n == 100) {
                        bool = true;
                      } else {
                        bool = false;
                      } 
                      boardPageItem.isDeleted = bool;
                      if (n == 109 || n == 77) {
                        bool = true;
                      } else {
                        bool = false;
                      } 
                      boardPageItem.isMarked = bool;
                      boardPageItem.GY = j;
                      boardPageItem.Title = str4;
                      if (!str2.equals("◇") && !str2.equals("◆")) {
                        bool = true;
                      } else {
                        bool = false;
                      } 
                      boardPageItem.isReply = bool;
                      boardPageBlock.setItem(m - 3, boardPageItem);
                      if (i != 0)
                        boardPageBlock.selectedItem = boardPageItem; 
                    } 
                    m++;
                    continue;
                  } 
                } 
                return boardPageBlock;
              } 
              break;
            } 
            break;
          } 
          break;
        } 
        break;
      } 
      break;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\BoardPageHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */