package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.core.content.pm.PackageInfoCompat;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.NotificationSettings;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.TelnetClient;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TextView.TelnetTextViewSmall;

public class StartPage extends TelnetPage {
    /** 連線 */
    View.OnClickListener _connect_listener = v -> StartPage.this.onConnectButtonClicked();
    /** 離開 */
    View.OnClickListener _exit_listener = v -> StartPage.this.onExitButtonClicked();

    /** 按下教學 */
    View.OnClickListener urlClickListener = v -> {
        int id = v.getId();
        String url = "";
        if (id == R.id.Start_instructions)
            url = "https://kodaks-organization-1.gitbook.io/bahabbs-zhan-ba-ha-shi-yong-shou-ce/";
        if (id == R.id.Start_Icon_Discord)
            url = "https://discord.gg/YP8dthZ";
        if (id == R.id.Start_Icon_Facebook)
            url = "https://www.facebook.com/groups/264144897071532";
        if (id == R.id.Start_Icon_Reddit)
            url = "https://www.reddit.com/r/bahachat";
        if (id == R.id.Start_Icon_Steam)
            url = "https://steamcommunity.com/groups/BAHACHAT";
        if (id == R.id.Start_Icon_Telegram)
            url = "https://t.me/joinchat/MF5hqkuZN3B0NFqSyiz30A";

        if (!url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    /** 避難所 */

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
        findViewById(R.id.Start_instructions).setOnClickListener(urlClickListener);
        // url
        findViewById(R.id.Start_Icon_Discord).setOnClickListener(urlClickListener);
        findViewById(R.id.Start_Icon_Facebook).setOnClickListener(urlClickListener);
        findViewById(R.id.Start_Icon_Reddit).setOnClickListener(urlClickListener);
        findViewById(R.id.Start_Icon_Steam).setOnClickListener(urlClickListener);
        findViewById(R.id.Start_Icon_Telegram).setOnClickListener(urlClickListener);
        // ip位置
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioButtonIP);
        RadioButton radioButton1 = (RadioButton) findViewById(R.id.radioButtonIP1);
        RadioButton radioButton2 = (RadioButton) findViewById(R.id.radioButtonIP2);

        // 連線位址
        String connectIp = NotificationSettings.getConnectIpAddress();
        assert connectIp != null;
        if (connectIp.equals(radioButton1.getText().toString())) {
            radioButton1.setChecked(true);
        } else {
            radioButton2.setChecked(true);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = (RadioButton) findViewById(checkedId);
            NotificationSettings.setConnectIpAddress(rb.getText().toString());
        });

        // 連線方式
        RadioGroup connectMethodGroup = (RadioGroup) findViewById(R.id.radioButtonConnectMethod);
        RadioButton connectMethodButton1 = (RadioButton) findViewById(R.id.radioButtonConnectMethod1);
        RadioButton connectMethodButton2 = (RadioButton) findViewById(R.id.radioButtonConnectMethod2);

        String connectMethod = NotificationSettings.getConnectMethod();
        assert connectMethod != null;
        if (connectMethod.equals(connectMethodButton1.getText().toString())) {
            connectMethodButton1.setChecked(true);
        } else {
            connectMethodButton2.setChecked(true);
        }
        connectMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = (RadioButton) findViewById(checkedId);
            NotificationSettings.setConnectMethod(rb.getText().toString());
            updateIPSelectionState(checkedId == R.id.radioButtonConnectMethod2, radioGroup, radioButton1, radioButton2);
        });

        // 初始化時設置狀態
        updateIPSelectionState(connectMethodButton2.isChecked(), radioGroup, radioButton1, radioButton2);

        PackageInfo packageInfo;
        try {
            packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            int versionCode = (int) PackageInfoCompat.getLongVersionCode(packageInfo);
            String versionName = packageInfo.versionName;
            ((TelnetTextViewSmall) findViewById(R.id.version)).setText(versionCode + " - " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout) findViewById(R.id.toolbar));
    }

    public void onPageWillAppear() {
        PageContainer pageContainer = PageContainer.getInstance();
        pageContainer.cleanStartPage();
    }

    public void onPageDidDisappear() {
        clear();
        super.onPageDidDisappear();
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
        // 顯示權限對話框
        if (NotificationSettings.getShowNotificationPermissionDialog()) {
            connect();
        } else {
            NotificationSettings.setShowNotificationPermissionDialog(true);
            checkAndRequestNotificationPermission();
        }

    }

    /**
     * 檢查並要求通知權限 (Android 13+)
     */
    public static void checkAndRequestNotificationPermission() {
        ASNavigationController controller = ASNavigationController.getCurrentController();
        if (controller == null)
            return;

        // 只在 Android 13+ 需要通知權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (controller.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                // 顯示對話框詢問使用者
                ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.notification_permission_title))
                        .setMessage(getContextString(R.string.notification_permission_message))
                        .addButton(getContextString(R.string.notification_permission_later))
                        .addButton(getContextString(R.string.notification_permission_goto_settings))
                        .setDefaultButtonIndex(0)
                        .setListener((aDialog, index) -> {
                            if (index == 1) {
                                // 前往設定頁面
                                try {
                                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, controller.getPackageName());
                                    controller.startActivity(intent);
                                } catch (Exception e) {
                                    // 如果上面的方法失敗，使用應用設定頁面
                                    try {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + controller.getPackageName()));
                                        controller.startActivity(intent);
                                    } catch (Exception ex) {
                                        ASToast.showShortToast("無法開啟設定頁面");
                                    }
                                }
                            }
                        })
                        .show();
            }
        }
    }

    /** 連線 */
    public void connect() {
        int _transportType = getNavigationController().getDeviceController().isNetworkAvailable();
        TempSettings.transportType = _transportType;
        if (_transportType > -1) {
            ASProcessingDialog.showProcessingDialog("連線中", aDialog -> {
                TelnetClient.getClient().close();
                return false;
            });
            String connectIpAddress = NotificationSettings.getConnectIpAddress();
            ASRunner.runInNewThread(() -> TelnetClient.getClient().connect(connectIpAddress, 23));
            return;
        }
        ASToast.showShortToast("您未連接網路");
    }

    // 添加輔助方法
    private void updateIPSelectionState(boolean isWebSocket, RadioGroup ipGroup, RadioButton ip1, RadioButton ip2) {
        if (isWebSocket) {
            // WebSocket 模式：禁用 IP 選擇
            ipGroup.setEnabled(false);
            ip1.setEnabled(false);
            ip2.setEnabled(false);
            ipGroup.setAlpha(0.5f);
        } else {
            // Telnet 模式：啟用 IP 選擇
            ipGroup.setEnabled(true);
            ip1.setEnabled(true);
            ip2.setEnabled(true);
            ipGroup.setAlpha(1.0f);
        }
    }
}
