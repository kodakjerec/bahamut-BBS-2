package com.kumi.Bahamut.Command;

import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetOutputBuilder;

public class BahamutCommandTheSameTitleBottom extends TelnetCommand {
  int _article_index = 0;
  
  public BahamutCommandTheSameTitleBottom(int paramInt) {
    this._article_index = paramInt;
  }
  
  public void execute(final TelnetListPage aListPage) {
    if (aListPage.getListType() == 1 || aListPage.getListType() == 2) {
      if (this._article_index == aListPage.getItemSize()) {
        (new ASRunner() {
            final BahamutCommandTheSameTitleBottom this$0;
            
            final TelnetListPage val$aListPage;
            
            public void run() {
              ASToast.showShortToast("找沒有了耶...:(");
              aListPage.onLoadItemFinished();
            }
          }).runInMainThread();
        setDone(true);
        return;
      } 
      TelnetOutputBuilder.create().pushString(aListPage.getItemSize() + "\n").sendToServer();
      return;
    } 
    if (this._article_index > 0) {
      TelnetOutputBuilder.create().pushKey(263).pushString("[").sendToServer();
      return;
    } 
    setDone(true);
  }
  
  public void executeFinished(final TelnetListPage aListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    if (paramTelnetListPageBlock.selectedItem.isDeleted || aListPage.isItemBlocked(paramTelnetListPageBlock.selectedItem)) {
      this._article_index = paramTelnetListPageBlock.selectedItemNumber;
      setDone(false);
      return;
    } 
    if (aListPage.isItemLoadingByNumber(paramTelnetListPageBlock.selectedItemNumber)) {
      (new ASRunner() {
          final BahamutCommandTheSameTitleBottom this$0;
          
          final TelnetListPage val$aListPage;
          
          public void run() {
            ASToast.showShortToast("找沒有了耶...:(");
            aListPage.onLoadItemFinished();
          }
        }).runInMainThread();
      setDone(true);
      return;
    } 
    aListPage.loadItemAtNumber(paramTelnetListPageBlock.selectedItemNumber);
    setDone(true);
  }
  
  public String toString() {
    return "[BahamutCommandTheSameTitleBottom][articleIndex=" + this._article_index + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandTheSameTitleBottom.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */