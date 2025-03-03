package com.kota.Bahamut;

import static com.kota.Bahamut.Service.CommonFunctions.changeScreenOrientation;
import static com.kota.Bahamut.Service.MyBillingClient.checkPurchase;
import static com.kota.Bahamut.Service.MyBillingClient.closeBillingClient;
import static com.kota.Bahamut.Service.MyBillingClient.initBillingClient;

import android.content.Intent;
import android.util.Log;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASAlertDialogListener;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.DataModels.ArticleTempStore;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.Pages.Model.BoardEssencePageItem;
import com.kota.Bahamut.Pages.Model.BoardPageBlock;
import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.Bahamut.Pages.Model.ClassPageBlock;
import com.kota.Bahamut.Pages.Model.ClassPageItem;
import com.kota.Bahamut.Pages.Model.MailBoxPageBlock;
import com.kota.Bahamut.Pages.Model.MailBoxPageItem;
import com.kota.Bahamut.Pages.StartPage;
import com.kota.Bahamut.Pages.Theme.ThemeStore;
import com.kota.Bahamut.Service.BahaBBSBackgroundService;
import com.kota.Bahamut.Service.CloudBackup;
import com.kota.Bahamut.Service.NotificationSettings;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetClientListener;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TextEncoder.B2UEncoder;
import com.kota.TextEncoder.U2BEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Vector;

public class BahamutController extends ASNavigationController implements TelnetClientListener {
    @Override // com.kota.ASFramework.PageController.ASNavigationController
    protected void onControllerWillLoad() {
        requestWindowFeature(1);
        try {
            B2UEncoder.constructInstance(getResources().openRawResource(R.raw.b2u));
            U2BEncoder.constructInstance(getResources().openRawResource(R.raw.u2b));
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage()!=null?e.getMessage():"");
        }
        // 書籤
        String bookmark_file_path = getFilesDir().getPath() + "/bookmark.dat";
        BookmarkStore.upgrade(this, bookmark_file_path);

        // 暫存檔
        String article_file_path = getFilesDir().getPath() + "/article_temp.dat";
        ArticleTempStore.upgrade(this, article_file_path);

        // 系統架構
        TelnetClient.construct(BahamutStateHandler.getInstance());
        TelnetClient.getClient().setListener(this);
        PageContainer.constructInstance();

        // UserSettings
        setAnimationEnable(UserSettings.getPropertiesAnimationEnable());
        // 啟用wifi鎖定
        if (UserSettings.getPropertiesKeepWifi()) {
            getDeviceController().lockWifi();
        }

        // 共用函數
        TempSettings.myContext = this;
        TempSettings.myActivity = ASNavigationController.getCurrentController();
        changeScreenOrientation();

        // 以下需等待 共用函數 設定完畢
        // VIP
        TempSettings.applicationContext = getApplicationContext();
        initBillingClient();

        // 外觀
        ThemeStore.INSTANCE.upgrade(this);
    }

    @Override // com.kota.ASFramework.PageController.ASNavigationController
    protected void onControllerDidLoad() {
        StartPage start_page = PageContainer.getInstance().getStartPage();
        pushViewController(start_page, false);
    }

    @Override
    protected void onResume() {
        checkPurchase();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 關閉VIP
        closeBillingClient();

        // 備份雲端
        if (NotificationSettings.getCloudSave()) {
            CloudBackup cloudBackup = new CloudBackup();
            cloudBackup.backup();
        }

        // 強制關閉連線
        TelnetClient.getClient().close();

        super.onDestroy();
    }

    @Override // com.kota.ASFramework.PageController.ASNavigationController, android.app.Activity
    protected void onPause() {
        super.onPause();
    }

    @Override // com.kota.ASFramework.PageController.ASNavigationController
    protected String getControllerName() {
        return String.valueOf(R.string.app_name);
    }

    @Override // com.kota.ASFramework.PageController.ASNavigationController
    public boolean onBackLongPressed() {
        boolean result = true;
        if (TelnetClient.getConnector().isConnecting()) {
            ASAlertDialog dialog = new ASAlertDialog();
            dialog
                    .setMessage("是否確定要強制斷線?")
                    .addButton("取消")
                    .addButton("斷線")
                    .setListener(new ASAlertDialogListener() { // from class: com.kota.Bahamut.BahamutController.1
                @Override // com.kota.ASFramework.Dialog.ASAlertDialogListener
                public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                    if (index == 1) {
                        TelnetClient.getClient().close();
                    }
                }
            }).show();
        } else {
            result = false;
        }
        ASProcessingDialog.dismissProcessingDialog();
        return result;
    }

    private void showConnectionStartMessage() {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd kk:hh:ss");
        date_format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String time_string = date_format.format(new Date());
        System.out.println("BahaBBS connection start:" + time_string);
    }

    @Override // com.kota.Telnet.TelnetClientListener
    public void onTelnetClientConnectionStart(TelnetClient aClient) {
        new ASRunner() { // from class: com.kota.Bahamut.BahamutController.2
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                BahamutController.this.showConnectionStartMessage();
            }
        }.runInMainThread();
    }

    @Override // com.kota.Telnet.TelnetClientListener
    public void onTelnetClientConnectionSuccess(TelnetClient aClient) {
        Intent intent = new Intent(this, BahaBBSBackgroundService.class);
        startService(intent);
    }

    @Override // com.kota.Telnet.TelnetClientListener
    public void onTelnetClientConnectionFail(TelnetClient aClient) {
        ASProcessingDialog.dismissProcessingDialog();
        ASToast.showShortToast("連線失敗，請檢查網路連線或稍後再試");
    }

    @Override // com.kota.Telnet.TelnetClientListener
    public void onTelnetClientConnectionClosed(TelnetClient aClient) {
        Intent intent = new Intent(this, BahaBBSBackgroundService.class);
        stopService(intent);
        new ASRunner() { // from class: com.kota.Bahamut.BahamutController.3
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd kk:hh:ss");
                date_format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                String time_string = date_format.format(new Date());
                System.out.println("BahaBBS connection close:" + time_string);

                BahamutController.this.handleNormalConnectionClosed();

                ASToast.showShortToast("連線已中斷");

                ASProcessingDialog.dismissProcessingDialog();

                if (NotificationSettings.getCloudSave()) {
                  CloudBackup cloudBackup = new CloudBackup();
                  cloudBackup.backup();
                }

                if (TempSettings.getMessageSmall()!=null) {
                    getCurrentController().removeForeverView(TempSettings.getMessageSmall());
                    TempSettings.setMessageSmall(null);
                }
            }
        }.runInMainThread();
    }

    private void handleNormalConnectionClosed() {
        Vector<ASViewController> pages = ASNavigationController.getCurrentController().getViewControllers();
        Vector<ASViewController> new_controllers = new Vector<>();
        for (ASViewController controller : pages) {
            TelnetPage telnet_page = (TelnetPage) controller;
            if (telnet_page.getPageType() == 0 || telnet_page.isKeepOnOffline()) {
                new_controllers.add(telnet_page);
            }
        }
        StartPage start_page = PageContainer.getInstance().getStartPage();
        if (!new_controllers.contains(start_page)) {
            new_controllers.insertElementAt(start_page, 0);
        }
        setViewControllers(new_controllers);
    }

    @Override // com.kota.ASFramework.PageController.ASNavigationController, android.app.Activity, android.content.ComponentCallbacks
    public void onLowMemory() {
        super.onLowMemory();
        BoardPageBlock.release();
        BoardPageItem.release();
        BoardEssencePageItem.Companion.release();
        ClassPageBlock.release();
        ClassPageItem.release();
        MailBoxPageBlock.release();
        MailBoxPageItem.release();
        System.gc();
    }

    @Override // com.kota.ASFramework.PageController.ASNavigationController
    public boolean isAnimationEnable() {
        return UserSettings.getPropertiesAnimationEnable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
