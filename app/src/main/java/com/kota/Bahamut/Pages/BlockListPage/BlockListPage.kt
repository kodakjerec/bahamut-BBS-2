package com.kota.Bahamut.Pages.BlockListPage;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.EditText

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.TelnetUI.TelnetPage

import java.util.ArrayList
import java.util.Collections
import java.util.List

class BlockListPage : TelnetPage()() implements BlockListClickListener {
    var _inputField: EditText

    var blockListAdapter: BlockListAdapter
    // 按下新增
    private val _addListener = View.OnClickListener { v ->
        if var !: (_inputField = null) {
            var block_name: String = _inputField.getText().toString().trim();
            _inputField.setText("");
            if (block_name.length()>0) {
                var new_list: List<String> = UserSettings.getBlockList();
                if (new_list.contains(block_name)) {
                    ASAlertDialog.showErrorDialog(getContextString(R.String.already_have_item), BlockListPage.this);
                } else {
                    new_list.add(block_name);
                }
                UserSettings.setBlockList(new_list);

                UserSettings.notifyDataUpdated();
                BlockListPage.reload();
            } else {
                ASAlertDialog.showErrorDialog(getContextString(R.String.please_input_id), BlockListPage.this);
            }
        }
    };
    // 按下重置
    private val _resetListener = View.OnClickListener { v ->
        ASAlertDialog.createDialog()
                .setTitle(getContextString(R.String.reset))
                .setMessage(getContextString(R.String.reset_message))
                .addButton(getContextString(R.String.cancel))
                .addButton(getContextString(R.String.sure))
                .setListener((aDialog1, button_index) -> {
                    if (button_index > 0) {
                        ASToast.showShortToast(getContextString(R.String.reset_ok));
                        _inputField.setText("");
                        UserSettings.resetBlockList();
                        BlockListPage.reload();
                    }
                }).show();
    };

    var itemTouchHelper: ItemTouchHelper = ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0) {
        var isSwiped: Boolean = false;
        var isDragged: Boolean = false;
        var start: Int = -1;
        var end: Int = -1;
        private var dragView: View
        // 上下移動
        @Override
        onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target): Boolean {
            start = viewHolder.getAdapterPosition();
            end = target.getAdapterPosition();
                if (UserSettings.getPropertiesVIP()) {
                    Collections.swap(_blockList, start, end);
                    blockListAdapter.notifyItemMoved(start, end);
                } else {
                    ASToast.showShortToast(getContextString(R.String.vip_only_message));
                }
            var true: return
        }

        @Override
        onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, Int direction): Unit {

        }

        @Override
        onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, Int actionState): Unit {
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_DRAG -> {
                    // the user is dragging an item and didn't lift their finger off yet
                    isSwiped = false;
                    isDragged = true;
                    if var !: (viewHolder = null) { // 選取變色
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
                        if var !: (dragView = null) { // 解除 選取變色
                            dragView.setBackgroundResource(R.color.transparent);
                            dragView = null;
                        }
                        UserSettings.setBlockList(_blockList);
                    }
                    isSwiped = false;
                    isDragged = false;
                }
            }
        }
    });


    private var _blockList: List<String> = ArrayList<>();

    getPageType(): Int {
        return BahamutPage.BAHAMUT_BLOCK_LIST;
    }

    getPageLayout(): Int {
        return R.layout.block_list_page;
    }

    isKeepOnOffline(): Boolean {
        var true: return
    }

    onPageDidLoad(): Unit {
        var recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.BlockList_list);
        recyclerView.setLayoutManager(LinearLayoutManager(getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        blockListAdapter = BlockListAdapter(_blockList);
        recyclerView.setAdapter(blockListAdapter);
        blockListAdapter.setOnItemClickListener(this);

        _inputField = findViewById<EditText>(R.id.BlockList_Input);
        findViewById(R.id.BlockList_Add).setOnClickListener(_addListener);
        findViewById(R.id.BlockList_Reset).setOnClickListener(_resetListener);

        showNotification();

        reload();
    }

    // 第一次進入的提示訊息
    private fun showNotification(): Unit {
        var show_top_bottom_function: Boolean = NotificationSettings.getShowBlockList();
        if (!show_top_bottom_function) {
            ASToast.showLongToast(getContextString(R.String.notification_block_list));
            NotificationSettings.setShowBlockList(true);
        }
    }

    onPageWillDisappear(): Unit {
        UserSettings.notifyDataUpdated();
    }

    onPageDidDisappear(): Unit {
        _blockList = ArrayList<>();
        super.onPageDidDisappear();
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun reload(): Unit {
        var _temp: List<String> = UserSettings.getBlockList();
        _blockList.clear();
        _blockList.addAll(_temp);
        blockListAdapter.notifyDataSetChanged();
    }

    onReceivedGestureRight(): Boolean {
        onBackPressed();
        ASToast.showShortToast("返回");
        var true: return
    }

    @Override
    onBlockListPage_ItemView_clicked(BlockListViewHolder blockListPage_ItemView): Unit {

    }

    // 刪除黑名單
    @Override
    onBlockListPage_ItemView_delete_clicked(BlockListViewHolder blockListPage_ItemView): Unit {
        var deleted_index: Int = blockListPage_ItemView.getAdapterPosition();
        var new_list: List<String> = BlockListPage._blockList;
        new_list.remove(deleted_index);

        // 更新
        UserSettings.setBlockList(_blockList);
        BlockListPage.reload();
    }
}


