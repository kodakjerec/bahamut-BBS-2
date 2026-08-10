package com.kota.Bahamut.pages.bookmarkPage

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.pages.theme.ThemeStore.getSelectTheme
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.rgbToInt
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetHeaderItemView
import com.kota.telnetUI.TelnetPage
import java.util.Collections
import java.util.Vector

open class BookmarkManagePage(
    aBoardName: String,
    private val boardExtendOptionalPageListener: BoardExtendOptionalPageListener?
) : TelnetPage(), BookmarkClickListener, DialogSearchArticleListener {
    var boardName: String = aBoardName
    private val bookmarks: MutableList<Bookmark> = Vector()
    protected var headerItemView: TelnetHeaderItemView? = null
    lateinit var bookmarkButton: Button
    lateinit var historyButton: Button
    lateinit var waterBallButton: Button
    private lateinit var tabButtons: Array<Button>
    private var currentMode = 0
    var bookmarkAdapter: BookmarkAdapter? = null
    var historyAdapter: HistoryAdapter? = null
    var bookmarkStore: BookmarkStore? = TempSettings.bookmarkStore
    private var isUnderRecycleView = false

    override val pageLayout: Int
        get() = R.layout.bookmark_manage_page
    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOOKMARK

    var itemTouchHelper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        var start: Int = -1
        var end: Int = -1
        private var dragView: View? = null

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            start = viewHolder.bindingAdapterPosition
            end = target.bindingAdapterPosition
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
                        if (viewHolder != null) { 
                            dragView = viewHolder.itemView
                            dragView?.setBackgroundResource(R.color.ripple_material)
                        }
                    }
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        if (dragView != null) {
                            dragView?.setBackgroundResource(R.color.transparent)
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

    override fun onPageDidLoad() {
        val recyclerView = findViewById(R.id.recycleView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        headerItemView = findViewById(R.id.BoardExtendOptionalPage_headerView) as TelnetHeaderItemView?
        bookmarkButton = findViewById(R.id.BoardExtendOptionalPage_bookmarkButton) as Button
        historyButton = findViewById(R.id.BoardExtendOptionalPage_historyButton) as Button
        waterBallButton = findViewById(R.id.BoardExtendOptionalPage_waterBallButton) as Button
        
        listOf(bookmarkButton, historyButton, waterBallButton).forEach { it.setOnClickListener(buttonClickListener) }
        tabButtons = arrayOf(bookmarkButton, historyButton, waterBallButton)

        if (currentMode == 0) bookmarkButton.performClick()
        else historyButton.performClick()
    }

    private fun reloadList() {
        val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
        if (currentMode == 1) bookmarkList?.loadHistoryList(bookmarks)
        else bookmarkList?.loadBookmarkList(bookmarks)

        val mainLayout: ViewGroup = findViewById(R.id.content_view) as ViewGroup
        mainLayout.post { ThemeFunctions().applyThemeToContent(mainLayout) }
    }

    var buttonClickListener: View.OnClickListener = View.OnClickListener { aView ->
        val recyclerView = findViewById(R.id.recycleView) as RecyclerView?
        when(aView) {
            bookmarkButton -> {
                headerItemView?.setData("我的書籤", boardName, "長按可移動位置")
                currentMode = 0
                reloadList()
                bookmarkAdapter = BookmarkAdapter(bookmarks)
                recyclerView?.adapter = bookmarkAdapter
                bookmarkAdapter?.setOnItemClickListener(this)
            }
            historyButton -> {
                headerItemView?.setData("瀏覽紀錄", boardName, "")
                currentMode = 1
                reloadList()
                historyAdapter = HistoryAdapter(bookmarks)
                recyclerView?.adapter = historyAdapter
                historyAdapter?.setOnItemClickListener(this)
            }
        }

        val theme = getSelectTheme()
        tabButtons.forEach { btn ->
            if (btn === aView) {
                btn.setTextColor(rgbToInt(theme.textColor)); btn.setBackgroundColor(rgbToInt(theme.backgroundColor))
            } else {
                btn.setTextColor(rgbToInt(theme.textColorDisabled)); btn.setBackgroundColor(rgbToInt(theme.backgroundColorDisabled))
            }
        }
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

    @SuppressLint("NotifyDataSetChanged")
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

    private var editBookmarkIndex = -1

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
}
