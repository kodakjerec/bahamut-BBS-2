package com.kota.Bahamut;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASAlertDialogListener;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.PageController.ASViewController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.DataModels.ArticleTempStore;
import com.kota.Bahamut.DataModels.BookmarkStore;
import com.kota.Bahamut.Pages.Model.BoardPageBlock;
import com.kota.Bahamut.Pages.Model.BoardPageItem;
import com.kota.Bahamut.Pages.Model.ClassPageBlock;
import com.kota.Bahamut.Pages.Model.ClassPageItem;
import com.kota.Bahamut.Pages.Model.MailBoxPageBlock;
import com.kota.Bahamut.Pages.Model.MailBoxPageItem;
import com.kota.Bahamut.Pages.StartPage;
import com.kota.Bahamut.Service.BahaBBSBackgroundService;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetClientListener;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TextEncoder.B2UEncoder;
import com.kota.TextEncoder.U2BEncoder;
import com.kota.Bahamut.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;

public class BahamutController extends ASNavigationController implements TelnetClientListener {
    /* access modifiers changed from: protected */
    public void onControllerWillLoad() {
        requestWindowFeature(1);
        try {
            B2UEncoder.constructInstance(getResources().openRawResource(R.raw.b2u));
            U2BEncoder.constructInstance(getResources().openRawResource(R.raw.u2b));
        } catch (Exception e) {
            e.printStackTrace();
        }
        BookmarkStore.upgrade(this, getFilesDir().getPath() + "/bookmark.dat");
        setAnimationEnable(new UserSettings(this).isAnimationEnable());
        ArticleTempStore.upgrade(this, getFilesDir().getPath() + "/article_temp.dat");
        TelnetClient.construct(BahamutStateHandler.getInstance());
        TelnetClient.getClient().setListener(this);
        PageContainer.constructInstance();
    }

    /* access modifiers changed from: protected */
    public void onControllerDidLoad() {
        pushViewController(PageContainer.getInstance().getStartPage(), false);
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
    }

    /* access modifiers changed from: protected */
    public String getControllerName() {
        return "Baha!BBS";
    }

    public boolean onBackLongPressed() {
        boolean result = true;
        if (TelnetClient.getConnector().isConnecting()) {
            new ASAlertDialog().setMessage("是否確定要強制斷線?").addButton("取消").addButton("斷線").setListener(new ASAlertDialogListener() {
                public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                    if (index == 1) {
                        TelnetClient.getClient().close();
                        UserSettings settings = new UserSettings(BahamutController.this);
                        settings.setLastConnectionIsOfflineByUser(true);
                        settings.notifyDataUpdated();
                    }
                }
            }).show();
        } else {
            result = false;
        }
        ASProcessingDialog.hideProcessingDialog();
        return result;
    }

    /* access modifiers changed from: private */
    public void showConnectionStartMessage() {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd kk:hh:ss");
        date_format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        System.out.println("Baha!BBS connection start:" + date_format.format(new Date()));
    }

    public void onTelnetClientConnectionStart(TelnetClient aClient) {
        new ASRunner() {
            public void run() {
                BahamutController.this.showConnectionStartMessage();
            }
        }.runInMainThread();
    }

    public void onTelnetClientConnectionSuccess(TelnetClient aClient) {
        startService(new Intent(this, BahaBBSBackgroundService.class));
    }

    public void onTelnetClientConnectionFail(TelnetClient aClient) {
        ASProcessingDialog.hideProcessingDialog();
        ASToast.showShortToast("連線失敗，請檢查網路連線或稍後再試");
    }

    public void onTelnetClientConnectionClosed(TelnetClient aClient) {
        stopService(new Intent(this, BahaBBSBackgroundService.class));
        new ASRunner() {
            public void run() {
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd kk:hh:ss");
                date_format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                System.out.println("Baha!BBS connection close:" + date_format.format(new Date()));
                BahamutController.this.handleNormalConnectionClosed();
                ASToast.showShortToast("連線已中斷");
                ASProcessingDialog.hideProcessingDialog();
            }
        }.runInMainThread();
    }

    /* access modifiers changed from: private */
    public void handleNormalConnectionClosed() {
        Vector<ASViewController> pages = ASNavigationController.getCurrentController().getViewControllers();
        Vector<ASViewController> new_controllers = new Vector<>();
        Iterator<ASViewController> it = pages.iterator();
        while (it.hasNext()) {
            TelnetPage telnet_page = (TelnetPage) it.next();
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

    public void onLowMemory() {
        super.onLowMemory();
        BoardPageBlock.release();
        BoardPageItem.release();
        ClassPageBlock.release();
        ClassPageItem.release();
        MailBoxPageBlock.release();
        MailBoxPageItem.release();
        System.gc();
    }

    public boolean isAnimationEnable() {
        return !getSharedPreferences(UserSettings.PERF_NAME, 0).getBoolean(UserSettings.PROPERTIES_ANIMATION_DISABLE, false);
    }
}
