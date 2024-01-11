package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;

public class BahamutCommandMoveToLastBlock extends BahamutCommandLoadLastBlock {
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    super.executeFinished(paramTelnetListPage, paramTelnetListPageBlock);
    paramTelnetListPage.pushRefreshCommand(1);
    setDone(true);
  }
  
  public String toString() {
    return "[MoveToLastBlock]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandMoveToLastBlock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */