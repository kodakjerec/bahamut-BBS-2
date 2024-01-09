package com.kota.Bahamut.Pages;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.R;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;

public class SystemSettingsPage extends TelnetPage {
    CompoundButton.OnCheckedChangeListener _animation_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setAnimationEnable(isChecked);
            ASNavigationController.getCurrentController().setAnimationEnable(SystemSettingsPage.this._settings.isAnimationEnable());
        }
    };
    CompoundButton.OnCheckedChangeListener _article_move_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setArticleMoveDisable(isChecked);
        }
    };
    CompoundButton.OnCheckedChangeListener _block_list_enable_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setBlockListEnable(isChecked);
        }
    };
    View.OnClickListener _block_list_setting_listener = new View.OnClickListener() {
        public void onClick(View v) {
            SystemSettingsPage.this.getNavigationController().pushViewController(new BlockListPage());
        }
    };
    CompoundButton.OnCheckedChangeListener _keep_wifi_listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemSettingsPage.this._settings.setKeepWifi(isChecked);
        }
    };
    UserSettings _settings;

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
        ((LinearLayout) findViewById(R.id.SystemSettings_BlockListSetting)).setOnClickListener(this._block_list_setting_listener);
        CheckBox keep_wifi_box = (CheckBox) findViewById(R.id.SystemSettings_KeepWifi);
        keep_wifi_box.setOnCheckedChangeListener(this._keep_wifi_listener);
        keep_wifi_box.setChecked(this._settings.isKeepWifi());
        CheckBox block_list_enable_box = (CheckBox) findViewById(R.id.SystemSettings_BlockListEnable);
        block_list_enable_box.setChecked(this._settings.isBlockListEnable());
        block_list_enable_box.setOnCheckedChangeListener(this._block_list_enable_listener);
        CheckBox animation_enable_box = (CheckBox) findViewById(R.id.SystemSettings_AnimationEnable);
        animation_enable_box.setChecked(this._settings.isAnimationEnable());
        animation_enable_box.setOnCheckedChangeListener(this._animation_enable_listener);
        CheckBox article_move_enable_box = (CheckBox) findViewById(R.id.SystemSettings_DisableArticleMove);
        article_move_enable_box.setChecked(this._settings.isArticleMoveDisable());
        article_move_enable_box.setOnCheckedChangeListener(this._article_move_enable_listener);
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
