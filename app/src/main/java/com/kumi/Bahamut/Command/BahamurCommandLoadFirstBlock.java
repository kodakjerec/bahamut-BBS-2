package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;

public class BahamurCommandLoadFirstBlock extends TelnetCommand {
  public void execute(TelnetListPage paramTelnetListPage) {}
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public String toString() {
    return "[LoadFirstBlock]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamurCommandLoadFirstBlock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */