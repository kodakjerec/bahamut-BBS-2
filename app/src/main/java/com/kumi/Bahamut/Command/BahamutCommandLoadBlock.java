package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.Logic.ItemUtils;
import com.kumi.Telnet.TelnetClient;

public class BahamutCommandLoadBlock extends TelnetCommand {
  private int _block = -1;
  
  public BahamutCommandLoadBlock(int paramInt) {
    this._block = paramInt;
  }
  
  public boolean containsArticle(int paramInt) {
    paramInt = ItemUtils.getBlock(paramInt);
    return (this._block == paramInt);
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    if (this._block >= 0) {
      int i = this._block;
      TelnetClient.getClient().sendStringToServer(String.valueOf(i * 20 + 1));
    } 
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public boolean isOperationCommand() {
    return false;
  }
  
  public String toString() {
    int i = this._block;
    return "[LoadBlock][block=" + this._block + " targetIndex=" + (i * 20 + 1) + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandLoadBlock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */