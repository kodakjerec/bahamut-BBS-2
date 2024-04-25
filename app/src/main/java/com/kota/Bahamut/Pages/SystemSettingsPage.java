package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.changeScreenOrientation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.google.android.material.slider.Slider;
import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.BlockListPage.ArticleExpressionListPage;
import com.kota.Bahamut.Pages.BlockListPage.ArticleHeaderListPage;
import com.kota.Bahamut.Pages.BlockListPage.BlockListPage;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TextView.TelnetTextViewSmall;

public class SystemSettingsPage extends TelnetPage {
    CompoundButton.OnCheckedChangeListener _auto_to_chat_enable_listener = (buttonView, isChecked) -> UserSettings.setPropertiesAutoToChat(isChecked);
    CompoundButton.OnCheckedChangeListener _gesture_on_board_enable_listener = (buttonView, isChecked) -> UserSettings.setPropertiesGestureOnBoardEnable(isChecked);
    CompoundButton.OnCheckedChangeListener _animation_enable_listener = (buttonView, isChecked) -> {
        UserSettings.setPropertiesAnimationEnable(isChecked);
        ASNavigationController.getCurrentController().setAnimationEnable(UserSettings.getPropertiesAnimationEnable());
    };
    CompoundButton.OnCheckedChangeListener _article_move_enable_listener = (buttonView, isChecked) -> UserSettings.setPropertiesArticleMoveDisable(isChecked);
    CompoundButton.OnCheckedChangeListener _block_list_enable_listener = (buttonView, isChecked) -> UserSettings.setPropertiesBlockListEnable(isChecked);
    // 切換到黑名單設定
    View.OnClickListener _block_list_setting_listener = v -> getNavigationController().pushViewController(new BlockListPage());
    // 切換到發文標題設定
    View.OnClickListener _article_header_setting_listener = v -> getNavigationController().pushViewController(new ArticleHeaderListPage());
    // 切換到發文表情符號設定
    View.OnClickListener _article_expression_setting_listener = v -> getNavigationController().pushViewController(new ArticleExpressionListPage());

    // 防止Wifi斷線
    CompoundButton.OnCheckedChangeListener _keep_wifi_listener = (buttonView, isChecked) -> {
        UserSettings.setPropertiesKeepWifi(isChecked);
        if (isChecked)
            getNavigationController().getDeviceController().lockWifi();
        else
            getNavigationController().getDeviceController().unlockWifi();
    };

    // 不受電池最佳化限制
    @SuppressLint("BatteryLife")
    View.OnClickListener _ignore_battery_listener = view -> {
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        String packageName = getContext().getPackageName();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        } else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:"+packageName));
        }
        getContext().startActivity(intent);
    };

    // 開啟贊助葉面
    View.OnClickListener _billing_page_listener = v -> {
        BillingPage page = PageContainer.getInstance().getBillingPage();
        getNavigationController().pushViewController(page);};

    // 畫面旋轉
    AdapterView.OnItemSelectedListener _screen_orientation_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            UserSettings.setPropertiesScreenOrientation(i);
            changeScreenOrientation();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            UserSettings.setPropertiesScreenOrientation(0);
        }
    };

    // 側邊選單位置
    AdapterView.OnItemSelectedListener _drawer_location_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            UserSettings.setPropertiesDrawerLocation(i);
            changeScreenOrientation();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            UserSettings.setPropertiesDrawerLocation(0);
        }
    };

    // 工具列位置
    AdapterView.OnItemSelectedListener _toolbar_location_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i <= 2) {
                findViewById(R.id.SystemSettings_item_toolbar_order_item_1).setVisibility(View.VISIBLE);
                findViewById(R.id.SystemSettings_item_toolbar_order_item_2).setVisibility(View.GONE);
                findViewById(R.id.SystemSettings_item_toolbar_order_item_3).setVisibility(View.GONE);
            } else {
                findViewById(R.id.SystemSettings_item_toolbar_order_item_1).setVisibility(View.GONE);
                findViewById(R.id.SystemSettings_item_toolbar_order_item_2).setVisibility(View.VISIBLE);
                findViewById(R.id.SystemSettings_item_toolbar_order_item_3).setVisibility(View.VISIBLE);
            }
            UserSettings.setPropertiesToolbarLocation(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            UserSettings.setPropertiesToolbarLocation(0);
        }
    };

    // 工具列排序
    AdapterView.OnItemSelectedListener _toolbar_order_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            UserSettings.setPropertiesToolbarOrder(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            UserSettings.setPropertiesToolbarOrder(0);
        }
    };

    // 連結自動預覽
    CompoundButton.OnCheckedChangeListener _link_auto_show_listener = (buttonView, isChecked) -> {
        UserSettings.setPropertiesLinkAutoShow(isChecked);
        changeLinkAutoShowStatus(isChecked);
    };
    void changeLinkAutoShowStatus(boolean enable) {
        if (enable) {
            findViewById(R.id.SystemSettings_item_enableLinkShowThumbnail).setVisibility(View.VISIBLE);
            ((CheckBox) findViewById(R.id.SystemSettings_enableLinkShowThumbnail)).setChecked(UserSettings.getLinkShowThumbnail());
            changeLinkOnlyWifiStatus(UserSettings.getLinkShowThumbnail());
        } else {
            UserSettings.setPropertiesLinkShowThumbnail(false);
            findViewById(R.id.SystemSettings_item_enableLinkShowThumbnail).setVisibility(View.GONE);
            changeLinkOnlyWifiStatus(false);
        }
    }

    // 顯示預覽圖
    CompoundButton.OnCheckedChangeListener _link_show_thumbnail_listener = (buttonView, isChecked) -> {
        UserSettings.setPropertiesLinkShowThumbnail(isChecked);
        changeLinkOnlyWifiStatus(isChecked);
    };
    void changeLinkOnlyWifiStatus(boolean enable) {
        if (enable) {
            findViewById(R.id.SystemSettings_item_enableLinkShowOnlyWifi).setVisibility(View.VISIBLE);
            ((CheckBox) findViewById(R.id.SystemSettings_enableLinkShowOnlyWifi)).setChecked(UserSettings.getLinkShowOnlyWifi());
        } else {
            UserSettings.setPropertiesLinkShowOnlyWifi(false);
            findViewById(R.id.SystemSettings_item_enableLinkShowOnlyWifi).setVisibility(View.GONE);
        }
    }
    // 只在Wifi下預覽
    CompoundButton.OnCheckedChangeListener _link_show_only_wifi_listener = (buttonView, isChecked) -> UserSettings.setPropertiesLinkShowOnlyWifi(isChecked);


    public int getPageLayout() {
        return R.layout.system_settings_page;
    }

    public int getPageType() {
        return BahamutPage.BAHAMUT_SYSTEM_SETTINGS;
    }

    public boolean isPopupPage() {
        return true;
    }

    @SuppressLint("SetTextI18n")
    public void onPageDidLoad() {
        // 黑名單
        findViewById(R.id.SystemSettings_blockListSetting).setOnClickListener(_block_list_setting_listener);
        CheckBox block_list_enable_box = (CheckBox) findViewById(R.id.SystemSettings_blockListEnable);
        block_list_enable_box.setChecked(UserSettings.getPropertiesBlockListEnable());
        block_list_enable_box.setOnCheckedChangeListener(_block_list_enable_listener);
        findViewById(R.id.SystemSettings_item_blockListEnable).setOnClickListener(view -> block_list_enable_box.setChecked(!block_list_enable_box.isChecked()));

        // keep-wifi
        CheckBox keep_wifi_box = (CheckBox) findViewById(R.id.SystemSettings_keepWifi);
        keep_wifi_box.setChecked(UserSettings.getPropertiesKeepWifi());
        keep_wifi_box.setOnCheckedChangeListener(_keep_wifi_listener);
        findViewById(R.id.SystemSettings_item_keepWifi).setOnClickListener(view -> keep_wifi_box.setChecked(!keep_wifi_box.isChecked()));
        
        // 換頁動畫
        CheckBox animation_enable_box = (CheckBox) findViewById(R.id.SystemSettings_animationEnable);
        animation_enable_box.setChecked(UserSettings.getPropertiesAnimationEnable());
        animation_enable_box.setOnCheckedChangeListener(_animation_enable_listener);
        findViewById(R.id.SystemSettings_item_animationEnable).setOnClickListener(view -> animation_enable_box.setChecked(!animation_enable_box.isChecked()));

        // 文章首篇/末篇
        CheckBox article_move_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableArticleMove);
        article_move_enable_box.setChecked(UserSettings.getPropertiesArticleMoveEnable());
        article_move_enable_box.setOnCheckedChangeListener(_article_move_enable_listener);
        findViewById(R.id.SystemSettings_item_enableArticleMove).setOnClickListener(view -> article_move_enable_box.setChecked(!article_move_enable_box.isChecked()));

        // 螢幕方向
        ArrayAdapter<String> adapter_screen_orientation = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_screen_orientation_items));
        adapter_screen_orientation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner_screen_orientation = (Spinner) findViewById(R.id.SystemSettings_screen_orientation_spinner);
        spinner_screen_orientation.setAdapter(adapter_screen_orientation);
        spinner_screen_orientation.setSelection(UserSettings.getPropertiesScreenOrientation());
        spinner_screen_orientation.setOnItemSelectedListener(_screen_orientation_listener);

        // 不受電池最佳化限制
        findViewById(R.id.SystemSettings_item_IgnoreBatteryOptimizations).setOnClickListener(_ignore_battery_listener);

        // 連結自動預覽
        CheckBox link_auto_show_box = (CheckBox) findViewById(R.id.SystemSettings_enableLinkAutoShow);
        link_auto_show_box.setChecked(UserSettings.getLinkAutoShow());
        link_auto_show_box.setOnCheckedChangeListener(_link_auto_show_listener);
        findViewById(R.id.SystemSettings_item_enableLinkAutoShow).setOnClickListener(view -> link_auto_show_box.setChecked(!link_auto_show_box.isChecked()));
        changeLinkAutoShowStatus(UserSettings.getLinkAutoShow());

        // 顯示預覽圖
        CheckBox link_show_thumbnail = (CheckBox) findViewById(R.id.SystemSettings_enableLinkShowThumbnail);
        link_show_thumbnail.setChecked(UserSettings.getLinkShowThumbnail());
        link_show_thumbnail.setOnCheckedChangeListener(_link_show_thumbnail_listener);
        findViewById(R.id.SystemSettings_item_enableLinkShowThumbnail).setOnClickListener(view -> link_show_thumbnail.setChecked(!link_show_thumbnail.isChecked()));

        // 顯示預覽圖
        CheckBox link_show_only_wifi = (CheckBox) findViewById(R.id.SystemSettings_enableLinkShowOnlyWifi);
        link_show_only_wifi.setChecked(UserSettings.getLinkShowOnlyWifi());
        link_show_only_wifi.setOnCheckedChangeListener(_link_show_only_wifi_listener);
        findViewById(R.id.SystemSettings_item_enableLinkShowOnlyWifi).setOnClickListener(view -> link_show_only_wifi.setChecked(!link_show_only_wifi.isChecked()));

        // VIP
        if (UserSettings.getPropertiesVIP()) {
            // 使用手勢在看板/文章
            findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setVisibility(View.VISIBLE);
            CheckBox gesture_on_board_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableGestureOnBoard);
            gesture_on_board_enable_box.setChecked(UserSettings.getPropertiesGestureOnBoardEnable());
            gesture_on_board_enable_box.setOnCheckedChangeListener(_gesture_on_board_enable_listener);
            findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setOnClickListener(view -> gesture_on_board_enable_box.setChecked(!gesture_on_board_enable_box.isChecked()));

            // 自動登入洽特
            findViewById(R.id.SystemSettings_item_enableAutoToChat).setVisibility(View.VISIBLE);
            CheckBox auto_to_chat_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableAutoToChat);
            auto_to_chat_enable_box.setChecked(UserSettings.getPropertiesAutoToChat());
            auto_to_chat_enable_box.setOnCheckedChangeListener(_auto_to_chat_enable_listener);
            findViewById(R.id.SystemSettings_item_enableAutoToChat).setOnClickListener(view -> auto_to_chat_enable_box.setChecked(!auto_to_chat_enable_box.isChecked()));

            // 工具列位置
            ArrayAdapter<String> adapter_toolbar_location = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_toolbar_location_items));
            adapter_toolbar_location.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner_toolbar_location = (Spinner) findViewById(R.id.SystemSettings_toolbar_location_spinner);
            spinner_toolbar_location.setAdapter(adapter_toolbar_location);
            spinner_toolbar_location.setSelection(UserSettings.getPropertiesToolbarLocation());
            spinner_toolbar_location.setOnItemSelectedListener(_toolbar_location_listener);

            // 工具列順序
            ArrayAdapter<String> adapter_toolbar_order = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_toolbar_order_items));
            adapter_toolbar_order.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner_toolbar_order = (Spinner) findViewById(R.id.SystemSettings_toolbar_order_spinner);
            spinner_toolbar_order.setAdapter(adapter_toolbar_order);
            spinner_toolbar_order.setSelection(UserSettings.getPropertiesToolbarOrder());
            spinner_toolbar_order.setOnItemSelectedListener(_toolbar_order_listener);
            if (UserSettings.getPropertiesToolbarLocation() <= 2) { // 底部工具列
                findViewById(R.id.SystemSettings_item_toolbar_order_item_1).setVisibility(View.VISIBLE);
                findViewById(R.id.SystemSettings_item_toolbar_order_item_2).setVisibility(View.GONE);
                findViewById(R.id.SystemSettings_item_toolbar_order_item_3).setVisibility(View.GONE);
            } else { // 浮動
                findViewById(R.id.SystemSettings_item_toolbar_order_item_1).setVisibility(View.GONE);
                findViewById(R.id.SystemSettings_item_toolbar_order_item_2).setVisibility(View.VISIBLE);
                findViewById(R.id.SystemSettings_item_toolbar_order_item_3).setVisibility(View.VISIBLE);
            }
            TelnetTextViewSmall textSmall_idle = (TelnetTextViewSmall) findViewById(R.id.system_setting_page_toolbar_idle_text);
            Slider slider_idle = (Slider) findViewById(R.id.system_setting_page_toolbar_idle);
            slider_idle.setValue(UserSettings.getToolbarIdle());
            textSmall_idle.setText(slider_idle.getValue() + "s");
            slider_idle.addOnChangeListener((slider, value, fromUser) -> {
                UserSettings.setToolbarIdle(value);
                textSmall_idle.setText(value + "s");
            });
            TelnetTextViewSmall textSmall_alpha = (TelnetTextViewSmall) findViewById(R.id.system_setting_page_toolbar_alpha_text);
            Slider slider_alpha = (Slider) findViewById(R.id.system_setting_page_toolbar_alpha);
            slider_alpha.setValue(UserSettings.getToolbarAlpha());
            textSmall_alpha.setText((int)slider_alpha.getValue() + "%");
            slider_alpha.addOnChangeListener((slider, value, fromUser) -> {
                UserSettings.setToolbarAlpha(value);
                textSmall_alpha.setText((int)value + "%");
            });
            // 側滑選單位置
            ArrayAdapter<String> adapter_drawer_location = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_drawer_location_items));
            adapter_drawer_location.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner_drawer_location = (Spinner) findViewById(R.id.SystemSettings_drawer_location_spinner);
            spinner_drawer_location.setAdapter(adapter_drawer_location);
            spinner_drawer_location.setSelection(UserSettings.getPropertiesDrawerLocation());
            spinner_drawer_location.setOnItemSelectedListener(_drawer_location_listener);

            // 表情符號設定
            findViewById(R.id.SystemSettings_ArticleHeaderSetting).setOnClickListener(_article_header_setting_listener);
            findViewById(R.id.SystemSettings_ArticleExpressionSetting).setOnClickListener(_article_expression_setting_listener);
        } else {
            findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_item_enableAutoToChat).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_item_toolbar_location).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_item_toolbar_order).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_item_drawer_location).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_ArticleHeaderSetting).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_ArticleExpressionSetting).setVisibility(View.GONE);
        }

        // billing-page
        findViewById(R.id.SystemSettings_goBillingPage).setOnClickListener(this._billing_page_listener);
    }

    public String getName() {
        return "TelnetSystemSettingsDialog";
    }

    public boolean onBackPressed() {
        UserSettings.notifyDataUpdated();
        return super.onBackPressed();
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast("返回");
        return true;
    }

}
