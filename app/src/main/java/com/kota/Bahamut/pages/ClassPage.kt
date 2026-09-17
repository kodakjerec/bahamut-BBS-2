package com.kota.Bahamut.pages

import android.content.Context
import android.content.res.Configuration
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dialogs.DialogSearchBoard
import com.kota.Bahamut.dialogs.DialogSearchBoardListener
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.model.ClassPageBlock
import com.kota.Bahamut.pages.model.ClassPageBlock.Companion.recycle
import com.kota.Bahamut.pages.model.ClassPageHandler
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.pages.model.ClassPageItem.Companion.recycle
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.dialogs.BahaListDialog
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.logic.SearchBoardHandler
import com.kota.telnet.reference.TelnetKeyboard
import kotlinx.coroutines.launch

/**
 * 分類看板列表頁面 (純 Jetpack Compose 實作)
 */
class ClassPage : TelnetListPage(), View.OnClickListener, DialogSearchBoardListener {

    private var classTitle: String? = ""
    var currentDisplayTitle by mutableStateOf("")
    var lastVisitBoardText by mutableStateOf("")
    var searchResultBoards by mutableStateOf<List<String>?>(null)

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_CLASS

    override val pageLayout: Int
        get() = 0

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                ClassPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageDidLoad() {
        super.onPageDidLoad()

        updateTitles()

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            object : ASCoroutine() {
                override suspend fun run() {
                    TelnetClient.myInstance?.sendStringToServerInBackground("sChat")
                }
            }.postDelayed(500L)
        }
    }

    private fun updateTitles() {
        var displayTitle = this.classTitle
        if (displayTitle.isNullOrEmpty()) {
            displayTitle = getContextString(R.string.loading)
        }
        currentDisplayTitle = displayTitle

        if (TempSettings.lastVisitBoard.isNotEmpty()) {
            lastVisitBoardText = TempSettings.lastVisitBoard
        } else {
            lastVisitBoardText = ""
        }
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        updateTitles()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onPageRefresh()
        safeNotifyDataSetChanged()
    }

    override fun onBackPressed(): Boolean {
        clear()
        PageContainer.instance?.popClassPage()
        navigationController.popViewController()
        TelnetClient.myInstance?.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return true
    }

    override fun onSearchButtonClicked(): Boolean {
        showSearchBoardDialog()
        return true
    }

    private fun showSearchBoardDialog() {
        val dialog = DialogSearchBoard()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSearchButtonClickedWithKeyword(str: String) {
        SearchBoardHandler.instance.clear()
        showProcessingDialog("搜尋中")
        create().pushString("s$str ").sendToServerInBackground()
    }

    fun setClassTitle(aTitle: String?) {
        this.classTitle = aTitle
        this.currentDisplayTitle = aTitle ?: ""
    }

    override fun onClick(aView: View) {
        // legacy View.OnClickListener 相容 stub
    }

    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        if (listName == "Favorite") {
            val itemIndex = index + 1
            ASAlertDialog.createDialog().setMessage("確定要將此看板移出我的最愛?").addButton("取消")
                .addButton("確定")
                .setListener { _, index1 ->
                    if (index1 == 1) {
                        TelnetClient.myInstance?.sendStringToServerInBackground("$itemIndex\nd")
                        this@ClassPage.loadLastBlock()
                    }
                }.scheduleDismissOnPageDisappear(this).show()
            return true
        } else {
            val item = getItem(index) as? ClassPageItem
            if (item?.isDirectory == true) {
                return false
            }
            val itemIndex2 = index + 1
            ASAlertDialog.createDialog().setMessage("確定要將此看板加入我的最愛?").addButton("取消")
                .addButton("確定")
                .setListener { _, index12 ->
                    if (index12 == 1) {
                        TelnetClient.myInstance?.sendStringToServerInBackground("$itemIndex2\na")
                    }
                }.show()
            return true
        }
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    fun onSearchBoardFinished() {
        dismissProcessingDialog()
        searchResultBoards = SearchBoardHandler.instance.boards.toList()
    }

    fun showAddBoardToFavoriteDialog(boardName: String?) {
        ASAlertDialog.createDialog().setMessage("是否將看板" + boardName + "加入我的最愛?")
            .addButton("取消").addButton("加入")
            .setListener { _, index ->
                if (index == 1) {
                    create().pushKey(TelnetKeyboard.LEFT_ARROW).pushString("B\n")
                        .pushKey(TelnetKeyboard.HOME).pushString("/$boardName\na ")
                        .pushKey(TelnetKeyboard.LEFT_ARROW).pushString("F\ns$boardName\n")
                        .sendToServerInBackground()
                    return@setListener
                }
                if (TempSettings.lastVisitBoard != boardName) {
                    TempSettings.lastVisitArticleNumber = 0
                }
                TelnetClient.myInstance?.sendStringToServerInBackground("s$boardName")
                SearchBoardHandler.instance.clear()
            }.scheduleDismissOnPageDisappear(this).show()
    }

    override fun loadPage(): TelnetListPageBlock? {
        return ClassPageHandler.instance.load()
    }

    override val isAutoLoadEnable: Boolean
        get() = false

    override fun getListIdFromListName(aName: String?): String {
        return "$aName[Class]"
    }

    override fun loadItemAtIndex(index: Int) {
        val item = getItem(index) as? ClassPageItem ?: return

        if (item.isDirectory) {
            PageContainer.instance?.pushClassPage(item.name, item.title)
            navigationController.pushViewController(PageContainer.instance!!.classPage)
        } else {
            if (TempSettings.lastVisitBoard != item.name) {
                TempSettings.lastVisitArticleNumber = 0
            }
            val page = PageContainer.instance!!.boardPage
            page.prepareInitial()
            navigationController.pushViewController(page)
        }
        super.loadItemAtIndex(index)
    }

    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {
        recycle(telnetListPageBlock as ClassPageBlock)
    }

    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {
        recycle(telnetListPageItem as ClassPageItem)
    }

    // -------------------------------------------------------------
    // Compose 畫面主體
    // -------------------------------------------------------------

    @Composable
    fun ClassPageContent() {
        val colors = AppTheme.colors
        val coroutineScope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        // 監聽 TelnetListPage 資料變更
        var dataVersion by remember { mutableIntStateOf(0) }
        DisposableEffect(Unit) {
            val observer = object : DataSetObserver() {
                override fun onChanged() {
                    dataVersion++
                }

                override fun onInvalidated() {
                    dataVersion++
                }
            }
            registerDataSetObserver(observer)
            onDispose {
                unregisterDataSetObserver(observer)
            }
        }

        // 搜尋看板結果對話框
        val searchBoards = searchResultBoards
        if (searchBoards != null) {
            BahaListDialog(
                title = "搜尋結果",
                items = searchBoards,
                onDismissRequest = { searchResultBoards = null },
                onItemSelected = { index, _ ->
                    val board = SearchBoardHandler.instance.getBoard(index)
                    if (this@ClassPage.listName == "Favorite") {
                        this@ClassPage.showAddBoardToFavoriteDialog(board)
                    } else {
                        if (TempSettings.lastVisitBoard != board) {
                            TempSettings.lastVisitArticleNumber = 0
                        }
                        TelnetClient.myInstance?.sendStringToServerInBackground("s$board")
                        SearchBoardHandler.instance.clear()
                    }
                    searchResultBoards = null
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 1. 頂部標題列 (經典 BBS 深海藍底，雙行文字，無返回按鈕)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.titleBarBackground)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                BahaText(
                    text = currentDisplayTitle.ifEmpty { stringResource(R.string.loading) },
                    color = colors.titleBarTitle,
                    size = BahaTextSize.TITLE
                )
                Spacer(modifier = Modifier.height(2.dp))
                BahaText(
                    text = "看板列表",
                    color = colors.titleBarDetail,
                    size = BahaTextSize.BODY
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider)
            )

            // 2. 看板清單主體
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // 讀取版本以觸發重組
                val currentCount = if (dataVersion >= 0) count else 0

                if (currentCount == 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        BahaText(
                            text = stringResource(R.string.loading_),
                            color = colors.textSecondary,
                            size = BahaTextSize.BODY
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = currentCount,
                            key = { index -> index }
                        ) { index ->
                            @Suppress("UNUSED_VARIABLE")
                            val version = dataVersion
                            val itemIndex = index + 1
                            val itemBlock = ItemUtils.getBlock(itemIndex)
                            val item = getItem(index) as? ClassPageItem

                            // 按需觸發 Telnet 區塊加載
                            if (item == null && currentBlock != itemBlock && !isLoadingBlock(itemIndex)) {
                                loadBoardBlock(itemBlock)
                            }

                            ClassPageRowItem(
                                item = item,
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

                // 前次造訪看板快速連結浮動文字 (如 Chat▶▶)，沉底靠右
                if (lastVisitBoardText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 8.dp)
                            .clickable {
                                TelnetClient.myInstance?.sendStringToServer("s$lastVisitBoardText")
                            }
                    ) {
                        BahaText(
                            text = "$lastVisitBoardText▶▶",
                            color = colors.textPrimary,
                            size = BahaTextSize.TITLE
                        )
                    }
                }
            }

            // 3. 底部操作工具列 (滿版無縫 50dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.toolbarBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BahaButton(
                    text = stringResource(R.string.search),
                    type = ButtonType.NORMAL,
                    onClick = { onSearchButtonClicked() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.first_page),
                    type = ButtonType.NORMAL,
                    onClick = {
                        moveToFirstPosition()
                        coroutineScope.launch { listState.scrollToItem(0) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.last_page),
                    type = ButtonType.NORMAL,
                    onClick = {
                        moveToLastPosition()
                        coroutineScope.launch {
                            val lastIdx = (count - 1).coerceAtLeast(0)
                            listState.scrollToItem(lastIdx)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

/**
 * 分類看板單列項目 Composable
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ClassPageRowItem(
    item: ClassPageItem?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = AppTheme.colors

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
            // 左側看板文字資訊區塊
            Column(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    .padding(start = 10.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            ) {
                // 看板中文標題 (白字)
                BahaText(
                    text = item?.title ?: stringResource(R.string.loading_),
                    color = colors.textPrimary,
                    size = BahaTextSize.SUBTITLE,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 看板英文名稱 (黃字) 與板主資訊 (紫藍字)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = item?.name ?: stringResource(R.string.loading),
                        color = colors.classItemName,
                        size = BahaTextSize.BODY
                    )

                    val manager = item?.manager
                    if (!manager.isNullOrEmpty()) {
                        BahaText(
                            text = manager,
                            color = colors.classItemManager,
                            size = BahaTextSize.BODY,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 垂直分隔線
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.divider)
            )

            // 右箭頭按鈕區塊 (點擊亦可進入)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable { onClick() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BahaText(
                    text = ">",
                    color = colors.textPrimary,
                    size = BahaTextSize.BODY
                )
            }
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
