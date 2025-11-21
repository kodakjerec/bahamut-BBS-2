package com.kota.Bahamut.pages.blockListPage

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getShowBlockList
import com.kota.Bahamut.service.NotificationSettings.setShowBlockList
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.service.UserSettings.Companion.resetBlockList
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetPage
import java.util.Collections

class BlockListPage : TelnetPage(), BlockListClickListener {
    var inputField: EditText? = null

    var blockListAdapter: BlockListAdapter? = null

    // 按下新增
    var addListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (inputField != null) {
            val blockName = inputField?.text.toString().trim()
            inputField?.setText("")
            if (blockName.isNotEmpty()) {
                val newList: MutableList<String> = UserSettings.Companion.blockList
                if (newList.contains(blockName)) {
                    showErrorDialog(
                        getContextString(R.string.already_have_item),
                        this@BlockListPage
                    )
                } else {
                    newList.add(blockName)
                }
                UserSettings.Companion.blockList = newList

                notifyDataUpdated()
                this@BlockListPage.reload()
            } else {
                showErrorDialog(getContextString(R.string.please_input_id), this@BlockListPage)
            }
        }
    }

    // 按下重置
    var resetListener: View.OnClickListener = View.OnClickListener { v: View? ->
        createDialog()
            .setTitle(getContextString(R.string.reset))
            .setMessage(getContextString(R.string.reset_message))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.sure))
            .setListener { aDialog1: ASAlertDialog?, buttonIndex: Int ->
                if (buttonIndex > 0) {
                    showShortToast(getContextString(R.string.reset_ok))
                    inputField?.setText("")
                    resetBlockList()
                    this@BlockListPage.reload()
                }
            }.show()
    }

    var itemTouchHelper: ItemTouchHelper = ItemTouchHelper(object :
        ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        var isSwiped: Boolean = false
        var isDragged: Boolean = false
        var start: Int = -1
        var end: Int = -1
        private var dragView: View? = null

        // 上下移動
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            start = viewHolder.bindingAdapterPosition
            end = target.bindingAdapterPosition
            if (propertiesVIP) {
                Collections.swap(this@BlockListPage.blockList, start, end)
                blockListAdapter?.notifyItemMoved(start, end)
            } else {
                showShortToast(getContextString(R.string.vip_only_message))
            }
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    // the user is dragging an item and didn't lift their finger off yet
                    isSwiped = false
                    isDragged = true
                    if (viewHolder != null) { // 選取變色
                        dragView = viewHolder.itemView
                        dragView?.setBackgroundResource(R.color.ripple_material)
                    }
                }

                ItemTouchHelper.ACTION_STATE_SWIPE -> {
                    // the user is swiping an item and didn't lift their finger off yet
                    isSwiped = true
                    isDragged = false
                }

                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    // the user just dropped the item (after dragging it), and lift their finger off.
                    //
                    if (isSwiped) { // The user used onSwiped()
                        Log.e("swipe", "swipe is over")
                    }
                    if (!isSwiped && isDragged) { // The user used onMove()
                        if (dragView != null) { // 解除 選取變色
                            dragView?.setBackgroundResource(R.color.transparent)
                            dragView = null
                        }
                        UserSettings.Companion.blockList = this@BlockListPage.blockList
                    }
                    isSwiped = false
                    isDragged = false
                }
            }
        }
    })


    private var blockList: MutableList<String> = ArrayList()

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BLOCK_LIST

    override val pageLayout: Int
        get() = R.layout.block_list_page

    override val isKeepOnOffline: Boolean
        get() = true

    override fun onPageDidLoad() {
        val recyclerView = findViewById(R.id.BlockList_list) as RecyclerView
        recyclerView.setLayoutManager(LinearLayoutManager(context))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        blockListAdapter = BlockListAdapter(this@BlockListPage.blockList)
        recyclerView.setAdapter(blockListAdapter)
        blockListAdapter?.setOnItemClickListener(this)

        inputField = findViewById(R.id.BlockList_Input) as EditText?
        findViewById(R.id.BlockList_Add)?.setOnClickListener(addListener)
        findViewById(R.id.BlockList_Reset)?.setOnClickListener(resetListener)

        showNotification()

        reload()
    }

    // 第一次進入的提示訊息
    private fun showNotification() {
        val showTopBottomFunction = getShowBlockList()
        if (!showTopBottomFunction) {
            showLongToast(getContextString(R.string.notification_block_list))
            setShowBlockList(true)
        }
    }

    override fun onPageWillDisappear() {
        notifyDataUpdated()
    }

    override fun onPageDidDisappear() {
        this@BlockListPage.blockList = ArrayList()
        super.onPageDidDisappear()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun reload() {
        val temp: MutableList<String> = UserSettings.Companion.blockList
        blockList.clear()
        blockList.addAll(temp)
        blockListAdapter?.notifyDataSetChanged()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    override fun onBlockListPageItemViewClicked(blockListPageItemView: BlockListViewHolder) {
//        super.onBlockListPageItemViewClicked(blockListPageItemView)
    }

    // 刪除黑名單
    override fun onBlockListPageItemViewDeleteClicked(blockListPageItemView: BlockListViewHolder) {
        val deletedIndex = blockListPageItemView.bindingAdapterPosition
        val newList = this@BlockListPage.blockList
        newList.removeAt(deletedIndex)

        // 更新
        UserSettings.Companion.blockList = this@BlockListPage.blockList
        this@BlockListPage.reload()
    }
}
