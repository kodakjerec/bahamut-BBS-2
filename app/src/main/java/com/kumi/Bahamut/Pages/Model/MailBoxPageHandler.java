package com.kumi.Bahamut.Pages.Model;

import com.kumi.Telnet.Model.TelnetRow;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetUtils;

public class MailBoxPageHandler {
  private static MailBoxPageHandler _instance = null;
  
  public static MailBoxPageHandler getInstance() {
    if (_instance == null)
      _instance = new MailBoxPageHandler(); 
    return _instance;
  }
  
  public MailBoxPageBlock load() {
    MailBoxPageBlock mailBoxPageBlock = MailBoxPageBlock.create();
    for (byte b = 3; b < 3 + 20; b++) {
      TelnetRow telnetRow = TelnetClient.getModel().getRow(b);
      String str = telnetRow.getSpaceString(0, 0).trim();
      int i = TelnetUtils.getIntegerFromData(telnetRow, 1, 5);
      if (i != 0) {
        boolean bool;
        if (str.length() > 0 && str.charAt(0) == '>')
          mailBoxPageBlock.selectedItemNumber = i; 
        byte b1 = telnetRow.data[6];
        str = telnetRow.getSpaceString(8, 12).trim();
        String str1 = telnetRow.getSpaceString(14, 25).trim();
        String str2 = telnetRow.getSpaceString(27, 28).trim();
        String str3 = telnetRow.getSpaceString(30, 79).trim();
        MailBoxPageItem mailBoxPageItem = MailBoxPageItem.create();
        if (b == 3)
          mailBoxPageBlock.minimumItemNumber = i; 
        mailBoxPageBlock.maximumItemNumber = i;
        mailBoxPageItem.Number = i;
        mailBoxPageItem.Date = str;
        mailBoxPageItem.Author = str1;
        if (b1 != 43 && b1 != 77) {
          bool = true;
        } else {
          bool = false;
        } 
        mailBoxPageItem.isRead = bool;
        if (b1 == 114 || b1 == 82) {
          bool = true;
        } else {
          bool = false;
        } 
        mailBoxPageItem.isReply = bool;
        if (b1 == 109 || b1 == 82 || b1 == 77) {
          bool = true;
        } else {
          bool = false;
        } 
        mailBoxPageItem.isMarked = bool;
        mailBoxPageItem.Title = str3;
        if (str2.equals("◇") || str2.equals("◆")) {
          bool = true;
        } else {
          bool = false;
        } 
        mailBoxPageItem.isOrigin = bool;
        mailBoxPageBlock.setItem(b - 3, mailBoxPageItem);
      } 
    } 
    return mailBoxPageBlock;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\Model\MailBoxPageHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */