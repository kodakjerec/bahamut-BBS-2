package com.kota.Bahamut.Pages;

import android.os.Build;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.R;
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetCursor;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

public class LoginPage extends TelnetPage {
    boolean _cache_telnet_view = false;
    int _error_count = 0;
    ASProcessingDialog _login_process_dialog = null;
    View.OnClickListener _logout_listener = v -> {
        String err_message;
        LoginPage.this._username = ((EditText) LoginPage.this.findViewById(R.id.Login_UsernameEdit)).getText().toString().trim();
        LoginPage.this._password = ((EditText) LoginPage.this.findViewById(R.id.Login_PasswordEdit)).getText().toString().trim();
        LoginPage.this._save_logon_user = ((CheckBox) LoginPage.this.findViewById(R.id.Login_LoginRememberCheckBox)).isChecked();
        if (LoginPage.this._username.length() == 0 && LoginPage.this._password.length() == 0) {
            err_message = "帳號、密碼不可為空，請重新輸入。";
        } else if (LoginPage.this._username.length() == 0) {
            err_message = "帳號不可為空，請重新輸入。";
        } else if (LoginPage.this._password.length() == 0) {
            err_message = "密碼不可為空，請重新輸入。";
        } else {
            err_message = null;
        }
        if (err_message != null) {
            ASAlertDialog.createDialog().setTitle("錯誤").setMessage(err_message).addButton("確定").setListener((aDialog, index) -> aDialog.dismiss()).scheduleDismissOnPageDisappear(LoginPage.this).show();
        } else {
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

    public int getPageType() {
        return 1;
    }

    public int getPageLayout() {
        return R.layout.login_page;
    }

    public void onPageDidLoad() {
        this._settings = new UserSettings(getContext());
        // 登入
        getNavigationController().setNavigationTitle("勇者登入");
        findViewById(R.id.Login_LoginButton).setOnClickListener(this._logout_listener);
        // checkbox區塊點擊
        CheckBox checkBox = (CheckBox) findViewById(R.id.Login_LoginRememberCheckBox);
        findViewById(R.id.toolbar).setOnClickListener(view -> checkBox.setChecked(!checkBox.isChecked()));
        this._telnet_view = (TelnetView) findViewById(R.id.Login_TelnetView);
        // 讀取預設勇者設定
        loadLogonUser();
        System.out.println("current  version:" + Build.VERSION.SDK_INT);
    }

    public synchronized boolean onPagePreload() {
        return handleNormalState();
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        TelnetClient.getClient().close();
        return true;
    }

    private boolean handleNormalState() {
        String row_23 = TelnetClient.getModel().getRowString(23);
        TelnetCursor cursor = TelnetClient.getModel().getCursor();
        if (row_23.endsWith("再見 ...")) {
            onLoginAccountOverLimit();
            return false;
        } else if (row_23.startsWith("您想刪除其他重複的 login")) {
            onCheckRemoveLogonUser();
            return false;
        } else if (row_23.startsWith("★ 密碼輸入錯誤") && cursor.row == 23) {
            this._error_count++;
            onPasswordError();
            TelnetClient.getClient().sendStringToServer("");
            return false;
        } else if (row_23.startsWith("★ 錯誤的使用者代號") && cursor.row == 23) {
            this._error_count++;
            onUsernameError();
            TelnetClient.getClient().sendStringToServer("");
            return false;
        } else if (cursor.equals(23, 16)) {
            sendPassword();
            return false;
        } else {
            return true;
        }
    }

    private void loadLogonUser() {
        EditText login_username_field = (EditText) findViewById(R.id.Login_UsernameEdit);
        EditText login_password_field = (EditText) findViewById(R.id.Login_PasswordEdit);
        CheckBox login_remember = (CheckBox) findViewById(R.id.Login_LoginRememberCheckBox);
        String username = this._settings.getUsername();
        String password = this._settings.getPassword();
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }
        String username2 = username.trim();
        String password2 = password.trim();
        login_username_field.setText(username2);
        login_password_field.setText(password2);
        login_remember.setChecked(this._settings.isSaveLogonUser());
    }

    private void saveLogonUserToProperties() {
        CheckBox login_remember = (CheckBox) findViewById(R.id.Login_LoginRememberCheckBox);
        if (login_remember.isChecked()) {
            String username = ((EditText) findViewById(R.id.Login_UsernameEdit)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.Login_PasswordEdit)).getText().toString().trim();
            this._settings.setUsername(username);
            this._settings.setPassword(password);
        } else {
            this._settings.setUsername("");
            this._settings.setPassword("");
        }
        this._settings.setSaveLogonUser(login_remember.isChecked());
        this._settings.notifyDataUpdated();
    }

    public void onPageDidDisappear() {
        this._telnet_view = null;
        this._login_process_dialog = null;
        this._remove_logon_user_dialog = null;
        this._save_unfinished_article_dialog = null;
        clear();
    }

    public void onPageRefresh() {
        if (this._telnet_view != null) {
            setFrameToTelnetView();
        }
    }

    private void setFrameToTelnetView() {
        TelnetFrame frame = TelnetClient.getModel().getFrame().clone();
        frame.removeRow(23);
        frame.removeRow(22);
        this._telnet_view.setFrame(frame);
    }

    public void onPageWillDisappear() {
        ASProcessingDialog.hideProcessingDialog();
    }

    public void clear() {
        this._error_count = 0;
        this._cache_telnet_view = false;
        if (this._login_process_dialog != null) {
            this._login_process_dialog.dismiss();
            this._login_process_dialog = null;
        }
    }

    /* access modifiers changed from: private */
    public void login() {
        ASProcessingDialog.showProcessingDialog("登入中");
        new Thread(() -> TelnetClient.getClient().sendStringToServerInBackground(LoginPage.this._username)).start();
    }

    public void onCheckRemoveLogonUser() {
        new ASRunner() {
            public void run() {
                ASProcessingDialog.hideProcessingDialog();
                if (LoginPage.this._remove_logon_user_dialog == null) {
                    LoginPage.this._remove_logon_user_dialog = (ASAlertDialog) ASAlertDialog.createDialog().setTitle("提示").setMessage("您想刪除其他重複的登入嗎？").addButton("否").addButton("是").setListener((aDialog, index) -> {
                        if (index == 0) {
                            TelnetClient.getClient().sendStringToServerInBackground("n");
                        } else {
                            TelnetClient.getClient().sendStringToServerInBackground("y");
                        }
                        LoginPage.this._remove_logon_user_dialog = null;
                        ASProcessingDialog.showProcessingDialog("登入中");
                    }).setOnBackDelegate(aDialog -> {
                        TelnetClient.getClient().sendStringToServerInBackground("n");
                        LoginPage.this._remove_logon_user_dialog.dismiss();
                        LoginPage.this._remove_logon_user_dialog = null;
                        ASProcessingDialog.showProcessingDialog("登入中");
                        return true;
                    });
                    LoginPage.this._remove_logon_user_dialog.show();
                }
            }
        }.runInMainThread();
    }

    public void onPasswordError() {
        if (this._error_count < 3) {
            new ASRunner() {
                public void run() {
                    ASProcessingDialog.hideProcessingDialog();
                    ASAlertDialog.createDialog().setTitle("勇者密碼錯誤").setMessage("勇者密碼錯誤，請重新輸入勇者密碼").addButton("確定").scheduleDismissOnPageDisappear(LoginPage.this).show();
                }
            }.runInMainThread();
        } else {
            onLoginErrorAndDisconnected();
        }
    }

    public void onUsernameError() {
        if (this._error_count < 3) {
            new ASRunner() {
                public void run() {
                    ASProcessingDialog.hideProcessingDialog();
                    ASAlertDialog.createDialog().setTitle("勇者代號錯誤").setMessage("勇者代號錯誤，請重新輸入勇者代號").addButton("確定").scheduleDismissOnPageDisappear(LoginPage.this).show();
                }
            }.runInMainThread();
        } else {
            onLoginErrorAndDisconnected();
        }
    }

    public void onLoginErrorAndDisconnected() {
        new ASRunner() {
            public void run() {
                ASProcessingDialog.hideProcessingDialog();
                ASAlertDialog.createDialog().setTitle("斷線").setMessage("帳號密碼輸入錯誤次數過多，請重新連線。").addButton("確定").show();
            }
        }.runInMainThread();
    }

    public void sendPassword() {
        TelnetClient.getClient().sendStringToServer(this._password);
    }

    public void onLoginSuccess() {
        TelnetClient.getClient().setUsername(this._username);
        this._settings.setLastConnectionIsOfflineByUser(false);
        saveLogonUserToProperties();
        this._settings.notifyDataUpdated();
    }

    public void onSaveArticle() {
        if (this._save_unfinished_article_dialog == null) {
            this._save_unfinished_article_dialog = ASAlertDialog.createDialog().setTitle("提示").setMessage("您有一篇文章尚未完成").addButton("放棄").addButton("寫入暫存檔").setListener((aDialog, index) -> {
                switch (index) {
                    case 0:
                        TelnetClient.getClient().sendStringToServer("Q");
                        break;
                    case 1:
                        TelnetClient.getClient().sendStringToServer("S");
                        break;
                }
                LoginPage.this._save_unfinished_article_dialog = null;
            }).scheduleDismissOnPageDisappear(this);
            this._save_unfinished_article_dialog.show();
        }
    }

    public void onLoginAccountOverLimit() {
        new ASRunner() {
            public void run() {
                ASProcessingDialog.hideProcessingDialog();
                ASAlertDialog.createDialog().setTitle("警告").setMessage("您的帳號重覆登入超過上限，請選擇刪除其他重複的登入或將其它帳號登出。").addButton("確定").show();
            }
        }.runInMainThread();
    }
}
