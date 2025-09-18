package com.kota.Bahamut.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.pm.PackageInfoCompat
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialogOnBackDelegate
import com.kota.asFramework.thread.ASRunner.Companion.runInNewThread
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.NotificationSettings.getConnectIpAddress
import com.kota.Bahamut.service.NotificationSettings.getConnectMethod
import com.kota.Bahamut.service.NotificationSettings.setConnectIpAddress
import com.kota.Bahamut.service.NotificationSettings.setConnectMethod
import com.kota.Bahamut.service.TempSettings
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.textView.TelnetTextViewSmall

class StartPage : TelnetPage() {
    /** 連線  */
    var _connect_listener: View.OnClickListener =
        View.OnClickListener { v: View? -> this@StartPage.onConnectButtonClicked() }

    /** 離開  */
    var _exit_listener: View.OnClickListener =
        View.OnClickListener { v: View? -> this@StartPage.onExitButtonClicked() }

    /** 按下教學  */
    var urlClickListener: View.OnClickListener = View.OnClickListener { v: View? ->
        val id = v!!.getId()
        var url = ""
        if (id == R.id.Start_instructions) url =
            "https://kodaks-organization-1.gitbook.io/bahabbs-zhan-ba-ha-shi-yong-shou-ce/"
        if (id == R.id.Start_Icon_Discord) url = "https://discord.gg/YP8dthZ"
        if (id == R.id.Start_Icon_Facebook) url = "https://www.facebook.com/groups/264144897071532"
        if (id == R.id.Start_Icon_Reddit) url = "https://www.reddit.com/r/bahachat"
        if (id == R.id.Start_Icon_Steam) url = "https://steamcommunity.com/groups/BAHACHAT"
        if (id == R.id.Start_Icon_Telegram) url = "https://t.me/joinchat/MF5hqkuZN3B0NFqSyiz30A"
        if (!url.isEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    val pageLayout: Int
        /** 避難所  */
        get() = R.layout.start_page

    val pageType: Int
        get() = BahamutPage.START

    @SuppressLint("SetTextI18n")
    public override fun onPageDidLoad() {
        navigationController!!.setNavigationTitle("勇者入口")
        findViewById(R.id.Start_exitButton)!!.setOnClickListener(_exit_listener)
        findViewById(R.id.Start_connectButton)!!.setOnClickListener(_connect_listener)
        findViewById(R.id.Start_instructions)!!.setOnClickListener(urlClickListener)
        // url
        findViewById(R.id.Start_Icon_Discord)!!.setOnClickListener(urlClickListener)
        findViewById(R.id.Start_Icon_Facebook)!!.setOnClickListener(urlClickListener)
        findViewById(R.id.Start_Icon_Reddit)!!.setOnClickListener(urlClickListener)
        findViewById(R.id.Start_Icon_Steam)!!.setOnClickListener(urlClickListener)
        findViewById(R.id.Start_Icon_Telegram)!!.setOnClickListener(urlClickListener)
        // ip位置
        val radioGroup = findViewById(R.id.radioButtonIP) as RadioGroup?
        val radioButton1 = findViewById(R.id.radioButtonIP1) as RadioButton?
        val radioButton2 = findViewById(R.id.radioButtonIP2) as RadioButton?

        // 連線位址
        val connectIp: String? = checkNotNull(getConnectIpAddress())
        if (connectIp == radioButton1!!.getText().toString()) {
            radioButton1.setChecked(true)
        } else {
            radioButton2!!.setChecked(true)
        }
        radioGroup!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            val rb = findViewById(checkedId) as RadioButton?
            setConnectIpAddress(rb!!.getText().toString())
        })

        // 連線方式
        val connectMethodGroup = findViewById(R.id.radioButtonConnectMethod) as RadioGroup?
        val connectMethodButton1 = findViewById(R.id.radioButtonConnectMethod1) as RadioButton?
        val connectMethodButton2 = findViewById(R.id.radioButtonConnectMethod2) as RadioButton?

        val connectMethod: String? = checkNotNull(getConnectMethod())
        if (connectMethod == connectMethodButton1!!.getText().toString()) {
            connectMethodButton1.setChecked(true)
        } else {
            connectMethodButton2!!.setChecked(true)
        }
        connectMethodGroup!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            val rb = findViewById(checkedId) as RadioButton?
            setConnectMethod(rb!!.getText().toString())
            updateIPSelectionState(
                checkedId == R.id.radioButtonConnectMethod2,
                radioGroup,
                radioButton1,
                radioButton2!!
            )
        })

        // 初始化時設置狀態
        updateIPSelectionState(
            connectMethodButton2!!.isChecked(),
            radioGroup,
            radioButton1,
            radioButton2!!
        )

        val packageInfo: PackageInfo
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
            val versionName = packageInfo.versionName
            (findViewById(R.id.version) as TelnetTextViewSmall).setText(versionCode.toString() + " - " + versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    public override fun onPageWillAppear() {
        val pageContainer = PageContainer.getInstance()
        pageContainer.cleanStartPage()
    }

    public override fun onPageDidDisappear() {
        clear()
        super.onPageDidDisappear()
    }

    public override fun clear() {
        dismissProcessingDialog()
        super.clear()
    }

    /** 按下離開  */
    fun onExitButtonClicked() {
        dismissProcessingDialog()
        navigationController!!.finish()
    }

    /** 手機: 上一步  */
    public override fun onBackPressed(): Boolean {
        onExitButtonClicked()
        return true
    }

    /** 按下連線按鈕  */
    fun onConnectButtonClicked() {
        connect()
    }

    /** 連線  */
    fun connect() {
        val _transportType = navigationController!!.deviceController!!.isNetworkAvailable
        TempSettings.transportType = _transportType
        if (_transportType > -1) {
            showProcessingDialog(
                "連線中",
                ASProcessingDialogOnBackDelegate { aDialog: ASProcessingDialog? ->
                    TelnetClient.client!!.close()
                    false
                })
            val connectIpAddress = getConnectIpAddress()
            runInNewThread(Runnable { TelnetClient.client!!.connect(connectIpAddress, 23) })
            return
        }
        showShortToast("您未連接網路")
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
            ipGroup.setEnabled(false)
            ip1.setEnabled(false)
            ip2.setEnabled(false)
            ipGroup.setAlpha(0.5f)
        } else {
            // Telnet 模式：啟用 IP 選擇
            ipGroup.setEnabled(true)
            ip1.setEnabled(true)
            ip2.setEnabled(true)
            ipGroup.setAlpha(1.0f)
        }
    }
}
