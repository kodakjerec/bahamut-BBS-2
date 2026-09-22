package com.kota.Bahamut.pages.login

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.dialogs.DialogWebLoginSettings
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.MyBillingClient
import com.kota.Bahamut.service.SyncManager
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.TempSettings.clearTempSettings
import com.kota.Bahamut.service.TempSettings.getWebAutoLoginSuccessTime
import com.kota.Bahamut.service.TempSettings.setWebAutoLoginSuccessTime
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaCheckbox
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetComposePage
import com.kota.telnetUI.TelnetView
import java.util.Calendar

/**
 * 勇者登入頁面 (純 Jetpack Compose 實作)
 */
class LoginPage : TelnetComposePage() {

    var cacheTelnetView: Boolean = false
    var errorCount: Int = 0

    // 狀態變數 (由 Compose 響應式持有)
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var checkWebSignIn by mutableStateOf(false)
    var saveLogonUser by mutableStateOf(false)
    var isVip by mutableStateOf(false)

    var dialogRemoveLoginUser: ASAlertDialog? = null // 刪除重複登入對話框
    var dialogSaveUnfinishedArticle: ASDialog? = null // 儲存未完成文章對話框
    var telnetView: TelnetView? = null // Telnet視圖
    var dailyCheckThread: Thread? = null // 每日檢查執行緒

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_LOGIN

    override fun onPageDidLoad() {
        // 清空暫存和執行中變數
        clearTempSettings()
        try {
            UrlDatabase(context).use { urlDatabase ->
                urlDatabase.clearDb()
            }
        } catch (_: Exception) {
            Log.e(javaClass.simpleName, "initial fail")
        }

        navigationController.setNavigationTitle("勇者登入")

        // 讀取預設勇者設定
        loadLogonUser()

        // VIP 區塊顯示狀態
        isVip = UserSettings.propertiesVIP
        if (!isVip && UserSettings.propertiesUsername.isNotEmpty()) {
            MyBillingClient.checkPurchaseHistoryCloud { _ ->
                ASCoroutine.ensureMainThread {
                    isVip = UserSettings.propertiesVIP
                }
            }
        }
    }

    @Synchronized
    override fun onPagePreload(): Boolean {
        return handleNormalState()
    }

    /** 按下返回 */
    override fun onBackPressed(): Boolean {
        TelnetClient.myInstance?.close()
        return true
    }

    override fun onPageDidDisappear() {
        clear()
    }

    override fun onPageDidUnload() {
        telnetView = null
        dialogRemoveLoginUser = null
        dialogSaveUnfinishedArticle = null

        // 停止每日檢查執行緒
        if (dailyCheckThread != null && dailyCheckThread!!.isAlive) {
            dailyCheckThread?.interrupt()
            dailyCheckThread = null
        }

        super.onPageDidUnload()
    }

    override fun onPageRefresh() {
        if (telnetView != null) {
            setFrameToTelnetView()
        }
    }

    override fun onPageWillDisappear() {
        ASProcessingDialog.dismissProcessingDialog()
    }

    override fun clear() {
        errorCount = 0
        cacheTelnetView = false
    }

    fun handleNormalState(): Boolean {
        val row23 = TelnetClient.model.getRowString(23)
        val cursor = TelnetClient.model.cursor
        if (row23.endsWith("再見 ...")) {
            onLoginAccountOverLimit()
            return false
        } else if (row23.startsWith("您想刪除其他重複的 login")) {
            onCheckRemoveLogonUser()
            return false
        } else if (row23.startsWith("★ 密碼輸入錯誤") && cursor.row == 23) {
            errorCount++
            onPasswordError()
            TelnetClient.myInstance?.sendStringToServer("")
            return false
        } else if (row23.startsWith("★ 錯誤的使用者代號") && cursor.row == 23) {
            errorCount++
            onUsernameError()
            TelnetClient.myInstance?.sendStringToServer("")
            return false
        } else if (cursor.equals(23, 16)) {
            // 開啟"自動登入中"
            if (UserSettings.propertiesAutoToChat) {
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
        if (UserSettings.propertiesSaveLogonUser) {
            username = UserSettings.propertiesUsername
            password = UserSettings.propertiesPassword
            saveLogonUser = true
            checkWebSignIn = UserSettings.propertiesWebSignIn
        }
    }

    /**
     * 儲存勇者設定到屬性
     */
    fun saveLogonUserToProperties() {
        if (saveLogonUser) {
            UserSettings.propertiesUsername = username.trim()
            UserSettings.propertiesPassword = password.trim()
            UserSettings.propertiesSaveLogonUser = true
            UserSettings.propertiesWebSignIn = checkWebSignIn
        } else {
            UserSettings.propertiesUsername = ""
            UserSettings.propertiesPassword = ""
            UserSettings.propertiesSaveLogonUser = false
        }
    }

    /**
     * 設定TelnetView的畫面
     */
    fun setFrameToTelnetView() {
        val modelFrame = TelnetClient.model.frame
        if (modelFrame != null) {
            val frame = modelFrame.clone()
            frame.removeRow(23)
            frame.removeRow(22)
            telnetView?.frame = frame
        }
    }

    /**
     * 點擊登入檢核
     */
    fun onLoginButtonClicked() {
        val trimmedUser = username.trim()
        val trimmedPass = password.trim()

        val errMessage = if (trimmedUser.isEmpty() && trimmedPass.isEmpty()) {
            "帳號、密碼不可為空，請重新輸入。"
        } else if (trimmedUser.isEmpty()) {
            "帳號不可為空，請重新輸入。"
        } else if (trimmedPass.isEmpty()) {
            "密碼不可為空，請重新輸入。"
        } else {
            null
        }

        if (errMessage != null) {
            ASAlertDialog.showErrorDialog(errMessage, this@LoginPage)
        } else {
            login()
        }
    }

    /**
     * 登入
     */
    fun login() {
        ASProcessingDialog.showProcessingDialog("登入中")
        ASCoroutine.runInNewCoroutine {
            TelnetClient.myInstance?.sendStringToServerInBackground(username.trim())
        }
    }

    /**
     * 檢查是否刪除重複登入
     */
    fun onCheckRemoveLogonUser() {
        ASCoroutine.ensureMainThread {
            ASProcessingDialog.dismissProcessingDialog()
            if (dialogRemoveLoginUser == null) {
                dialogRemoveLoginUser = ASAlertDialog.createDialog().setTitle("提示")
                    .setMessage("您想刪除其他重複的登入嗎？").addButton("否").addButton("是")
                    .setListener { _, index: Int ->
                        if (index == 0) {
                            TelnetClient.myInstance?.sendStringToServerInBackground("n")
                        } else {
                            TelnetClient.myInstance?.sendStringToServerInBackground("y")
                        }
                        dialogRemoveLoginUser = null
                        ASProcessingDialog.showProcessingDialog("登入中")
                    }.setOnBackDelegate {
                        TelnetClient.myInstance?.sendStringToServerInBackground("n")
                        if (dialogRemoveLoginUser != null) {
                            dialogRemoveLoginUser?.dismiss()
                            dialogRemoveLoginUser = null
                        }
                        ASProcessingDialog.showProcessingDialog("登入中")
                        true
                    } as ASAlertDialog?
            }
            dialogRemoveLoginUser?.show()
        }
    }

    /**
     * 密碼錯誤
     */
    fun onPasswordError() {
        if (errorCount < 3) {
            ASCoroutine.ensureMainThread {
                ASProcessingDialog.dismissProcessingDialog()
                ASAlertDialog.createDialog().setTitle("勇者密碼錯誤")
                    .setMessage("勇者密碼錯誤，請重新輸入勇者密碼").addButton("確定")
                    .scheduleDismissOnPageDisappear(this@LoginPage).show()
            }
        } else {
            onLoginErrorAndDisconnected()
        }
    }

    /**
     * 使用者名稱錯誤
     */
    fun onUsernameError() {
        if (errorCount < 3) {
            ASCoroutine.ensureMainThread {
                ASProcessingDialog.dismissProcessingDialog()
                ASAlertDialog.createDialog().setTitle("勇者代號錯誤")
                    .setMessage("勇者代號錯誤，請重新輸入勇者代號").addButton("確定")
                    .scheduleDismissOnPageDisappear(this@LoginPage).show()
            }
        } else {
            onLoginErrorAndDisconnected()
        }
    }

    /**
     * 登入錯誤並斷線
     */
    fun onLoginErrorAndDisconnected() {
        ASCoroutine.ensureMainThread {
            ASProcessingDialog.dismissProcessingDialog()
            ASAlertDialog.createDialog().setTitle("斷線")
                .setMessage("帳號密碼輸入錯誤次數過多，請重新連線。").addButton("確定").show()
        }
    }

    /**
     * 傳送密碼
     */
    fun sendPassword() {
        TelnetClient.myInstance?.sendStringToServer(password.trim())
    }

    /**
     * 登入成功
     */
    fun onLoginSuccess() {
        // 存檔客戶資料
        TelnetClient.myInstance?.username = username.trim()
        saveLogonUserToProperties()

        // 登入時重試本機待送達佇列；唯有當不是 VIP 才做購買狀態檢查
        MyBillingClient.processPendingPurchases()
        if (!UserSettings.propertiesVIP) {
            MyBillingClient.checkPurchaseHistoryQuery()
        }

        // 雲端同步
        SyncManager.performLoginSync()

        // 調用WebView登入（如果需要的話）
        if (checkWebSignIn) {
            ASCoroutine.ensureMainThread {
                try {
                    ASToast.showShortToast(getContextString(R.string.login_web_sign_in_msg01))
                    val debugView = LoginWebDebugView(context!!)
                    debugView.startAutoLogin {
                        setWebAutoLoginSuccessTime()
                        null
                    }
                } catch (e: Exception) {
                    ASToast.showShortToast(getContextString(R.string.login_web_sign_in_msg04))
                    Log.e(javaClass.simpleName, e.message ?: "")
                }
            }

            // 每小時檢查是否換日，如果換日則執行自動簽到
            if (dailyCheckThread == null) {
                dailyCheckThread = Thread {
                    while (true) {
                        try {
                            Thread.sleep((60 * 60 * 1000).toLong())
                            if (!this.isWebAutoLoginToday) {
                                ASCoroutine.ensureMainThread {
                                    try {
                                        ASToast.showShortToast(getContextString(R.string.login_web_sign_in_msg01))
                                        val debugView = LoginWebDebugView(context!!)
                                        debugView.startAutoLogin {
                                            setWebAutoLoginSuccessTime()
                                            null
                                        }
                                    } catch (e: Exception) {
                                        ASToast.showShortToast(getContextString(R.string.login_web_sign_in_msg04))
                                        Log.e(javaClass.simpleName, e.message ?: "")
                                    }
                                }
                            }
                        } catch (e: InterruptedException) {
                            Log.e(javaClass.simpleName, e.message ?: "")
                            Thread.currentThread().interrupt()
                            break
                        }
                    }
                }
                dailyCheckThread?.start()
            }
        }
    }

    private val isWebAutoLoginToday: Boolean
        get() {
            val lastLoginTime = getWebAutoLoginSuccessTime()
            if (lastLoginTime <= 0L) {
                return false
            }

            try {
                val lastTime = lastLoginTime.toLong()
                val currentTime = System.currentTimeMillis()

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = currentTime
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val todayStartTime = calendar.timeInMillis

                return lastTime >= todayStartTime
            } catch (_: NumberFormatException) {
                setWebAutoLoginSuccessTime()
                return false
            }
        }

    private fun setWebAutoLoginSuccessTime() {
        val currentTime = System.currentTimeMillis()
        setWebAutoLoginSuccessTime(currentTime)
    }

    /**
     * 儲存未完成文章
     */
    fun onSaveArticle() {
        if (dialogSaveUnfinishedArticle == null) {
            dialogSaveUnfinishedArticle =
                ASAlertDialog.createDialog().setTitle("提示").setMessage("您有一篇文章尚未完成")
                    .addButton("放棄").addButton("寫入暫存檔")
                    .setListener { _, index: Int ->
                        when (index) {
                            0 -> TelnetClient.myInstance?.sendStringToServer("Q")
                            1 -> TelnetClient.myInstance?.sendStringToServer("S")
                        }
                        dialogSaveUnfinishedArticle = null
                    }.scheduleDismissOnPageDisappear(this)
            dialogSaveUnfinishedArticle?.show()
        }
    }

    /**
     * 帳號重覆登入超過上限
     */
    fun onLoginAccountOverLimit() {
        ASCoroutine.ensureMainThread {
            ASProcessingDialog.dismissProcessingDialog()
            ASAlertDialog.createDialog().setTitle("警告")
                .setMessage("您的帳號重覆登入超過上限，請選擇刪除其他重複的登入或將其它帳號登出。")
                .addButton("確定").show()
        }
    }

    @Composable
    override fun ComposeContent() {
        val colors = AppTheme.colors

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 1. Telnet 終端文字畫面 (由 TelnetView 繪製，左右對齊視窗寬度)
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    TelnetView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        telnetView = this
                        setFrameToTelnetView()
                    }
                },
                update = { view ->
                    telnetView = view
                }
            )

            // 彈性留白 (讓下方輸入區塊沉底)
            Spacer(modifier = Modifier.weight(1f))

            // 2. 輸入面板區塊
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // VIP Web 簽到區塊 (置中)
                if (isVip) {
                    @OptIn(ExperimentalFoundationApi::class)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (!checkWebSignIn && username.isNotEmpty() && password.isNotEmpty()) {
                                            DialogWebLoginSettings().show()
                                        }
                                        checkWebSignIn = !checkWebSignIn
                                        UserSettings.propertiesWebSignIn = checkWebSignIn
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BahaCheckbox(
                                checked = checkWebSignIn,
                                onCheckedChange = { checked ->
                                    checkWebSignIn = checked
                                    UserSettings.propertiesWebSignIn = checked
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            BahaText(
                                text = stringResource(R.string.login_web_sign_in)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        BahaText(
                            text = stringResource(R.string.login_web_settings_btn),
                            fontSize = AppTheme.fontSize.caption,
                            color = colors.textLink,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    DialogWebLoginSettings().show()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // 帳號輸入行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.account),
                        modifier = Modifier.width(52.dp)
                    )
                    BahaInputField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = stringResource(R.string.Username_hint),
                        maxLength = 12,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 密碼輸入行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.password),
                        modifier = Modifier.width(52.dp)
                    )
                    BahaInputField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = stringResource(R.string.password_hint),
                        isPassword = true,
                        maxLength = 8,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 記住我的資料核取方塊 (置中)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                saveLogonUser = !saveLogonUser
                                UserSettings.propertiesSaveLogonUser = saveLogonUser
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BahaCheckbox(
                            checked = saveLogonUser,
                            onCheckedChange = { checked ->
                                saveLogonUser = checked
                                UserSettings.propertiesSaveLogonUser = checked
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        BahaText(
                            text = stringResource(R.string.save_data)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. 底部登入工具列 (滿版無縫，60dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            BahaButton(
                text = stringResource(R.string.login),
                onClick = { onLoginButtonClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}
