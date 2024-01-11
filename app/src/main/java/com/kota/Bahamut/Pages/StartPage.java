package com.kota.Bahamut.Pages;

import android.view.View;
import android.widget.Button;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Dialog.ASProcessingDialogOnBackDelegate;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.PageContainer;
import com.kota.bahamut_bbs_2.R;;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;

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

    /* access modifiers changed from: private */
    public void onExitButtonClicked() {
        getNavigationController().finish();
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        onExitButtonClicked();
        return true;
    }

    /* access modifiers changed from: private */
    public void onConnectButtonClicked() {
        connect();
    }

    public void connect() {
        if (getNavigationController().getDeviceController().isNetworkAvailable()) {
            ASProcessingDialog.showProcessingDialog("連線中", new ASProcessingDialogOnBackDelegate() {
                public boolean onASProcessingDialogOnBackDetected(ASProcessingDialog aDialog) {
                    TelnetClient.getClient().close();
                    return false;
                }
            });
            new ASRunner() {
                public void run() {
                    TelnetClient.getClient().connect("bbs.gamer.com.tw", 23);
                }
            }.runInNewThread();
            return;
        }
        ASToast.showShortToast("您未連接網路");
    }
}
