package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetOutputBuilder;

public class BahamutCommandPostArticle extends TelnetCommand {
  String _article_number = null;
  
  String _content = "";
  
  TelnetListPage _list_page;
  
  String _sign = "";
  
  String _target = "F";
  
  String _title = "";
  
  public BahamutCommandPostArticle(TelnetListPage paramTelnetListPage, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5) {
    this._title = paramString1;
    this._content = paramString2;
    this.Action = 8;
    this._target = paramString3;
    this._article_number = paramString4;
    this._list_page = paramTelnetListPage;
    this._sign = paramString5;
    if (this._sign == null)
      this._sign = ""; 
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    TelnetOutputBuilder telnetOutputBuilder = TelnetOutputBuilder.create();
    if (this._article_number != null && this._target != null) {
      telnetOutputBuilder.pushString(this._article_number + "\ny" + this._target + "\n");
      if (this._target != null && (this._target.equals("F") || this._target.equals("B")))
        telnetOutputBuilder.pushString("\n"); 
      if (this._title != null) {
        telnetOutputBuilder.pushKey(25);
        telnetOutputBuilder.pushString(this._title);
        telnetOutputBuilder.pushString("\nn\n" + this._sign + "\n");
      } else {
        telnetOutputBuilder.pushString("\nn\n" + this._sign + "\n");
      } 
      telnetOutputBuilder.pushString(this._content);
      telnetOutputBuilder.pushKey(24);
      telnetOutputBuilder.pushString("s\n");
      if (this._target.equals("M"))
        telnetOutputBuilder.pushString("Y\n\n"); 
      telnetOutputBuilder.sendToServer();
      return;
    } 
    telnetOutputBuilder.pushKey(16);
    telnetOutputBuilder.pushData((byte)13);
    telnetOutputBuilder.pushString(this._title);
    telnetOutputBuilder.pushString("\n" + this._sign + "\n");
    telnetOutputBuilder.pushString(this._content);
    telnetOutputBuilder.pushKey(24);
    telnetOutputBuilder.pushString("s\n");
    telnetOutputBuilder.sendToServer();
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    paramTelnetListPage.pushPreloadCommand(0);
    setDone(true);
  }
  
  public String toString() {
    return "[PostArticle][title=" + this._title + " content" + this._content + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandPostArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */