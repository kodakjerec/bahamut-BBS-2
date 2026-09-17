package com.kota.Bahamut.pages.essencePage

import android.content.Context
import android.database.DataSetObserver
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
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
import com.kota.Bahamut.listPage.ListStateStore
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.pages.model.BoardEssencePageItem
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BBSToolbar
import com.kota.Bahamut.ui.components.BBSToolbarDivider
import com.kota.Bahamut.ui.components.BBSTopBar
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard
import kotlinx.coroutines.launch
import kotlin.math.max

class BoardEssencePage : TelnetListPage() {
    private var myTitle: String = ""

    // Compose states
    var headerTitleState by mutableStateOf("精華文章")
    var headerSubtitleState by mutableStateOf("")
    var headerDetailState by mutableStateOf("")

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOARD_ESSENCE

    override val pageLayout: Int
        get() = 0

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                BoardEssencePageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        headerTitleState = "精華文章"
        headerSubtitleState = myTitle
        headerDetailState = listName ?: ""
    }

    override val listType: Int
        get() = BoardPageAction.ESSENCE

    override fun getListIdFromListName(aName: String?): String {
        return "$aName[Board][Essence][$myTitle]"
    }

    // 搜尋, 現在為不做事
    override fun onSearchButtonClicked(): Boolean {
        return true
    }

    override fun onBackPressed(): Boolean {
        clear()
        PageContainer.instance!!.popBoardEssencePage()
        navigationController.popViewController()
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return true
    }

    override val isAutoLoadEnable: Boolean
        get() = false

    override fun loadPage(): TelnetListPageBlock {
        return BoardEssencePageHandler.instance!!.load()
    }

    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {
        BoardPageBlock.recycle(telnetListPageBlock as BoardPageBlock)
    }

    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {
        BoardEssencePageItem.recycle(telnetListPageItem as BoardEssencePageItem)
    }

    override fun isItemCanLoadAtIndex(index: Int): Boolean {
        val boardEssencePageItem = getItem(index) as? BoardEssencePageItem ?: return false
        return !boardEssencePageItem.isDeleted && boardEssencePageItem.isBBSClickable
    }

    // 點下文章
    override fun loadItemAtIndex(index: Int) {
        val item = getItem(index) as? BoardEssencePageItem ?: return
        if (!item.isBBSClickable) {
            ASToast.showShortToast("找沒有了耶...:(")
            return
        }

        lastLoadItemIndex = index
        val listId = getListIdFromListName(listName)
        val state = ListStateStore.instance.getState(listId)
        state.position = index

        if (item.isDirectory) {
            // 目錄
            val lastPage = ASNavigationController.currentController?.viewControllers?.lastOrNull()
            if (lastPage?.pageType == BahamutPage.BAHAMUT_ARTICLE_ESSENCE) {
                ASToast.showShortToast("找沒有了耶...:(")
            } else {
                // 進入目錄
                PageContainer.instance!!.pushBoardEssencePage(listName, item.title ?: "")
                navigationController.pushViewController(PageContainer.instance!!.boardEssencePage)
                super.loadItemAtIndex(index)
            }
        } else {
            // 文章
            val articleEssencePage = PageContainer.instance!!.getArticleEssencePage()
            articleEssencePage.setBoardEssencePage(this)
            articleEssencePage.clear()
            navigationController.pushViewController(articleEssencePage)
            super.loadItemAtIndex(index)
        }
    }

    override fun isItemBlocked(aItem: TelnetListPageItem?): Boolean {
        if (aItem != null && aItem is BoardEssencePageItem) {
            return !aItem.isBBSClickable
        }
        return false
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    fun setClassTitle(aTitle: String) {
        myTitle = aTitle
    }

    fun loadPreviousArticle() {
        val targetNumber = loadingItemNumber - 1
        if (targetNumber < 1) {
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.already_to_top))
        } else {
            loadItemAtNumber(targetNumber)
        }
    }

    fun loadNextArticle() {
        val targetIndex = loadingItemNumber + 1
        if (targetIndex > getItemSize()) {
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.already_to_bottom))
        } else {
            loadItemAtNumber(targetIndex)
        }
    }

    // ---------------------------------------------------------------
    // Compose UI
    // ---------------------------------------------------------------

    @Composable
    fun BoardEssencePageContent() {
        val colors = AppTheme.colors
        val coroutineScope = rememberCoroutineScope()
        val listId = getListIdFromListName(listName)
        val savedPosition = remember(listId) {
            ListStateStore.instance.getState(listId).position
        }
        val savedOffset = remember(listId) {
            ListStateStore.instance.getState(listId).top
        }
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = savedPosition.coerceAtLeast(0),
            initialFirstVisibleItemScrollOffset = savedOffset
        )

        // 即時同步滾動位置至 ListStateStore
        LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            val state = ListStateStore.instance.getState(listId)
            state.position = listState.firstVisibleItemIndex
            state.top = listState.firstVisibleItemScrollOffset
        }

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

        // 恢復保存的位置
        var hasRestoredSavedPosition by remember { mutableStateOf(false) }
        LaunchedEffect(currentCount) {
            if (!hasRestoredSavedPosition && currentCount > 0 && savedPosition >= 0) {
                val target = savedPosition.coerceIn(0, currentCount - 1)
                listState.scrollToItem(target, savedOffset)
                hasRestoredSavedPosition = true
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 頂部標題列 (BBS 經典深海藍底，雙行資訊)
            BBSTopBar(
                title = headerTitleState.ifEmpty { "精華文章" },
                subtitle = headerSubtitleState,
                subtitleTrailing = headerDetailState
            )

            // 文章列表
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
                            text = stringResource(R.string.loading_),
                            color = colors.textSecondary,
                            size = BahaTextSize.BODY
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
                            val item = getItem(index) as? BoardEssencePageItem
                            if (item == null && currentBlock != block && !isLoadingBlock(itemIndex)) {
                                loadBoardBlock(block)
                            }
                            EssenceRowItem(
                                item = item,
                                itemIndex = itemIndex,
                                onClick = { loadItemAtIndex(index) }
                            )
                        }
                    }
                }
            }

            // 底部操作工具列
            BBSToolbar {
                BahaButton(
                    text = stringResource(R.string.first_page),
                    type = ButtonType.NORMAL,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        moveToFirstPosition()
                        coroutineScope.launch { listState.scrollToItem(0) }
                    }
                )
                BBSToolbarDivider()
                BahaButton(
                    text = stringResource(R.string.last_page),
                    type = ButtonType.NORMAL,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        moveToLastPosition()
                        coroutineScope.launch { listState.scrollToItem(max(0, currentCount - 1)) }
                    }
                )
            }
        }
    }

    @Composable
    private fun EssenceRowItem(
        item: BoardEssencePageItem?,
        itemIndex: Int,
        onClick: () -> Unit
    ) {
        val colors = AppTheme.colors
        val isDir = item?.isDirectory == true
        val statusText = when {
            item == null -> "..."
            isDir -> "◆"
            !item.isBBSClickable -> "◇("
            else -> "◇"
        }
        val titleColor = when {
            isDir -> colors.titleBarTitle
            else -> colors.bbsBoardNormal
        }

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
                // 左側項目資訊區塊
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    // 第一列：狀態圖示 + 標題 (目錄用黃色)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BahaText(
                            text = statusText,
                            color = if (isDir) colors.titleBarTitle else colors.bbsMailStatus,
                            size = BahaTextSize.BODY,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        BahaText(
                            text = item?.title ?: stringResource(R.string.loading_),
                            color = titleColor,
                            size = BahaTextSize.BODY,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // 第二列：編號、日期、作者 (靠右)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 編號 (5 碼)
                        BahaText(
                            text = String.format("%05d", itemIndex),
                            color = colors.bbsMailNumber,
                            size = BahaTextSize.BODY
                        )

                        // 日期
                        Spacer(modifier = Modifier.width(12.dp))
                        BahaText(
                            text = item?.date ?: "",
                            color = colors.bbsMailDate,
                            size = BahaTextSize.BODY
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // 作者 (靠右對齊)
                        BahaText(
                            text = item?.author ?: "",
                            color = colors.bbsMailAuthor,
                            size = BahaTextSize.BODY,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 垂直分隔線
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.divider)
                )

                // 右側箭頭按鈕區塊
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
}
