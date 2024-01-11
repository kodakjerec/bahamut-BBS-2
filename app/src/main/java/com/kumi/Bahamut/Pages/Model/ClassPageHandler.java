package com.kumi.Bahamut.Pages.Model;

import com.kumi.Telnet.Model.TelnetRow;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetUtils;

public class ClassPageHandler {
  private static ClassPageHandler _instance = null;
  
  public static ClassPageHandler getInstance() {
    if (_instance == null)
      _instance = new ClassPageHandler(); 
    return _instance;
  }
  
  public ClassPageBlock load() {
    ClassPageBlock classPageBlock = ClassPageBlock.create();
    if (TelnetClient.getModel().getRowString(2).trim().startsWith("編號")) {
      classPageBlock.mode = 0;
    } else {
      classPageBlock.mode = 1;
    } 
    byte b = 3;
    label33: while (true) {
      if (b >= 3 + 20 || TelnetClient.getModel().getRowString(b).length() == 0)
        return classPageBlock; 
      TelnetRow telnetRow = TelnetClient.getModel().getRow(b);
      int j = TelnetUtils.getIntegerFromData(telnetRow, 1, 5);
      if (b == 3)
        classPageBlock.minimumItemNumber = j; 
      String str = telnetRow.getSpaceString(0, 0).trim();
      if (str.length() > 0 && str.charAt(0) == '>')
        classPageBlock.selectedItemNumber = j; 
      int i = 9;
      for (byte b1 = 0;; b1++) {
        if (b1 >= 12 || telnetRow.data[b1 + 9] == 32) {
          String str1;
          String str2 = telnetRow.getSpaceString(9, i).trim();
          String str3 = telnetRow.getSpaceString(i + 1, 64).trim();
          String str4 = telnetRow.getSpaceString(65, 79).trim();
          ClassPageItem classPageItem = ClassPageItem.create();
          if (str2.endsWith("/")) {
            classPageItem.isDirectory = true;
            str1 = str2.substring(0, str2.length() - 1);
            str = str3;
          } else {
            classPageItem.isDirectory = false;
            str1 = str2;
            str = str3;
            if (str3.length() > 2) {
              str = str3.substring(2);
              str1 = str2;
            } 
          } 
          classPageItem.Name = str1;
          classPageItem.Title = str;
          classPageItem.Manager = str4;
          classPageBlock.maximumItemNumber = j;
          classPageItem.Number = j;
          classPageBlock.setItem(b - 3, classPageItem);
          b++;
          continue label33;
        } 
        i = b1 + 9;
      } 
      break;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\ClassPageHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */