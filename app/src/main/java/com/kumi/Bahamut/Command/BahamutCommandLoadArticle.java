package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.BahamutStateHandler;
import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetClient;

public class BahamutCommandLoadArticle extends TelnetCommand {
  int _article_number = 0;
  
  public BahamutCommandLoadArticle(int paramInt) {
    this._article_number = paramInt;
    this.Action = 5;
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    if (this._article_number > 0) {
      String str = String.valueOf(this._article_number);
      BahamutStateHandler.getInstance().setArticleNumber(str);
      TelnetClient.getClient().sendStringToServerInBackground(str + "\n");
    } 
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public int getArticleIndex() {
    return this._article_number;
  }
  
  public String toString() {
    return "[LoadArticle][articleIndex=" + this._article_number + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandLoadArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */