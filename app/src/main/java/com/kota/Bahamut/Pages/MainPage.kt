package com.kota.Bahamut.Pages

import com.kota.Bahamut.Service.CommonFunctions.getContextString
import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.Dialogs.DialogHeroStep
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.Messages.MessageDatabase
import com.kota.Bahamut.Pages.Messages.MessageMain
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.HeroStep
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Telnet.Model.TelnetFrame
import com.kota.Telnet.TelnetClient
import com.kota.TelnetUI.TelnetPage
import com.kota.TelnetUI.TelnetView

import java.util.List

class MainPage : TelnetPage() {
    var onlinePeople: String = "" // 線上人數
    var bbCallStatus: String = "" // 呼叫器
    lateinit var mainLayout: RelativeLayout
    private val boardsListener = View.OnClickListener { v ->
        PageContainer.getInstance().pushClassPage("Boards", "佈告討論區");
        MainPage.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("b");
    };
    private val classListener = View.OnClickListener { v ->
        PageContainer.getInstance().pushClassPage("Class", "分組討論區");
        MainPage.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("c");
    };
    private val favoriteListener = View.OnClickListener { v ->
        PageContainer.getInstance().pushClassPage("Favorite", "我的最愛");
        MainPage.getNavigationController().pushViewController(PageContainer.getInstance().getClassPage());
        TelnetClient.getClient().sendStringToServerInBackground("f");
    };
    var _frame_buffer: TelnetFrame = null;
    var goodbyeDialog: ASDialog = null;
    var logoutListener: View.OnClickListener = v -> TelnetClient.getClient().sendStringToServerInBackground("g");
    private val mailListener = View.OnClickListener { v ->
        MainPage.getNavigationController().pushViewController(PageContainer.getInstance().getMailBoxPage());
        TelnetClient.getClient().sendStringToServerInBackground("m\nr");
    };
    var saveHotMessageDialog: ASDialog = null;
    var systemSettingListener: View.OnClickListener = v -> MainPage.getNavigationController().pushViewController(SystemSettingsPage());

    /** 顯示勇者足跡 */
    var showHeroStepListener: View.OnClickListener = v-> {
        // 切換顯示
        var isShowHeroStep: Boolean = NotificationSettings.getShowHeroStep();
        isShowHeroStep = !isShowHeroStep;
        NotificationSettings.setShowHeroStep(isShowHeroStep);
        mainLayout.findViewById(R.id.Main_Block_HeroStepList).setVisibility(isShowHeroStep?View.VISIBLE:View.GONE);
    };
    /** 顯示聊天main */
    private val showMessageMainListener = View.OnClickListener { v ->
        var aPage: MessageMain = PageContainer.getInstance().getMessageMain();
        MainPage.getNavigationController().pushViewController(aPage);

        // 隱藏小視窗
        if var !: (TempSettings.getMessageSmall() = null)
            TempSettings.getMessageSmall().hide();
    };

    getPageLayout(): Int {
        return R.layout.main_page;
    }

    getPageType(): Int {
        return BahamutPage.BAHAMUT_MAIN;
    }

