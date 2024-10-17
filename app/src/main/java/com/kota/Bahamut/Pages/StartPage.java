package com.kota.Bahamut.Pages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.core.content.pm.PackageInfoCompat;

import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.NotificationSettings;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TextView.TelnetTextViewSmall;

public class StartPage extends TelnetPage {
    /** 連線 */
    View.OnClickListener _connect_listener = v -> StartPage.this.onConnectButtonClicked();
    /** 離開 */
    View.OnClickListener _exit_listener = v -> StartPage.this.onExitButtonClicked();

    /** 按下教學 */
    View.OnClickListener urlClickListener = v -> {
        int id = v.getId();
        String url = "";
        if (id == R.id.Start_instructions) url = "https://kodaks-organization-1.gitbook.io/bahabbs-zhan-ba-ha-shi-yong-shou-ce/";
        if (id == R.id.Start_Icon_Discord) url = "https://discord.gg/YP8dthZ";
        if (id == R.id.Start_Icon_Facebook) url = "https://www.facebook.com/groups/264144897071532";
        if (id == R.id.Start_Icon_Reddit) url = "https://www.reddit.com/r/bahachat";
        if (id == R.id.Start_Icon_Steam) url = "https://steamcommunity.com/groups/BAHACHAT";
        if (id == R.id.Start_Icon_Telegram) url = "https://t.me/joinchat/MF5hqkuZN3B0NFqSyiz30A";

        if (!url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };
    /** 避難所 */


    /** 切換IP */
    RadioGroup.OnCheckedChangeListener radioGroupCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            RadioButton rb = (RadioButton) findViewById(checkedId);
            NotificationSettings.setConnectIpAddress(rb.getText().toString());
        }
    };

    public int getPageLayout() {
        return R.layout.start_page;
    }

    public int getPageType() {
        return BahamutPage.START;
    }

    @SuppressLint("SetTextI18n")
    public void onPageDidLoad() {
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
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioButtonIP);
        RadioButton radioButton1 = (RadioButton) findViewById(R.id.radioButtonIP1);
        RadioButton radioButton2 = (RadioButton) findViewById(R.id.radioButtonIP2);

        String connectIp = NotificationSettings.getConnectIpAddress();
        assert connectIp != null;
        if (connectIp.equals(radioButton1.getText().toString())) {
            radioButton1.setChecked(true);
        } else {
            radioButton2.setChecked(true);
        }
        radioGroup.setOnCheckedChangeListener(radioGroupCheckedChangeListener);

        PackageInfo packageInfo;
        try {
            packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(),0);
            int versionCode = (int) PackageInfoCompat.getLongVersionCode(packageInfo);
            String versionName = packageInfo.versionName;
            ((TelnetTextViewSmall)findViewById(R.id.version)).setText(versionCode + " - "+versionName );
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));
    }

    public void onPageWillAppear() {
        PageContainer pageContainer = PageContainer.getInstance();
        pageContainer.cleanStartPage();
    }

    public void onPageDidDisappear() {
        clear();
        super.onPageDidDisappear();
    }

    @Override
    public void clear() {
        ASProcessingDialog.dismissProcessingDialog();
        super.clear();
    }

    /** 按下離開 */
    public void onExitButtonClicked() {
        ASProcessingDialog.dismissProcessingDialog();
        getNavigationController().finish();
    }

    /** 手機: 上一步 */
    public boolean onBackPressed() {
        onExitButtonClicked();
        return true;
    }

    /** 按下連線按鈕 */
    public void onConnectButtonClicked() {
        connect();
    }

    /** 連線 */
    public void connect() {
        int _transportType = getNavigationController().getDeviceController().isNetworkAvailable();
        TempSettings.transportType = _transportType;
        if (_transportType>-1) {
            ASProcessingDialog.showProcessingDialog("連線中", aDialog -> {
                TelnetClient.getClient().close();
                return false;
            });
            String connectIpAddress = NotificationSettings.getConnectIpAddress();
            ASRunner.runInNewThread(()-> TelnetClient.getClient().connect(connectIpAddress, 23));
            return;
        }
        ASToast.showShortToast("您未連接網路");
    }
}
