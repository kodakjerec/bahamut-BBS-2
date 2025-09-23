package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.BahamutStateHandler;
import com.kota.Bahamut.Dialogs.DialogHeroStep;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Messages.MessageDatabase;
import com.kota.Bahamut.Pages.Messages.MessageMain;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.HeroStep;
import com.kota.Bahamut.Service.NotificationSettings;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TelnetView;

import java.util.List;

public class MainPage extends TelnetPage {
    public String onlinePeople = ""; // 線上人數
    public String bbCallStatus = ""; // 呼叫器
    RelativeLayout mainLayout;
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
    ASDialog saveHotMessageDialog = null;
    View.OnClickListener systemSettingListener = v -> MainPage.this.getNavigationController().pushViewController(new SystemSettingsPage());

    /** 顯示勇者足跡 */
    View.OnClickListener showHeroStepListener = v-> {
        // 切換顯示
        boolean isShowHeroStep = NotificationSettings.getShowHeroStep();
        isShowHeroStep = !isShowHeroStep;
        NotificationSettings.setShowHeroStep(isShowHeroStep);
        mainLayout.findViewById(R.id.Main_Block_HeroStepList).setVisibility(isShowHeroStep?View.VISIBLE:View.GONE);
    };
    /** 顯示聊天main */
    View.OnClickListener showMessageMainListener = v -> {
        MessageMain aPage = PageContainer.getInstance().getMessageMain();
        MainPage.this.getNavigationController().pushViewController(aPage);

        // 隱藏小視窗
        if (TempSettings.getMessageSmall() != null)
            TempSettings.getMessageSmall().hide();
    };

    public int getPageLayout() {
        return R.layout.main_page;
    }

    public int getPageType() {
        return BahamutPage.BAHAMUT_MAIN;
    }

    public void onPageDidLoad() {
        mainLayout = (RelativeLayout) findViewById(R.id.content_view);
        mainLayout.findViewById(R.id.Main_BoardsButton).setOnClickListener(this.boardsListener);
        mainLayout.findViewById(R.id.Main_ClassButton).setOnClickListener(this.classListener);
        mainLayout.findViewById(R.id.Main_FavoriteButton).setOnClickListener(this.favoriteListener);
        mainLayout.findViewById(R.id.Main_LogoutButton).setOnClickListener(this.logoutListener);
        mainLayout.findViewById(R.id.Main_MailButton).setOnClickListener(this.mailListener);
        mainLayout.findViewById(R.id.Main_SystemSettingsButton).setOnClickListener(this.systemSettingListener);
        TextView mainOnlinePeople = mainLayout.findViewById(R.id.Main_OnlinePeople);
        mainOnlinePeople.setText(onlinePeople); // 線上人數

        // 顯示勇者足跡
        List<HeroStep> heroStepList = TempSettings.getHeroStepList();
        LinearLayout heroStepListLayout = mainLayout.findViewById(R.id.Main_HeroStepList);
        for (HeroStep heroStep : heroStepList) {
            HeroStepItemView itemView = new HeroStepItemView(getContext());
            itemView.setItem(heroStep);
            heroStepListLayout.addView(itemView);
        }
        // 沒資料不顯示
        if (heroStepList.size()==0) {
            NotificationSettings.setShowHeroStep(false);
        }
        boolean isShowHeroStep = NotificationSettings.getShowHeroStep();
        mainLayout.findViewById(R.id.Main_Block_HeroStepList).setVisibility(isShowHeroStep?View.VISIBLE:View.GONE);
        mainLayout.findViewById(R.id.Main_HeroStepButton).setOnClickListener(this.showHeroStepListener);

        // 顯示呼叫器
        TextView txtBBCall = mainLayout.findViewById(R.id.Main_BBCall);
        txtBBCall.setText(bbCallStatus);
        mainLayout.findViewById(R.id.Main_BBCall_Layout).setOnClickListener(showMessageMainListener);

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));

        // 檢查不斷線掛網
        checkBatteryLife();

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            new ASRunner(){
                @Override
                public void run() {
                    ASProcessingDialog.showProcessingDialog(getContextString(R.string.is_under_auto_logging_chat));
                    // 進入布告討論區
                    mainLayout.findViewById(R.id.Main_BoardsButton).performClick();
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
            if (BahamutStateHandler.getInstance().getCurrentPage() == BahamutPage.BAHAMUT_MAIN) {
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

    public void onPageWillDisappear() {
        clear();
    }

    public void onPageDidDisappear() {
        this.goodbyeDialog = null;
        this.saveHotMessageDialog = null;
        super.onPageDidDisappear();
    }

    /* access modifiers changed from: protected */
    public boolean onBackPressed() {
        this.logoutListener.onClick(null);
        return true;
    }

    /** 給其他頁面呼叫訊息 */
    public void onProcessHotMessage() {
        if (this.saveHotMessageDialog == null) {
            this.saveHotMessageDialog = ASAlertDialog.createDialog()
                    .setTitle("熱訊")
                    .setMessage("本次上站熱訊處理 ")
                    .addButton("備忘錄")
                    .addButton("保留")
                    .addButton("清除")
                    .setListener((aDialog, index) -> {
                MainPage.this.saveHotMessageDialog = null;
                switch (index) {
                    case 0 -> TelnetClient.getClient().sendStringToServerInBackground("M");
                    case 1 -> TelnetClient.getClient().sendStringToServerInBackground("K");
                    case 2 -> {
                        TelnetClient.getClient().sendStringToServerInBackground("C");
                        try(MessageDatabase db = new MessageDatabase(getContext())) {
                            db.clearDb();
                        }
                    }
                    default -> TelnetClient.getClient().sendStringToServerInBackground("K");
                }
            });
            this.saveHotMessageDialog.setOnDismissListener( (dialog)-> {
                // 預設離開
                if (this.saveHotMessageDialog != null) {
                    TelnetClient.getClient().sendStringToServerInBackground("K");
                }
            });
            this.saveHotMessageDialog.show();
        }
    }

    /** 給其他頁面呼叫離開 */
    public void onCheckGoodbye() {
        if (this.goodbyeDialog == null) {
            this.goodbyeDialog = ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.string.logout))
                    .setMessage("是否確定要登出?")
                    .addButton(getContextString(R.string.cancel))
                    .addButton(getContextString(R.string.main_hero_step))
                    .addButton(getContextString(R.string.confirm))
                    .setListener((aDialog, index) -> {
                        MainPage.this.goodbyeDialog = null;
                        switch (index) {
                            case 2 -> // 確定
                                    TelnetClient.getClient().sendStringToServerInBackground("G");
                            case 1 -> {// 勇者足跡
                                TelnetClient.getClient().sendStringToServerInBackground("N");
                                DialogHeroStep dialogHeroStep = new DialogHeroStep();
                                dialogHeroStep.show();
                            }
                            case 0 -> // 取消
                                    TelnetClient.getClient().sendStringToServerInBackground("Q");
                            default -> {}
                        }
                    })
                    .scheduleDismissOnPageDisappear(this);
            this.goodbyeDialog.setOnDismissListener( (dialog)-> {
                // 預設離開
                if (this.goodbyeDialog != null) {
                    TelnetClient.getClient().sendStringToServerInBackground("G");
                }
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
        if (this.saveHotMessageDialog != null) {
            if (this.saveHotMessageDialog.isShowing()) {
                this.saveHotMessageDialog.dismiss();
            }
            this.saveHotMessageDialog = null;
        }
    }

    /** 設定線上人數 */
    public void setOnlinePeople(String peoples) {
        onlinePeople = peoples;
    }

    /** 設定呼叫器 */
    public void setBBCall(String status) {
        bbCallStatus = status;
    }

    /** 不受電池最佳化限制 */
    @SuppressLint("BatteryLife")
    private void checkBatteryLife() {
        if (!NotificationSettings.getAlarmIgnoreBatteryOptimizations()) {
            NotificationSettings.setAlarmIgnoreBatteryOptimizations(true);

            PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            String packageName = getContext().getPackageName();

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string._warning))
                        .setMessage(getContextString(R.string.ignoreBattery_msg01))
                        .addButton(getContextString(R.string.notification_permission_later))
                        .addButton(getContextString(R.string.notification_permission_goto_settings))
                        .setDefaultButtonIndex(0)
                        .setListener((aDialog, index) -> {
                            if (index == 1) {
                                // 前往設定頁面
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:"+packageName));
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    };
}
