package com.kota.Bahamut.pages.blockListPage

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.ui.components.BahaInputField
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
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetPage
import java.util.Collections

class BlockListPage : TelnetPage(), BlockListClickListener {

    // Compose state for input text
    var inputTextState by mutableStateOf("")

    private var blockListAdapter: BlockListAdapter? = null
    private var blockList: MutableList<String> = ArrayList()

    override val isPopupPage: Boolean
        get() = true

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BLOCK_LIST

    override val pageLayout: Int
        get() = 0

    override val isKeepOnOffline: Boolean
        get() = true

    val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(object :
        ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        var isSwiped: Boolean = false
        var isDragged: Boolean = false
        var start: Int = -1
        var end: Int = -1
        private var dragView: View? = null

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

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    isSwiped = false; isDragged = true
                    dragView = viewHolder?.itemView
                    dragView?.setBackgroundResource(R.color.ripple_material)
                }
                ItemTouchHelper.ACTION_STATE_SWIPE -> {
                    isSwiped = true; isDragged = false
                }
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    if (isSwiped) Log.e("swipe", "swipe is over")
                    if (!isSwiped && isDragged) {
                        dragView?.setBackgroundResource(R.color.transparent)
                        dragView = null
                        UserSettings.blockList = this@BlockListPage.blockList
                    }
                    isSwiped = false; isDragged = false
                }
            }
        }
    })

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                BlockListPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageWillDisappear() {
        notifyDataUpdated()
    }

    override fun onPageDidDisappear() {
        blockList = ArrayList()
        super.onPageDidDisappear()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reload() {
        val temp: MutableList<String> = UserSettings.blockList
        blockList.clear()
        blockList.addAll(temp)
        blockListAdapter?.notifyDataSetChanged()
    }

    private fun showNotification() {
        if (!getShowBlockList()) {
            showLongToast(getContextString(R.string.notification_block_list))
            setShowBlockList(true)
        }
    }

    private fun onAddClicked() {
        val blockName = inputTextState.trim()
        inputTextState = ""
        if (blockName.isNotEmpty()) {
            val newList: MutableList<String> = UserSettings.blockList
            if (newList.contains(blockName)) {
                showErrorDialog(getContextString(R.string.already_have_item), this)
            } else {
                newList.add(blockName)
            }
            UserSettings.blockList = newList
            notifyDataUpdated()
            reload()
        } else {
            showErrorDialog(getContextString(R.string.please_input_id), this)
        }
    }

    private fun onResetClicked() {
        createDialog()
            .setTitle(getContextString(R.string.reset))
            .setMessage(getContextString(R.string.reset_message))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.sure))
            .setListener { _: ASAlertDialog?, buttonIndex: Int ->
                if (buttonIndex > 0) {
                    showShortToast(getContextString(R.string.reset_ok))
                    inputTextState = ""
                    resetBlockList()
                    reload()
                }
            }.show()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    override fun onBlockListPageItemViewClicked(blockListPageItemView: BlockListViewHolder) {}

    override fun onBlockListPageItemViewDeleteClicked(blockListPageItemView: BlockListViewHolder) {
        val deletedIndex = blockListPageItemView.bindingAdapterPosition
        val newList = this@BlockListPage.blockList
        newList.removeAt(deletedIndex)
        UserSettings.blockList = this@BlockListPage.blockList
        reload()
    }

    // ---------------------------------------------------------------
    // Compose UI
    // ---------------------------------------------------------------

    @Composable
    fun BlockListPageContent() {
        val colors = AppTheme.colors

        // Init adapter and load data on first composition
        if (blockListAdapter == null) {
            blockListAdapter = BlockListAdapter(blockList).also {
                it.setOnItemClickListener(this)
            }
            showNotification()
            reload()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // 輸入列 (滿版無縫)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.pageBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BahaButton(
                    text = stringResource(R.string.reset),
                    type = ButtonType.DANGER,
                    modifier = Modifier.fillMaxHeight(),
                    onClick = { onResetClicked() }
                )
                BahaInputField(
                    value = inputTextState,
                    onValueChange = { inputTextState = it },
                    placeholder = stringResource(R.string.please_input_id),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddClicked() })
                )
                BahaButton(
                    text = stringResource(R.string.add),
                    type = ButtonType.DANGER,
                    modifier = Modifier.fillMaxHeight(),
                    onClick = { onAddClicked() }
                )
            }

            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // RecyclerView with ItemTouchHelper (kept in AndroidView for drag-reorder)
            AndroidView(
                factory = { ctx ->
                    RecyclerView(ctx).apply {
                        layoutManager = LinearLayoutManager(ctx)
                        adapter = blockListAdapter
                        itemTouchHelper.attachToRecyclerView(this)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // 底部工具列 (滿版無縫)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            BahaButton(
                text = stringResource(R.string._back),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = { onBackPressed() }
            )
        }
    }
}
