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
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.R;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;

public class SystemSettingsPage extends TelnetPage {
    UserSettings _settings;
    CompoundButton.OnCheckedChangeListener _auto_to_chat_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            _settings.setPropertiesAutoToChat(isChecked);}
    };
    CompoundButton.OnCheckedChangeListener _gesture_on_board_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            _settings.setPropertiesGestureOnBoardEnable(isChecked);}
    };
    CompoundButton.OnCheckedChangeListener _animation_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            _settings.setPropertiesAnimationEnable(isChecked);
            ASNavigationController.getCurrentController().setAnimationEnable(_settings.getPropertiesAnimationEnable());
        }
    };
    CompoundButton.OnCheckedChangeListener _article_move_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            _settings.setPropertiesArticleMoveDisable(isChecked);
        }
    };
    CompoundButton.OnCheckedChangeListener _block_list_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            _settings.setPropertiesBlockListEnable(isChecked);
        }
    };
    // 切換到黑名單設定
    View.OnClickListener _block_list_setting_listener = v -> getNavigationController().pushViewController(new BlockListPage());

    // 防止Wifi斷線
    CompoundButton.OnCheckedChangeListener _keep_wifi_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            _settings.setPropertiesKeepWifi(isChecked);
            if (isChecked)
                getNavigationController().getDeviceController().lockWifi();
            else
                getNavigationController().getDeviceController().unlockWifi();
        }
    };

    // 不受電池最佳化限制
    View.OnClickListener _ignore_battery_listener = new View.OnClickListener() {
        @SuppressLint("BatteryLife")
        @Override
        public void onClick(View view) {
            PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            String packageName = getContext().getPackageName();
            Intent intent = new Intent();

            if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            } else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+packageName));
            }
            getContext().startActivity(intent);
        }
    };

    View.OnClickListener _billing_page_listener = v -> {
        BillingPage page = PageContainer.getInstance().getBillingPage();
        getNavigationController().pushViewController(page);};

    AdapterView.OnItemSelectedListener _screen_orientation_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            _settings.setPropertiesScreenOrientation(i);
            changeScreenOrientation();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            _settings.setPropertiesScreenOrientation(0);
        }
    };

    AdapterView.OnItemSelectedListener _drawer_location_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            _settings.setPropertiesDrawerLocation(i);
            changeScreenOrientation();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            _settings.setPropertiesDrawerLocation(0);
        }
    };

    AdapterView.OnItemSelectedListener _toolbar_location_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            _settings.setPropertiesToolbarLocation(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            _settings.setPropertiesToolbarLocation(0);
        }
    };
    AdapterView.OnItemSelectedListener _toolbar_order_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            _settings.setPropertiesToolbarOrder(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            _settings.setPropertiesToolbarOrder(0);
        }
    };


    public int getPageLayout() {
        return R.layout.system_settings_page;
    }

    public int getPageType() {
        return 7;
    }

    public boolean isPopupPage() {
        return true;
    }

    public void onPageDidLoad() {
        this._settings = new UserSettings(getContext());

        // 黑名單
        ((LinearLayout) findViewById(R.id.SystemSettings_blockListSetting)).setOnClickListener(_block_list_setting_listener);
        CheckBox block_list_enable_box = (CheckBox) findViewById(R.id.SystemSettings_blockListEnable);
        block_list_enable_box.setChecked(_settings.getPropertiesBlockListEnable());
        block_list_enable_box.setOnCheckedChangeListener(_block_list_enable_listener);
        findViewById(R.id.SystemSettings_item_blockListEnable).setOnClickListener(view -> block_list_enable_box.setChecked(!block_list_enable_box.isChecked()));

        // keep-wifi
        CheckBox keep_wifi_box = (CheckBox) findViewById(R.id.SystemSettings_keepWifi);
        keep_wifi_box.setChecked(_settings.getPropertiesKeepWifi());
        keep_wifi_box.setOnCheckedChangeListener(_keep_wifi_listener);
        findViewById(R.id.SystemSettings_item_keepWifi).setOnClickListener(view -> keep_wifi_box.setChecked(!keep_wifi_box.isChecked()));
        
        // 換頁動畫
        CheckBox animation_enable_box = (CheckBox) findViewById(R.id.SystemSettings_animationEnable);
        animation_enable_box.setChecked(_settings.getPropertiesAnimationEnable());
        animation_enable_box.setOnCheckedChangeListener(_animation_enable_listener);
        findViewById(R.id.SystemSettings_item_animationEnable).setOnClickListener(view -> animation_enable_box.setChecked(!animation_enable_box.isChecked()));

        // 文章首篇/末篇
        CheckBox article_move_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableArticleMove);
        article_move_enable_box.setChecked(_settings.getPropertiesArticleMoveEnable());
        article_move_enable_box.setOnCheckedChangeListener(_article_move_enable_listener);
        findViewById(R.id.SystemSettings_item_enableArticleMove).setOnClickListener(view -> article_move_enable_box.setChecked(!article_move_enable_box.isChecked()));

        // 螢幕方向
        ArrayAdapter<String> adapter_screen_orientation = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_screen_orientation_items));
        adapter_screen_orientation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner_screen_orientation = (Spinner) findViewById(R.id.SystemSettings_screen_orientation_spinner);
        spinner_screen_orientation.setAdapter(adapter_screen_orientation);
        spinner_screen_orientation.setSelection(_settings.getPropertiesScreenOrientation());
        spinner_screen_orientation.setOnItemSelectedListener(_screen_orientation_listener);

        // 不受電池最佳化限制
        ((LinearLayout) findViewById(R.id.SystemSettings_item_IgnoreBatteryOptimizations)).setOnClickListener(_ignore_battery_listener);


        // VIP
        if (_settings.getPropertiesVIP()) {
            // 使用手勢在看板/文章
            findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setVisibility(View.VISIBLE);
            CheckBox gesture_on_board_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableGestureOnBoard);
            gesture_on_board_enable_box.setChecked(_settings.getPropertiesGestureOnBoardEnable());
            gesture_on_board_enable_box.setOnCheckedChangeListener(_gesture_on_board_enable_listener);
            findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setOnClickListener(view -> gesture_on_board_enable_box.setChecked(!gesture_on_board_enable_box.isChecked()));

            // 自動登入洽特
            findViewById(R.id.SystemSettings_item_enableAutoToChat).setVisibility(View.VISIBLE);
            CheckBox auto_to_chat_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableAutoToChat);
            auto_to_chat_enable_box.setChecked(_settings.getPropertiesAutoToChat());
            auto_to_chat_enable_box.setOnCheckedChangeListener(_auto_to_chat_enable_listener);
            findViewById(R.id.SystemSettings_item_enableAutoToChat).setOnClickListener(view -> auto_to_chat_enable_box.setChecked(!auto_to_chat_enable_box.isChecked()));

            // 工具列位置
            ArrayAdapter<String> adapter_toolbar_location = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_toolbar_location_items));
            adapter_toolbar_location.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner_toolbar_location = (Spinner) findViewById(R.id.SystemSettings_toolbar_location_spinner);
            spinner_toolbar_location.setAdapter(adapter_toolbar_location);
            spinner_toolbar_location.setSelection(_settings.getPropertiesToolbarLocation());
            spinner_toolbar_location.setOnItemSelectedListener(_toolbar_location_listener);
            // 工具列順序
            ArrayAdapter<String> adapter_toolbar_order = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_toolbar_order_items));
            adapter_toolbar_order.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner_toolbar_order = (Spinner) findViewById(R.id.SystemSettings_toolbar_order_spinner);
            spinner_toolbar_order.setAdapter(adapter_toolbar_order);
            spinner_toolbar_order.setSelection(_settings.getPropertiesToolbarOrder());
            spinner_toolbar_order.setOnItemSelectedListener(_toolbar_order_listener);
            // 側滑選單位置
            ArrayAdapter<String> adapter_drawer_location = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_drawer_location_items));
            adapter_drawer_location.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner_drawer_location = (Spinner) findViewById(R.id.SystemSettings_drawer_location_spinner);
            spinner_drawer_location.setAdapter(adapter_drawer_location);
            spinner_drawer_location.setSelection(_settings.getPropertiesDrawerLocation());
            spinner_drawer_location.setOnItemSelectedListener(_drawer_location_listener);

        } else {
            findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_item_enableAutoToChat).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_item_toolbar_location).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_item_toolbar_order).setVisibility(View.GONE);
            findViewById(R.id.SystemSettings_item_drawer_location).setVisibility(View.GONE);
        }

        // billing-page
        ((LinearLayout) findViewById(R.id.SystemSettings_goBillingPage)).setOnClickListener(this._billing_page_listener);
    }

    public String getName() {
        return "TelnetSystemSettingsDialog";
    }

    public boolean onBackPressed() {
        this._settings.notifyDataUpdated();
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
