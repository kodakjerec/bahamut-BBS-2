package com.kota.Bahamut.pages.login

import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASDialogOnBackPressedDelegate
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.thread.ASRunner.Companion.runInNewThread
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CloudBackup
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getCloudSave
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.clearTempSettings
import com.kota.Bahamut.service.TempSettings.getWebAutoLoginSuccessTime
import com.kota.Bahamut.service.TempSettings.setWebAutoLoginSuccessTime
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAutoToChat
import com.kota.Bahamut.service.UserSettings.Companion.propertiesPassword
import com.kota.Bahamut.service.UserSettings.Companion.propertiesSaveLogonUser
import com.kota.Bahamut.service.UserSettings.Companion.propertiesUsername
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.service.UserSettings.Companion.propertiesWebSignIn
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.model.TelnetFrame.clone
import com.kota.telnet.model.TelnetModel.cursor
import com.kota.telnet.model.TelnetModel.frame
import com.kota.telnet.model.TelnetModel.getRowString
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetClient.model
import com.kota.telnet.TelnetCursor
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView
import com.kota.telnetUI.TelnetView.frame
import java.util.Calendar

class LoginPage : TelnetPage() {
    var _cache_telnet_view: Boolean = false
    var _error_count: Int = 0
    var _login_listener: View.OnClickListener = View.OnClickListener { v: View? ->
        val err_message: String?
        this@LoginPage._username =
            (this@LoginPage.findViewById(R.id.Login_UsernameEdit) as EditText).getText()
                .toString().trim { it <= ' ' }
        this@LoginPage._password =
            (this@LoginPage.findViewById(R.id.Login_passwordEdit) as EditText).getText()
                .toString().trim { it <= ' ' }
        this@LoginPage._save_logon_user =
            (this@LoginPage.findViewById(R.id.Login_loginRememberCheckBox) as CheckBox)
                .isChecked()
        this@LoginPage.checkWebSignIn =
            (this@LoginPage.findViewById(R.id.LoginWebSignInCheckBox) as CheckBox)
                .isChecked()

        if (this@LoginPage._username.isEmpty() && this@LoginPage._password.isEmpty()) {
            err_message = "帳號、密碼不可為空，請重新輸入。"
        } else if (this@LoginPage._username.isEmpty()) {
            err_message = "帳號不可為空，請重新輸入。"
        } else if (this@LoginPage._password.isEmpty()) {
            err_message = "密碼不可為空，請重新輸入。"
        } else {
            err_message = null
        }
        if (err_message != null) {
            showErrorDialog(err_message, this@LoginPage)
        } else {
            this@LoginPage.login()
        }
    }
    var _username: String = "" // 使用者名稱
    var _password: String = "" // 密碼
    var _save_logon_user: Boolean = false // 是否儲存登入使用者
    var checkWebSignIn: Boolean = false // 是否勾選Web登入
    var _remove_logon_user_dialog: ASAlertDialog? = null // 刪除重複登入對話框
    var _save_unfinished_article_dialog: ASDialog? = null // 儲存未完成文章對話框
    var _telnet_view: TelnetView? = null // Telnet視圖

    val pageType: Int
        get() = BahamutPage.BAHAMUT_LOGIN

    val pageLayout: Int
        get() = R.layout.login_page

    public override fun onPageDidLoad() {
        // 清空暫存和執行中變數
        clearTempSettings() // 清除暫存資料
        try {
            UrlDatabase(context).use { urlDatabase ->  // 清除URL資料庫
                urlDatabase.clearDb()
            }
        } catch (e: Exception) {
            Log.e("Bookmark", "initial fail")
        }

        // 登入
        navigationController!!.setNavigationTitle("勇者登入")
        findViewById(R.id.Login_loginButton)!!.setOnClickListener(_login_listener)
        // checkbox區塊點擊
        val checkBox = findViewById(R.id.Login_loginRememberCheckBox) as CheckBox?
        findViewById(R.id.loginRememberLabel)!!.setOnClickListener(View.OnClickListener { view: View? ->
            checkBox!!.setChecked(!checkBox.isChecked())
            propertiesSaveLogonUser = checkBox.isChecked()
            notifyDataUpdated()
        })
        // web登入
        val webLoginCheckBox = findViewById(R.id.LoginWebSignInCheckBox) as CheckBox?
        findViewById(R.id.LoginWebSignInLabel)!!.setOnClickListener(View.OnClickListener { view: View? ->
            webLoginCheckBox!!.setChecked(!webLoginCheckBox.isChecked())
            propertiesWebSignIn = webLoginCheckBox.isChecked()
            notifyDataUpdated()
        })
        // TelnetView
        _telnet_view = findViewById(R.id.Login_TelnetView) as TelnetView?

        // 讀取預設勇者設定
        loadLogonUser()

        // VIP
        if (propertiesVIP) {
            val blockWebSignIn = findViewById(R.id.BlockWebSignIn) as RelativeLayout?
            blockWebSignIn!!.setVisibility(View.VISIBLE)
        }

        // 替換外觀
        val mainLayout = findViewById(R.id.toolbar) as LinearLayout?
        ThemeFunctions().layoutReplaceTheme(mainLayout)
    }

    @Synchronized
    public override fun onPagePreload(): Boolean {
        return handleNormalState()
    }

    // 按下返回
    protected override fun onBackPressed(): Boolean {
        TelnetClient.getClient().close()
        return true
    }

    public override fun onPageDidDisappear() {
        _telnet_view = null
        _remove_logon_user_dialog = null
        _save_unfinished_article_dialog = null
        clear()
    }

    public override fun onPageRefresh() {
        if (_telnet_view != null) {
            setFrameToTelnetView()
        }
    }

    public override fun onPageWillDisappear() {
        dismissProcessingDialog()
    }

    public override fun clear() {
        _error_count = 0
        _cache_telnet_view = false
    }

    fun handleNormalState(): Boolean {
        val row_23: String = TelnetClient.model.getRowString(23)
        val cursor: TelnetCursor = TelnetClient.model.cursor
        if (row_23.endsWith("再見 ...")) {
            onLoginAccountOverLimit()
            return false
        } else if (row_23.startsWith("您想刪除其他重複的 login")) {
            onCheckRemoveLogonUser()
            return false
        } else if (row_23.startsWith("★ 密碼輸入錯誤") && cursor.row == 23) {
            _error_count++
            onPasswordError()
            TelnetClient.getClient().sendStringToServer("")
            return false
        } else if (row_23.startsWith("★ 錯誤的使用者代號") && cursor.row == 23) {
            _error_count++
            onUsernameError()
            TelnetClient.getClient().sendStringToServer("")
            return false
        } else if (cursor.equals(23, 16)) {
            // 開啟"自動登入中"
            if (propertiesAutoToChat) {
                TempSettings.isUnderAutoToChat = true
            }
            sendPassword()
            return false
        } else {
            return true
        }
    }

    /**
     * 讀取預設勇者設定
     */
    fun loadLogonUser() {
        val login_username_field = findViewById(R.id.Login_UsernameEdit) as EditText?
        val login_password_field = findViewById(R.id.Login_passwordEdit) as EditText?
        val login_remember = findViewById(R.id.Login_loginRememberCheckBox) as CheckBox?
        val login_web_sign_in = findViewById(R.id.LoginWebSignInCheckBox) as CheckBox?
        val username = propertiesUsername
        val password = propertiesPassword
        val username2 = username!!.trim { it <= ' ' }
        val password2 = password!!.trim { it <= ' ' }
        login_username_field!!.setText(username2)
        login_password_field!!.setText(password2)
        login_remember!!.setChecked(propertiesSaveLogonUser)
        login_web_sign_in!!.setChecked(propertiesWebSignIn)
    }

    /**
     * 儲存勇者設定到屬性
     */
    fun saveLogonUserToProperties() {
        val login_remember = findViewById(R.id.Login_loginRememberCheckBox) as CheckBox?
        val username = (findViewById(R.id.Login_UsernameEdit) as EditText).getText().toString()
            .trim { it <= ' ' }
        val password = (findViewById(R.id.Login_passwordEdit) as EditText).getText().toString()
            .trim { it <= ' ' }

        if (login_remember!!.isChecked()) {
            propertiesUsername = username
            propertiesPassword = password
        } else {
            propertiesUsername = ""
            propertiesPassword = ""
        }
    }

    /**
     * 設定TelnetView的畫面
     */
    fun setFrameToTelnetView() {
        val frame: TelnetFrame = TelnetClient.model.frame.clone()
        frame.removeRow(23)
        frame.removeRow(22)
        _telnet_view!!.frame = frame
    }

    /**
     * 登入
     */
    fun login() {
        showProcessingDialog("登入中")
        runInNewThread(Runnable {
            TelnetClient.getClient()
                .sendStringToServerInBackground(this@LoginPage._username)
        })
    }

    /**
     * 檢查是否刪除重複登入
     */
    fun onCheckRemoveLogonUser() {
        object : ASRunner() {
            public override fun run() {
                dismissProcessingDialog()
                if (this@LoginPage._remove_logon_user_dialog == null) {
                    this@LoginPage._remove_logon_user_dialog = createDialog()
                        .setTitle("提示")
                        .setMessage("您想刪除其他重複的登入嗎？")
                        .addButton("否")
                        .addButton("是")
                        .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                            if (index == 0) {
                                TelnetClient.getClient().sendStringToServerInBackground("n")
                            } else {
                                TelnetClient.getClient().sendStringToServerInBackground("y")
                            }
                            this@LoginPage._remove_logon_user_dialog = null
                            showProcessingDialog("登入中")
                        }).setOnBackDelegate(ASDialogOnBackPressedDelegate { aDialog: ASDialog? ->
                            TelnetClient.getClient().sendStringToServerInBackground("n")
                            if (this@LoginPage._remove_logon_user_dialog != null) {
                                this@LoginPage._remove_logon_user_dialog!!.dismiss()
                                this@LoginPage._remove_logon_user_dialog = null
                            }
                            showProcessingDialog("登入中")
                            true
                        }) as ASAlertDialog
                }
                this@LoginPage._remove_logon_user_dialog!!.show()
            }
        }.runInMainThread()
    }

    /**
     * 密碼錯誤
     */
    fun onPasswordError() {
        if (_error_count < 3) {
            object : ASRunner() {
                public override fun run() {
                    dismissProcessingDialog()
                    createDialog().setTitle("勇者密碼錯誤")
                        .setMessage("勇者密碼錯誤，請重新輸入勇者密碼").addButton("確定")
                        .scheduleDismissOnPageDisappear(this@LoginPage).show()
                }
            }.runInMainThread()
        } else {
            onLoginErrorAndDisconnected()
        }
    }

    /**
     * 使用者名稱錯誤
     */
    fun onUsernameError() {
        if (_error_count < 3) {
            object : ASRunner() {
                public override fun run() {
                    dismissProcessingDialog()
                    createDialog().setTitle("勇者代號錯誤")
                        .setMessage("勇者代號錯誤，請重新輸入勇者代號").addButton("確定")
                        .scheduleDismissOnPageDisappear(this@LoginPage).show()
                }
            }.runInMainThread()
        } else {
            onLoginErrorAndDisconnected()
        }
    }

    /**
     * 登入錯誤並斷線
     */
    fun onLoginErrorAndDisconnected() {
        object : ASRunner() {
            public override fun run() {
                dismissProcessingDialog()
                createDialog().setTitle("斷線").setMessage("帳號密碼輸入錯誤次數過多，請重新連線。")
                    .addButton("確定").show()
            }
        }.runInMainThread()
    }

    /**
     * 傳送密碼
     */
    fun sendPassword() {
        TelnetClient.getClient().sendStringToServer(_password)
    }

    /**
     * 登入成功
     */
    fun onLoginSuccess() {
        // 存檔客戶資料
        TelnetClient.getClient().setUsername(_username)
        saveLogonUserToProperties()

        // 讀取雲端
        if (getCloudSave()) {
            val cloudBackup = CloudBackup()
            cloudBackup.restore()
        }

        // 調用WebView登入（如果需要的話）
        if (checkWebSignIn) {
            // 檢查今日是否已經自動簽到過
            if (this.isWebAutoLoginToday) {
                // 如果今日已經登入過，跳過自動簽到
                showShortToast(getContextString(R.string.login_web_sign_in_msg05))
            } else {
                object : ASRunner() {
                    public override fun run() {
                        try {
                            showShortToast(getContextString(R.string.login_web_sign_in_msg01))


                            // 使用 LoginWebDebugView 來顯示和處理自動登入
                            val debugView = LoginWebDebugView(context)
                            debugView.startAutoLogin {
                                // 記錄web自動簽到成功時間
                                setWebAutoLoginSuccessTime()
                                null
                            }
                        } catch (e: Exception) {
                            showShortToast(getContextString(R.string.login_web_sign_in_msg04))
                            Log.e(
                                javaClass.getSimpleName(),
                                (if (e.message != null) e.message else "")!!
                            )
                        }
                    }
                }.runInMainThread()
            }
        }
    }

    private val isWebAutoLoginToday: Boolean
        /**
         * 檢查web自動簽到是否在今日已執行過
         */
        get() {
            val lastLoginTime = getWebAutoLoginSuccessTime()
            if (lastLoginTime.isEmpty()) {
                return false
            }

            try {
                val lastTime = lastLoginTime.toLong()
                val currentTime = System.currentTimeMillis()


                // 取得昨日與今日的時間邊界 (今日00:00:00)
                val calendar = Calendar.getInstance()
                calendar.setTimeInMillis(currentTime)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val todayStartTime = calendar.getTimeInMillis()

                return lastTime >= todayStartTime
            } catch (e: NumberFormatException) {
                // 如果時間格式錯誤，重置時間
                setWebAutoLoginSuccessTime("")
                return false
            }
        }

    /**
     * 設置web自動簽到成功時間
     */
    private fun setWebAutoLoginSuccessTime() {
        val currentTime = System.currentTimeMillis().toString()
        setWebAutoLoginSuccessTime(currentTime)
    }

    /**
     * 儲存未完成文章
     */
    fun onSaveArticle() {
        if (_save_unfinished_article_dialog == null) {
            _save_unfinished_article_dialog =
                createDialog().setTitle("提示").setMessage("您有一篇文章尚未完成")
                    .addButton("放棄").addButton("寫入暫存檔")
                    .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                        when (index) {
                            0 -> TelnetClient.getClient().sendStringToServer("Q")
                            1 -> TelnetClient.getClient().sendStringToServer("S")
                        }
                        this@LoginPage._save_unfinished_article_dialog = null
                    }).scheduleDismissOnPageDisappear(this)
            _save_unfinished_article_dialog!!.show()
        }
    }

    /**
     * 帳號重覆登入超過上限
     */
    fun onLoginAccountOverLimit() {
        object : ASRunner() {
            public override fun run() {
                dismissProcessingDialog()
                createDialog().setTitle("警告")
                    .setMessage("您的帳號重覆登入超過上限，請選擇刪除其他重複的登入或將其它帳號登出。")
                    .addButton("確定").show()
            }
        }.runInMainThread()
    }
}
