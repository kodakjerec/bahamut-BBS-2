package com.kota.Bahamut.pages.bookmarkPage

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.dataModels.BookmarkStore
import com.kota.Bahamut.dialogs.DialogSearchArticle
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
import com.kota.Bahamut.listPage.ListStateStore.Companion.instance
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetPage
import java.util.Vector

open class BookmarkManagePage(
    aBoardName: String,
    private val boardExtendOptionalPageListener: BoardExtendOptionalPageListener?
) : TelnetPage(), DialogSearchArticleListener {

    var boardName: String = aBoardName
    val bookmarks = mutableStateListOf<Bookmark>()
    var bookmarkStore: BookmarkStore? = TempSettings.bookmarkStore

    // Compose state
    var currentMode by mutableIntStateOf(0) // 0=書籤 1=紀錄
    private var editBookmarkIndex = -1

    override val pageLayout: Int
        get() = 0
    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOOKMARK

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                BookmarkManagePageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    private fun reloadList() {
        bookmarks.clear()
        val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
        val temp = mutableListOf<Bookmark>()
        if (currentMode == 1) bookmarkList?.loadHistoryList(temp)
        else bookmarkList?.loadBookmarkList(temp)
        bookmarks.addAll(temp)
    }

    private fun switchToBookmark() {
        currentMode = 0
        reloadList()
    }

    private fun switchToHistory() {
        currentMode = 1
        reloadList()
    }

    fun onItemClick(bookmark: Bookmark) {
        val page = PageContainer.instance!!.boardSearchPage
        page.clear()
        page.listName = boardName
        page.boardManager = "文章搜尋"
        page.refreshHeaderView()
        instance.getState(page.getListIdFromListName(boardName)).let { state ->
            state.top = 0
            state.position = 0
        }
        page.setKeyword(bookmark.keyword)
        page.setAuthor(bookmark.author)
        page.setMark(bookmark.mark)
        page.setGy(bookmark.gy)
        val controllers = navigationController.viewControllers
        controllers.removeAt(controllers.size - 1)
        controllers.add(page)
        navigationController.setViewControllers(controllers, true)
        boardExtendOptionalPageListener?.onBoardExtendOptionalPageDidSelectBookmark(bookmark)
    }

    fun onEditClick(position: Int) {
        if (propertiesVIP) {
            editBookmarkIndex = position
            showSearchArticleDialog()
        } else {
            showShortToast(getContextString(R.string.vip_only_message))
        }
    }

    fun onDeleteClick(position: Int) {
        if (position !in bookmarks.indices) return
        val bookmark = bookmarks[position]
        createDialog()
            .setTitle(getContextString(R.string.delete) + getContextString(R.string.bookmark))
            .setMessage(getContextString(R.string.delete_this_bookmark) + "\n\"" + bookmark.title + "\"")
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.delete))
            .setListener { _, index ->
                if (index == 1) {
                    if (currentMode == 0) bookmarkStore?.getBookmarkList(boardName)?.removeBookmark(position)
                    else bookmarkStore?.getBookmarkList(boardName)?.removeHistoryBookmark(position)
                    bookmarkStore?.store()
                    reloadList()
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    fun moveBookmark(from: Int, to: Int) {
        if (currentMode == 0 && propertiesVIP && from in bookmarks.indices && to in bookmarks.indices) {
            val item = bookmarks.removeAt(from)
            bookmarks.add(to, item)
            val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
            bookmarkList?.clear()
            for (b in bookmarks) { bookmarkList?.addBookmark(b) }
            bookmarkStore?.store()
        }
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        return true
    }

    private fun showSearchArticleDialog() {
        if (editBookmarkIndex in bookmarks.indices) {
            val bookmark = bookmarks[editBookmarkIndex]
            val searchOptions = Vector<String?>().apply {
                add(bookmark.keyword); add(bookmark.author); add(bookmark.mark); add(bookmark.gy)
            }
            DialogSearchArticle().apply {
                setListener(this@BookmarkManagePage)
                editContent(searchOptions)
                show()
            }
        }
    }

    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String>) {
        if (editBookmarkIndex in bookmarks.indices) {
            val bookmark = bookmarks[editBookmarkIndex]
            bookmark.keyword = vector[0]; bookmark.author = vector[1]
            bookmark.mark = if (vector[2] == "YES") "y" else "n"
            bookmark.gy = vector[3]; bookmark.title = bookmark.generateTitle()
            bookmarkStore?.getBookmarkList(boardName)?.updateBookmark(editBookmarkIndex, bookmark)
            bookmarkStore?.store()
            reloadList()
        }
        editBookmarkIndex = -1
    }

    override fun onSearchDialogCancelButtonClicked() {
        editBookmarkIndex = -1
    }

    // ---------------------------------------------------------------
    // Compose UI
    // ---------------------------------------------------------------

    @Composable
    fun BookmarkManagePageContent() {
        val colors = AppTheme.colors

        // Initialize on first composition
        LaunchedEffect(Unit) {
            if (bookmarks.isEmpty()) {
                switchToBookmark()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 1. 頂部標題列
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.titleBarBackground)
                    .padding(start = 10.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            ) {
                BahaText(
                    text = if (currentMode == 0) "我的書籤" else "瀏覽紀錄",
                    color = colors.titleBarTitle,
                    fontSize = BahaTextSize.TITLE
                )
                BahaText(
                    text = boardName,
                    color = colors.titleBarDetail,
                    fontSize = BahaTextSize.BODY
                )
            }

            // Content list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        BahaText(
                            text = if (currentMode == 0) "沒有書籤" else "沒有瀏覽紀錄",
                            color = colors.textSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = bookmarks.size,
                            key = { index -> "${currentMode}_${index}_${bookmarks[index].title}_${bookmarks[index].keyword}" }
                        ) { index ->
                            val bookmark = bookmarks[index]
                            if (currentMode == 0) {
                                BookmarkRowItem(
                                    bookmark = bookmark,
                                    isVip = propertiesVIP,
                                    canMoveUp = index > 0,
                                    canMoveDown = index < bookmarks.size - 1,
                                    onClick = { onItemClick(bookmark) },
                                    onEdit = { onEditClick(index) },
                                    onDelete = { onDeleteClick(index) },
                                    onMoveUp = { moveBookmark(index, index - 1) },
                                    onMoveDown = { moveBookmark(index, index + 1) }
                                )
                            } else {
                                HistoryRowItem(
                                    bookmark = bookmark,
                                    onClick = { onItemClick(bookmark) },
                                    onDelete = { onDeleteClick(index) }
                                )
                            }
                            HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // Tab 工具列 (書籤 / 紀錄 / 零字) (滿版無縫 50dp)
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
                    text = stringResource(R.string.bookmark),
                    isSelected = currentMode == 0,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { switchToBookmark() }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.record),
                    isSelected = currentMode == 1,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { switchToHistory() }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.zero_word),
                    isSelected = false,
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {}
                )
            }
        }
    }

    @Composable
    private fun BookmarkRowItem(
        bookmark: Bookmark,
        isVip: Boolean,
        canMoveUp: Boolean,
        canMoveDown: Boolean,
        onClick: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onMoveUp: () -> Unit,
        onMoveDown: () -> Unit
    ) {
        val colors = AppTheme.colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.pageBackground)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                BahaText(
                    text = bookmark.keyword.ifEmpty { stringResource(R.string.un_input) },
                    color = colors.textPrimary,
                    fontSize = BahaTextSize.TITLE,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.author_),
                        color = colors.textSecondary,
                        fontSize = BahaTextSize.TINY
                    )
                    BahaText(
                        text = bookmark.author.ifEmpty { stringResource(R.string.un_input) },
                        color = colors.bbsMailAuthor,
                        fontSize = BahaTextSize.TINY,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (bookmark.mark == "y") {
                        Spacer(modifier = Modifier.width(6.dp))
                        BahaText(
                            text = stringResource(R.string.word_m),
                            color = colors.bbsMailMark,
                            fontSize = BahaTextSize.TINY
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    BahaText(
                        text = stringResource(R.string.gy_),
                        color = colors.textSecondary,
                        fontSize = BahaTextSize.TINY
                    )
                    BahaText(
                        text = if (bookmark.gy.isEmpty()) stringResource(R.string.number_0) else bookmark.gy,
                        color = colors.bbsBoardGy,
                        fontSize = BahaTextSize.TINY
                    )
                }
            }

            // 右側按鈕區塊
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isVip) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (canMoveUp) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (canMoveDown) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f)
                        )
                    }
                }
                BahaButton(
                    text = stringResource(R.string.edit_short),
                    type = ButtonType.NORMAL,
                    fontSize = BahaTextSize.BODY,
                    modifier = Modifier.height(34.dp),
                    onClick = onEdit
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(24.dp)
                        .background(colors.divider)
                )
                BahaButton(
                    text = stringResource(R.string.delete_short),
                    type = ButtonType.DANGER,
                    fontSize = BahaTextSize.BODY,
                    modifier = Modifier.height(34.dp),
                    onClick = onDelete
                )
            }
        }
    }

    @Composable
    private fun HistoryRowItem(
        bookmark: Bookmark,
        onClick: () -> Unit,
        onDelete: () -> Unit
    ) {
        val colors = AppTheme.colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.pageBackground)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BahaText(
                text = bookmark.keyword.ifEmpty { stringResource(R.string.un_input) },
                color = colors.textPrimary,
                fontSize = BahaTextSize.TITLE,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            BahaButton(
                text = stringResource(R.string.delete_short),
                type = ButtonType.DANGER,
                modifier = Modifier.height(34.dp),
                onClick = onDelete
            )
        }
    }
}
