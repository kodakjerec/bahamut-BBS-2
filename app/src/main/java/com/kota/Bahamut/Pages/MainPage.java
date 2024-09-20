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
    View.OnClickListener userListener = v -> {
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getUserPage());
        TelnetClient.getClient().sendStringToServerInBackground("u");
    };
    View.OnClickListener boardsListener = v -> {
        PageContainer.getInstance().pushClassPage("Boards", "佈告討論區");
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("b");
    };
    View.OnClickListener classListener = v -> {
        PageContainer.getInstance().pushClassPage("Class", "分組討論區");
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("c");
    };
    View.OnClickListener favoriteListener = v -> {
        PageContainer.getInstance().pushClassPage("Favorite", "我的最愛");
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("f");
    };
    TelnetFrame _frame_buffer = null;
    ASDialog goodbyeDialog = null;
    View.OnClickListener logoutListener = v -> TelnetClient.getClient().sendStringToServerInBackground("g");
    View.OnClickListener mailListener = v -> {
        MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getMailBoxPage());
        TelnetClient.getClient().sendStringToServerInBackground("m\nr");
    };
    ASDialog _save_hot_message_dialog = null;
    View.OnClickListener systemSettingListener = v -> MainPage.this.getNavigationController().pushViewController(new SystemSettingsPage());

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
        mainLayout.findViewById(R.id.Main_userButton).setOnClickListener(this.userListener);
        mainLayout.findViewById(R.id.Main_boardsButton).setOnClickListener(this.boardsListener);
        mainLayout.findViewById(R.id.Main_classButton).setOnClickListener(this.classListener);
        mainLayout.findViewById(R.id.Main_FavoriteButton).setOnClickListener(this.favoriteListener);
        mainLayout.findViewById(R.id.Main_logoutButton).setOnClickListener(this.logoutListener);
        mainLayout.findViewById(R.id.Main_mailButton).setOnClickListener(this.mailListener);
        mainLayout.findViewById(R.id.Main_systemSettingsButton).setOnClickListener(this.systemSettingListener);

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
        this.goodbyeDialog = null;
        this._save_hot_message_dialog = null;
        super.onPageDidDisappear();
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        this.logoutListener.onClick(null);
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
                    default -> TelnetClient.getClient().sendStringToServerInBackground("K");
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
        if (this.goodbyeDialog == null) {
            this.goodbyeDialog = ASAlertDialog.createDialog()
                    .setTitle("登出")
                    .setMessage("是否確定要登出?")
                    .addButton("取消")
                    .addButton("確認")
                    .setListener((aDialog, index) -> {
                        MainPage.this.goodbyeDialog = null;
                        switch (index) {
                            case 1 -> // 確定
                                    TelnetClient.getClient().sendStringToServerInBackground("G");
                            case 0 -> // 取消
                                    TelnetClient.getClient().sendStringToServerInBackground("Q");
                            default -> {}
                        }
                    })
                    .scheduleDismissOnPageDisappear(this);
            this.goodbyeDialog.setOnDismissListener( (dialog)-> {
                        if (this.goodbyeDialog != null)
                            TelnetClient.getClient().sendStringToServerInBackground("G");
                    });
        }
        this.goodbyeDialog.show();
    }

    public void clear() {
        if (this.goodbyeDialog != null) {
            if (this.goodbyeDialog.isShowing()) {
                this.goodbyeDialog.dismiss();
            }
            this.goodbyeDialog = null;
        }
        if (this._save_hot_message_dialog != null) {
            if (this._save_hot_message_dialog.isShowing()) {
                this._save_hot_message_dialog.dismiss();
            }
            this._save_hot_message_dialog = null;
        }
    }
}
