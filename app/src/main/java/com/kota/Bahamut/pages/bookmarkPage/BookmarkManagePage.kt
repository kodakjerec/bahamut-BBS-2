package com.kota.Bahamut.pages.bookmarkPage

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.dataModels.BookmarkStore
import com.kota.Bahamut.dialogs.DialogSearchArticle
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
import com.kota.Bahamut.listPage.ListStateStore.Companion.instance
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetPage
import java.util.Collections
import java.util.Vector

open class BookmarkManagePage(
    aBoardName: String,
    private val boardExtendOptionalPageListener: BoardExtendOptionalPageListener?
) : TelnetPage(), BookmarkClickListener, DialogSearchArticleListener {

    var boardName: String = aBoardName
    private val bookmarks: MutableList<Bookmark> = Vector()
    var bookmarkStore: BookmarkStore? = TempSettings.bookmarkStore

    // Compose state
    var currentMode by mutableIntStateOf(0) // 0=書籤 1=紀錄
    private var isUnderRecycleView = false

    private var bookmarkAdapter: BookmarkAdapter? = null
    private var historyAdapter: HistoryAdapter? = null
    private var recyclerViewRef: RecyclerView? = null
    private var editBookmarkIndex = -1

    override val pageLayout: Int
        get() = 0
    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOOKMARK

    val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        private var dragView: View? = null

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val start = viewHolder.bindingAdapterPosition
            val end = target.bindingAdapterPosition
            if (this@BookmarkManagePage.currentMode == 0 && propertiesVIP) {
                Collections.swap(bookmarks, start, end)
                bookmarkAdapter?.notifyItemMoved(start, end)
            }
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            isUnderRecycleView = actionState != ItemTouchHelper.ACTION_STATE_IDLE
            if (this@BookmarkManagePage.currentMode == 0) {
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {
                        dragView = viewHolder?.itemView
                        dragView?.setBackgroundResource(R.color.ripple_material)
                    }
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        dragView?.let {
                            it.setBackgroundResource(R.color.transparent)
                            dragView = null
                            val bookmarkList = bookmarkStore?.getBookmarkList(this@BookmarkManagePage.boardName)
                            bookmarkList?.clear()
                            for (bookmark in bookmarks) { bookmarkList?.addBookmark(bookmark) }
                            bookmarkStore?.store()
                        }
                    }
                }
            }
        }
    })

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                BookmarkManagePageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    private fun reloadList() {
        val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
        if (currentMode == 1) bookmarkList?.loadHistoryList(bookmarks)
        else bookmarkList?.loadBookmarkList(bookmarks)
    }

    private fun switchToBookmark() {
        currentMode = 0
        reloadList()
        bookmarkAdapter = BookmarkAdapter(bookmarks).also {
            it.setOnItemClickListener(this)
        }
        recyclerViewRef?.adapter = bookmarkAdapter
    }

    private fun switchToHistory() {
        currentMode = 1
        reloadList()
        historyAdapter = HistoryAdapter(bookmarks).also {
            it.setOnItemClickListener(this)
        }
        recyclerViewRef?.adapter = historyAdapter
    }

    override fun onItemClick(view: View?, position: Int) {
        val bookmark = if (currentMode == 0) bookmarkAdapter?.getItem(position) else historyAdapter?.getItem(position)
        val page = PageContainer.instance!!.boardSearchPage
        if (bookmark == null) return
        page.clear()
        instance.getState(page.getListIdFromListName(boardName)).let { state ->
            state.top = 0
            state.position = 0
        }
        page.setKeyword(bookmark.keyword); page.setAuthor(bookmark.author); page.setMark(bookmark.mark); page.setGy(bookmark.gy)
        val controllers = navigationController.viewControllers
        controllers.removeAt(controllers.size - 1)
        controllers.add(page)
        navigationController.setViewControllers(controllers, true)
        boardExtendOptionalPageListener?.onBoardExtendOptionalPageDidSelectBookmark(bookmark)
    }

    override fun onEditClick(view: View?, position: Int) {
        if (propertiesVIP) {
            editBookmarkIndex = position
            showSearchArticleDialog()
        } else {
            showShortToast(getContextString(R.string.vip_only_message))
        }
    }

    override fun onDeleteClick(view: View?, position: Int) {
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
                    if (currentMode == 0) bookmarkAdapter?.notifyDataSetChanged()
                    else historyAdapter?.notifyDataSetChanged()
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    override fun onReceivedGestureRight(): Boolean {
        if (!isUnderRecycleView) { onBackPressed(); return true }
        return false
    }

    private fun showSearchArticleDialog() {
        if (editBookmarkIndex > -1) {
            val bookmark = bookmarkAdapter?.getItem(editBookmarkIndex) ?: return
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
        val bookmark = bookmarkAdapter?.getItem(editBookmarkIndex) ?: return
        bookmark.keyword = vector[0]; bookmark.author = vector[1]
        bookmark.mark = if (vector[2] == "YES") "y" else "n"
        bookmark.gy = vector[3]; bookmark.title = bookmark.generateTitle()
        bookmarkStore?.getBookmarkList(boardName)?.updateBookmark(editBookmarkIndex, bookmark)
        bookmarkStore?.store()
        reloadList()
        bookmarkAdapter?.notifyItemChanged(editBookmarkIndex)
        editBookmarkIndex = -1
    }

    override fun onSearchDialogCancelButtonClicked() {
        bookmarkAdapter?.notifyItemChanged(editBookmarkIndex)
        editBookmarkIndex = -1
    }

    // ---------------------------------------------------------------
    // Compose UI
    // ---------------------------------------------------------------

    @Composable
    fun BookmarkManagePageContent() {
        val colors = AppTheme.colors

        // Initialize on first composition
        if (bookmarkAdapter == null) {
            switchToBookmark()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // Header row showing board name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.toolbarBackground)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (currentMode == 0) "我的書籤" else "瀏覽紀錄",
                    color = colors.titleBarTitle,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = boardName,
                    color = colors.titleBarDetail
                )
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // RecyclerView
            AndroidView(
                factory = { ctx ->
                    RecyclerView(ctx).apply {
                        layoutManager = LinearLayoutManager(ctx)
                        adapter = bookmarkAdapter
                        itemTouchHelper.attachToRecyclerView(this)
                        recyclerViewRef = this
                    }
                },
                update = { rv ->
                    recyclerViewRef = rv
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

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
}
