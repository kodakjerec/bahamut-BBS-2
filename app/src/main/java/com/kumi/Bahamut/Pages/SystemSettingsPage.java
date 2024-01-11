package com.kumi.Bahamut.Pages;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.PageController.ASViewController;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetPage;

public class SystemSettingsPage extends TelnetPage {
  CompoundButton.OnCheckedChangeListener _animation_enable_listener = new CompoundButton.OnCheckedChangeListener() {
      final SystemSettingsPage this$0;
      
      public void onCheckedChanged(CompoundButton param1CompoundButton, boolean param1Boolean) {
        SystemSettingsPage.this._settings.setAnimationEnable(param1Boolean);
        ASNavigationController.getCurrentController().setAnimationEnable(SystemSettingsPage.this._settings.isAnimationEnable());
      }
    };
  
  CompoundButton.OnCheckedChangeListener _article_move_enable_listener = new CompoundButton.OnCheckedChangeListener() {
      final SystemSettingsPage this$0;
      
      public void onCheckedChanged(CompoundButton param1CompoundButton, boolean param1Boolean) {
        SystemSettingsPage.this._settings.setArticleMoveDisable(param1Boolean);
      }
    };
  
  CompoundButton.OnCheckedChangeListener _block_list_enable_listener = new CompoundButton.OnCheckedChangeListener() {
      final SystemSettingsPage this$0;
      
      public void onCheckedChanged(CompoundButton param1CompoundButton, boolean param1Boolean) {
        SystemSettingsPage.this._settings.setBlockListEnable(param1Boolean);
      }
    };
  
  View.OnClickListener _block_list_setting_listener = new View.OnClickListener() {
      final SystemSettingsPage this$0;
      
      public void onClick(View param1View) {
        BlockListPage blockListPage = new BlockListPage();
        SystemSettingsPage.this.getNavigationController().pushViewController((ASViewController)blockListPage);
      }
    };
  
  CompoundButton.OnCheckedChangeListener _keep_wifi_listener = new CompoundButton.OnCheckedChangeListener() {
      final SystemSettingsPage this$0;
      
      public void onCheckedChanged(CompoundButton param1CompoundButton, boolean param1Boolean) {
        SystemSettingsPage.this._settings.setKeepWifi(param1Boolean);
      }
    };
  
  UserSettings _settings;
  
  public String getName() {
    return "TelnetSystemSettingsDialog";
  }
  
  public int getPageLayout() {
    return 2131361872;
  }
  
  public int getPageType() {
    return 7;
  }
  
  public boolean isKeepOnOffline() {
    return true;
  }
  
  public boolean isPopupPage() {
    return true;
  }
  
  public boolean onBackPressed() {
    this._settings.notifyDataUpdated();
    return super.onBackPressed();
  }
  
  public void onPageDidLoad() {
    this._settings = new UserSettings(getContext());
    ((LinearLayout)findViewById(2131230934)).setOnClickListener(this._block_list_setting_listener);
    CheckBox checkBox = (CheckBox)findViewById(2131230936);
    checkBox.setOnCheckedChangeListener(this._keep_wifi_listener);
    checkBox.setChecked(this._settings.isKeepWifi());
    checkBox = (CheckBox)findViewById(2131230933);
    checkBox.setChecked(this._settings.isBlockListEnable());
    checkBox.setOnCheckedChangeListener(this._block_list_enable_listener);
    checkBox = (CheckBox)findViewById(2131230931);
    checkBox.setChecked(this._settings.isAnimationEnable());
    checkBox.setOnCheckedChangeListener(this._animation_enable_listener);
    checkBox = (CheckBox)findViewById(2131230935);
    checkBox.setChecked(this._settings.isArticleMoveDisable());
    checkBox.setOnCheckedChangeListener(this._article_move_enable_listener);
  }
  
  public boolean onReceivedGestureRight() {
    onBackPressed();
    ASToast.showShortToast("返回");
    return true;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\SystemSettingsPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */