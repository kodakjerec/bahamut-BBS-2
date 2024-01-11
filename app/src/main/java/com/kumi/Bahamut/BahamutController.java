package com.kumi.Bahamut;

import android.content.Context;
import android.content.Intent;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.Dialog.ASProcessingDialog;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Bahamut.DataModels.ArticleTempStore;
import com.kumi.Bahamut.DataModels.BookmarkStore;
import com.kumi.Bahamut.Pages.Model.BoardPageBlock;
import com.kumi.Bahamut.Pages.Model.BoardPageItem;
import com.kumi.Bahamut.Pages.Model.ClassPageBlock;
import com.kumi.Bahamut.Pages.Model.ClassPageItem;
import com.kumi.Bahamut.Pages.Model.MailBoxPageBlock;
import com.kumi.Bahamut.Pages.Model.MailBoxPageItem;
import com.kumi.Bahamut.Pages.StartPage;
import com.kumi.Bahamut.Service.BahaBBSBackgroundService;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetClientListener;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetPage;
import com.kumi.TextEncoder.B2UEncoder;
import com.kumi.TextEncoder.U2BEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

public class BahamutController extends ASNavigationController implements TelnetClientListener {
  private void handleNormalConnectionClosed() {
    Vector vector1 = ASNavigationController.getCurrentController().getViewControllers();
    Vector<TelnetPage> vector = new Vector();
    for (TelnetPage telnetPage : vector1) {
      if (telnetPage.getPageType() == 0 || telnetPage.isKeepOnOffline())
        vector.add(telnetPage); 
    } 
    StartPage startPage = PageContainer.getInstance().getStartPage();
    if (!vector.contains(startPage))
      vector.insertElementAt(startPage, 0); 
    setViewControllers(vector);
  }
  
  private void showConnectionStartMessage() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:hh:ss");
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    String str = simpleDateFormat.format(new Date());
    System.out.println("Baha!BBS connection start:" + str);
  }
  
  protected String getControllerName() {
    return "Baha!BBS";
  }
  
  public boolean isAnimationEnable() {
    boolean bool = false;
    if (!getSharedPreferences("user_setting", 0).getBoolean("AnimationDisable", false))
      bool = true; 
    return bool;
  }
  
  public boolean onBackLongPressed() {
    boolean bool = true;
    if (TelnetClient.getConnector().isConnecting()) {
      (new ASAlertDialog()).setMessage("是否確定要強制斷線?").addButton("取消").addButton("斷線").setListener(new ASAlertDialogListener() {
            final BahamutController this$0;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              if (param1Int == 1) {
                TelnetClient.getClient().close();
                UserSettings userSettings = new UserSettings((Context)BahamutController.this);
                userSettings.setLastConnectionIsOfflineByUser(true);
                userSettings.notifyDataUpdated();
              } 
            }
          }).show();
      ASProcessingDialog.hideProcessingDialog();
      return bool;
    } 
    bool = false;
    ASProcessingDialog.hideProcessingDialog();
    return bool;
  }
  
  protected void onControllerDidLoad() {
    pushViewController((ASViewController)PageContainer.getInstance().getStartPage(), false);
  }
  
  protected void onControllerWillLoad() {
    requestWindowFeature(1);
    try {
      B2UEncoder.constructInstance(getResources().openRawResource(2131492864));
      U2BEncoder.constructInstance(getResources().openRawResource(2131492865));
    } catch (Exception exception) {
      exception.printStackTrace();
    } 
    BookmarkStore.upgrade((Context)this, getFilesDir().getPath() + "/bookmark.dat");
    setAnimationEnable((new UserSettings((Context)this)).isAnimationEnable());
    ArticleTempStore.upgrade((Context)this, getFilesDir().getPath() + "/article_temp.dat");
    TelnetClient.construct(BahamutStateHandler.getInstance());
    TelnetClient.getClient().setListener(this);
    PageContainer.constructInstance();
  }
  
  public void onLowMemory() {
    super.onLowMemory();
    BoardPageBlock.release();
    BoardPageItem.release();
    ClassPageBlock.release();
    ClassPageItem.release();
    MailBoxPageBlock.release();
    MailBoxPageItem.release();
    System.gc();
  }
  
  protected void onPause() {
    super.onPause();
  }
  
  public void onTelnetClientConnectionClosed(TelnetClient paramTelnetClient) {
    stopService(new Intent((Context)this, BahaBBSBackgroundService.class));
    (new ASRunner() {
        final BahamutController this$0;
        
        public void run() {
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:hh:ss");
          simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
          String str = simpleDateFormat.format(new Date());
          System.out.println("Baha!BBS connection close:" + str);
          BahamutController.this.handleNormalConnectionClosed();
          ASToast.showShortToast("連線已中斷");
          ASProcessingDialog.hideProcessingDialog();
        }
      }).runInMainThread();
  }
  
  public void onTelnetClientConnectionFail(TelnetClient paramTelnetClient) {
    ASProcessingDialog.hideProcessingDialog();
    ASToast.showShortToast("連線失敗，請檢查網路連線或稍後再試");
  }
  
  public void onTelnetClientConnectionStart(TelnetClient paramTelnetClient) {
    (new ASRunner() {
        final BahamutController this$0;
        
        public void run() {
          BahamutController.this.showConnectionStartMessage();
        }
      }).runInMainThread();
  }
  
  public void onTelnetClientConnectionSuccess(TelnetClient paramTelnetClient) {
    startService(new Intent((Context)this, BahaBBSBackgroundService.class));
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\BahamutController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */