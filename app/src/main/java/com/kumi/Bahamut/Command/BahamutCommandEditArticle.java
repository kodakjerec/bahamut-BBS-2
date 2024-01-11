package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetOutputBuilder;

public class BahamutCommandEditArticle extends TelnetCommand {
  String _article_number;
  
  String _content = "";
  
  String _title = "";
  
  public BahamutCommandEditArticle(String paramString1, String paramString2, String paramString3) {
    this._article_number = paramString1;
    this._title = paramString2;
    this._content = paramString3;
    this.Action = 8;
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    if (this._article_number != null && this._content != null && this._content.length() > 0) {
      TelnetOutputBuilder telnetOutputBuilder = TelnetOutputBuilder.create().pushString(this._article_number + "\nE").pushData((byte)7).pushData((byte)25).pushString(this._content).pushData((byte)24).pushString("S\n");
      if (this._title != null)
        telnetOutputBuilder.pushString("T").pushData((byte)25).pushString(this._title + "\nY\n"); 
      byte[] arrayOfByte = telnetOutputBuilder.build();
      TelnetClient.getClient().sendDataToServer(arrayOfByte);
    } 
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public String toString() {
    return "[EditArticle][articleIndex=" + this._article_number + " title=" + this._title + " content=" + this._content + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandEditArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */