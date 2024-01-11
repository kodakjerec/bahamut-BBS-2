package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetClient;

public class BahamutCommandListArticle extends TelnetCommand {
  int _article_index = 0;
  
  public BahamutCommandListArticle(int paramInt) {
    this._article_index = paramInt;
    this.Action = 6;
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    if (this._article_index > 0) {
      String str = String.valueOf(this._article_index) + "\nS";
      TelnetClient.getClient().sendDataToServer(str.getBytes());
    } 
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public String toString() {
    return "[ListArticle][articleIndex=" + this._article_index + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandListArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */