package com.kota.Bahamut.Pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.R;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import java.util.Vector;

public class BlockListPage extends TelnetPage {
    View.OnClickListener _add_listener = v -> {
        EditText input_field = (EditText) BlockListPage.this.findViewById(R.id.BlockList_Input);
        if (input_field != null) {
            String block_name = input_field.getText().toString().trim();
            input_field.setText("");
            BlockListPage.this._settings.addBlockName(block_name);
            BlockListPage.this._settings.notifyDataUpdated();
            BlockListPage.this.reload();
        }
    };
    /* access modifiers changed from: private */
    public Vector<String> _block_list = null;
    BlockListPage_ItemView_Listener _block_listener = aItemView -> {
        int deleted_index = aItemView.index;
        Vector<String> new_list = new Vector<>(BlockListPage.this._block_list);
        new_list.remove(deleted_index);
        BlockListPage.this._settings.updateBlockList(new_list);
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
            return BlockListPage.this._block_list.get(index);
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
                item_view = new BlockListPage_ItemView(BlockListPage.this.getContext());
                item_view.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
                item_view.listener = BlockListPage.this._block_listener;
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
    UserSettings _settings;

    public int getPageType() {
        return 8;
    }

    public int getPageLayout() {
        return R.layout.block_list_page;
    }

    public boolean isKeepOnOffline() {
        return true;
    }

    public void onPageDidLoad() {
        this._settings = new UserSettings(getContext());
        this._block_list = this._settings.getBlockList();
        ((ListView) findViewById(R.id.BlockList_List)).setAdapter(this._list_adapter);
        findViewById(R.id.BlockList_Add).setOnClickListener(this._add_listener);
    }

    public void onPageWillDisappear() {
        this._settings.notifyDataUpdated();
    }

    public void onPageDidDisappear() {
        this._block_list = null;
        super.onPageDidDisappear();
    }

    /* access modifiers changed from: private */
    public void reload() {
        this._block_list = this._settings.getBlockList();
        this._list_adapter.notifyDataSetChanged();
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast("返回");
        return true;
    }
}
