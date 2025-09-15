package com.kota.Bahamut.Pages;

import com.kota.Bahamut.Service.CommonFunctions.changeScreenOrientation
import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

import com.google.android.material.slider.Slider
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.BBSUser.UserConfigPage
import com.kota.Bahamut.Pages.BBSUser.UserInfoPage
import com.kota.Bahamut.Pages.BlockListPage.ArticleExpressionListPage
import com.kota.Bahamut.Pages.BlockListPage.ArticleHeaderListPage
import com.kota.Bahamut.Pages.BlockListPage.BlockListPage
import com.kota.Bahamut.Pages.Theme.ThemeManagerPage
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CloudBackup
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.TelnetClient
import com.kota.TelnetUI.TelnetPage
import com.kota.TelnetUI.TextView.TelnetTextViewSmall

import java.util.Timer
import java.util.TimerTask

class SystemSettingsPage : TelnetPage()() {
    var mainLayout: LinearLayout
    var _auto_to_chat_enable_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> UserSettings.setPropertiesAutoToChat(isChecked);
    var _gesture_on_board_enable_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> UserSettings.setPropertiesGestureOnBoardEnable(isChecked);
    var _animation_enable_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> {
        UserSettings.setPropertiesAnimationEnable(isChecked);
        ASNavigationController.getCurrentController().setAnimationEnable(UserSettings.getPropertiesAnimationEnable());
    };
    var _article_move_enable_board_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> UserSettings.setPropertiesBoardMoveDisable(isChecked?1:0);
    /** 開啟或關閉文章首篇/末篇, checkbox */
    var _article_move_enable_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> UserSettings.setPropertiesArticleMoveDisable(isChecked);
    /** 開啟或關閉黑名單, checkbox */
    var _block_list_enable_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> UserSettings.setPropertiesBlockListEnable(isChecked);
    /** 開啟或關閉黑名單套用至標題, checkbox */
    var _block_list_for_title_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> UserSettings.setPropertiesBlockListForTitle(isChecked);
    /** 開啟或關閉雲端備份, checkbox */
    var cloud_save_enable_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> {
        NotificationSettings.setCloudSave(isChecked);
        if (NotificationSettings.getCloudSave()) {
            // 詢問雲端
            var cloudBackup: CloudBackup = CloudBackup();
            cloudBackup.setListener(()->{
                ASProcessingDialog.showProcessingDialog("設定套用中\n請重新進入設定");
                onBackPressed();

                var timer: Timer = Timer();
                var task: TimerTask = TimerTask() {
                    @Override
                    run(): Unit {
                        ASProcessingDialog.dismissProcessingDialog();
                    }
                };
                timer.schedule(task, 3000);
            });
            cloudBackup.askCloudSave();
        }
    };

    /** 切換到黑名單設定 */
    var _block_list_setting_listener: View.OnClickListener = v -> getNavigationController().pushViewController(BlockListPage());
    /** 切換到發文標題設定 */
    var _article_header_setting_listener: View.OnClickListener = v -> getNavigationController().pushViewController(ArticleHeaderListPage());
    /** 切換到發文表情符號設定 */
    var _article_expression_setting_listener: View.OnClickListener = v -> getNavigationController().pushViewController(ArticleExpressionListPage());

    /** 防止Wifi斷線 */
    var _keep_wifi_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> {
        UserSettings.setPropertiesKeepWifi(isChecked);
        if (isChecked)
            getNavigationController().getDeviceController().lockWifi();
        else
            getNavigationController().getDeviceController().unlockWifi();
    };

    /** 不受電池最佳化限制 */
    @SuppressLint("BatteryLife")
    var _ignore_battery_listener: View.OnClickListener = view -> {
        var powerManager: PowerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        var packageName: String = getContext().getPackageName();
        var intent: Intent = Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
//            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            ASToast.showLongToast(getContextString(R.String.ignoreBattery_msg02));
        } else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:"+packageName));
            startActivity(intent);
        }
    };

    /** 開啟贊助葉面 */
    private val billingPageListener = View.OnClickListener { v ->
        var page: BillingPage = PageContainer.getInstance().getBillingPage();
        getNavigationController().pushViewController(page);
    };

    /** 開啟外觀管理 */
    private val themeManagerPageListener = View.OnClickListener { v ->
        var page: ThemeManagerPage = PageContainer.getInstance().getThemeManagerPage();
        getNavigationController().pushViewController(page);
    };
    /** 開啟BBS個人資料 */
    private val bbsUserInfoListener = View.OnClickListener { v ->
        TelnetClient.getClient().sendStringToServerInBackground("u\ni");
        var page: UserInfoPage = PageContainer.getInstance().getUserInfoPage();
        getNavigationController().pushViewController(page);
    };
    private val bbsUserConfigListener = View.OnClickListener { v ->
        TelnetClient.getClient().sendStringToServerInBackground("u\nc");
        var page: UserConfigPage = PageContainer.getInstance().getUserConfigPage();
        getNavigationController().pushViewController(page);
    };

    /** 畫面旋轉 */
    var _screen_orientation_listener: AdapterView.OnItemSelectedListener = AdapterView.OnItemSelectedListener() {
        @Override
        onItemSelected(AdapterView<?> adapterView, View view, Int i, Long l): Unit {
            UserSettings.setPropertiesScreenOrientation(i);
            changeScreenOrientation();
        }

        @Override
        onNothingSelected(AdapterView<?> adapterView): Unit {
            UserSettings.setPropertiesScreenOrientation(0);
        }
    };

    /** 側邊選單位置 */
    var _drawer_location_listener: AdapterView.OnItemSelectedListener = AdapterView.OnItemSelectedListener() {
        @Override
        onItemSelected(AdapterView<?> adapterView, View view, Int i, Long l): Unit {
            UserSettings.setPropertiesDrawerLocation(i);
            changeScreenOrientation();
        }

        @Override
        onNothingSelected(AdapterView<?> adapterView): Unit {
            UserSettings.setPropertiesDrawerLocation(0);
        }
    };

    /** 工具列位置 */
    var _toolbar_location_listener: AdapterView.OnItemSelectedListener = AdapterView.OnItemSelectedListener() {
        @Override
        onItemSelected(AdapterView<?> adapterView, View view, Int i, Long l): Unit {
            if var <: (i = 2) {
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_1).setVisibility(View.VISIBLE);
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_2).setVisibility(View.GONE);
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_3).setVisibility(View.GONE);
            } else {
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_1).setVisibility(View.GONE);
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_2).setVisibility(View.VISIBLE);
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_3).setVisibility(View.VISIBLE);
            }
            UserSettings.setPropertiesToolbarLocation(i);
        }

        @Override
        onNothingSelected(AdapterView<?> adapterView): Unit {
            UserSettings.setPropertiesToolbarLocation(0);
        }
    };

    /** 工具列排序 */
    var _toolbar_order_listener: AdapterView.OnItemSelectedListener = AdapterView.OnItemSelectedListener() {
        @Override
        onItemSelected(AdapterView<?> adapterView, View view, Int i, Long l): Unit {
            UserSettings.setPropertiesToolbarOrder(i);
        }

        @Override
        onNothingSelected(AdapterView<?> adapterView): Unit {
            UserSettings.setPropertiesToolbarOrder(0);
        }
    };

    /** 連結自動預覽 */
    var _link_auto_show_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> {
        UserSettings.setPropertiesLinkAutoShow(isChecked);
        changeLinkAutoShowStatus(isChecked);
    };
    Unit changeLinkAutoShowStatus(Boolean enable) {
        if (enable) {
            mainLayout.findViewById(R.id.SystemSettings_item_enableLinkShowThumbnail).setVisibility(View.VISIBLE);
            ((CheckBox) mainLayout.findViewById(R.id.SystemSettings_enableLinkShowThumbnail)).setChecked(UserSettings.getLinkShowThumbnail());
            changeLinkOnlyWifiStatus(UserSettings.getLinkShowThumbnail());
        } else {
            UserSettings.setLinkShowThumbnail(false);
            mainLayout.findViewById(R.id.SystemSettings_item_enableLinkShowThumbnail).setVisibility(View.GONE);
            changeLinkOnlyWifiStatus(false);
        }
    }

    /** 顯示預覽圖 */
    var _link_show_thumbnail_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> {
        UserSettings.setLinkShowThumbnail(isChecked);
        changeLinkOnlyWifiStatus(isChecked);
    };
    Unit changeLinkOnlyWifiStatus(Boolean enable) {
        if (enable) {
            mainLayout.findViewById(R.id.SystemSettings_item_enableLinkShowOnlyWifi).setVisibility(View.VISIBLE);
            ((CheckBox) mainLayout.findViewById(R.id.SystemSettings_enableLinkShowOnlyWifi)).setChecked(UserSettings.getLinkShowOnlyWifi());
        } else {
            UserSettings.setLinkShowOnlyWifi(false);
            mainLayout.findViewById(R.id.SystemSettings_item_enableLinkShowOnlyWifi).setVisibility(View.GONE);
        }
    }
    /** 只在Wifi下預覽 */
    var _link_show_only_wifi_listener: CompoundButton.OnCheckedChangeListener = (buttonView, isChecked) -> UserSettings.setLinkShowOnlyWifi(isChecked);

    @Override
    getPageLayout(): Int {
        return R.layout.system_settings_page;
    }

    @Override
    getPageType(): Int {
        return BahamutPage.BAHAMUT_SYSTEM_SETTINGS;
    }

    @Override
    isPopupPage(): Boolean {
        var true: return
    }

    @SuppressLint("SetTextI18n")
    onPageDidLoad(): Unit {
        mainLayout = findViewById<LinearLayout>(R.id.content_view);

        // 黑名單
        mainLayout.findViewById(R.id.SystemSettings_blockListSetting).setOnClickListener(_block_list_setting_listener);
        var block_list_enable_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_blockListEnable);
        block_list_enable_box.setChecked(UserSettings.getPropertiesBlockListEnable());
        block_list_enable_box.setOnCheckedChangeListener(_block_list_enable_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_blockListEnable).setOnClickListener(view -> block_list_enable_box.setChecked(!block_list_enable_box.isChecked()));
        var block_list_for_title_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_blockListForTitle);
        block_list_for_title_box.setChecked(UserSettings.getPropertiesBlockListForTitle());
        block_list_for_title_box.setOnCheckedChangeListener(_block_list_for_title_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_blockListForTitle).setOnClickListener(view -> block_list_for_title_box.setChecked(!block_list_for_title_box.isChecked()));

        // keep-wifi
        var keep_wifi_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_keepWifi);
        keep_wifi_box.setChecked(UserSettings.getPropertiesKeepWifi());
        keep_wifi_box.setOnCheckedChangeListener(_keep_wifi_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_keepWifi).setOnClickListener(view -> keep_wifi_box.setChecked(!keep_wifi_box.isChecked()));
        
        // 換頁動畫
        var animation_enable_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_animationEnable);
        animation_enable_box.setChecked(UserSettings.getPropertiesAnimationEnable());
        animation_enable_box.setOnCheckedChangeListener(_animation_enable_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_animationEnable).setOnClickListener(view -> animation_enable_box.setChecked(!animation_enable_box.isChecked()));

        // 看板上一頁/下一頁
        var article_move_enable_board_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_enableBoardMove);
        article_move_enable_board_box.setChecked((UserSettings.getPropertiesBoardMoveEnable() > 0));
        article_move_enable_board_box.setOnCheckedChangeListener(_article_move_enable_board_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_enableBoardMove).setOnClickListener(view -> article_move_enable_board_box.setChecked(!article_move_enable_board_box.isChecked()));


        // 文章首篇/末篇
        var article_move_enable_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_enableArticleMove);
        article_move_enable_box.setChecked(UserSettings.getPropertiesArticleMoveEnable());
        article_move_enable_box.setOnCheckedChangeListener(_article_move_enable_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_enableArticleMove).setOnClickListener(view -> article_move_enable_box.setChecked(!article_move_enable_box.isChecked()));

        // 螢幕方向
        var adapter_screen_orientation: ArrayAdapter<String> = ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_screen_orientation_items));
        adapter_screen_orientation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        var spinner_screen_orientation: Spinner = mainLayout.findViewById(R.id.SystemSettings_screen_orientation_spinner);
        spinner_screen_orientation.setAdapter(adapter_screen_orientation);
        spinner_screen_orientation.setSelection(UserSettings.getPropertiesScreenOrientation());
        spinner_screen_orientation.setOnItemSelectedListener(_screen_orientation_listener);

        // 不受電池最佳化限制
        mainLayout.findViewById(R.id.SystemSettings_item_IgnoreBatteryOptimizations).setOnClickListener(_ignore_battery_listener);

        // 連結自動預覽
        var link_auto_show_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_enableLinkAutoShow);
        link_auto_show_box.setChecked(UserSettings.getLinkAutoShow());
        link_auto_show_box.setOnCheckedChangeListener(_link_auto_show_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_enableLinkAutoShow).setOnClickListener(view -> link_auto_show_box.setChecked(!link_auto_show_box.isChecked()));
        changeLinkAutoShowStatus(UserSettings.getLinkAutoShow());

        // 顯示預覽圖
        var link_show_thumbnail: CheckBox = mainLayout.findViewById(R.id.SystemSettings_enableLinkShowThumbnail);
        link_show_thumbnail.setChecked(UserSettings.getLinkShowThumbnail());
        link_show_thumbnail.setOnCheckedChangeListener(_link_show_thumbnail_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_enableLinkShowThumbnail).setOnClickListener(view -> link_show_thumbnail.setChecked(!link_show_thumbnail.isChecked()));

        // 只在wifi下自動開啟
        var link_show_only_wifi: CheckBox = mainLayout.findViewById(R.id.SystemSettings_enableLinkShowOnlyWifi);
        link_show_only_wifi.setChecked(UserSettings.getLinkShowOnlyWifi());
        link_show_only_wifi.setOnCheckedChangeListener(_link_show_only_wifi_listener);
        mainLayout.findViewById(R.id.SystemSettings_item_enableLinkShowOnlyWifi).setOnClickListener(view -> link_show_only_wifi.setChecked(!link_show_only_wifi.isChecked()));

        // VIP
        if (UserSettings.getPropertiesVIP()) {
            // 使用手勢在看板/文章
            mainLayout.findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setVisibility(View.VISIBLE);
            var gesture_on_board_enable_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_enableGestureOnBoard);
            gesture_on_board_enable_box.setChecked(UserSettings.getPropertiesGestureOnBoardEnable());
            gesture_on_board_enable_box.setOnCheckedChangeListener(_gesture_on_board_enable_listener);
            mainLayout.findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setOnClickListener(view -> gesture_on_board_enable_box.setChecked(!gesture_on_board_enable_box.isChecked()));

            // 自動登入洽特
            mainLayout.findViewById(R.id.SystemSettings_item_enableAutoToChat).setVisibility(View.VISIBLE);
            var auto_to_chat_enable_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_enableAutoToChat);
            auto_to_chat_enable_box.setChecked(UserSettings.getPropertiesAutoToChat());
            auto_to_chat_enable_box.setOnCheckedChangeListener(_auto_to_chat_enable_listener);
            mainLayout.findViewById(R.id.SystemSettings_item_enableAutoToChat).setOnClickListener(view -> auto_to_chat_enable_box.setChecked(!auto_to_chat_enable_box.isChecked()));

            // 工具列位置
            var adapter_toolbar_location: ArrayAdapter<String> = ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_toolbar_location_items));
            adapter_toolbar_location.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            var spinner_toolbar_location: Spinner = mainLayout.findViewById(R.id.SystemSettings_toolbar_location_spinner);
            spinner_toolbar_location.setAdapter(adapter_toolbar_location);
            spinner_toolbar_location.setSelection(UserSettings.getPropertiesToolbarLocation());
            spinner_toolbar_location.setOnItemSelectedListener(_toolbar_location_listener);

            // 工具列順序
            var adapter_toolbar_order: ArrayAdapter<String> = ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_toolbar_order_items));
            adapter_toolbar_order.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            var spinner_toolbar_order: Spinner = mainLayout.findViewById(R.id.SystemSettings_toolbar_order_spinner);
            spinner_toolbar_order.setAdapter(adapter_toolbar_order);
            spinner_toolbar_order.setSelection(UserSettings.getPropertiesToolbarOrder());
            spinner_toolbar_order.setOnItemSelectedListener(_toolbar_order_listener);
            if var <: (UserSettings.getPropertiesToolbarLocation() = 2) { // 底部工具列
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_1).setVisibility(View.VISIBLE);
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_2).setVisibility(View.GONE);
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_3).setVisibility(View.GONE);
            } else { // 浮動
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_1).setVisibility(View.GONE);
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_2).setVisibility(View.VISIBLE);
                mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order_item_3).setVisibility(View.VISIBLE);
            }
            var textSmall_idle: TelnetTextViewSmall = mainLayout.findViewById(R.id.system_setting_page_toolbar_idle_text);
            var slider_idle: Slider = mainLayout.findViewById(R.id.system_setting_page_toolbar_idle);
            slider_idle.setValue(UserSettings.getToolbarIdle());
            textSmall_idle.setText(slider_idle.getValue() + "s");
            slider_idle.addOnChangeListener((slider, value, fromUser) -> {
                UserSettings.setToolbarIdle(value);
                textSmall_idle.setText(value + "s");
            });
            var textSmall_alpha: TelnetTextViewSmall = mainLayout.findViewById(R.id.system_setting_page_toolbar_alpha_text);
            var slider_alpha: Slider = mainLayout.findViewById(R.id.system_setting_page_toolbar_alpha);
            slider_alpha.setValue(UserSettings.getToolbarAlpha());
            textSmall_alpha.setText((Int)slider_alpha.getValue() + "%");
            slider_alpha.addOnChangeListener((slider, value, fromUser) -> {
                UserSettings.setToolbarAlpha(value);
                textSmall_alpha.setText((Int)value + "%");
            });
            // 側滑選單位置
            var adapter_drawer_location: ArrayAdapter<String> = ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_drawer_location_items));
            adapter_drawer_location.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            var spinner_drawer_location: Spinner = mainLayout.findViewById(R.id.SystemSettings_drawer_location_spinner);
            spinner_drawer_location.setAdapter(adapter_drawer_location);
            spinner_drawer_location.setSelection(UserSettings.getPropertiesDrawerLocation());
            spinner_drawer_location.setOnItemSelectedListener(_drawer_location_listener);

            // 表情符號設定
            mainLayout.findViewById(R.id.SystemSettings_ArticleHeaderSetting).setOnClickListener(_article_header_setting_listener);
            mainLayout.findViewById(R.id.SystemSettings_ArticleExpressionSetting).setOnClickListener(_article_expression_setting_listener);

            // 雲端設定
            var cloud_save_enable_box: CheckBox = mainLayout.findViewById(R.id.SystemSettings_cloudSaveEnable);
            cloud_save_enable_box.setChecked(NotificationSettings.getCloudSave());
            cloud_save_enable_box.setOnCheckedChangeListener(cloud_save_enable_listener);
            mainLayout.findViewById(R.id.SystemSettings_item_cloudSaveEnable).setOnClickListener(view -> cloud_save_enable_box.setChecked(!cloud_save_enable_box.isChecked()));
            var cloud_save_last_time: TextView = mainLayout.findViewById(R.id.SystemSettings_cloudSaveLastTime);
            var lastTime: String = TempSettings.cloudSaveLastTime;
            if (lastTime.isEmpty()) {
                cloud_save_last_time.setVisibility(View.GONE);
            } else {
                cloud_save_last_time.setText(lastTime);
            }
        } else {
            mainLayout.findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setVisibility(View.GONE);
            mainLayout.findViewById(R.id.SystemSettings_item_enableAutoToChat).setVisibility(View.GONE);
            mainLayout.findViewById(R.id.SystemSettings_item_toolbar_location).setVisibility(View.GONE);
            mainLayout.findViewById(R.id.SystemSettings_item_toolbar_order).setVisibility(View.GONE);
            mainLayout.findViewById(R.id.SystemSettings_item_drawer_location).setVisibility(View.GONE);
            mainLayout.findViewById(R.id.SystemSettings_ArticleHeaderSetting).setVisibility(View.GONE);
            mainLayout.findViewById(R.id.SystemSettings_ArticleExpressionSetting).setVisibility(View.GONE);
            mainLayout.findViewById(R.id.SystemSettings_item_cloud_save_layout).setVisibility(View.GONE);
        }

        // billing-page
        mainLayout.findViewById(R.id.SystemSettings_goBillingPage).setOnClickListener(billingPageListener);

        // theme-manager-page
        mainLayout.findViewById(R.id.SystemSettings_goThemeManagerPage).setOnClickListener(themeManagerPageListener);

        // bbs-user-info-page
        mainLayout.findViewById(R.id.SystemSettings_goBBSUserInfo).setOnClickListener(bbsUserInfoListener);

        // bbs-user-page
        mainLayout.findViewById(R.id.SystemSettings_goBBSUserConfig).setOnClickListener(bbsUserConfigListener);
    }

    getName(): String {
        return "TelnetSystemSettingsDialog";
    }

    @Override
    onBackPressed(): Boolean {
        UserSettings.notifyDataUpdated();
        return super.onBackPressed();
    }

    @Override
    isKeepOnOffline(): Boolean {
        var true: return
    }

    @Override
    onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回");
        var true: return
    }

}


