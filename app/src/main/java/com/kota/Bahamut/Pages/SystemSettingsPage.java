package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.changeScreenOrientation;

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
            SystemSettingsPage.this._settings.setPropertiesAutoToChat(isChecked);}
    };
    CompoundButton.OnCheckedChangeListener _getsure_on_board_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setPropertiesGetsureOnBoardEnable(isChecked);}
    };
    CompoundButton.OnCheckedChangeListener _animation_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setPropertiesAnimationEnable(isChecked);
            ASNavigationController.getCurrentController().setAnimationEnable(SystemSettingsPage.this._settings.getPropertiesAnimationEnable());
        }
    };
    CompoundButton.OnCheckedChangeListener _article_move_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setPropertiesArticleMoveDisable(isChecked);
        }
    };
    CompoundButton.OnCheckedChangeListener _block_list_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setPropertiesBlockListEnable(isChecked);
        }
    };
    View.OnClickListener _block_list_setting_listener = new View.OnClickListener() {
        public void onClick(View v) {
            SystemSettingsPage.this.getNavigationController().pushViewController(new BlockListPage());
        }
    };
    CompoundButton.OnCheckedChangeListener _keep_wifi_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setPropertiesKeepWifi(isChecked);
        }
    };

    View.OnClickListener _billing_page_listener = new View.OnClickListener() {
        public void onClick(View v) {
            BillingPage page = PageContainer.getInstance().getBillingPage();
            getNavigationController().pushViewController(page);}
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
        ((LinearLayout) findViewById(R.id.SystemSettings_blockListSetting)).setOnClickListener(this._block_list_setting_listener);
        CheckBox block_list_enable_box = (CheckBox) findViewById(R.id.SystemSettings_blockListEnable);
        block_list_enable_box.setChecked(this._settings.getPropertiesBlockListEnable());
        block_list_enable_box.setOnCheckedChangeListener(this._block_list_enable_listener);
        findViewById(R.id.SystemSettings_item_blockListEnable).setOnClickListener(view -> block_list_enable_box.setChecked(!block_list_enable_box.isChecked()));

        // keep-wifi
        CheckBox keep_wifi_box = (CheckBox) findViewById(R.id.SystemSettings_keepWifi);
        keep_wifi_box.setChecked(this._settings.getPropertiesKeepWifi());
        keep_wifi_box.setOnCheckedChangeListener(this._keep_wifi_listener);
        findViewById(R.id.SystemSettings_item_keepWifi).setOnClickListener(view -> keep_wifi_box.setChecked(!keep_wifi_box.isChecked()));

        // 換頁動畫
        CheckBox animation_enable_box = (CheckBox) findViewById(R.id.SystemSettings_animationEnable);
        animation_enable_box.setChecked(this._settings.getPropertiesAnimationEnable());
        animation_enable_box.setOnCheckedChangeListener(this._animation_enable_listener);
        findViewById(R.id.SystemSettings_item_animationEnable).setOnClickListener(view -> animation_enable_box.setChecked(!animation_enable_box.isChecked()));

        // 文章首篇/末篇
        CheckBox article_move_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableArticleMove);
        article_move_enable_box.setChecked(this._settings.getPropertiesArticleMoveEnsable());
        article_move_enable_box.setOnCheckedChangeListener(this._article_move_enable_listener);
        findViewById(R.id.SystemSettings_item_enableArticleMove).setOnClickListener(view -> article_move_enable_box.setChecked(!article_move_enable_box.isChecked()));

        // 螢幕方向
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getResource().getStringArray(R.array.system_setting_page_screen_orientation_items));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.SystemSettings_screen_orientation_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(_settings.getPropertiesScreenOrientation());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                _settings.setPropertiesScreenOrientation(i);
                changeScreenOrientation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                _settings.setPropertiesScreenOrientation(0);
            }
        });

        // VIP
        if (_settings.getPropertiesVIP()) {
            // 使用手勢在看板/文章
            findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setVisibility(View.VISIBLE);
            CheckBox gesture_on_board_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableGestureOnBoard);
            gesture_on_board_enable_box.setChecked(this._settings.getPropertiesGetsureOnBoardEnable());
            gesture_on_board_enable_box.setOnCheckedChangeListener(this._getsure_on_board_enable_listener);
            findViewById(R.id.SystemSettings_item_enableGestureOnBoard).setOnClickListener(view -> gesture_on_board_enable_box.setChecked(!gesture_on_board_enable_box.isChecked()));

            // 自動登入洽特
            findViewById(R.id.SystemSettings_item_enableAutoToChat).setVisibility(View.VISIBLE);
            CheckBox auto_to_chat_enable_box = (CheckBox) findViewById(R.id.SystemSettings_enableAutoToChat);
            auto_to_chat_enable_box.setChecked(this._settings.getPropertiesAutoToChat());
            auto_to_chat_enable_box.setOnCheckedChangeListener(this._auto_to_chat_enable_listener);
            findViewById(R.id.SystemSettings_item_enableAutoToChat).setOnClickListener(view -> auto_to_chat_enable_box.setChecked(!auto_to_chat_enable_box.isChecked()));
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
