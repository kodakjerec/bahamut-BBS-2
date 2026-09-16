package com.kota.Bahamut.pages.mailPage

import android.content.Context
import android.database.DataSetObserver
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListAdapter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandDeleteArticle
import com.kota.Bahamut.command.BahamutCommandSearchArticle
import com.kota.Bahamut.command.BahamutCommandSendMail
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
import com.kota.Bahamut.dialogs.DialogSelectArticle
import com.kota.Bahamut.dialogs.DialogSelectArticleListener
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.model.MailBoxPageBlock
import com.kota.Bahamut.pages.model.MailBoxPageHandler
import com.kota.Bahamut.pages.model.MailBoxPageItem
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard
import java.util.Vector

@OptIn(ExperimentalFoundationApi::class)
class MailBoxPage : TelnetListPage(), ListAdapter, DialogSearchArticleListener,
    DialogSelectArticleListener, SendMailPageListener {

    // Compose state
    var headerTitleState by mutableStateOf("我的信箱")
    var headerSubtitleState by mutableStateOf("")

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_MAIL_BOX

    override val pageLayout: Int
        get() = 0

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                MailBoxPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun loadPage(): TelnetListPageBlock? {
        return MailBoxPageHandler.instance.load()
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        val myListCount = getItemSize()
        headerTitleState = "我的信箱"
        headerSubtitleState = "您有 $myListCount 封信在信箱內"
    }

    override fun onBackPressed(): Boolean {
        clear()
        navigationController.popViewController()
        create().pushKey(TelnetKeyboard.LEFT_ARROW).pushKey(TelnetKeyboard.LEFT_ARROW)
            .sendToServerInBackground(1)
        return true
    }

    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        if (isItemCanLoadAtIndex(index)) {
            onDeleteArticle(index + 1)
            return true
        }
        return false
    }

    fun showSelectArticleDialog() {
        val dialog = DialogSelectArticle()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String>) {
        pushCommand(
            BahamutCommandSearchArticle(
                vector[0]!!,
                vector[1],
                if (vector[2] == "YES") "y" else "n",
                vector[3]
            )
        )
    }

    override fun onSelectDialogDismissWIthIndex(str: String) {
        var itemIndex = -1
        try {
            itemIndex = str.toInt() - 1
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
        if (itemIndex >= 0) {
            setListViewSelection(itemIndex)
        }
    }

    override fun onSendMailDialogSendButtonClicked(
        sendMailPage: SendMailPage,
        receiver: String,
        title: String,
        content: String
    ) {
        pushCommand(BahamutCommandSendMail(receiver, title, content))
    }

    override fun isItemCanLoadAtIndex(index: Int): Boolean {
        val mailBoxPageItem = getItem(index) as MailBoxPageItem?
        if (mailBoxPageItem == null || mailBoxPageItem.isDeleted) {
            showShortToast("此信件已被刪除")
            return false
        }
        return true
    }

    fun onDeleteArticle(itemIndex: Int) {
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string.delete))
            .setMessage(getContextString(R.string.del_this_mail))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.delete))
            .setListener { _: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    val mailBoxPageItem = getItem(itemIndex - 1) as MailBoxPageItem?
                    mailBoxPageItem?.isDeleted = true
                    val command: TelnetCommand = BahamutCommandDeleteArticle(itemIndex)
                    this@MailBoxPage.pushCommand(command)
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast(getContextString(R.string._back))
        return true
    }

    fun onPostButtonClicked() {
        val sendMailPage = SendMailPage()
        sendMailPage.setListener(this)
        navigationController.pushViewController(sendMailPage)
    }

    fun loadPreviousArticle() {
        val targetNumber = loadingItemNumber - 1
        if (targetNumber < 1) {
            showShortToast(getContextString(R.string.already_to_top))
        } else {
            loadItemAtNumber(targetNumber)
        }
    }

    fun loadNextArticle() {
        val targetIndex = loadingItemNumber + 1
        if (targetIndex > getItemSize()) {
            showShortToast(getContextString(R.string.already_to_bottom))
        } else {
            loadItemAtNumber(targetIndex)
        }
    }

    override val isAutoLoadEnable: Boolean
        get() = false

    override var listName: String = ""
        get() = "[MailBox]"

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        var v = view
        val itemIndex = i + 1
        val itemBlock = ItemUtils.getBlock(itemIndex)
        val item = getItem(i) as MailBoxPageItem?
        val curBlock = currentBlock
        if (item == null && curBlock != itemBlock && !isLoadingBlock(itemIndex)) {
            loadBoardBlock(itemBlock)
        }
        if (v == null) {
            v = MailBoxPageItemView(context)
            v.layoutParams = AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val itemView = v as MailBoxPageItemView
        itemView.setItem(item)
        itemView.setIndex(itemIndex)
        return v
    }

    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {
        MailBoxPageBlock.recycle(telnetListPageBlock as MailBoxPageBlock)
    }

    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {
        MailBoxPageItem.recycle(telnetListPageItem as MailBoxPageItem)
    }

    fun recoverPost() {}
    fun finishPost() {}

    override fun onSearchDialogCancelButtonClicked() {}

    // ---------------------------------------------------------------
    // Compose UI
    // ---------------------------------------------------------------

    @Composable
    fun MailBoxPageContent() {
        val colors = AppTheme.colors
        val listState = rememberLazyListState()

        var dataVersion by remember { mutableIntStateOf(0) }
        DisposableEffect(Unit) {
            val observer = object : DataSetObserver() {
                override fun onChanged() { dataVersion++ }
                override fun onInvalidated() { dataVersion++ }
            }
            registerDataSetObserver(observer)
            onDispose { unregisterDataSetObserver(observer) }
        }

        val currentCount = if (dataVersion >= 0) count else 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 頂部 header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.toolbarBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = headerTitleState,
                    color = colors.titleBarTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (headerSubtitleState.isNotEmpty()) {
                    Text(
                        text = headerSubtitleState,
                        color = colors.titleBarDetail,
                        fontSize = 13.sp
                    )
                }
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // 郵件列表
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (currentCount == 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.mainbox_no_mail),
                            color = colors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        items(count = currentCount) { index ->
                            val itemIndex = index + 1
                            val block = ItemUtils.getBlock(itemIndex)
                            val item = getItem(index) as? MailBoxPageItem
                            if (item == null && currentBlock != block && !isLoadingBlock(itemIndex)) {
                                loadBoardBlock(block)
                            }
                            MailBoxRowItem(
                                item = item,
                                itemIndex = itemIndex,
                                onClick = { loadItemAtIndex(index) },
                                onLongClick = { onListViewItemLongClicked(null, index) }
                            )
                            HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // 底部工具列
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            Row(modifier = Modifier.fillMaxWidth()) {
                BahaButton(
                    text = stringResource(R.string.write_mail),
                    modifier = Modifier.weight(1f),
                    onClick = { onPostButtonClicked() }
                )
                BahaButton(
                    text = stringResource(R.string.search),
                    modifier = Modifier.weight(1f),
                    onClick = { showSelectArticleDialog() }
                )
                BahaButton(
                    text = stringResource(R.string.first_page),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        moveToFirstPosition()
                        showShortToast(getContextString(R.string.already_to_top))
                    }
                )
                BahaButton(
                    text = stringResource(R.string.last_page),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        setManualLoadPage()
                        moveToLastPosition()
                        showShortToast(getContextString(R.string.already_to_bottom))
                    }
                )
            }
        }
    }

    @Composable
    private fun MailBoxRowItem(
        item: MailBoxPageItem?,
        itemIndex: Int,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        val colors = AppTheme.colors
        val isRead = item?.isRead ?: false
        val textColor = if (isRead) colors.bbsBoardNormalRead else colors.bbsBoardNormal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (item?.isMarked == true) "★" else "  ",
                color = colors.bbsMailMark,
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item?.title ?: stringResource(R.string.loading_),
                    color = textColor,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        text = String.format("%05d", itemIndex),
                        color = colors.bbsMailNumber,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item?.author ?: "",
                        color = colors.bbsMailAuthor,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item?.date ?: "",
                        color = colors.bbsMailDate,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
