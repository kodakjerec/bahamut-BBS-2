package com.kota.Bahamut.pages.essencePage

import android.content.Context
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
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
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.pages.model.BoardEssencePageItem
import com.kota.Bahamut.pages.model.BoardEssencePageItemView
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard

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
        return "[Board][Essence]"
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

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
        var view1 = view
        val itemIndex = i + 1
        val block = ItemUtils.getBlock(itemIndex)
        val boardEssencePageItem = getItem(i) as BoardEssencePageItem?
        if (boardEssencePageItem == null && currentBlock != block && !isLoadingBlock(itemIndex)) {
            loadBoardBlock(block)
        }
        if (view1 == null) {
            view1 = BoardEssencePageItemView(context)
            view1.layoutParams = AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val boardEssencePageItemView = view1 as BoardEssencePageItemView
        boardEssencePageItemView.setItem(boardEssencePageItem)
        boardEssencePageItemView.setNumber(itemIndex)
        return boardEssencePageItemView
    }

    override fun isItemCanLoadAtIndex(index: Int): Boolean {
        val boardEssencePageItem = getItem(index) as BoardEssencePageItem
        return !boardEssencePageItem.isDeleted && boardEssencePageItem.isBBSClickable
    }

    // 點下文章
    override fun loadItemAtIndex(index: Int) {
        val item = getItem(index) as BoardEssencePageItem? ?: return
        if (!item.isBBSClickable) {
            ASToast.showShortToast("找沒有了耶...:(")
            return
        }

        if (item.isDirectory) {
            // 目錄
            // 如果現在最上層是article essence page, 表示是在內文按上一篇/下一篇
            val lastPage = ASNavigationController.currentController!!.viewControllers.lastElement()!!
            if (lastPage.pageType == BahamutPage.BAHAMUT_ARTICLE_ESSENCE) {
                ASToast.showShortToast("找沒有了耶...:(")
            } else {
                // 進入目錄
                PageContainer.instance!!.pushBoardEssencePage(listName, myTitle)
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
        return if (aItem != null) {
            val boardEssencePageItem = aItem as BoardEssencePageItem
            return !boardEssencePageItem.isBBSClickable
        } else
            false
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
                BahaText(
                    text = headerTitleState.ifEmpty { stringResource(R.string.loading) },
                    color = colors.titleBarTitle,
                    size = BahaTextSize.BODY,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (headerSubtitleState.isNotEmpty() || headerDetailState.isNotEmpty()) {
                    BahaText(
                        text = listOfNotNull(
                            headerSubtitleState.takeIf { it.isNotEmpty() },
                            headerDetailState.takeIf { it.isNotEmpty() }
                        ).joinToString("  "),
                        color = colors.titleBarDetail,
                        size = BahaTextSize.CAPTION,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

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
                        items(count = currentCount) { index ->
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
                            HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // 底部工具列
            HorizontalDivider(color = colors.toolbarDivider, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.toolbarBackground)
            ) {
                BahaButton(
                    text = stringResource(R.string.first_page),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { moveToFirstPosition() }
                )
                BahaButton(
                    text = stringResource(R.string.last_page),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { moveToLastPosition() }
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
        val statusText = when {
            item == null -> "..."
            item.isDirectory -> "◆"
            !item.isBBSClickable -> "◇("
            else -> "◇"
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BahaText(
                text = statusText,
                color = colors.bbsBoardNormal,
                size = BahaTextSize.CAPTION,
                modifier = Modifier.padding(end = 4.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                BahaText(
                    text = item?.title ?: stringResource(R.string.loading_),
                    color = colors.bbsBoardNormal,
                    size = BahaTextSize.CAPTION,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    BahaText(
                        text = String.format("%05d", itemIndex),
                        color = colors.bbsMailNumber,
                        size = BahaTextSize.TINY,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    BahaText(
                        text = item?.author ?: "",
                        color = colors.bbsMailAuthor,
                        size = BahaTextSize.TINY,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    BahaText(
                        text = item?.date ?: "",
                        color = colors.bbsMailDate,
                        size = BahaTextSize.TINY
                    )
                }
            }
        }
    }
}
