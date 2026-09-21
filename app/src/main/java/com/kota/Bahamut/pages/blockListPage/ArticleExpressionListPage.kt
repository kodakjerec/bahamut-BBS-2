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
import com.kota.Bahamut.service.NotificationSettings.getShowExpression
import com.kota.Bahamut.service.NotificationSettings.setShowExpression
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.service.UserSettings.Companion.articleExpressions
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.service.UserSettings.Companion.resetArticleExpressions
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

class ArticleExpressionListPage : TelnetPage(), BlockListClickListener {

    var inputTextState by mutableStateOf("")

    private var articleExpressionListAdapter: BlockListAdapter? = null
    private var articleExpressionsList: MutableList<String> = ArrayList()

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
        private var dragView: View? = null

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val start = viewHolder.bindingAdapterPosition
            val end = target.bindingAdapterPosition
            if (propertiesVIP) {
                Collections.swap(articleExpressionsList, start, end)
                articleExpressionListAdapter?.notifyItemMoved(start, end)
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
                ItemTouchHelper.ACTION_STATE_SWIPE -> { isSwiped = true; isDragged = false }
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    if (isSwiped) Log.e("swipe", "swipe is over")
                    if (!isSwiped && isDragged) {
                        dragView?.setBackgroundResource(R.color.transparent)
                        dragView = null
                        UserSettings.setArticleExpressions(articleExpressionsList)
                    }
                    isSwiped = false; isDragged = false
                }
            }
        }
    })

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                ArticleExpressionListPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageWillDisappear() {
        notifyDataUpdated()
    }

    override fun onPageDidDisappear() {
        articleExpressionsList = ArrayList()
        super.onPageDidDisappear()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reload() {
        val elements = listOf(*articleExpressions)
        articleExpressionsList.clear()
        articleExpressionsList.addAll(elements)
        articleExpressionListAdapter?.notifyDataSetChanged()
    }

    private fun showNotification() {
        if (!getShowExpression()) {
            showLongToast(getContextString(R.string.notification_expression))
            setShowExpression(true)
        }
    }

    private fun onAddClicked() {
        val name = inputTextState.trim()
        inputTextState = ""
        if (name.isNotEmpty()) {
            val newList: MutableList<String> = ArrayList(listOf(*articleExpressions))
            if (newList.contains(name)) {
                showErrorDialog(getContextString(R.string.already_have_item), this)
            } else {
                newList.add(name)
            }
            UserSettings.setArticleExpressions(newList)
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
                    resetArticleExpressions()
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
        articleExpressionsList.removeAt(deletedIndex)
        UserSettings.setArticleExpressions(articleExpressionsList)
        reload()
    }

    @Composable
    fun ArticleExpressionListPageContent() {
        val colors = AppTheme.colors

        if (articleExpressionListAdapter == null) {
            articleExpressionListAdapter = BlockListAdapter(articleExpressionsList).also {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
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
                    modifier = Modifier.fillMaxHeight(),
                    onClick = { onAddClicked() }
                )
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            AndroidView(
                factory = { ctx ->
                    RecyclerView(ctx).apply {
                        layoutManager = LinearLayoutManager(ctx)
                        adapter = articleExpressionListAdapter
                        itemTouchHelper.attachToRecyclerView(this)
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
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
                    .height(60.dp),
                onClick = { onBackPressed() }
            )
        }
    }
}
