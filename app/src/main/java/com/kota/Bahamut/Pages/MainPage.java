package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.BahamutStateHandler;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

public class MainPage extends TelnetPage {
    RelativeLayout mainLayout;
    View.OnClickListener _boards_listener = v -> {
        PageContainer.getInstance().pushClassPage("Boards", "佈告討論區");
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("b");
    };
    View.OnClickListener _class_listener = v -> {
        PageContainer.getInstance().pushClassPage("Class", "分組討論區");
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("c");
    };
    View.OnClickListener _favorite_listener = v -> {
        PageContainer.getInstance().pushClassPage("Favorite", "我的最愛");
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("f");
    };
    TelnetFrame _frame_buffer = null;
    ASDialog _goodbye_dialog = null;
    View.OnClickListener _logout_listener = v -> TelnetClient.getClient().sendStringToServerInBackground("g");
    View.OnClickListener _mail_listener = v -> {
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getMailBoxPage());
        TelnetClient.getClient().sendStringToServerInBackground("m\nr");
    };
    ASDialog _save_hot_message_dialog = null;
    View.OnClickListener _system_setting_listener = v -> MainPage.this.getNavigationController().pushViewController(new SystemSettingsPage());

    private enum LastLoadClass {
        Unload,
        Boards,
        Class,
        Favorite
    }

    public int getPageLayout() {
        return R.layout.main_page;
    }

    public int getPageType() {
        return BahamutPage.BAHAMUT_MAIN;
    }

    public void onPageDidLoad() {
        mainLayout = (RelativeLayout) findViewById(R.id.content_view);
        mainLayout.findViewById(R.id.Main_boardsButton).setOnClickListener(this._boards_listener);
        mainLayout.findViewById(R.id.Main_classButton).setOnClickListener(this._class_listener);
        mainLayout.findViewById(R.id.Main_FavoriteButton).setOnClickListener(this._favorite_listener);
        mainLayout.findViewById(R.id.Main_logoutButton).setOnClickListener(this._logout_listener);
        mainLayout.findViewById(R.id.Main_mailButton).setOnClickListener(this._mail_listener);
        mainLayout.findViewById(R.id.Main_systemSettingsButton).setOnClickListener(this._system_setting_listener);

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            new ASRunner(){
                @Override
                public void run() {
                    ASProcessingDialog.showProcessingDialog(getContextString(R.string.is_under_auto_logging_chat));
                    // 進入布告討論區
                    mainLayout.findViewById(R.id.Main_boardsButton).performClick();
                }
            }.postDelayed(300);
        }
    }

    public void onPageRefresh() {
        setFrameToTelnetView();
    }

    private void setFrameToTelnetView() {
        TelnetView telnet_view = mainLayout.findViewById(R.id.Main_TelnetView);
        if (telnet_view != null) {
            if (BahamutStateHandler.getInstance().getCurrentPage() == 5) {
                this._frame_buffer = TelnetClient.getModel().getFrame().clone();
                for (int i = 12; i < 24; i++) {
                    this._frame_buffer.removeRow(12);
                }
                this._frame_buffer.removeRow(0);
            }
            if (this._frame_buffer != null) {
                telnet_view.setFrame(this._frame_buffer);
            }
        }
    }

    public boolean onPagePreload() {
        LastLoadClass _last_load_class = LastLoadClass.Unload;
        return true;
    }

    public void onPageWillDisappear() {
        clear();
    }

    public void onPageDidDisappear() {
        this._goodbye_dialog = null;
        this._save_hot_message_dialog = null;
        super.onPageDidDisappear();
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        this._logout_listener.onClick(null);
        return true;
    }

    public void onProcessHotMessage() {
        if (this._save_hot_message_dialog == null) {
            this._save_hot_message_dialog = ASAlertDialog.createDialog()
                    .setTitle("熱訊")
                    .setMessage("本次上站熱訊處理 ")
                    .addButton("備忘錄")
                    .addButton("保留")
                    .addButton("清除")
                    .setListener((aDialog, index) -> {
                MainPage.this._save_hot_message_dialog = null;
                switch (index) {
                    case 0 -> TelnetClient.getClient().sendStringToServerInBackground("M");
                    case 1 -> TelnetClient.getClient().sendStringToServerInBackground("K");
                    case 2 -> TelnetClient.getClient().sendStringToServerInBackground("C");
                    default -> {
                        TelnetClient.getClient().sendStringToServerInBackground("K");
                    }
                }
            }).scheduleDismissOnPageDisappear(this).setOnBackDelegate(aDialog -> {
                TelnetClient.getClient().sendStringToServerInBackground("K\nQ");
                MainPage.this._save_hot_message_dialog = null;
                return false;
            });
            this._save_hot_message_dialog.show();
        }
    }

    public void onCheckGoodbye() {
        if (this._goodbye_dialog == null) {
            this._goodbye_dialog = ASAlertDialog.createDialog().setTitle("登出").setMessage("是否確定要登出?").addButton("取消").addButton("確認").setListener((aDialog, index) -> {
                MainPage.this._goodbye_dialog = null;
                switch (index) {
                    case 0 -> // 取消
                            TelnetClient.getClient().sendStringToServerInBackground("Q");
                    case 1 -> // 確定
                            TelnetClient.getClient().sendStringToServerInBackground("G");
                    default -> {
                    }
                }
            }).scheduleDismissOnPageDisappear(this);
            this._goodbye_dialog.show();
        }
    }

    public void clear() {
        if (this._goodbye_dialog != null) {
            if (this._goodbye_dialog.isShowing()) {
                this._goodbye_dialog.dismiss();
            }
            this._goodbye_dialog = null;
        }
        if (this._save_hot_message_dialog != null) {
            if (this._save_hot_message_dialog.isShowing()) {
                this._save_hot_message_dialog.dismiss();
            }
            this._save_hot_message_dialog = null;
        }
    }
}
