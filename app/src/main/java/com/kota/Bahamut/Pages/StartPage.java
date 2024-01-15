package com.kota.Bahamut.Pages;

import com.kota.TelnetUI.TelnetPage;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Dialog.ASProcessingDialogOnBackDelegate;
import com.kota.ASFramework.UI.ASToast;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetClient;

import android.view.View;
import android.widget.Button;

public class StartPage extends TelnetPage {
    View.OnClickListener _connect_listener = new View.OnClickListener() {
        public void onClick(View v) {
            StartPage.this.onConnectButtonClicked();
        }
    };
    View.OnClickListener _exit_listener = new View.OnClickListener() {
        public void onClick(View v) {
            StartPage.this.onExitButtonClicked();
        }
    };

    public int getPageLayout() {
        return R.layout.start_page;
    }

    public int getPageType() {
        return 0;
    }

    public void onPageDidLoad() {
        getNavigationController().setNavigationTitle("勇者入口");
        ((Button) findViewById(R.id.Start_ExitButton)).setOnClickListener(this._exit_listener);
        ((Button) findViewById(R.id.Start_ConnectButton)).setOnClickListener(this._connect_listener);
    }

    public void onPageWillAppear() {
        PageContainer.getInstance().cleanLoginPage();
        PageContainer.getInstance().cleanMainPage();
        PageContainer.getInstance().cleanClassPage();
        PageContainer.getInstance().cleanBoardPage();
        PageContainer.getInstance().cleanBoardSearchPage();
        PageContainer.getInstance().cleanBoardTitleLinkedPage();
        PageContainer.getInstance().cleanMainBoxPage();
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

    public void clear() {
        ASProcessingDialog.hideProcessingDialog();
    }

    /** 按下離開 */
    public void onExitButtonClicked() {
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
        if (getNavigationController().getDeviceController().isNetworkAvailable()) {
            ASProcessingDialog.showProcessingDialog("連線中", new ASProcessingDialogOnBackDelegate() {
                public boolean onASProcessingDialogOnBackDetected(ASProcessingDialog aDialog) {
                    TelnetClient.getClient().close();
                    return false;
                }
            });
            ASRunner.runInNewThread(()->{
                    TelnetClient.getClient().connect("bbs.gamer.com.tw", 23);
            });
            return;
        }
        ASToast.showShortToast("您未連接網路");
    }
}
