package com.kota.Bahamut.pages

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getConnectIpAddress
import com.kota.Bahamut.service.NotificationSettings.getConnectMethod
import com.kota.Bahamut.service.NotificationSettings.getShowNotificationPermissionDialog
import com.kota.Bahamut.service.NotificationSettings.setConnectIpAddress
import com.kota.Bahamut.service.NotificationSettings.setConnectMethod
import com.kota.Bahamut.service.NotificationSettings.setShowNotificationPermissionDialog
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.textView.TelnetTextViewSmall

class StartPage : TelnetPage() {
    /** 連線  */
    var connectListener: View.OnClickListener =
        View.OnClickListener { v: View? -> this@StartPage.onConnectButtonClicked() }

    /** 離開  */
    var exitListener: View.OnClickListener =
        View.OnClickListener { v: View? -> this@StartPage.onExitButtonClicked() }

    /** 按下教學  */
    var urlClickListener: View.OnClickListener = View.OnClickListener { v: View? ->
        val id = v!!.id
        var url = ""
        if (id == R.id.Start_instructions) url =
            "https://kodaks-organization-1.gitbook.io/bahabbs-zhan-ba-ha-shi-yong-shou-ce/"
        if (id == R.id.Start_Icon_Discord) url = "https://discord.gg/YP8dthZ"
        if (id == R.id.Start_Icon_Facebook) url = "https://www.facebook.com/groups/264144897071532"
        if (id == R.id.Start_Icon_Reddit) url = "https://www.reddit.com/r/bahachat"
        if (id == R.id.Start_Icon_Steam) url = "https://steamcommunity.com/groups/BAHACHAT"
        if (id == R.id.Start_Icon_Telegram) url = "https://t.me/joinchat/MF5hqkuZN3B0NFqSyiz30A"
        if (!url.isEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    /** 避難所  */
    override val pageLayout: Int
        get() = R.layout.start_page

    override val pageType: Int
        get() = BahamutPage.START

    @SuppressLint("SetTextI18n")
    override fun onPageDidLoad() {
        navigationController.setNavigationTitle("勇者入口")
        findViewById(R.id.Start_exitButton)!!.setOnClickListener(exitListener)
        findViewById(R.id.Start_connectButton)!!.setOnClickListener(connectListener)
        findViewById(R.id.Start_instructions)!!.setOnClickListener(urlClickListener)
        // url
        findViewById(R.id.Start_Icon_Discord)!!.setOnClickListener(urlClickListener)
        findViewById(R.id.Start_Icon_Facebook)!!.setOnClickListener(urlClickListener)
        findViewById(R.id.Start_Icon_Reddit)!!.setOnClickListener(urlClickListener)
        findViewById(R.id.Start_Icon_Steam)!!.setOnClickListener(urlClickListener)
        findViewById(R.id.Start_Icon_Telegram)!!.setOnClickListener(urlClickListener)
        // ip位置
        val radioGroup = findViewById(R.id.radioButtonIP) as RadioGroup
        val radioButton1 = findViewById(R.id.radioButtonIP1) as RadioButton
        val radioButton2 = findViewById(R.id.radioButtonIP2) as RadioButton

        // 連線位址
        val connectIp: String? = checkNotNull(getConnectIpAddress())
        if (connectIp == radioButton1.text.toString()) {
            radioButton1.isChecked = true
        } else {
            radioButton2.isChecked = true
        }
        radioGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            val rb = findViewById(checkedId) as RadioButton
            setConnectIpAddress(rb.text.toString())
        }

        // 連線方式
        val connectMethodGroup = findViewById(R.id.radioButtonConnectMethod) as RadioGroup
        val connectMethodButton1 = findViewById(R.id.radioButtonConnectMethod1) as RadioButton
        val connectMethodButton2 = findViewById(R.id.radioButtonConnectMethod2) as RadioButton

        val connectMethod: String? = checkNotNull(getConnectMethod())
        if (connectMethod == connectMethodButton1.text.toString()) {
            connectMethodButton1.isChecked = true
        } else {
            connectMethodButton2.isChecked = true
        }
        connectMethodGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            val rb = findViewById(checkedId) as RadioButton
            setConnectMethod(rb.text.toString())
            updateIPSelectionState(
                checkedId == R.id.radioButtonConnectMethod2,
                radioGroup,
                radioButton1,
                radioButton2
            )
        }

        // 初始化時設置狀態
        updateIPSelectionState(
            connectMethodButton2.isChecked,
            radioGroup,
            radioButton1,
            radioButton2
        )

        val packageInfo: PackageInfo
        try {
            packageInfo =
                context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
            val versionName = packageInfo.versionName
            (findViewById(R.id.version) as TelnetTextViewSmall).text = "$versionCode - $versionName"
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    override fun onPageWillAppear() {
        val pageContainer = PageContainer.instance!!
        pageContainer.cleanStartPage()
    }

    override fun onPageDidDisappear() {
        clear()
        super.onPageDidDisappear()
    }

    override fun clear() {
        ASProcessingDialog.dismissProcessingDialog()
        super.clear()
    }

    /** 按下離開  */
    fun onExitButtonClicked() {
        ASProcessingDialog.dismissProcessingDialog()
        navigationController.finish()
    }

    /** 手機: 上一步  */
    override fun onBackPressed(): Boolean {
        onExitButtonClicked()
        return true
    }

    /** 按下連線按鈕  */
    fun onConnectButtonClicked() {
        // 顯示權限對話框
        if (getShowNotificationPermissionDialog()) {
            connect()
        } else {
            setShowNotificationPermissionDialog(true)
            checkAndRequestNotificationPermission()
        }
    }

    /** 連線  */
    fun connect() {
        val transportType = navigationController.deviceController!!.isNetworkAvailable
        TempSettings.transportType = transportType
        if (transportType > -1) {
            ASProcessingDialog.showProcessingDialog(
                "連線中"
            ) { aDialog: ASProcessingDialog? ->
                TelnetClient.client!!.close()
                false
            }
            val connectIpAddress = getConnectIpAddress()
            ASRunner.runInNewThread {
                TelnetClient.client!!.connect(connectIpAddress, 23)
            }
            return
        }
        ASToast.showShortToast("您未連接網路")
    }

    // 添加輔助方法
    private fun updateIPSelectionState(
        isWebSocket: Boolean,
        ipGroup: RadioGroup,
        ip1: RadioButton,
        ip2: RadioButton
    ) {
        if (isWebSocket) {
            // WebSocket 模式：禁用 IP 選擇
            ipGroup.isEnabled = false
            ip1.isEnabled = false
            ip2.isEnabled = false
            ipGroup.alpha = 0.5f
        } else {
            // Telnet 模式：啟用 IP 選擇
            ipGroup.isEnabled = true
            ip1.isEnabled = true
            ip2.isEnabled = true
            ipGroup.alpha = 1.0f
        }
    }

    companion object {
        /**
         * 檢查並要求通知權限 (Android 13+)
         */
        fun checkAndRequestNotificationPermission() {
            val controller = ASNavigationController.currentController
            if (controller == null) return

            // 只在 Android 13+ 需要通知權限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (controller.checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // 顯示對話框詢問使用者

                    ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.notification_permission_title))
                        .setMessage(getContextString(R.string.notification_permission_message))
                        .addButton(getContextString(R.string.notification_permission_later))
                        .addButton(getContextString(R.string.notification_permission_goto_settings))
                        .setDefaultButtonIndex(0)
                        .setListener { aDialog: ASAlertDialog?, index: Int ->
                            if (index == 1) {
                                // 前往設定頁面
                                try {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    intent.putExtra(
                                        Settings.EXTRA_APP_PACKAGE,
                                        controller.packageName
                                    )
                                    controller.startActivity(intent)
                                } catch (_: Exception) {
                                    // 如果上面的方法失敗，使用應用設定頁面
                                    try {
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.data = ("package:" + controller.packageName).toUri()
                                        controller.startActivity(intent)
                                    } catch (_: Exception) {
                                        ASToast.showShortToast("無法開啟設定頁面")
                                    }
                                }
                            }
                        }
                        .show()
                }
            }
        }
    }
}
