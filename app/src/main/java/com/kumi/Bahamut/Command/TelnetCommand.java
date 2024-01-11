package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;

public abstract class TelnetCommand implements BahamutCommandDefs {
  public int Action = 0;
  
  private boolean _is_done = false;
  
  public boolean recordTime = true;
  
  public abstract void execute(TelnetListPage paramTelnetListPage);
  
  public abstract void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock);
  
  public boolean isDone() {
    return this._is_done;
  }
  
  public boolean isOperationCommand() {
    return true;
  }
  
  public void setDone(boolean paramBoolean) {
    this._is_done = paramBoolean;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\TelnetCommand.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */