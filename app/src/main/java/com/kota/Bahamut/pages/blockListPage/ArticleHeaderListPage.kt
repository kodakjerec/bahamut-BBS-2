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
import com.kota.Bahamut.service.NotificationSettings.getShowHeader
import com.kota.Bahamut.service.NotificationSettings.setShowHeader
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.service.UserSettings.Companion.articleHeaders
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.service.UserSettings.Companion.resetArticleHeaders
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetPage
import java.util.Collections

class ArticleHeaderListPage : TelnetPage(), BlockListClickListener {
    var inputField: EditText? = null

    var articleHeaderListAdapter: BlockListAdapter? = null

    // 按下新增
    var addListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (inputField != null) {
            val blockName = inputField?.text.toString().trim { it <= ' ' }
            inputField?.setText("")
            if (blockName.isNotEmpty()) {
                val newList: MutableList<String?> =
                    ArrayList(listOf(*articleHeaders))
                if (newList.contains(blockName)) {
                    showErrorDialog(
                        getContextString(R.string.already_have_item),
                        this@ArticleHeaderListPage
                    )
                } else {
                    newList.add(blockName)
                }
                UserSettings.setArticleHeaders(newList)

                notifyDataUpdated()
                this@ArticleHeaderListPage.reload()
            } else {
                showErrorDialog(
                    getContextString(R.string.please_input_id),
                    this@ArticleHeaderListPage
                )
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
                    resetArticleHeaders()
                    this@ArticleHeaderListPage.reload()
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
                if (start == 0 || end == 0) {
                    showShortToast(getContextString(R.string.article_header_zero_error_message))
                } else {
                    Collections.swap(articleHeadersList, start, end)
                    articleHeaderListAdapter?.notifyItemMoved(start, end)
                }
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
                        UserSettings.setArticleHeaders(articleHeadersList)
                    }
                    isSwiped = false
                    isDragged = false
                }
            }
        }
    })


    private var articleHeadersList: MutableList<String?> = ArrayList()

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

        articleHeaderListAdapter = BlockListAdapter(articleHeadersList)
        recyclerView.setAdapter(articleHeaderListAdapter)
        articleHeaderListAdapter?.setOnItemClickListener(this)

        inputField = findViewById(R.id.BlockList_Input) as EditText?
        findViewById(R.id.BlockList_Add)?.setOnClickListener(addListener)
        findViewById(R.id.BlockList_Reset)?.setOnClickListener(resetListener)

        showNotification()

        reload()
    }

    // 第一次進入的提示訊息
    private fun showNotification() {
        val showTopBottomFunction = getShowHeader()
        if (!showTopBottomFunction) {
            showLongToast(getContextString(R.string.notification_header))
            setShowHeader(true)
        }
    }

    override fun onPageWillDisappear() {
        notifyDataUpdated()
    }

    override fun onPageDidDisappear() {
        articleHeadersList = ArrayList()
        super.onPageDidDisappear()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun reload() {
        val elements = listOf<String?>(*articleHeaders)
        articleHeadersList.clear()
        articleHeadersList.addAll(elements)
        articleHeaderListAdapter?.notifyDataSetChanged()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    // 刪除黑名單
    override fun onBlockListPageItemViewClicked(blockListPageItemView: BlockListViewHolder) {
        val deletedIndex = blockListPageItemView.bindingAdapterPosition
        if (deletedIndex == 0) {
            showShortToast(getContextString(R.string.article_header_zero_error_message))
            return
        }
        val newList = this@ArticleHeaderListPage.articleHeadersList
        newList.removeAt(deletedIndex)

        // 更新
        UserSettings.setArticleHeaders(articleHeadersList)
        this@ArticleHeaderListPage.reload()
    }

    override fun onBlockListPageItemViewDeleteClicked(blockListPageItemView: BlockListViewHolder) {
        TODO("Not yet implemented")
    }
}
