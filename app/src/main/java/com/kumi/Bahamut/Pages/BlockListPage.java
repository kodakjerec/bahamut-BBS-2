package com.kumi.Bahamut.Pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.kumi.ASFramework.UI.ASToast;
import com.kumi.Telnet.UserSettings;
import com.kumi.TelnetUI.TelnetPage;
import java.util.Vector;

public class BlockListPage extends TelnetPage {
  View.OnClickListener _add_listener = new View.OnClickListener() {
      final BlockListPage this$0;
      
      public void onClick(View param1View) {
        EditText editText = (EditText)BlockListPage.this.findViewById(2131230790);
        if (editText != null) {
          String str = editText.getText().toString().trim();
          editText.setText("");
          BlockListPage.this._settings.addBlockName(str);
          BlockListPage.this._settings.notifyDataUpdated();
          BlockListPage.this.reload();
        } 
      }
    };
  
  private Vector<String> _block_list = null;
  
  BlockListPage_ItemView_Listener _block_listener = new BlockListPage_ItemView_Listener() {
      final BlockListPage this$0;
      
      public void onBlockListPage_ItemView_Clicked(BlockListPage_ItemView param1BlockListPage_ItemView) {
        int i = param1BlockListPage_ItemView.index;
        Vector vector = new Vector();
        vector.addAll(BlockListPage.this._block_list);
        vector.remove(i);
        BlockListPage.this._settings.updateBlockList(vector);
        BlockListPage.this.reload();
      }
    };
  
  BaseAdapter _list_adapter = new BaseAdapter() {
      final BlockListPage this$0;
      
      public boolean areAllItemsEnabled() {
        return true;
      }
      
      public int getCount() {
        return BlockListPage.this._block_list.size();
      }
      
      public String getItem(int param1Int) {
        return BlockListPage.this._block_list.get(param1Int);
      }
      
      public long getItemId(int param1Int) {
        return param1Int;
      }
      
      public int getItemViewType(int param1Int) {
        return 0;
      }
      
      public View getView(int param1Int, View param1View, ViewGroup param1ViewGroup) {
        BlockListPage_ItemView blockListPage_ItemView;
        if (param1View == null) {
          blockListPage_ItemView = new BlockListPage_ItemView(BlockListPage.this.getContext());
          blockListPage_ItemView.setLayoutParams((ViewGroup.LayoutParams)new AbsListView.LayoutParams(-1, -2));
          blockListPage_ItemView.listener = BlockListPage.this._block_listener;
        } else {
          blockListPage_ItemView = blockListPage_ItemView;
        } 
        if (param1Int == 0) {
          boolean bool1 = true;
          blockListPage_ItemView.setDividerTopVisible(bool1);
          blockListPage_ItemView.setName(getItem(param1Int));
          blockListPage_ItemView.index = param1Int;
          return (View)blockListPage_ItemView;
        } 
        boolean bool = false;
        blockListPage_ItemView.setDividerTopVisible(bool);
        blockListPage_ItemView.setName(getItem(param1Int));
        blockListPage_ItemView.index = param1Int;
        return (View)blockListPage_ItemView;
      }
      
      public int getViewTypeCount() {
        return 1;
      }
      
      public boolean hasStableIds() {
        return false;
      }
      
      public boolean isEmpty() {
        return false;
      }
      
      public boolean isEnabled(int param1Int) {
        return false;
      }
    };
  
  UserSettings _settings;
  
  private void reload() {
    this._block_list = this._settings.getBlockList();
    this._list_adapter.notifyDataSetChanged();
  }
  
  public int getPageLayout() {
    return 2131361825;
  }
  
  public int getPageType() {
    return 8;
  }
  
  public boolean isKeepOnOffline() {
    return true;
  }
  
  public void onPageDidDisappear() {
    this._block_list = null;
    super.onPageDidDisappear();
  }
  
  public void onPageDidLoad() {
    this._settings = new UserSettings(getContext());
    this._block_list = this._settings.getBlockList();
    ((ListView)findViewById(2131230791)).setAdapter((ListAdapter)this._list_adapter);
    ((Button)findViewById(2131230789)).setOnClickListener(this._add_listener);
  }
  
  public void onPageWillDisappear() {
    this._settings.notifyDataUpdated();
  }
  
  public boolean onReceivedGestureRight() {
    onBackPressed();
    ASToast.showShortToast("返回");
    return true;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Pages\BlockListPage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */