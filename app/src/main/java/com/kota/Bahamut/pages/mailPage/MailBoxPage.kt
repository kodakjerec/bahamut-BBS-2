package com.kota.Bahamut.pages.mailPage

import android.content.Context
import android.database.DataSetObserver
import android.util.Log
import android.view.View
import android.widget.ListAdapter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.kota.Bahamut.ui.components.BBSToolbar
import com.kota.Bahamut.ui.components.BBSToolbarDivider
import com.kota.Bahamut.ui.components.BBSTopBar
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.components.RightArrow
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard
import kotlinx.coroutines.launch
import java.util.Vector
import kotlin.math.max

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
        val coroutineScope = rememberCoroutineScope()
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
            // 頂部導覽列 (BBS 經典深海藍底，雙行文字資訊)
            BBSTopBar(
                title = headerTitleState.ifEmpty { "我的信箱" },
                subtitle = headerSubtitleState
            )

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
                        BahaText(
                            text = stringResource(R.string.mainbox_no_mail),
                            color = colors.textSecondary,
                            fontSize = BahaTextSize.BODY
                        )
                    }
                } else {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        items(
                            count = currentCount,
                            key = { index -> index }
                        ) { index ->
                            @Suppress("UNUSED_VARIABLE")
                            val version = dataVersion
                            val itemIndex = index + 1
                            val block = ItemUtils.getBlock(itemIndex)
                            val item = getItem(index) as? MailBoxPageItem
                            if (item == null && currentBlock != block && !isLoadingBlock(itemIndex)) {
                                loadBoardBlock(block)
                            }
                            MailBoxRowItem(
                                item = item,
                                itemIndex = itemIndex,
                                onClick = {
                                    coroutineScope.launch {
                                        loadItemAtIndex(index)
                                    }
                                },
                                onLongClick = {
                                    coroutineScope.launch {
                                        onListViewItemLongClicked(null, index)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 底部工具列 (BBS 50dp 全寬操作列)
            BBSToolbar {
                BahaButton(
                    text = stringResource(R.string.write_mail),
                    type = ButtonType.NORMAL,
                    fontSize = BahaTextSize.TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onPostButtonClicked() }
                )
                BBSToolbarDivider()
                BahaButton(
                    text = stringResource(R.string.search),
                    type = ButtonType.NORMAL,
                    fontSize = BahaTextSize.TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { showSelectArticleDialog() }
                )
                BBSToolbarDivider()
                BahaButton(
                    text = stringResource(R.string.first_page),
                    type = ButtonType.NORMAL,
                    fontSize = BahaTextSize.TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        moveToFirstPosition()
                        coroutineScope.launch { listState.scrollToItem(0) }
                        showShortToast(getContextString(R.string.already_to_top))
                    }
                )
                BBSToolbarDivider()
                BahaButton(
                    text = stringResource(R.string.last_page),
                    type = ButtonType.NORMAL,
                    fontSize = BahaTextSize.TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        setManualLoadPage()
                        moveToLastPosition()
                        coroutineScope.launch { listState.scrollToItem(max(0, currentCount - 1)) }
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
        val statusText = if (isRead) "◇" else "◆"
        val isMarked = item?.isMarked == true
        val isReply = item?.isReply == true

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.pageBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左側信件資訊區塊
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = onClick,
                            onLongClick = onLongClick
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    // 第一列：狀態圖示與信件標題
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BahaText(
                            text = statusText,
                            color = colors.bbsMailStatus,
                            fontSize = BahaTextSize.TITLE,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        BahaText(
                            text = item?.title ?: stringResource(R.string.loading_),
                            color = textColor,
                            fontSize = BahaTextSize.TITLE,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // 第二列：編號、標記(M)、回信(R)、日期、作者
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 編號 (5 碼)
                        BahaText(
                            text = String.format("%05d", itemIndex),
                            color = colors.bbsMailNumber,
                            fontSize = BahaTextSize.BODY
                        )

                        // 標記 M
                        if (isMarked) {
                            Spacer(modifier = Modifier.width(8.dp))
                            BahaText(
                                text = "M",
                                color = colors.bbsMailMark,
                                fontSize = BahaTextSize.BODY
                            )
                        } else {
                            Spacer(modifier = Modifier.width(42.dp))
                        }

                        // 回信 R
                        if (isReply) {
                            Spacer(modifier = Modifier.width(8.dp))
                            BahaText(
                                text = "R",
                                color = colors.bbsMailReply,
                                fontSize = BahaTextSize.BODY
                            )
                        } else {
                            Spacer(modifier = Modifier.width(42.dp))
                        }

                        // 日期
                        Spacer(modifier = Modifier.width(12.dp))
                        BahaText(
                            text = item?.date ?: "",
                            color = colors.bbsMailDate,
                            fontSize = BahaTextSize.BODY
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // 作者 (靠右對齊)
                        BahaText(
                            text = item?.author ?: "",
                            color = colors.bbsMailAuthor,
                            fontSize = BahaTextSize.BODY,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 右側箭頭按鈕區塊
                RightArrow { onClick() }
            }

            // 底部分隔線
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider)
            )
        }
    }
}
