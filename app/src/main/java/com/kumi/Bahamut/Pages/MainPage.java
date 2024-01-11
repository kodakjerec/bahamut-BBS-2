package com.kumi.Bahamut.Pages;

import android.view.View;
import android.widget.Button;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.Dialog.ASDialog;
import com.kumi.ASFramework.Dialog.ASDialogOnBackPressedDelegate;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.Bahamut.BahamutStateHandler;
import com.kumi.Bahamut.PageContainer;
import com.kumi.Telnet.Model.TelnetFrame;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetPage;
import com.kumi.TelnetUI.TelnetView;

public class MainPage extends TelnetPage {
  View.OnClickListener _boards_listener = new View.OnClickListener() {
      final MainPage this$0;
      
      public void onClick(View param1View) {
        PageContainer.getInstance().pushClassPage("Boards", "佈告討論區");
        MainPage.this.getNavigationController().pushViewController((ASViewController)PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("b");
      }
    };
  
  View.OnClickListener _class_listener = new View.OnClickListener() {
      final MainPage this$0;
      
      public void onClick(View param1View) {
        PageContainer.getInstance().pushClassPage("Class", "分組討論區");
        MainPage.this.getNavigationController().pushViewController((ASViewController)PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("c");
      }
    };
  
  View.OnClickListener _favorite_listener = new View.OnClickListener() {
      final MainPage this$0;
      
      public void onClick(View param1View) {
        PageContainer.getInstance().pushClassPage("Favorite", "我的最愛");
        MainPage.this.getNavigationController().pushViewController((ASViewController)PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("f");
      }
    };
  
  TelnetFrame _frame_buffer = null;
  
  ASDialog _goodbye_dialog = null;
  
  private LastLoadClass _last_load_class = LastLoadClass.Unload;
  
  View.OnClickListener _logout_listener = new View.OnClickListener() {
      final MainPage this$0;
      
      public void onClick(View param1View) {
        TelnetClient.getClient().sendStringToServerInBackground("g");
      }
    };
  
  View.OnClickListener _mail_listener = new View.OnClickListener() {
      final MainPage this$0;
      
      public void onClick(View param1View) {
        MainPage.this.getNavigationController().pushViewController((ASViewController)PageContainer.getInstance().getMailBoxPage());
        TelnetClient.getClient().sendStringToServerInBackground("m\nr");
      }
    };
  
  ASDialog _save_hot_message_dialog = null;
  
  View.OnClickListener _system_setting_listener = new View.OnClickListener() {
      final MainPage this$0;
      
      public void onClick(View param1View) {
        SystemSettingsPage systemSettingsPage = new SystemSettingsPage();
        MainPage.this.getNavigationController().pushViewController((ASViewController)systemSettingsPage);
      }
    };
  
  private void setFrameToTelnetView() {
    TelnetView telnetView = (TelnetView)findViewById(2131230898);
    if (telnetView != null) {
      if (BahamutStateHandler.getInstance().getCurrentPage() == 5) {
        this._frame_buffer = TelnetClient.getModel().getFrame().clone();
        for (byte b = 12; b < 24; b++)
          this._frame_buffer.removeRow(12); 
        this._frame_buffer.removeRow(0);
      } 
      if (this._frame_buffer != null)
        telnetView.setFrame(this._frame_buffer); 
    } 
  }
  
  public void clear() {
    if (this._goodbye_dialog != null) {
      if (this._goodbye_dialog.isShowing())
        this._goodbye_dialog.dismiss(); 
      this._goodbye_dialog = null;
    } 
    if (this._save_hot_message_dialog != null) {
      if (this._save_hot_message_dialog.isShowing())
        this._save_hot_message_dialog.dismiss(); 
      this._save_hot_message_dialog = null;
    } 
  }
  
  public int getPageLayout() {
    return 2131361847;
  }
  
  public int getPageType() {
    return 5;
  }
  
  protected boolean onBackPressed() {
    this._logout_listener.onClick(null);
    return true;
  }
  
  public void onCheckGoodbye() {
    if (this._goodbye_dialog == null) {
      this._goodbye_dialog = ASAlertDialog.createDialog().setTitle("登出").setMessage("是否確定要登出?").addButton("取消").addButton("確認").setListener(new ASAlertDialogListener() {
            final MainPage this$0;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              MainPage.this._goodbye_dialog = null;
              switch (param1Int) {
                default:
                  return;
                case 0:
                  TelnetClient.getClient().sendStringToServerInBackground("Q");
                case 1:
                  break;
              } 
              UserSettings userSettings = new UserSettings(MainPage.this.getContext());
              userSettings.setLastConnectionIsOfflineByUser(true);
              userSettings.notifyDataUpdated();
              TelnetClient.getClient().sendStringToServerInBackground("G");
            }
          }).scheduleDismissOnPageDisappear((ASViewController)this);
      this._goodbye_dialog.show();
    } 
  }
  
  public void onPageDidDisappear() {
    this._goodbye_dialog = null;
    this._save_hot_message_dialog = null;
    super.onPageDidDisappear();
  }
  
  public void onPageDidLoad() {
    ((Button)findViewById(2131230891)).setOnClickListener(this._boards_listener);
    ((Button)findViewById(2131230892)).setOnClickListener(this._class_listener);
    ((Button)findViewById(2131230893)).setOnClickListener(this._favorite_listener);
    ((Button)findViewById(2131230895)).setOnClickListener(this._logout_listener);
    ((Button)findViewById(2131230896)).setOnClickListener(this._mail_listener);
    ((Button)findViewById(2131230897)).setOnClickListener(this._system_setting_listener);
  }
  
  public boolean onPagePreload() {
    this._last_load_class = LastLoadClass.Unload;
    return true;
  }
  
  public void onPageRefresh() {
    setFrameToTelnetView();
  }
  
  public void onPageWillDisappear() {
    clear();
  }
  
  public void onProcessHotMessage() {
    if (this._save_hot_message_dialog == null) {
      this._save_hot_message_dialog = ASAlertDialog.createDialog().setTitle("熱訊").setMessage("本次上站熱訊處理 ").addButton("備忘錄").addButton("保留").addButton("清除").setListener(new ASAlertDialogListener() {
            final MainPage this$0;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              MainPage.this._save_hot_message_dialog = null;
              switch (param1Int) {
                default:
                  return;
                case 0:
                  TelnetClient.getClient().sendStringToServerInBackground("M");
                case 1:
                  TelnetClient.getClient().sendStringToServerInBackground("K");
                case 2:
                  break;
              } 
              TelnetClient.getClient().sendStringToServerInBackground("C");
            }
          }).scheduleDismissOnPageDisappear((ASViewController)this).setOnBackDelegate(new ASDialogOnBackPressedDelegate() {
            final MainPage this$0;
            
            public boolean onASDialogBackPressed(ASDialog param1ASDialog) {
              TelnetClient.getClient().sendStringToServerInBackground("K\nQ");
              MainPage.this._save_hot_message_dialog = null;
              return false;
            }
          });
      this._save_hot_message_dialog.show();
    } 
  }
  
  private enum LastLoadClass {
    Boards, Class, Favorite, Unload;
    
    private static final LastLoadClass[] $VALUES;
    
    static {
      $VALUES = new LastLoadClass[] { Unload, Boards, Class, Favorite };
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\MainPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */