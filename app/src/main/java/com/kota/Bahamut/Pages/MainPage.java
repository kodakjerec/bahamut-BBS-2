package com.kota.Bahamut.Pages;

import android.view.View;
import android.widget.Button;
import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASAlertDialogListener;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.ASFramework.Dialog.ASDialogOnBackPressedDelegate;
import com.kota.Bahamut.BahamutStateHandler;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.R;
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

public class MainPage extends TelnetPage {
    View.OnClickListener _boards_listener = new View.OnClickListener() {
        public void onClick(View v) {
            PageContainer.getInstance().pushClassPage("Boards", "佈告討論區");
            MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
            TelnetClient.getClient().sendStringToServerInBackground("b");
        }
    };
    View.OnClickListener _class_listener = new View.OnClickListener() {
        public void onClick(View v) {
            PageContainer.getInstance().pushClassPage("Class", "分組討論區");
            MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
            TelnetClient.getClient().sendStringToServerInBackground("c");
        }
    };
    View.OnClickListener _favorite_listener = new View.OnClickListener() {
        public void onClick(View v) {
            PageContainer.getInstance().pushClassPage("Favorite", "我的最愛");
            MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
            TelnetClient.getClient().sendStringToServerInBackground("f");
        }
    };
    TelnetFrame _frame_buffer = null;
    ASDialog _goodbye_dialog = null;
    private LastLoadClass _last_load_class = LastLoadClass.Unload;
    View.OnClickListener _logout_listener = new View.OnClickListener() {
        public void onClick(View v) {
            TelnetClient.getClient().sendStringToServerInBackground("g");
        }
    };
    View.OnClickListener _mail_listener = new View.OnClickListener() {
        public void onClick(View v) {
            MainPage.this.getNavigationController().pushViewController(PageContainer.getInstance().getMailBoxPage());
            TelnetClient.getClient().sendStringToServerInBackground("m\nr");
        }
    };
    ASDialog _save_hot_message_dialog = null;
    View.OnClickListener _system_setting_listener = new View.OnClickListener() {
        public void onClick(View v) {
            MainPage.this.getNavigationController().pushViewController(new SystemSettingsPage());
        }
    };

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
        return 5;
    }

    public void onPageDidLoad() {
        ((Button) findViewById(R.id.Main_BoardsButton)).setOnClickListener(this._boards_listener);
        ((Button) findViewById(R.id.Main_ClassButton)).setOnClickListener(this._class_listener);
        ((Button) findViewById(R.id.Main_FavoriteButton)).setOnClickListener(this._favorite_listener);
        ((Button) findViewById(R.id.Main_LogoutButton)).setOnClickListener(this._logout_listener);
        ((Button) findViewById(R.id.Main_MailButton)).setOnClickListener(this._mail_listener);
        ((Button) findViewById(R.id.Main_SystemSettingsButton)).setOnClickListener(this._system_setting_listener);
    }

    public void onPageRefresh() {
        setFrameToTelnetView();
    }

    private void setFrameToTelnetView() {
        TelnetView telnet_view = (TelnetView) findViewById(R.id.Main_TelnetView);
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
        this._last_load_class = LastLoadClass.Unload;
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
        this._logout_listener.onClick((View) null);
        return true;
    }

    public void onProcessHotMessage() {
        if (this._save_hot_message_dialog == null) {
            this._save_hot_message_dialog = ASAlertDialog.createDialog().setTitle("熱訊").setMessage("本次上站熱訊處理 ").addButton("備忘錄").addButton("保留").addButton("清除").setListener(new ASAlertDialogListener() {
                public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                    MainPage.this._save_hot_message_dialog = null;
                    switch (index) {
                        case 0:
                            TelnetClient.getClient().sendStringToServerInBackground("M");
                            return;
                        case 1:
                            TelnetClient.getClient().sendStringToServerInBackground("K");
                            return;
                        case 2:
                            TelnetClient.getClient().sendStringToServerInBackground("C");
                            return;
                        default:
                            return;
                    }
                }
            }).scheduleDismissOnPageDisappear(this).setOnBackDelegate(new ASDialogOnBackPressedDelegate() {
                public boolean onASDialogBackPressed(ASDialog aDialog) {
                    TelnetClient.getClient().sendStringToServerInBackground("K\nQ");
                    MainPage.this._save_hot_message_dialog = null;
                    return false;
                }
            });
            this._save_hot_message_dialog.show();
        }
    }

    public void onCheckGoodbye() {
        if (this._goodbye_dialog == null) {
            this._goodbye_dialog = ASAlertDialog.createDialog().setTitle("登出").setMessage("是否確定要登出?").addButton("取消").addButton("確認").setListener(new ASAlertDialogListener() {
                public void onAlertDialogDismissWithButtonIndex(ASAlertDialog aDialog, int index) {
                    MainPage.this._goodbye_dialog = null;
                    switch (index) {
                        case 0:
                            TelnetClient.getClient().sendStringToServerInBackground("Q");
                            return;
                        case 1:
                            UserSettings settings = new UserSettings(MainPage.this.getContext());
                            settings.setLastConnectionIsOfflineByUser(true);
                            settings.notifyDataUpdated();
                            TelnetClient.getClient().sendStringToServerInBackground("G");
                            return;
                        default:
                            return;
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
