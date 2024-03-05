package com.kota.Bahamut.Pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import java.util.Vector;

public class BlockListPage extends TelnetPage {
    EditText _input_field;
    View _blockList_Add;
    View.OnClickListener _add_listener = v -> {
        if (_input_field != null) {
            String block_name = _input_field.getText().toString().trim();
            _input_field.setText("");
            UserSettings.addBlockName(block_name);
            UserSettings.notifyDataUpdated();
            BlockListPage.this.reload();
        }
    };

    private Vector<String> _block_list = null;
    // 刪除黑名單
    BlockListPage_ItemView_Listener _block_listener = aItemView -> {
        int deleted_index = aItemView.index;
        Vector<String> new_list = new Vector<>(BlockListPage.this._block_list);
        new_list.remove(deleted_index);
        UserSettings.updateBlockList(new_list);
        BlockListPage.this.reload();
    };
    BaseAdapter _list_adapter = new BaseAdapter() {
        public int getCount() {
            if (BlockListPage.this._block_list!=null)
                return BlockListPage.this._block_list.size();
            else
                return 0;
        }

        public String getItem(int index) {
            return _block_list.get(index);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            BlockListPage_ItemView item_view;
            if (convertView == null) {
                item_view = new BlockListPage_ItemView(getContext());
                item_view.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
                item_view.listener = _block_listener;
            } else {
                item_view = (BlockListPage_ItemView) convertView;
            }
            item_view.setDividerTopVisible(position == 0);
            item_view.setName(getItem(position));
            item_view.index = position;
            return item_view;
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

        public boolean areAllItemsEnabled() {
            return true;
        }

        public boolean isEnabled(int position) {
            return false;
        }
    };

    public int getPageType() {
        return BahamutPage.BAHAMUT_BLOCK_LIST;
    }

    public int getPageLayout() {
        return R.layout.block_list_page;
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    public void onPageDidLoad() {
        _block_list = UserSettings.getBlockList();
        ListView mainLayout = (ListView) findViewById(R.id.BlockList_list);
        mainLayout.setAdapter(_list_adapter);
        _input_field = (EditText) findViewById(R.id.BlockList_Input);
        _blockList_Add = findViewById(R.id.BlockList_Add);
        _blockList_Add.setOnClickListener(_add_listener);
        mainLayout.requestFocus();
    }

    public void onPageWillDisappear() {
        UserSettings.notifyDataUpdated();
    }

    public void onPageDidDisappear() {
        _block_list = null;
        super.onPageDidDisappear();
    }

    private void reload() {
        _block_list = UserSettings.getBlockList();
        _list_adapter.notifyDataSetChanged();
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast("返回");
        return true;
    }
}
