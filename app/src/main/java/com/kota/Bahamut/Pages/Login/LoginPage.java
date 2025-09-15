package com.kota.Bahamut.Pages.Login;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.DataModels.UrlDatabase;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.CloudBackup;
import com.kota.Bahamut.Service.NotificationSettings;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetCursor;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

public class LoginPage extends TelnetPage {
    boolean _cache_telnet_view = false;
    int _error_count = 0;
    View.OnClickListener _login_listener = v -> {
        String err_message;
        LoginPage.this._username = ((EditText) LoginPage.this.findViewById(R.id.Login_UsernameEdit)).getText()
                .toString().trim();
        LoginPage.this._password = ((EditText) LoginPage.this.findViewById(R.id.Login_passwordEdit)).getText()
                .toString().trim();
        LoginPage.this._save_logon_user = ((CheckBox) LoginPage.this.findViewById(R.id.Login_loginRememberCheckBox))
                .isChecked();
        LoginPage.this.checkWebSignIn = ((CheckBox) LoginPage.this.findViewById(R.id.LoginWebSignInCheckBox))
                .isChecked();

        if (LoginPage.this._username.isEmpty() && LoginPage.this._password.isEmpty()) {
            err_message = "帳號、密碼不可為空，請重新輸入。";
        } else if (LoginPage.this._username.isEmpty()) {
            err_message = "帳號不可為空，請重新輸入。";
        } else if (LoginPage.this._password.isEmpty()) {
            err_message = "密碼不可為空，請重新輸入。";
        } else {
            err_message = null;
        }
        if (err_message != null) {
            ASAlertDialog.showErrorDialog(err_message, LoginPage.this);
        } else {
            LoginPage.this.login();
        }
    };
    String _username = ""; // 使用者名稱
    String _password = ""; // 密碼
    boolean _save_logon_user = false; // 是否儲存登入使用者
    boolean checkWebSignIn = false; // 是否勾選Web登入
    ASAlertDialog _remove_logon_user_dialog = null; // 刪除重複登入對話框
    ASDialog _save_unfinished_article_dialog = null; // 儲存未完成文章對話框
    TelnetView _telnet_view = null; // Telnet視圖

    @Override
    public int getPageType() {
        return BahamutPage.BAHAMUT_LOGIN;
    }

    @Override
    public int getPageLayout() {
        return R.layout.login_page;
    }

    @Override
    public void onPageDidLoad() {
        // 清空暫存和執行中變數
        TempSettings.clearTempSettings(); // 清除暫存資料
        try (UrlDatabase urlDatabase = new UrlDatabase(getContext())) { // 清除URL資料庫
            urlDatabase.clearDb();
        } catch (Exception e) {
            Log.e("Bookmark", "initial fail");
        }

        // 登入
        getNavigationController().setNavigationTitle("勇者登入");
        findViewById(R.id.Login_loginButton).setOnClickListener(_login_listener);
        // checkbox區塊點擊
        CheckBox checkBox = (CheckBox) findViewById(R.id.Login_loginRememberCheckBox);
        findViewById(R.id.loginRememberLabel).setOnClickListener(view -> {
            checkBox.setChecked(!checkBox.isChecked());
            UserSettings.setPropertiesSaveLogonUser(checkBox.isChecked());
            UserSettings.notifyDataUpdated();
        });
        // web登入
        CheckBox webLoginCheckBox = (CheckBox) findViewById(R.id.LoginWebSignInCheckBox);
        findViewById(R.id.LoginWebSignInLabel).setOnClickListener(view -> {
            webLoginCheckBox.setChecked(!webLoginCheckBox.isChecked());
            UserSettings.setPropertiesWebSignIn(webLoginCheckBox.isChecked());
            UserSettings.notifyDataUpdated();
        });
        // TelnetView
        _telnet_view = (TelnetView) findViewById(R.id.Login_TelnetView);

        // 讀取預設勇者設定
        loadLogonUser();

        // VIP
        if (UserSettings.getPropertiesVIP()) {
            RelativeLayout blockWebSignIn = (RelativeLayout) findViewById(R.id.BlockWebSignIn);
            blockWebSignIn.setVisibility(View.VISIBLE);
        }

        // 替換外觀
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.toolbar);
        new ThemeFunctions().layoutReplaceTheme(mainLayout);
    }

    @Override
    public synchronized boolean onPagePreload() {
        return handleNormalState();
    }

    @Override
    // 按下返回
    protected boolean onBackPressed() {
        TelnetClient.getClient().close();
        return true;
    }

    @Override
    public void onPageDidDisappear() {
        _telnet_view = null;
        _remove_logon_user_dialog = null;
        _save_unfinished_article_dialog = null;
        clear();
    }

    @Override
    public void onPageRefresh() {
        if (_telnet_view != null) {
            setFrameToTelnetView();
        }
    }

    @Override
    public void onPageWillDisappear() {
        ASProcessingDialog.dismissProcessingDialog();
    }

    @Override
    public void clear() {
        _error_count = 0;
        _cache_telnet_view = false;
    }

    boolean handleNormalState() {
        String row_23 = TelnetClient.getModel().getRowString(23);
        TelnetCursor cursor = TelnetClient.getModel().getCursor();
        if (row_23.endsWith("再見 ...")) {
            onLoginAccountOverLimit();
            return false;
        } else if (row_23.startsWith("您想刪除其他重複的 login")) {
            onCheckRemoveLogonUser();
            return false;
        } else if (row_23.startsWith("★ 密碼輸入錯誤") && cursor.row == 23) {
            _error_count++;
            onPasswordError();
            TelnetClient.getClient().sendStringToServer("");
            return false;
        } else if (row_23.startsWith("★ 錯誤的使用者代號") && cursor.row == 23) {
            _error_count++;
            onUsernameError();
            TelnetClient.getClient().sendStringToServer("");
            return false;
        } else if (cursor.equals(23, 16)) {
            // 開啟"自動登入中"
            if (UserSettings.getPropertiesAutoToChat()) {
                TempSettings.isUnderAutoToChat = true;
            }
            sendPassword();
            return false;
        } else {
            return true;
        }
    }

    /**
     * 讀取預設勇者設定
     */
    void loadLogonUser() {
        EditText login_username_field = (EditText) findViewById(R.id.Login_UsernameEdit);
        EditText login_password_field = (EditText) findViewById(R.id.Login_passwordEdit);
        CheckBox login_remember = (CheckBox) findViewById(R.id.Login_loginRememberCheckBox);
        CheckBox login_web_sign_in = (CheckBox) findViewById(R.id.LoginWebSignInCheckBox);
        String username = UserSettings.getPropertiesUsername();
        String password = UserSettings.getPropertiesPassword();
        String username2 = username.trim();
        String password2 = password.trim();
        login_username_field.setText(username2);
        login_password_field.setText(password2);
        login_remember.setChecked(UserSettings.getPropertiesSaveLogonUser());
        login_web_sign_in.setChecked(UserSettings.getPropertiesWebSignIn());
    }

    /**
     * 儲存勇者設定到屬性
     */
    void saveLogonUserToProperties() {
        CheckBox login_remember = (CheckBox) findViewById(R.id.Login_loginRememberCheckBox);
        String username = ((EditText) findViewById(R.id.Login_UsernameEdit)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.Login_passwordEdit)).getText().toString().trim();

        if (login_remember.isChecked()) {
            UserSettings.setPropertiesUsername(username);
            UserSettings.setPropertiesPassword(password);
        } else {
            UserSettings.setPropertiesUsername("");
            UserSettings.setPropertiesPassword("");
        }
    }

    /**
     * 設定TelnetView的畫面
     */
    void setFrameToTelnetView() {
        TelnetFrame frame = TelnetClient.getModel().getFrame().clone();
        frame.removeRow(23);
        frame.removeRow(22);
        _telnet_view.setFrame(frame);
    }

    /**
     * 登入
     */
    void login() {
        ASProcessingDialog.showProcessingDialog("登入中");
        ASRunner.runInNewThread(() -> TelnetClient.getClient()
                .sendStringToServerInBackground(LoginPage.this._username));
    }

    /**
     * 檢查是否刪除重複登入
     */
    public void onCheckRemoveLogonUser() {
        new ASRunner() {
            public void run() {
                ASProcessingDialog.dismissProcessingDialog();
                if (LoginPage.this._remove_logon_user_dialog == null) {
                    LoginPage.this._remove_logon_user_dialog = (ASAlertDialog) ASAlertDialog.createDialog()
                            .setTitle("提示")
                            .setMessage("您想刪除其他重複的登入嗎？")
                            .addButton("否")
                            .addButton("是")
                            .setListener((aDialog, index) -> {
                                if (index == 0) {
                                    TelnetClient.getClient().sendStringToServerInBackground("n");
                                } else {
                                    TelnetClient.getClient().sendStringToServerInBackground("y");
                                }
                                LoginPage.this._remove_logon_user_dialog = null;
                                ASProcessingDialog.showProcessingDialog("登入中");
                            }).setOnBackDelegate(aDialog -> {
                                TelnetClient.getClient().sendStringToServerInBackground("n");
                                if (LoginPage.this._remove_logon_user_dialog != null) {
                                    LoginPage.this._remove_logon_user_dialog.dismiss();
                                    LoginPage.this._remove_logon_user_dialog = null;
                                }
                                ASProcessingDialog.showProcessingDialog("登入中");
                                return true;
                            });
                }
                LoginPage.this._remove_logon_user_dialog.show();
            }
        }.runInMainThread();
    }

    /**
     * 密碼錯誤
     */
    public void onPasswordError() {
        if (_error_count < 3) {
            new ASRunner() {
                public void run() {
                    ASProcessingDialog.dismissProcessingDialog();
                    ASAlertDialog.createDialog().setTitle("勇者密碼錯誤").setMessage("勇者密碼錯誤，請重新輸入勇者密碼").addButton("確定")
                            .scheduleDismissOnPageDisappear(LoginPage.this).show();
                }
            }.runInMainThread();
        } else {
            onLoginErrorAndDisconnected();
        }
    }

    /**
     * 使用者名稱錯誤
     */
    public void onUsernameError() {
        if (_error_count < 3) {
            new ASRunner() {
                public void run() {
                    ASProcessingDialog.dismissProcessingDialog();
                    ASAlertDialog.createDialog().setTitle("勇者代號錯誤").setMessage("勇者代號錯誤，請重新輸入勇者代號").addButton("確定")
                            .scheduleDismissOnPageDisappear(LoginPage.this).show();
                }
            }.runInMainThread();
        } else {
            onLoginErrorAndDisconnected();
        }
    }

    /**
     * 登入錯誤並斷線
     */
    public void onLoginErrorAndDisconnected() {
        new ASRunner() {
            public void run() {
                ASProcessingDialog.dismissProcessingDialog();
                ASAlertDialog.createDialog().setTitle("斷線").setMessage("帳號密碼輸入錯誤次數過多，請重新連線。").addButton("確定").show();
            }
        }.runInMainThread();
    }

    /**
     * 傳送密碼
     */
    public void sendPassword() {
        TelnetClient.getClient().sendStringToServer(_password);
    }

    /**
     * 登入成功
     */
    public void onLoginSuccess() {
        // 存檔客戶資料
        TelnetClient.getClient().setUsername(_username);
        saveLogonUserToProperties();

        // 讀取雲端
        if (NotificationSettings.getCloudSave()) {
            CloudBackup cloudBackup = new CloudBackup();
            cloudBackup.restore();
        }

        // 調用WebView登入（如果需要的話）
        if (checkWebSignIn) {
            // 檢查今日是否已經自動簽到過
            if (isWebAutoLoginToday()) {
                // 如果今日已經登入過，跳過自動簽到
                ASToast.showShortToast(getContextString(R.string.login_web_sign_in_msg05));
            } else {
                new ASRunner() {
                    public void run() {
                        try {
                            ASToast.showShortToast(getContextString(R.string.login_web_sign_in_msg01));
                            
                            // 使用 LoginWebDebugView 來顯示和處理自動登入
                            LoginWebDebugView debugView = new LoginWebDebugView(getContext());
                            debugView.startAutoLogin(() -> {
                                // 記錄web自動簽到成功時間
                                setWebAutoLoginSuccessTime();
                                return null;
                            });
                        } catch (Exception e) {
                            ASToast.showShortToast(getContextString(R.string.login_web_sign_in_msg04));
                            Log.e(getClass().getSimpleName(), e.getMessage() != null ? e.getMessage() : "");
                        }
                    }
                }.runInMainThread();
            }
        }
    }

    /**
     * 檢查web自動簽到是否在今日已執行過
     */
    private boolean isWebAutoLoginToday() {
        String lastLoginTime = TempSettings.getWebAutoLoginSuccessTime();
        if (lastLoginTime.isEmpty()) {
            return false;
        }

        try {
            long lastTime = Long.parseLong(lastLoginTime);
            long currentTime = System.currentTimeMillis();
            
            // 取得昨日與今日的時間邊界 (今日00:00:00)
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(currentTime);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            long todayStartTime = calendar.getTimeInMillis();

            return lastTime >= todayStartTime;
        } catch (NumberFormatException e) {
            // 如果時間格式錯誤，重置時間
            TempSettings.setWebAutoLoginSuccessTime("");
            return false;
        }
    }

    /**
     * 設置web自動簽到成功時間
     */
    private void setWebAutoLoginSuccessTime() {
        String currentTime = String.valueOf(System.currentTimeMillis());
        TempSettings.setWebAutoLoginSuccessTime(currentTime);
    }

    /**
     * 儲存未完成文章
     */
    public void onSaveArticle() {
        if (_save_unfinished_article_dialog == null) {
            _save_unfinished_article_dialog = ASAlertDialog.createDialog().setTitle("提示").setMessage("您有一篇文章尚未完成")
                    .addButton("放棄").addButton("寫入暫存檔").setListener((aDialog, index) -> {
                        switch (index) {
                            case 0 -> TelnetClient.getClient().sendStringToServer("Q");
                            case 1 -> TelnetClient.getClient().sendStringToServer("S");
                        }
                        LoginPage.this._save_unfinished_article_dialog = null;
                    }).scheduleDismissOnPageDisappear(this);
            _save_unfinished_article_dialog.show();
        }
    }

    /**
     * 帳號重覆登入超過上限
     */
    public void onLoginAccountOverLimit() {
        new ASRunner() {
            public void run() {
                ASProcessingDialog.dismissProcessingDialog();
                ASAlertDialog.createDialog().setTitle("警告").setMessage("您的帳號重覆登入超過上限，請選擇刪除其他重複的登入或將其它帳號登出。")
                        .addButton("確定").show();
            }
        }.runInMainThread();
    }
}