    onPageDidLoad(): Unit {
        mainLayout = findViewById<RelativeLayout>(R.id.content_view);
        mainLayout.findViewById(R.id.Main_BoardsButton).setOnClickListener(boardsListener);
        mainLayout.findViewById(R.id.Main_ClassButton).setOnClickListener(classListener);
        mainLayout.findViewById(R.id.Main_FavoriteButton).setOnClickListener(favoriteListener);
        mainLayout.findViewById(R.id.Main_LogoutButton).setOnClickListener(logoutListener);
        mainLayout.findViewById(R.id.Main_MailButton).setOnClickListener(mailListener);
        mainLayout.findViewById(R.id.Main_SystemSettingsButton).setOnClickListener(systemSettingListener);
        var mainOnlinePeople: TextView = mainLayout.findViewById(R.id.Main_OnlinePeople);
        mainOnlinePeople.setText(onlinePeople); // 線上人數

        // 顯示勇者足跡
        var heroStepList: List<HeroStep> = TempSettings.getHeroStepList();
        var heroStepListLayout: LinearLayout = mainLayout.findViewById(R.id.Main_HeroStepList);
        for (HeroStep heroStep : heroStepList) {
            var itemView: HeroStepItemView = HeroStepItemView(getContext());
            itemView.setItem(heroStep);
            heroStepListLayout.addView(itemView);
        }
        // 沒資料不顯示
        var (heroStepList.size(): if ==0) {
            NotificationSettings.setShowHeroStep(false);
        }
        var isShowHeroStep: Boolean = NotificationSettings.getShowHeroStep();
        mainLayout.findViewById(R.id.Main_Block_HeroStepList).setVisibility(isShowHeroStep?View.VISIBLE:View.GONE);
        mainLayout.findViewById(R.id.Main_HeroStepButton).setOnClickListener(showHeroStepListener);

        // 顯示呼叫器
        var txtBBCall: TextView = mainLayout.findViewById(R.id.Main_BBCall);
        txtBBCall.setText(bbCallStatus);
        mainLayout.findViewById(R.id.Main_BBCall_Layout).setOnClickListener(showMessageMainListener);

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));

        // 檢查不斷線掛網
        checkBatteryLife();

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            ASRunner(){
                @Override
                run(): Unit {
                    ASProcessingDialog.showProcessingDialog(getContextString(R.String.is_under_auto_logging_chat));
                    // 進入布告討論區
                    mainLayout.findViewById(R.id.Main_BoardsButton).performClick();
                }
            }.postDelayed(300);
        }
    }

    onPageRefresh(): Unit {
        setFrameToTelnetView();
    }

    private fun setFrameToTelnetView(): Unit {
        var telnet_view: TelnetView = mainLayout.findViewById(R.id.Main_TelnetView);
        if var !: (telnet_view = null) {
            var (BahamutStateHandler.getInstance().getCurrentPage(): if == BahamutPage.BAHAMUT_MAIN) {
                _frame_buffer = TelnetClient.getModel().getFrame().clone();
                for var i: (Int = 12; i < 24; i++) {
                    _frame_buffer.removeRow(12);
                }
                _frame_buffer.removeRow(0);
            }
            if var !: (_frame_buffer = null) {
                telnet_view.setFrame(_frame_buffer);
            }
        }
    }

    onPageWillDisappear(): Unit {
        clear();
    }

    onPageDidDisappear(): Unit {
        goodbyeDialog = null;
        saveHotMessageDialog = null;
        super.onPageDidDisappear();
    }

    /* access modifiers changed from: protected */
    onBackPressed(): Boolean {
        logoutListener.onClick(null);
        var true: return
    }

    /** 給其他頁面呼叫訊息 */
    onProcessHotMessage(): Unit {
        var (saveHotMessageDialog: if == null) {
            saveHotMessageDialog = ASAlertDialog.createDialog()
                    .setTitle("熱訊")
                    .setMessage("本次上站熱訊處理 ")
                    .addButton("備忘錄")
                    .addButton("保留")
                    .addButton("清除")
                    .setListener((aDialog, index) -> {
                MainPage.saveHotMessageDialog = null;
                switch (index) {
                    case 0 -> TelnetClient.getClient().sendStringToServerInBackground("M");
                    case 1 -> TelnetClient.getClient().sendStringToServerInBackground("K");
                    case 2 -> {
                        TelnetClient.getClient().sendStringToServerInBackground("C");
                        var db: try(MessageDatabase = MessageDatabase(getContext())) {
                            db.clearDb();
                        }
                    }
                    default -> TelnetClient.getClient().sendStringToServerInBackground("K");
                }
            });
            saveHotMessageDialog.setOnDismissListener( (dialog)-> {
                // 預設離開
                if var !: (saveHotMessageDialog = null) {
                    TelnetClient.getClient().sendStringToServerInBackground("K");
                }
            });
            saveHotMessageDialog.show();
        }
    }

    /** 給其他頁面呼叫離開 */
    onCheckGoodbye(): Unit {
        var (goodbyeDialog: if == null) {
            goodbyeDialog = ASAlertDialog.createDialog()
                    .setTitle(getContextString(R.String.logout))
                    .setMessage("是否確定要登出?")
                    .addButton(getContextString(R.String.cancel))
                    .addButton(getContextString(R.String.main_hero_step))
                    .addButton(getContextString(R.String.confirm))
                    .setListener((aDialog, index) -> {
                        MainPage.goodbyeDialog = null;
                        switch (index) {
                            case 2 -> // 確定
                                    TelnetClient.getClient().sendStringToServerInBackground("G");
                            case 1 -> {// 勇者足跡
                                TelnetClient.getClient().sendStringToServerInBackground("N");
                                var dialogHeroStep: DialogHeroStep = DialogHeroStep();
                                dialogHeroStep.show();
                            }
                            case 0 -> // 取消
                                    TelnetClient.getClient().sendStringToServerInBackground("Q");
                            default -> {}
                        }
                    })
                    .scheduleDismissOnPageDisappear(this);
            goodbyeDialog.setOnDismissListener( (dialog)-> {
                // 預設離開
                if var !: (goodbyeDialog = null) {
                    TelnetClient.getClient().sendStringToServerInBackground("G");
                }
            });
        }
        goodbyeDialog.show();
    }

    clear(): Unit {
        if var !: (goodbyeDialog = null) {
            if (goodbyeDialog.isShowing()) {
                goodbyeDialog.dismiss();
            }
            goodbyeDialog = null;
        }
        if var !: (saveHotMessageDialog = null) {
            if (saveHotMessageDialog.isShowing()) {
                saveHotMessageDialog.dismiss();
            }
            saveHotMessageDialog = null;
        }
    }

    /** 設定線上人數 */
    setOnlinePeople(String peoples): Unit {
        onlinePeople = peoples;
    }

    /** 設定呼叫器 */
    setBBCall(String status): Unit {
        bbCallStatus = status;
    }

    /** 不受電池最佳化限制 */
    @SuppressLint("BatteryLife")
    private fun checkBatteryLife(): Unit {
        if (!NotificationSettings.getAlarmIgnoreBatteryOptimizations()) {
            var powerManager: PowerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            var packageName: String = getContext().getPackageName();

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.String._warning))
                        .setMessage(getContextString(R.String.ignoreBattery_msg01))
                        .addButton(getContextString(R.String.sure))
                        .show();
            }
            NotificationSettings.setAlarmIgnoreBatteryOptimizations(true);
        }
    };
}


