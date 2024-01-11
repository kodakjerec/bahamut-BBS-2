package com.kumi.Bahamut.Pages;

import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.kumi.ASFramework.Dialog.ASAlertDialog;
import com.kumi.ASFramework.Dialog.ASAlertDialogListener;
import com.kumi.ASFramework.Dialog.ASDialog;
import com.kumi.ASFramework.Dialog.ASDialogOnBackPressedDelegate;
import com.kumi.ASFramework.Dialog.ASProcessingDialog;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.Thread.ASRunner;
import com.kumi.Telnet.Model.TelnetFrame;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetCursor;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetPage;
import com.kumi.TelnetUI.TelnetView;

public class LoginPage extends TelnetPage {
  boolean _cache_telnet_view = false;
  
  int _error_count = 0;
  
  ASProcessingDialog _login_process_dialog = null;
  
  View.OnClickListener _logout_listener = new View.OnClickListener() {
      final LoginPage this$0;
      
      public void onClick(View param1View) {
        EditText editText1 = (EditText)LoginPage.this.findViewById(2131230865);
        EditText editText2 = (EditText)LoginPage.this.findViewById(2131230862);
        CheckBox checkBox = (CheckBox)LoginPage.this.findViewById(2131230860);
        LoginPage.this._username = editText1.getText().toString().trim();
        LoginPage.this._password = editText2.getText().toString().trim();
        LoginPage.this._save_logon_user = checkBox.isChecked();
        if (LoginPage.this._username.length() == 0 && LoginPage.this._password.length() == 0) {
          String str = "帳號、密碼不可為空，請重新輸入。";
        } else if (LoginPage.this._username.length() == 0) {
          String str = "帳號不可為空，請重新輸入。";
        } else if (LoginPage.this._password.length() == 0) {
          String str = "密碼不可為空，請重新輸入。";
        } else {
          checkBox = null;
        } 
        if (checkBox != null) {
          ASAlertDialog.createDialog().setTitle("錯誤").setMessage((String)checkBox).addButton("確定").setListener(new ASAlertDialogListener() {
                final LoginPage.null this$1;
                
                public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param2ASAlertDialog, int param2Int) {
                  param2ASAlertDialog.dismiss();
                }
              }).scheduleDismissOnPageDisappear((ASViewController)LoginPage.this).show();
          return;
        } 
        LoginPage.this.login();
      }
    };
  
  String _password = "";
  
  ASAlertDialog _remove_logon_user_dialog = null;
  
  boolean _save_logon_user = false;
  
  ASDialog _save_unfinished_article_dialog = null;
  
  UserSettings _settings;
  
  TelnetView _telnet_view = null;
  
  String _username = "";
  
  private boolean handleNormalState() {
    boolean bool2 = true;
    String str = TelnetClient.getModel().getRowString(23);
    TelnetCursor telnetCursor = TelnetClient.getModel().getCursor();
    if (str.endsWith("再見 ...")) {
      onLoginAccountOverLimit();
      return false;
    } 
    if (str.startsWith("您想刪除其他重複的 login")) {
      onCheckRemoveLogonUser();
      return false;
    } 
    if (str.startsWith("★ 密碼輸入錯誤") && telnetCursor.row == 23) {
      this._error_count++;
      onPasswordError();
      TelnetClient.getClient().sendStringToServer("");
      return false;
    } 
    if (str.startsWith("★ 錯誤的使用者代號") && telnetCursor.row == 23) {
      this._error_count++;
      onUsernameError();
      TelnetClient.getClient().sendStringToServer("");
      return false;
    } 
    if (telnetCursor.equals(23, 16)) {
      sendPassword();
      return false;
    } 
    boolean bool1 = bool2;
    if (telnetCursor.row == 22)
      bool1 = bool2; 
    return bool1;
  }
  
  private void loadLogonUser() {
    EditText editText1 = (EditText)findViewById(2131230865);
    EditText editText2 = (EditText)findViewById(2131230862);
    CheckBox checkBox = (CheckBox)findViewById(2131230860);
    String str2 = this._settings.getUsername();
    String str3 = this._settings.getPassword();
    String str1 = str2;
    if (str2 == null)
      str1 = ""; 
    str2 = str3;
    if (str3 == null)
      str2 = ""; 
    str1 = str1.trim();
    str2 = str2.trim();
    editText1.setText(str1);
    editText2.setText(str2);
    checkBox.setChecked(this._settings.isSaveLogonUser());
  }
  
  private void login() {
    ASProcessingDialog.showProcessingDialog("登入中");
    (new Thread() {
        final LoginPage this$0;
        
        public void run() {
          TelnetClient.getClient().sendStringToServerInBackground(LoginPage.this._username);
        }
      }).start();
  }
  
  private void saveLogonUserToProperties() {
    CheckBox checkBox = (CheckBox)findViewById(2131230860);
    if (checkBox.isChecked()) {
      EditText editText2 = (EditText)findViewById(2131230865);
      EditText editText1 = (EditText)findViewById(2131230862);
      String str2 = editText2.getText().toString().trim();
      String str1 = editText1.getText().toString().trim();
      this._settings.setUsername(str2);
      this._settings.setPassword(str1);
    } else {
      this._settings.setUsername("");
      this._settings.setPassword("");
    } 
    this._settings.setSaveLogonUser(checkBox.isChecked());
    this._settings.notifyDataUpdated();
  }
  
  private void setFrameToTelnetView() {
    TelnetFrame telnetFrame = TelnetClient.getModel().getFrame().clone();
    telnetFrame.removeRow(23);
    telnetFrame.removeRow(22);
    this._telnet_view.setFrame(telnetFrame);
  }
  
  public void clear() {
    this._error_count = 0;
    this._cache_telnet_view = false;
    if (this._login_process_dialog != null) {
      this._login_process_dialog.dismiss();
      this._login_process_dialog = null;
    } 
  }
  
  public int getPageLayout() {
    return 2131361843;
  }
  
  public int getPageType() {
    return 1;
  }
  
  protected boolean onBackPressed() {
    TelnetClient.getClient().close();
    return true;
  }
  
  public void onCheckRemoveLogonUser() {
    (new ASRunner() {
        final LoginPage this$0;
        
        public void run() {
          ASProcessingDialog.hideProcessingDialog();
          if (LoginPage.this._remove_logon_user_dialog == null) {
            LoginPage.this._remove_logon_user_dialog = (ASAlertDialog)ASAlertDialog.createDialog().setTitle("提示").setMessage("您想刪除其他重複的登入嗎？").addButton("否").addButton("是").setListener(new ASAlertDialogListener() {
                  final LoginPage.null this$1;
                  
                  public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param2ASAlertDialog, int param2Int) {
                    if (param2Int == 0) {
                      TelnetClient.getClient().sendStringToServerInBackground("n");
                    } else {
                      TelnetClient.getClient().sendStringToServerInBackground("y");
                    } 
                    LoginPage.this._remove_logon_user_dialog = null;
                    ASProcessingDialog.showProcessingDialog("登入中");
                  }
                }).setOnBackDelegate(new ASDialogOnBackPressedDelegate() {
                  final LoginPage.null this$1;
                  
                  public boolean onASDialogBackPressed(ASDialog param2ASDialog) {
                    TelnetClient.getClient().sendStringToServerInBackground("n");
                    LoginPage.this._remove_logon_user_dialog.dismiss();
                    LoginPage.this._remove_logon_user_dialog = null;
                    ASProcessingDialog.showProcessingDialog("登入中");
                    return true;
                  }
                });
            LoginPage.this._remove_logon_user_dialog.show();
          } 
        }
      }).runInMainThread();
  }
  
  public void onLoginAccountOverLimit() {
    (new ASRunner() {
        final LoginPage this$0;
        
        public void run() {
          ASProcessingDialog.hideProcessingDialog();
          ASAlertDialog.createDialog().setTitle("警告").setMessage("您的帳號重覆登入超過上限，請選擇刪除其他重複的登入或將其它帳號登出。").addButton("確定").show();
        }
      }).runInMainThread();
  }
  
  public void onLoginErrorAndDisconnected() {
    (new ASRunner() {
        final LoginPage this$0;
        
        public void run() {
          ASProcessingDialog.hideProcessingDialog();
          ASAlertDialog.createDialog().setTitle("斷線").setMessage("帳號密碼輸入錯誤次數過多，請重新連線。").addButton("確定").show();
        }
      }).runInMainThread();
  }
  
  public void onLoginSuccess() {
    TelnetClient.getClient().setUsername(this._username);
    this._settings.setLastConnectionIsOfflineByUser(false);
    saveLogonUserToProperties();
    this._settings.notifyDataUpdated();
  }
  
  public void onPageDidDisappear() {
    this._telnet_view = null;
    this._login_process_dialog = null;
    this._remove_logon_user_dialog = null;
    this._save_unfinished_article_dialog = null;
    clear();
  }
  
  public void onPageDidLoad() {
    this._settings = new UserSettings(getContext());
    getNavigationController().setNavigationTitle("勇者登入");
    ((Button)findViewById(2131230859)).setOnClickListener(this._logout_listener);
    this._telnet_view = (TelnetView)findViewById(2131230864);
    loadLogonUser();
    System.out.println("current  version:" + Build.VERSION.SDK_INT);
  }
  
  public boolean onPagePreload() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: invokespecial handleNormalState : ()Z
    //   6: istore_1
    //   7: aload_0
    //   8: monitorexit
    //   9: iload_1
    //   10: ireturn
    //   11: astore_2
    //   12: aload_0
    //   13: monitorexit
    //   14: aload_2
    //   15: athrow
    // Exception table:
    //   from	to	target	type
    //   2	7	11	finally
  }
  
  public void onPageRefresh() {
    if (this._telnet_view != null)
      setFrameToTelnetView(); 
  }
  
  public void onPageWillDisappear() {
    ASProcessingDialog.hideProcessingDialog();
  }
  
  public void onPasswordError() {
    if (this._error_count < 3) {
      (new ASRunner() {
          final LoginPage this$0;
          
          public void run() {
            ASProcessingDialog.hideProcessingDialog();
            ASAlertDialog.createDialog().setTitle("勇者密碼錯誤").setMessage("勇者密碼錯誤，請重新輸入勇者密碼").addButton("確定").scheduleDismissOnPageDisappear((ASViewController)LoginPage.this).show();
          }
        }).runInMainThread();
      return;
    } 
    onLoginErrorAndDisconnected();
  }
  
  public void onSaveArticle() {
    if (this._save_unfinished_article_dialog == null) {
      this._save_unfinished_article_dialog = ASAlertDialog.createDialog().setTitle("提示").setMessage("您有一篇文章尚未完成").addButton("放棄").addButton("寫入暫存檔").setListener(new ASAlertDialogListener() {
            final LoginPage this$0;
            
            public void onAlertDialogDismissWithButtonIndex(ASAlertDialog param1ASAlertDialog, int param1Int) {
              switch (param1Int) {
                default:
                  LoginPage.this._save_unfinished_article_dialog = null;
                  return;
                case 0:
                  TelnetClient.getClient().sendStringToServer("Q");
                case 1:
                  break;
              } 
              TelnetClient.getClient().sendStringToServer("S");
            }
          }).scheduleDismissOnPageDisappear((ASViewController)this);
      this._save_unfinished_article_dialog.show();
    } 
  }
  
  public void onUsernameError() {
    if (this._error_count < 3) {
      (new ASRunner() {
          final LoginPage this$0;
          
          public void run() {
            ASProcessingDialog.hideProcessingDialog();
            ASAlertDialog.createDialog().setTitle("勇者代號錯誤").setMessage("勇者代號錯誤，請重新輸入勇者代號").addButton("確定").scheduleDismissOnPageDisappear((ASViewController)LoginPage.this).show();
          }
        }).runInMainThread();
      return;
    } 
    onLoginErrorAndDisconnected();
  }
  
  public void sendPassword() {
    TelnetClient.getClient().sendStringToServer(this._password);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\LoginPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */