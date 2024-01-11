package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetClient;

public class BahamutCommandGoodArticle extends TelnetCommand {
  int _article_index = 0;
  
  public BahamutCommandGoodArticle(int paramInt) {
    this._article_index = paramInt;
    this.Action = 9;
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    if (this._article_index > 0) {
      String str = this._article_index + "\ngy";
      TelnetClient.getClient().sendStringToServer(str);
    } 
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public String toString() {
    return "[GoodArticle][articleIndex=" + this._article_index + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandGoodArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */