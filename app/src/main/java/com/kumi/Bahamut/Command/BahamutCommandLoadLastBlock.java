package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetOutputBuilder;

public class BahamutCommandLoadLastBlock extends TelnetCommand {
  private OperationMode getLoadLastBlockMode(TelnetListPage paramTelnetListPage) {
    OperationMode operationMode = OperationMode.End;
    switch (paramTelnetListPage.getListType()) {
      default:
        if (paramTelnetListPage != null && paramTelnetListPage.getSelectedIndex() == paramTelnetListPage.getItemSize())
          return (paramTelnetListPage.getItemSize() == 1) ? OperationMode.Left_Right_End : OperationMode.Home_End; 
        break;
      case 1:
        return OperationMode.Left_S_End;
      case 2:
        return (paramTelnetListPage.getSelectedIndex() == paramTelnetListPage.getItemSize()) ? ((paramTelnetListPage.getSelectedIndex() > 1) ? OperationMode.Home_End : OperationMode.NotAvailable) : OperationMode.End;
    } 
    return OperationMode.End;
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    if (paramTelnetListPage == null) {
      setDone(true);
      return;
    } 
    OperationMode operationMode = getLoadLastBlockMode(paramTelnetListPage);
    switch (operationMode) {
      default:
        setDone(true);
        return;
      case Left_Right_End:
        TelnetOutputBuilder.create().pushKey(256).pushKey(257).pushKey(263).sendToServer();
        return;
      case Home_End:
        TelnetOutputBuilder.create().pushKey(262).pushKey(263).sendToServer();
        return;
      case Left_S_End:
        TelnetOutputBuilder.create().pushKey(256).pushKey(83).pushKey(263).sendToServer();
        return;
      case End:
        break;
    } 
    TelnetOutputBuilder.create().pushKey(263).sendToServer();
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    if (paramTelnetListPage.getItemSize() > paramTelnetListPageBlock.maximumItemNumber) {
      paramTelnetListPage.setItemSize(0);
      paramTelnetListPage.cleanAllItem();
    } 
    setDone(true);
  }
  
  public String toString() {
    return "[LoadLastBlock]";
  }
  
  public enum OperationMode {
    End, Home_End, Left_Right_End, Left_S_End, NotAvailable;
    
    private static final OperationMode[] $VALUES;
    
    static {
      $VALUES = new OperationMode[] { End, Left_Right_End, Home_End, Left_S_End, NotAvailable };
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandLoadLastBlock.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */