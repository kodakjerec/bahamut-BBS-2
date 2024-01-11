package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetClient;

public class BahamutCommandDeleteArticle extends TelnetCommand {
  int _article_index = 0;
  
  public BahamutCommandDeleteArticle(int paramInt) {
    this._article_index = paramInt;
    this.Action = 7;
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    if (this._article_index > 0) {
      String str = this._article_index + "\ndy";
      TelnetClient.getClient().sendStringToServer(str);
    } 
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public String toString() {
    return "[DeleteArticle][articleIndex=" + this._article_index + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandDeleteArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */