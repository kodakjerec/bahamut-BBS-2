package com.kota.Bahamut.pages.login

import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CloudBackup
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getCloudSave
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.clearTempSettings
import com.kota.Bahamut.service.TempSettings.getWebAutoLoginSuccessTime
import com.kota.Bahamut.service.TempSettings.myContext
import com.kota.Bahamut.service.TempSettings.setWebAutoLoginSuccessTime
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAutoToChat
import com.kota.Bahamut.service.UserSettings.Companion.propertiesPassword
import com.kota.Bahamut.service.UserSettings.Companion.propertiesSaveLogonUser
import com.kota.Bahamut.service.UserSettings.Companion.propertiesUsername
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.service.UserSettings.Companion.propertiesWebSignIn
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.thread.ASRunner.Companion.runInNewThread
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetCursor
import com.kota.telnet.model.TelnetFrame
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView
import java.util.Calendar

class LoginPage : TelnetPage() {
    var errorCount: Int = 0
    var loginListener: View.OnClickListener = View.OnClickListener { v: View? ->
        this@LoginPage.username =
            (this@LoginPage.findViewById(R.id.Login_UsernameEdit) as EditText).text
                .toString().trim { it <= ' ' }
        this@LoginPage.password =
            (this@LoginPage.findViewById(R.id.Login_passwordEdit) as EditText).text
                .toString().trim { it <= ' ' }
        this@LoginPage.saveLogonUser =
            (this@LoginPage.findViewById(R.id.Login_loginRememberCheckBox) as CheckBox)
                .isChecked
        this@LoginPage.checkWebSignIn =
            (this@LoginPage.findViewById(R.id.LoginWebSignInCheckBox) as CheckBox)
                .isChecked

        val errMessage = if (this@LoginPage.username.isEmpty() && this@LoginPage.password.isEmpty()) {
            "帳號、密碼不可為空，請重新輸入。"
        } else if (this@LoginPage.username.isEmpty()) {
            "帳號不可為空，請重新輸入。"
        } else if (this@LoginPage.password.isEmpty()) {
            "密碼不可為空，請重新輸入。"
        } else {
            null
        }
        if (errMessage != null) {
            showErrorDialog(errMessage, this@LoginPage)
        } else {
            this@LoginPage.login()
        }
    }
    var username: String = "" // 使用者名稱
    var password: String = "" // 密碼
    var saveLogonUser: Boolean = false // 是否儲存登入使用者
    var checkWebSignIn: Boolean = false // 是否勾選Web登入
    var removeLogonUserDialog: ASAlertDialog? = null // 刪除重複登入對話框
    var saveUnfinishedArticleDialog: ASDialog? = null // 儲存未完成文章對話框
    var telnetView: TelnetView? = null // Telnet視圖

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_LOGIN

    override val pageLayout: Int
        get() = R.layout.login_page

    override fun onPageDidLoad() {
        // 清空暫存和執行中變數
        clearTempSettings() // 清除暫存資料
        try {
            UrlDatabase(context).use { urlDatabase ->  // 清除URL資料庫
                urlDatabase.clearDb()
            }
        } catch (_: Exception) {
            Log.e("Bookmark", "initial fail")
        }

        // 登入
        navigationController.setNavigationTitle("勇者登入")
        findViewById(R.id.Login_loginButton)?.setOnClickListener(loginListener)
        // checkbox區塊點擊
        val checkBox: CheckBox = findViewById(R.id.Login_loginRememberCheckBox) as CheckBox
        findViewById(R.id.loginRememberLabel)?.setOnClickListener { view: View? ->
            checkBox.isChecked = !checkBox.isChecked
            propertiesSaveLogonUser = checkBox.isChecked
            notifyDataUpdated()
        }
        // web登入
        val webLoginCheckBox: CheckBox = findViewById(R.id.LoginWebSignInCheckBox) as CheckBox
        findViewById(R.id.LoginWebSignInLabel)?.setOnClickListener { view: View? ->
            webLoginCheckBox.isChecked = !webLoginCheckBox.isChecked
            propertiesWebSignIn = webLoginCheckBox.isChecked
            notifyDataUpdated()
        }
        // TelnetView
        telnetView = findViewById(R.id.Login_TelnetView) as TelnetView?

        // 讀取預設勇者設定
        loadLogonUser()

        // VIP
        if (propertiesVIP) {
            val blockWebSignIn = findViewById(R.id.BlockWebSignIn) as RelativeLayout?
            blockWebSignIn?.visibility = View.VISIBLE
        }

        // 替換外觀
        val mainLayout = findViewById(R.id.toolbar) as LinearLayout?
        ThemeFunctions().layoutReplaceTheme(mainLayout)
    }

    @Synchronized
    override fun onPagePreload(): Boolean {
        return handleNormalState()
    }

    // 按下返回
    override fun onBackPressed(): Boolean {
        TelnetClient.client?.close()
        return true
    }

    override fun onPageDidDisappear() {
        telnetView = null
        removeLogonUserDialog = null
        saveUnfinishedArticleDialog = null
        clear()
    }

    override fun onPageRefresh() {
        if (telnetView != null) {
            setFrameToTelnetView()
        }
    }

    override fun onPageWillDisappear() {
        dismissProcessingDialog()
    }

    override fun clear() {
        errorCount = 0
    }

    fun handleNormalState(): Boolean {
        val row23: String? = TelnetClient.client?.model?.getRowString(23)
        if (row23 == null) return true
        val cursor: TelnetCursor? = TelnetClient.client?.model?.cursor
        if (cursor == null) return true

        if (row23.endsWith("再見 ...")) {
            onLoginAccountOverLimit()
            return false
        } else if (row23.startsWith("您想刪除其他重複的 login")) {
            onCheckRemoveLogonUser()
            return false
        } else if (row23.startsWith("★ 密碼輸入錯誤") && cursor.row == 23) {
            errorCount++
            onPasswordError()
            TelnetClient.client?.sendStringToServer("")
            return false
        } else if (row23.startsWith("★ 錯誤的使用者代號") && cursor.row == 23) {
            errorCount++
            onUsernameError()
            TelnetClient.client?.sendStringToServer("")
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
        val loginUsernameField = findViewById(R.id.Login_UsernameEdit) as EditText
        val loginPasswordField = findViewById(R.id.Login_passwordEdit) as EditText
        val loginRemember = findViewById(R.id.Login_loginRememberCheckBox) as CheckBox
        val loginWebSignIn = findViewById(R.id.LoginWebSignInCheckBox) as CheckBox
        val username = propertiesUsername
        val password = propertiesPassword
        val username2 = username?.trim { it <= ' ' }
        val password2 = password?.trim { it <= ' ' }
        loginUsernameField.setText(username2)
        loginPasswordField.setText(password2)
        loginRemember.isChecked = propertiesSaveLogonUser
        loginWebSignIn.isChecked = propertiesWebSignIn
    }

    /**
     * 儲存勇者設定到屬性
     */
    fun saveLogonUserToProperties() {
        val loginRemember = findViewById(R.id.Login_loginRememberCheckBox) as CheckBox
        val username = (findViewById(R.id.Login_UsernameEdit) as EditText).text.toString()
            .trim { it <= ' ' }
        val password = (findViewById(R.id.Login_passwordEdit) as EditText).text.toString()
            .trim { it <= ' ' }

        if (loginRemember.isChecked) {
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
        val frame: TelnetFrame? = TelnetClient.client?.model?.frame?.clone()
        if (frame == null) return
        frame.removeRow(23)
        frame.removeRow(22)
        telnetView?.frame = frame
    }

    /**
     * 登入
     */
    fun login() {
        showProcessingDialog("登入中")
        runInNewThread {
            TelnetClient.client!!
                .sendStringToServerInBackground(this@LoginPage.username)
        }
    }

    /**
     * 檢查是否刪除重複登入
     */
    fun onCheckRemoveLogonUser() {
        object : ASRunner() {
            override fun run() {
                dismissProcessingDialog()
                if (this@LoginPage.removeLogonUserDialog == null) {
                    this@LoginPage.removeLogonUserDialog = createDialog()
                        .setTitle("提示")
                        .setMessage("您想刪除其他重複的登入嗎？")
                        .addButton("否")
                        .addButton("是")
                        .setListener { aDialog: ASAlertDialog?, index: Int ->
                            if (index == 0) {
                                TelnetClient.client?.sendStringToServerInBackground("n")
                            } else {
                                TelnetClient.client?.sendStringToServerInBackground("y")
                            }
                            this@LoginPage.removeLogonUserDialog = null
                            showProcessingDialog("登入中")
                        }.setOnBackDelegate { aDialog: ASDialog? ->
                            TelnetClient.client?.sendStringToServerInBackground("n")
                            if (this@LoginPage.removeLogonUserDialog != null) {
                                this@LoginPage.removeLogonUserDialog?.dismiss()
                                this@LoginPage.removeLogonUserDialog = null
                            }
                            showProcessingDialog("登入中")
                            true
                        } as ASAlertDialog
                }
                this@LoginPage.removeLogonUserDialog?.show()
            }
        }.runInMainThread()
    }

    /**
     * 密碼錯誤
     */
    fun onPasswordError() {
        if (errorCount < 3) {
            object : ASRunner() {
                override fun run() {
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
        if (errorCount < 3) {
            object : ASRunner() {
                override fun run() {
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
            override fun run() {
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
        TelnetClient.client?.sendStringToServer(password)
    }

    /**
     * 登入成功
     */
    fun onLoginSuccess() {
        // 存檔客戶資料
        TelnetClient.client?.username = username
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
                    override fun run() {
                        try {
                            showShortToast(getContextString(R.string.login_web_sign_in_msg01))


                            // 使用 LoginWebDebugView 來顯示和處理自動登入
                            val debugView = LoginWebDebugView(myContext!!)
                            debugView.startAutoLogin {
                                // 記錄web自動簽到成功時間
                                setWebAutoLoginSuccessTime()
                                null
                            }
                        } catch (e: Exception) {
                            showShortToast(getContextString(R.string.login_web_sign_in_msg04))
                            Log.e(
                                javaClass.simpleName,
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
                calendar.timeInMillis = currentTime
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val todayStartTime = calendar.timeInMillis

                return lastTime >= todayStartTime
            } catch (_: NumberFormatException) {
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
        if (saveUnfinishedArticleDialog == null) {
            saveUnfinishedArticleDialog =
                createDialog().setTitle("提示").setMessage("您有一篇文章尚未完成")
                    .addButton("放棄").addButton("寫入暫存檔")
                    .setListener { aDialog: ASAlertDialog?, index: Int ->
                        when (index) {
                            0 -> TelnetClient.client?.sendStringToServer("Q")
                            1 -> TelnetClient.client?.sendStringToServer("S")
                        }
                        this@LoginPage.saveUnfinishedArticleDialog = null
                    }.scheduleDismissOnPageDisappear(this)
            saveUnfinishedArticleDialog?.show()
        }
    }

    /**
     * 帳號重覆登入超過上限
     */
    fun onLoginAccountOverLimit() {
        object : ASRunner() {
            override fun run() {
                dismissProcessingDialog()
                createDialog().setTitle("警告")
                    .setMessage("您的帳號重覆登入超過上限，請選擇刪除其他重複的登入或將其它帳號登出。")
                    .addButton("確定").show()
            }
        }.runInMainThread()
    }
}
