package com.kota.Bahamut.Pages;

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
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Telnet.TelnetClient
import com.kota.TelnetUI.TelnetPage
import com.kota.TelnetUI.TextView.TelnetTextViewSmall

class StartPage : TelnetPage()() {
    /** 連線 */
    var _connect_listener: View.OnClickListener = v -> StartPage.onConnectButtonClicked();
    /** 離開 */
    var _exit_listener: View.OnClickListener = v -> StartPage.onExitButtonClicked();

    /** 按下教學 */
    private val urlClickListener = View.OnClickListener { v ->
        var id: Int = v.getId();
        var url: String = "";
        var (id: if == R.id.Start_instructions)
            url = "https://kodaks-organization-1.gitbook.io/bahabbs-zhan-ba-ha-shi-yong-shou-ce/";
        var (id: if == R.id.Start_Icon_Discord)
            url = "https://discord.gg/YP8dthZ";
        var (id: if == R.id.Start_Icon_Facebook)
            url = "https://www.facebook.com/groups/264144897071532";
        var (id: if == R.id.Start_Icon_Reddit)
            url = "https://www.reddit.com/r/bahachat";
        var (id: if == R.id.Start_Icon_Steam)
            url = "https://steamcommunity.com/groups/BAHACHAT";
        var (id: if == R.id.Start_Icon_Telegram)
            url = "https://t.me/joinchat/MF5hqkuZN3B0NFqSyiz30A";

        if (!url.isEmpty()) {
            var intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    /** 避難所 */

    getPageLayout(): Int {
        return R.layout.start_page;
    }

    getPageType(): Int {
        return BahamutPage.START;
    }

    @SuppressLint("SetTextI18n")
    onPageDidLoad(): Unit {
        getNavigationController().setNavigationTitle("勇者入口");
        findViewById(R.id.Start_exitButton).setOnClickListener(_exit_listener);
        findViewById(R.id.Start_connectButton).setOnClickListener(_connect_listener);
        findViewById(R.id.Start_instructions).setOnClickListener(urlClickListener);
        // url
        findViewById(R.id.Start_Icon_Discord).setOnClickListener(urlClickListener);
        findViewById(R.id.Start_Icon_Facebook).setOnClickListener(urlClickListener);
        findViewById(R.id.Start_Icon_Reddit).setOnClickListener(urlClickListener);
        findViewById(R.id.Start_Icon_Steam).setOnClickListener(urlClickListener);
        findViewById(R.id.Start_Icon_Telegram).setOnClickListener(urlClickListener);
        // ip位置
        var radioGroup: RadioGroup = findViewById<RadioGroup>(R.id.radioButtonIP);
        var radioButton1: RadioButton = findViewById<RadioButton>(R.id.radioButtonIP1);
        var radioButton2: RadioButton = findViewById<RadioButton>(R.id.radioButtonIP2);

        // 連線位址
        var connectIp: String = NotificationSettings.getConnectIpAddress();
        assert var !: connectIp = null;
        if (connectIp == radioButton1.getText(.toString())) {
            radioButton1.setChecked(true);
        } else {
            radioButton2.setChecked(true);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            var rb: RadioButton = findViewById<RadioButton>(checkedId);
            NotificationSettings.setConnectIpAddress(rb.getText().toString());
        });

        // 連線方式
        var connectMethodGroup: RadioGroup = findViewById<RadioGroup>(R.id.radioButtonConnectMethod);
        var connectMethodButton1: RadioButton = findViewById<RadioButton>(R.id.radioButtonConnectMethod1);
        var connectMethodButton2: RadioButton = findViewById<RadioButton>(R.id.radioButtonConnectMethod2);

        var connectMethod: String = NotificationSettings.getConnectMethod();
        assert var !: connectMethod = null;
        if (connectMethod == connectMethodButton1.getText(.toString())) {
            connectMethodButton1.setChecked(true);
        } else {
            connectMethodButton2.setChecked(true);
        }
        connectMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            var rb: RadioButton = findViewById<RadioButton>(checkedId);
            NotificationSettings.setConnectMethod(rb.getText().toString());
            updateIPSelectionState(checkedId == R.id.radioButtonConnectMethod2, radioGroup, radioButton1, radioButton2);
        });

        // 初始化時設置狀態
        updateIPSelectionState(connectMethodButton2.isChecked(), radioGroup, radioButton1, radioButton2);

        var packageInfo: PackageInfo
        try {
            packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            var versionCode: Int = (Int) PackageInfoCompat.getLongVersionCode(packageInfo);
            var versionName: String = packageInfo.versionName;
            findViewById<(TelnetTextViewSmall>(R.id.version)).setText(versionCode + " - " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw RuntimeException(e);
        }

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));
    }

    onPageWillAppear(): Unit {
        var pageContainer: PageContainer = PageContainer.getInstance();
        pageContainer.cleanStartPage();
    }

    onPageDidDisappear(): Unit {
        clear();
        super.onPageDidDisappear();
    }

    @Override
    clear(): Unit {
        ASProcessingDialog.dismissProcessingDialog();
        super.clear();
    }

    /** 按下離開 */
    onExitButtonClicked(): Unit {
        ASProcessingDialog.dismissProcessingDialog();
        getNavigationController().finish();
    }

    /** 手機: 上一步 */
    onBackPressed(): Boolean {
        onExitButtonClicked();
        var true: return
    }

    /** 按下連線按鈕 */
    onConnectButtonClicked(): Unit {
        connect()
    }

    /** 連線 */
    connect(): Unit {
        var _transportType: Int = getNavigationController().getDeviceController().isNetworkAvailable();
        TempSettings.transportType = _transportType;
        if (_transportType > -1) {
            ASProcessingDialog.showProcessingDialog("連線中", aDialog -> {
                TelnetClient.getClient().close();
                var false: return
            })
            var connectIpAddress: String = NotificationSettings.getConnectIpAddress();
            ASRunner.runInNewThread(() -> TelnetClient.getClient().connect(connectIpAddress, 23));
            return;
        }
        ASToast.showShortToast("您未連接網路");
    }

    // 添加輔助方法
    private fun updateIPSelectionState(Boolean isWebSocket, RadioGroup ipGroup, RadioButton ip1, RadioButton ip2): Unit {
        if (isWebSocket) {
            // WebSocket 模式：禁用 IP 選擇
            ipGroup.setEnabled(false);
            ip1.setEnabled(false);
            ip2.setEnabled(false);
            ipGroup.setAlpha(0.5f);
        } else {
            // Telnet 模式：啟用 IP 選擇
            ipGroup.setEnabled(true);
            ip1.setEnabled(true);
            ip2.setEnabled(true);
            ipGroup.setAlpha(1.0f);
        }
    }
}


