package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetClient;

public class BahamutCommandSearchArticle extends TelnetCommand {
  private String _author = "";
  
  private String _gy = "";
  
  private String _keyword = "";
  
  private String _mark = "";
  
  public BahamutCommandSearchArticle(String paramString1, String paramString2, String paramString3, String paramString4) {
    this._keyword = paramString1;
    this._author = paramString2;
    this._mark = paramString3;
    this._gy = paramString4;
    this.Action = 4;
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    String str = "~" + this._keyword + "\n" + this._author + "\n" + this._mark + "\n" + this._gy;
    TelnetClient.getClient().sendStringToServerInBackground(str);
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public String toString() {
    return "[SearchArticle][keyword=" + this._keyword + " author=" + this._author + " mark=" + this._mark + " gy=" + this._gy + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandSearchArticle.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */