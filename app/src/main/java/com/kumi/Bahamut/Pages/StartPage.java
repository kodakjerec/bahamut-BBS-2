package com.kumi.Bahamut.Pages;

import android.view.View;
import android.widget.Button;
import com.kumi.ASFramework.Dialog.ASProcessingDialog;
import com.kumi.ASFramework.Dialog.ASProcessingDialogOnBackDelegate;
import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.PageContainer;
import com.kumi.Telnet.TelnetClient;
import com.kumi.TelnetUI.TelnetPage;

public class StartPage extends TelnetPage {
  View.OnClickListener _connect_listener = new View.OnClickListener() {
      final StartPage this$0;
      
      public void onClick(View param1View) {
        StartPage.this.onConnectButtonClicked();
      }
    };
  
  View.OnClickListener _exit_listener = new View.OnClickListener() {
      final StartPage this$0;
      
      public void onClick(View param1View) {
        StartPage.this.onExitButtonClicked();
      }
    };
  
  private void onConnectButtonClicked() {
    connect();
  }
  
  private void onExitButtonClicked() {
    getNavigationController().finish();
  }
  
  public void clear() {
    ASProcessingDialog.hideProcessingDialog();
  }
  
  public void connect() {
    if (getNavigationController().getDeviceController().isNetworkAvailable()) {
      ASProcessingDialog.showProcessingDialog("連線中", new ASProcessingDialogOnBackDelegate() {
            final StartPage this$0;
            
            public boolean onASProcessingDialogOnBackDetected(ASProcessingDialog param1ASProcessingDialog) {
              TelnetClient.getClient().close();
              return false;
            }
          });
      (new ASRunner() {
          final StartPage this$0;
          
          public void run() {
            TelnetClient.getClient().connect("bbs.gamer.com.tw", 23);
          }
        }).runInNewThread();
      return;
    } 
    ASToast.showShortToast("您未連接網路");
  }
  
  public int getPageLayout() {
    return 2131361870;
  }
  
  public int getPageType() {
    return 0;
  }
  
  protected boolean onBackPressed() {
    onExitButtonClicked();
    return true;
  }
  
  public void onPageDidDisappear() {
    clear();
    super.onPageDidDisappear();
  }
  
  public void onPageDidLoad() {
    getNavigationController().setNavigationTitle("勇者入口");
    ((Button)findViewById(2131230928)).setOnClickListener(this._exit_listener);
    ((Button)findViewById(2131230927)).setOnClickListener(this._connect_listener);
  }
  
  public boolean onPagePreload() {
    return true;
  }
  
  public void onPageRefresh() {}
  
  public void onPageWillAppear() {
    PageContainer.getInstance().cleanLoginPage();
    PageContainer.getInstance().cleanMainPage();
    PageContainer.getInstance().cleanClassPage();
    PageContainer.getInstance().cleanBoardPage();
    PageContainer.getInstance().cleanBoardSearchPage();
    PageContainer.getInstance().cleanBoardTitleLinkedPage();
    PageContainer.getInstance().cleanMainBoxPage();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\StartPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */