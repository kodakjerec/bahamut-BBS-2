package com.kota.Bahamut.Pages;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.core.content.pm.PackageInfoCompat;

import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TextView.TelnetTextViewSmall;

public class StartPage extends TelnetPage {
    View.OnClickListener _connect_listener = v -> StartPage.this.onConnectButtonClicked();
    View.OnClickListener _exit_listener = v -> StartPage.this.onExitButtonClicked();

    /** 按下教學 */
    View.OnClickListener _instruction_listener = v -> {
        InstructionsPage page = PageContainer.getInstance().getInstructionPage();
        getNavigationController().pushViewController(page);
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
        findViewById(R.id.Start_instructions).setOnClickListener(_instruction_listener);
        PackageInfo packageInfo;
        try {
            packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(),0);
            int versionCode = (int) PackageInfoCompat.getLongVersionCode(packageInfo);
            String versionName = packageInfo.versionName;
            ((TelnetTextViewSmall)findViewById(R.id.version)).setText(versionCode + " - "+versionName );
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void onPageWillAppear() {
        PageContainer.getInstance().cleanLoginPage();
        PageContainer.getInstance().cleanMainPage();
        PageContainer.getInstance().cleanClassPage();
        PageContainer.getInstance().cleanBoardPage();
        PageContainer.getInstance().cleanBoardSearchPage();
        PageContainer.getInstance().cleanBoardTitleLinkedPage();
        PageContainer.getInstance().cleanMainBoxPage();
        PageContainer.getInstance().cleanBillingPage();
    }

    public void onPageDidDisappear() {
        clear();
        super.onPageDidDisappear();
    }

    public void onPageRefresh() {
    }

    public boolean onPagePreload() {
        return true;
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
        TempSettings.setTransportType(_transportType);
        if (_transportType>-1) {
            ASProcessingDialog.showProcessingDialog("連線中", aDialog -> {
                TelnetClient.getClient().close();
                return false;
            });
            ASRunner.runInNewThread(()-> TelnetClient.getClient().connect("bbs.gamer.com.tw", 23));
            return;
        }
        ASToast.showShortToast("您未連接網路");
    }
}
