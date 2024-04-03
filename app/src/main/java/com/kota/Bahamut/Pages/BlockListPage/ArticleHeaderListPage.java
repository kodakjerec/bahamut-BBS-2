package com.kota.Bahamut.Pages.BlockListPage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.Pages.PostArticlePage;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.UserSettings;
import com.kota.TelnetUI.TelnetPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArticleHeaderListPage extends TelnetPage implements BlockListClickListener {
    EditText _inputField;

    BlockListAdapter articleHeaderListAdapter;
    // 按下新增
    View.OnClickListener _addListener = v -> {
        if (_inputField != null) {
            String block_name = _inputField.getText().toString().trim();
            _inputField.setText("");
            UserSettings.addBlockName(block_name);
            UserSettings.notifyDataUpdated();
            ArticleHeaderListPage.this.reload();
        }
    };
    // 按下重置
    View.OnClickListener _resetListener = v -> {
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.reset))
                .setMessage(getContextString(R.string.reset_message))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.sure))
                .setListener((aDialog1, button_index) -> {
                    if (button_index > 0) {
                        UserSettings.resetArticleHeaders();
                        ArticleHeaderListPage.this.reload();
                    }
                }).show();
    };
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0) {
        public boolean isSwiped = false;
        public boolean isDragged = false;
        int start = -1;
        int end = -1;
        private View dragView;
        // 上下移動
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            start = viewHolder.getAdapterPosition();
            end = target.getAdapterPosition();
                if (UserSettings.getPropertiesVIP()) {
                    if (start==0 || end==0) {
                        ASToast.showShortToast(getContextString(R.string.article_header_zero_error_message));
                    } else {
                        Collections.swap(_articleHeadersList, start, end);
                        articleHeaderListAdapter.notifyItemMoved(start, end);
                    }
                } else {
                    ASToast.showShortToast(getContextString(R.string.vip_only_message));
                }
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_DRAG -> {
                    // the user is dragging an item and didn't lift their finger off yet
                    isSwiped = false;
                    isDragged = true;
                    if (viewHolder != null) { // 選取變色
                        dragView = viewHolder.itemView;
                        dragView.setBackgroundResource(R.color.ripple_material);
                    }
                }
                case ItemTouchHelper.ACTION_STATE_SWIPE -> {
                    // the user is swiping an item and didn't lift their finger off yet
                    isSwiped = true;
                    isDragged = false;
                }
                case ItemTouchHelper.ACTION_STATE_IDLE -> {
                    // the user just dropped the item (after dragging it), and lift their finger off.
                    //
                    if (isSwiped) { // The user used onSwiped()
                        Log.e("swipe", "swipe is over");
                    }
                    if (!isSwiped && isDragged) { // The user used onMove()
                        if (dragView != null) { // 解除 選取變色
                            dragView.setBackgroundResource(R.color.transparent);
                            dragView = null;
                        }
                        UserSettings.setArticleHeaders(_articleHeadersList);
                    }
                    isSwiped = false;
                    isDragged = false;
                }
            }
        }
    });


    private List<String> _articleHeadersList = new ArrayList<>();

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
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.BlockList_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        articleHeaderListAdapter = new BlockListAdapter(_articleHeadersList);
        recyclerView.setAdapter(articleHeaderListAdapter);
        articleHeaderListAdapter.setOnItemClickListener(this);

        _inputField = (EditText) findViewById(R.id.BlockList_Input);
        findViewById(R.id.BlockList_Add).setOnClickListener(_addListener);
        findViewById(R.id.BlockList_Reset).setOnClickListener(_resetListener);

        reload();
    }

    public void onPageWillDisappear() {
        UserSettings.notifyDataUpdated();
    }

    public void onPageDidDisappear() {
        _articleHeadersList = null;
        super.onPageDidDisappear();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void reload() {
        List<String> _temp = Arrays.asList(UserSettings.getArticleHeaders());
        _articleHeadersList.clear();
        _articleHeadersList.addAll(_temp);
        articleHeaderListAdapter.notifyDataSetChanged();
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        ASToast.showShortToast("返回");
        return true;
    }

    @Override
    public void onBlockListPage_ItemView_clicked(BlockListViewHolder blockListPage_ItemView) {

    }

    // 刪除黑名單
    @Override
    public void onBlockListPage_ItemView_delete_clicked(BlockListViewHolder blockListPage_ItemView) {
        int deleted_index = blockListPage_ItemView.getAdapterPosition();
        if (deleted_index==0) {
            ASToast.showShortToast(getContextString(R.string.article_header_zero_error_message));
            return;
        }
        List<String> new_list = ArticleHeaderListPage.this._articleHeadersList;
        new_list.remove(deleted_index);

        // 更新
        UserSettings.setArticleHeaders(_articleHeadersList);
        ArticleHeaderListPage.this.reload();
    }
}
