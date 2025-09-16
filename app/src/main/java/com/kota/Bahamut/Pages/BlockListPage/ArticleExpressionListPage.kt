package com.kota.Bahamut.Pages.BlockListPage

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.ASFramework.Dialog.ASAlertDialog
import com.kota.ASFramework.Dialog.ASAlertDialog.Companion.createDialog
import com.kota.ASFramework.Dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.ASFramework.Dialog.ASAlertDialogListener
import com.kota.ASFramework.UI.ASToast.showLongToast
import com.kota.ASFramework.UI.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.NotificationSettings.getShowExpression
import com.kota.Bahamut.Service.NotificationSettings.setShowExpression
import com.kota.Bahamut.Service.UserSettings
import com.kota.Bahamut.Service.UserSettings.Companion.articleExpressions
import com.kota.Bahamut.Service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.Service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.Service.UserSettings.Companion.resetArticleExpressions
import com.kota.TelnetUI.TelnetPage
import java.util.Arrays
import java.util.Collections

class ArticleExpressionListPage : TelnetPage(), BlockListClickListener {
    var _inputField: EditText? = null

    var articleExpressionListAdapter: BlockListAdapter? = null

    // 按下新增
    var _addListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (_inputField != null) {
            val block_name = _inputField!!.getText().toString().trim { it <= ' ' }
            _inputField!!.setText("")
            if (block_name.length > 0) {
                val new_list: MutableList<String?> =
                    ArrayList<String?>(Arrays.asList<String>(*articleExpressions))
                if (new_list.contains(block_name)) {
                    showErrorDialog(
                        getContextString(R.string.already_have_item),
                        this@ArticleExpressionListPage
                    )
                } else {
                    new_list.add(block_name)
                }
                UserSettings.setArticleExpressions(new_list)

                notifyDataUpdated()
                this@ArticleExpressionListPage.reload()
            } else {
                showErrorDialog(
                    getContextString(R.string.please_input_id),
                    this@ArticleExpressionListPage
                )
            }
        }
    }

    // 按下重置
    var _resetListener: View.OnClickListener = View.OnClickListener { v: View? ->
        createDialog()
            .setTitle(getContextString(R.string.reset))
            .setMessage(getContextString(R.string.reset_message))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.sure))
            .setListener(ASAlertDialogListener { aDialog1: ASAlertDialog?, button_index: Int ->
                if (button_index > 0) {
                    showShortToast(getContextString(R.string.reset_ok))
                    _inputField!!.setText("")
                    resetArticleExpressions()
                    this@ArticleExpressionListPage.reload()
                }
            }).show()
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
            start = viewHolder.getAdapterPosition()
            end = target.getAdapterPosition()
            if (propertiesVIP) {
                Collections.swap(_articleExpressionsList, start, end)
                articleExpressionListAdapter!!.notifyItemMoved(start, end)
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
                        dragView!!.setBackgroundResource(R.color.ripple_material)
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
                            dragView!!.setBackgroundResource(R.color.transparent)
                            dragView = null
                        }
                        UserSettings.setArticleExpressions(_articleExpressionsList!!)
                    }
                    isSwiped = false
                    isDragged = false
                }
            }
        }
    })


    private var _articleExpressionsList: MutableList<String?>? = ArrayList<String?>()

    val pageType: Int
        get() = BahamutPage.BAHAMUT_BLOCK_LIST

    val pageLayout: Int
        get() = R.layout.block_list_page

    val isKeepOnOffline: Boolean
        get() = true

    public override fun onPageDidLoad() {
        val recyclerView = findViewById(R.id.BlockList_list) as RecyclerView?
        recyclerView!!.setLayoutManager(LinearLayoutManager(context))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        articleExpressionListAdapter = BlockListAdapter(_articleExpressionsList)
        recyclerView.setAdapter(articleExpressionListAdapter)
        articleExpressionListAdapter!!.setOnItemClickListener(this)

        _inputField = findViewById(R.id.BlockList_Input) as EditText?
        findViewById(R.id.BlockList_Add)!!.setOnClickListener(_addListener)
        findViewById(R.id.BlockList_Reset)!!.setOnClickListener(_resetListener)

        showNotification()

        reload()
    }

    // 第一次進入的提示訊息
    private fun showNotification() {
        val show_top_bottom_function = getShowExpression()
        if (!show_top_bottom_function) {
            showLongToast(getContextString(R.string.notification_expression))
            setShowExpression(true)
        }
    }

    public override fun onPageWillDisappear() {
        notifyDataUpdated()
    }

    public override fun onPageDidDisappear() {
        _articleExpressionsList = null
        super.onPageDidDisappear()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun reload() {
        val _temp = Arrays.asList<String?>(*articleExpressions)
        _articleExpressionsList!!.clear()
        _articleExpressionsList!!.addAll(_temp)
        articleExpressionListAdapter!!.notifyDataSetChanged()
    }

    public override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    override fun onBlockListPage_ItemView_clicked(blockListPage_ItemView: BlockListViewHolder?) {
    }

    // 刪除黑名單
    override fun onBlockListPage_ItemView_delete_clicked(blockListPage_ItemView: BlockListViewHolder) {
        val deleted_index = blockListPage_ItemView.getAdapterPosition()
        val new_list = this@ArticleExpressionListPage._articleExpressionsList
        new_list!!.removeAt(deleted_index)

        // 更新
        UserSettings.setArticleExpressions(_articleExpressionsList!!)
        this@ArticleExpressionListPage.reload()
    }
}
