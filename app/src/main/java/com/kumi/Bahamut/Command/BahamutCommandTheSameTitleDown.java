package com.kumi.Bahamut.Command;

import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetOutputBuilder;

public class BahamutCommandTheSameTitleDown extends TelnetCommand {
  int _article_index = 0;
  
  public BahamutCommandTheSameTitleDown(int paramInt) {
    this._article_index = paramInt;
  }
  
  public void execute(final TelnetListPage aListPage) {
    if (aListPage.getListType() == 1 || aListPage.getListType() == 2) {
      if (this._article_index == aListPage.getItemSize()) {
        (new ASRunner() {
            final BahamutCommandTheSameTitleDown this$0;
            
            final TelnetListPage val$aListPage;
            
            public void run() {
              ASToast.showShortToast("無下一篇同主題文章");
              aListPage.onLoadItemFinished();
            }
          }).runInMainThread();
        setDone(true);
        return;
      } 
      TelnetOutputBuilder.create().pushString(this._article_index + "\n").pushKey(259).sendToServer();
      return;
    } 
    if (this._article_index > 0) {
      TelnetOutputBuilder.create().pushString(this._article_index + "\n]").sendToServer();
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
          final BahamutCommandTheSameTitleDown this$0;
          
          final TelnetListPage val$aListPage;
          
          public void run() {
            ASToast.showShortToast("無下一篇同主題文章");
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
    return "[TheSameTitleUp][articleIndex=" + this._article_index + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandTheSameTitleDown.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */