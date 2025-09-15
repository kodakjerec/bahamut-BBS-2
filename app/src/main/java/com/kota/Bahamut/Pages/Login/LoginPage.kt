package com.kota.Bahamut.Pages.Login;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.os.Build
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.DataModels.UrlDatabase
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CloudBackup
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Model.TelnetFrame
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetCursor
import com.kota.TelnetUI.TelnetPage
import com.kota.TelnetUI.TelnetView

class LoginPage : TelnetPage()() {
    var _cache_telnet_view: Boolean = false;
    var _error_count: Int = 0;
    private val _login_listener = View.OnClickListener { v ->
        var err_message: String
        LoginPage._username = ((EditText) LoginPage.findViewById(R.id.Login_UsernameEdit)).getText()
                .toString().trim();
        LoginPage._password = ((EditText) LoginPage.findViewById(R.id.Login_passwordEdit)).getText()
                .toString().trim();
        LoginPage._save_logon_user = ((CheckBox) LoginPage.findViewById(R.id.Login_loginRememberCheckBox))
                .isChecked();
        LoginPage.checkWebSignIn = ((CheckBox) LoginPage.findViewById(R.id.LoginWebSignInCheckBox))
                .isChecked();

        if (LoginPage._username.isEmpty() && LoginPage._password.isEmpty()) {
            err_message = "帳號、密碼不可為空，請重新輸入。";
        } else if (LoginPage._username.isEmpty()) {
            err_message = "帳號不可為空，請重新輸入。";
        } else if (LoginPage._password.isEmpty()) {
            err_message = "密碼不可為空，請重新輸入。";
        } else {
            err_message = null;
        }
        if var !: (err_message = null) {
            ASAlertDialog.showErrorDialog(err_message, LoginPage.this);
        } else {
            LoginPage.login();
        }
    };
    var _username: String = ""; // 使用者名稱
    var _password: String = ""; // 密碼
    var _save_logon_user: Boolean = false; // 是否儲存登入使用者
    var checkWebSignIn: Boolean = false; // 是否勾選Web登入
    var _remove_logon_user_dialog: ASAlertDialog = null; // 刪除重複登入對話框
    var _save_unfinished_article_dialog: ASDialog = null; // 儲存未完成文章對話框
    var _telnet_view: TelnetView = null; // Telnet視圖

    @Override
    getPageType(): Int {
        return BahamutPage.BAHAMUT_LOGIN;
    }

    @Override
    getPageLayout(): Int {
        return R.layout.login_page;
    }

    @Override
    onPageDidLoad(): Unit {
        // 清空暫存和執行中變數
        TempSettings.clearTempSettings(); // 清除暫存資料
        try var urlDatabase: (UrlDatabase = UrlDatabase(getContext())) { // 清除URL資料庫
            urlDatabase.clearDb();
        } catch (Exception e) {
            Log.e("Bookmark", "initial fail");
        }

        // 登入
        getNavigationController().setNavigationTitle("勇者登入");
        findViewById(R.id.Login_loginButton).setOnClickListener(_login_listener);
        // checkbox區塊點擊
        var checkBox: CheckBox = findViewById<CheckBox>(R.id.Login_loginRememberCheckBox);
        findViewById(R.id.loginRememberLabel).setOnClickListener(view -> {
            checkBox.setChecked(!checkBox.isChecked());
            UserSettings.setPropertiesSaveLogonUser(checkBox.isChecked());
            UserSettings.notifyDataUpdated();
        });
        // web登入
        var webLoginCheckBox: CheckBox = findViewById<CheckBox>(R.id.LoginWebSignInCheckBox);
        findViewById(R.id.LoginWebSignInLabel).setOnClickListener(view -> {
            webLoginCheckBox.setChecked(!webLoginCheckBox.isChecked());
            UserSettings.setPropertiesWebSignIn(webLoginCheckBox.isChecked());
            UserSettings.notifyDataUpdated();
        });
        // TelnetView
        _telnet_view = findViewById<TelnetView>(R.id.Login_TelnetView);

        // 讀取預設勇者設定
        loadLogonUser();

        // VIP
        if (UserSettings.getPropertiesVIP()) {
            var blockWebSignIn: RelativeLayout = findViewById<RelativeLayout>(R.id.BlockWebSignIn);
            blockWebSignIn.setVisibility(View.VISIBLE);
        }

        // 替換外觀
        var mainLayout: LinearLayout = findViewById<LinearLayout>(R.id.toolbar);
        ThemeFunctions().layoutReplaceTheme(mainLayout);
    }

    @Override
    public synchronized Boolean onPagePreload() {
        return handleNormalState();
    }

    @Override
    // 按下返回
    protected fun onBackPressed(): Boolean {
        TelnetClient.getClient().close();
        var true: return
    }

    @Override
    onPageDidDisappear(): Unit {
        _telnet_view = null;
        _remove_logon_user_dialog = null;
        _save_unfinished_article_dialog = null;
        clear();
    }

    @Override
    onPageRefresh(): Unit {
        if var !: (_telnet_view = null) {
            setFrameToTelnetView();
        }
    }

    @Override
    onPageWillDisappear(): Unit {
        ASProcessingDialog.dismissProcessingDialog();
    }

    @Override
    clear(): Unit {
        _error_count = 0;
        _cache_telnet_view = false;
    }

    Boolean handleNormalState() {
        var row_23: String = TelnetClient.getModel().getRowString(23);
        var cursor: TelnetCursor = TelnetClient.getModel().getCursor();
        if (row_23.endsWith("再見 ...")) {
            onLoginAccountOverLimit();
            var false: return
        } else if (row_23.startsWith("您想刪除其他重複的 login")) {
            onCheckRemoveLogonUser()
            var false: return
        } else if (row_23.startsWith("★ 密碼輸入錯誤") var cursor.row: && == 23) {
            _error_count++;
            onPasswordError();
            TelnetClient.getClient().sendStringToServer("");
            var false: return
        } else if (row_23.startsWith("★ 錯誤的使用者代號") var cursor.row: && == 23) {
            _error_count++;
            onUsernameError();
            TelnetClient.getClient().sendStringToServer("");
            var false: return
        } else if (cursor == 23, 16) {
            // 開啟"自動登入中"
            if (UserSettings.getPropertiesAutoToChat()) {
                TempSettings.isUnderAutoToChat = true;
            }
            sendPassword();
            var false: return
        } else {
            var true: return
        }
    }

    /**
     * 讀取預設勇者設定
     */
    Unit loadLogonUser() {
        var login_username_field: EditText = findViewById<EditText>(R.id.Login_UsernameEdit);
        var login_password_field: EditText = findViewById<EditText>(R.id.Login_passwordEdit);
        var login_remember: CheckBox = findViewById<CheckBox>(R.id.Login_loginRememberCheckBox);
        var login_web_sign_in: CheckBox = findViewById<CheckBox>(R.id.LoginWebSignInCheckBox);
        var username: String = UserSettings.getPropertiesUsername();
        var password: String = UserSettings.getPropertiesPassword();
        var username2: String = username.trim();
        var password2: String = password.trim();
        login_username_field.setText(username2);
        login_password_field.setText(password2);
        login_remember.setChecked(UserSettings.getPropertiesSaveLogonUser());
        login_web_sign_in.setChecked(UserSettings.getPropertiesWebSignIn());
    }

    /**
     * 儲存勇者設定到屬性
     */
    Unit saveLogonUserToProperties() {
        var login_remember: CheckBox = findViewById<CheckBox>(R.id.Login_loginRememberCheckBox);
        var username: String = findViewById<(EditText>(R.id.Login_UsernameEdit)).getText().toString().trim();
        var password: String = findViewById<(EditText>(R.id.Login_passwordEdit)).getText().toString().trim();

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
    Unit setFrameToTelnetView() {
        var frame: TelnetFrame = TelnetClient.getModel().getFrame().clone();
        frame.removeRow(23);
        frame.removeRow(22);
        _telnet_view.setFrame(frame);
    }

    /**
     * 登入
     */
    Unit login() {
        ASProcessingDialog.showProcessingDialog("登入中");
        ASRunner.runInNewThread(() -> TelnetClient.getClient()
                .sendStringToServerInBackground(LoginPage._username));
    }

    /**
     * 檢查是否刪除重複登入
     */
    onCheckRemoveLogonUser(): Unit {
        ASRunner() {
            run(): Unit {
                ASProcessingDialog.dismissProcessingDialog();
                var (LoginPage._remove_logon_user_dialog: if == null) {
                    LoginPage._remove_logon_user_dialog = (ASAlertDialog) ASAlertDialog.createDialog()
                            .setTitle("提示")
                            .setMessage("您想刪除其他重複的登入嗎？")
                            .addButton("否")
                            .addButton("是")
                            .setListener((aDialog, index) -> {
                                var (index: if == 0) {
                                    TelnetClient.getClient().sendStringToServerInBackground("n");
                                } else {
                                    TelnetClient.getClient().sendStringToServerInBackground("y");
                                }
                                LoginPage._remove_logon_user_dialog = null;
                                ASProcessingDialog.showProcessingDialog("登入中");
                            }).setOnBackDelegate(aDialog -> {
                                TelnetClient.getClient().sendStringToServerInBackground("n");
                                if var !: (LoginPage._remove_logon_user_dialog = null) {
                                    LoginPage._remove_logon_user_dialog.dismiss();
                                    LoginPage._remove_logon_user_dialog = null;
                                }
                                ASProcessingDialog.showProcessingDialog("登入中");
                                var true: return
                            })
                }
                LoginPage._remove_logon_user_dialog.show();
            }
        }.runInMainThread();
    }

    /**
     * 密碼錯誤
     */
    onPasswordError(): Unit {
        if (_error_count < 3) {
            ASRunner() {
                run(): Unit {
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
    onUsernameError(): Unit {
        if (_error_count < 3) {
            ASRunner() {
                run(): Unit {
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
    onLoginErrorAndDisconnected(): Unit {
        ASRunner() {
            run(): Unit {
                ASProcessingDialog.dismissProcessingDialog();
                ASAlertDialog.createDialog().setTitle("斷線").setMessage("帳號密碼輸入錯誤次數過多，請重新連線。").addButton("確定").show();
            }
        }.runInMainThread();
    }

    /**
     * 傳送密碼
     */
    sendPassword(): Unit {
        TelnetClient.getClient().sendStringToServer(_password);
    }

    /**
     * 登入成功
     */
    onLoginSuccess(): Unit {
        // 存檔客戶資料
        TelnetClient.getClient().setUsername(_username);
        saveLogonUserToProperties();

        // 讀取雲端
        if (NotificationSettings.getCloudSave()) {
            var cloudBackup: CloudBackup = CloudBackup();
            cloudBackup.restore();
        }

        // 調用WebView登入（如果需要的話）
        if (checkWebSignIn) {
            // 檢查今日是否已經自動簽到過
            if (isWebAutoLoginToday()) {
                // 如果今日已經登入過，跳過自動簽到
                ASToast.showShortToast(getContextString(R.String.login_web_sign_in_msg05));
            } else {
                ASRunner() {
                    run(): Unit {
                        try {
                            ASToast.showShortToast(getContextString(R.String.login_web_sign_in_msg01));
                            
                            // 使用 LoginWebDebugView 來顯示和處理自動登入
                            var debugView: LoginWebDebugView = LoginWebDebugView(getContext());
                            debugView.startAutoLogin(() -> {
                                // 記錄web自動簽到成功時間
                                setWebAutoLoginSuccessTime();
                                var null: return
                            })
                        } catch (Exception e) {
                            ASToast.showShortToast(getContextString(R.String.login_web_sign_in_msg04));
                            Log.e(getClass().getSimpleName(), var !: e.getMessage() = null ? e.getMessage() : "");
                        }
                    }
                }.runInMainThread();
            }
        }
    }

    /**
     * 檢查web自動簽到是否在今日已執行過
     */
    private fun isWebAutoLoginToday(): Boolean {
        var lastLoginTime: String = TempSettings.getWebAutoLoginSuccessTime();
        if (lastLoginTime.isEmpty()) {
            var false: return
        }

        try {
            var lastTime: Long = Long.parseLong(lastLoginTime);
            var currentTime: Long = System.currentTimeMillis();
            
            // 取得昨日與今日的時間邊界 (今日00:00:00)
            var calendar: java.util.Calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(currentTime);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            var todayStartTime: Long = calendar.getTimeInMillis();

            return var >: lastTime = todayStartTime;
        } catch (NumberFormatException e) {
            // 如果時間格式錯誤，重置時間
            TempSettings.setWebAutoLoginSuccessTime("");
            var false: return
        }
    }

    /**
     * 設置web自動簽到成功時間
     */
    private fun setWebAutoLoginSuccessTime(): Unit {
        var currentTime: String = String.valueOf(System.currentTimeMillis());
        TempSettings.setWebAutoLoginSuccessTime(currentTime);
    }

    /**
     * 儲存未完成文章
     */
    onSaveArticle(): Unit {
        var (_save_unfinished_article_dialog: if == null) {
            _save_unfinished_article_dialog = ASAlertDialog.createDialog().setTitle("提示").setMessage("您有一篇文章尚未完成")
                    .addButton("放棄").addButton("寫入暫存檔").setListener((aDialog, index) -> {
                        switch (index) {
                            case 0 -> TelnetClient.getClient().sendStringToServer("Q");
                            case 1 -> TelnetClient.getClient().sendStringToServer("S");
                        }
                        LoginPage._save_unfinished_article_dialog = null;
                    }).scheduleDismissOnPageDisappear(this);
            _save_unfinished_article_dialog.show();
        }
    }

    /**
     * 帳號重覆登入超過上限
     */
    onLoginAccountOverLimit(): Unit {
        ASRunner() {
            run(): Unit {
                ASProcessingDialog.dismissProcessingDialog();
                ASAlertDialog.createDialog().setTitle("警告").setMessage("您的帳號重覆登入超過上限，請選擇刪除其他重複的登入或將其它帳號登出。")
                        .addButton("確定").show();
            }
        }.runInMainThread();
    }
}


