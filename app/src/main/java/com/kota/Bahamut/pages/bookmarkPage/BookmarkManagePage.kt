package com.kota.Bahamut.pages.bookmarkPage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.dataModels.BookmarkStore
import com.kota.Bahamut.dialogs.DialogSearchArticle
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
import com.kota.Bahamut.listPage.ListStateStore.Companion.instance
import com.kota.Bahamut.listPage.TelnetListPage2
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetHeaderItemView
import java.util.Collections
import java.util.Vector

open class BookmarkManagePage(
    aBoardName: String,
    private val boardExtendOptionalPageListener: BoardExtendOptionalPageListener?
) : TelnetListPage2(), BookmarkClickListener, DialogSearchArticleListener {

    var boardName: String = aBoardName
    private val bookmarks: MutableList<Bookmark> = Vector()
    protected var headerItemView: TelnetHeaderItemView? = null
    lateinit var bookmarkButton: Button
    lateinit var historyButton: Button
    lateinit var waterBallButton: Button
    private lateinit var tabButtons: Array<Button>
    private var currentMode = 0
    var bookmarkStore: BookmarkStore? = TempSettings.bookmarkStore
    var isReorderMode: Boolean = false

    override val pageLayout: Int
        get() = R.layout.bookmark_manage_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOOKMARK

    override val isAutoLoadEnable: Boolean
        get() = false

    override fun loadPage(): TelnetListPageBlock? = null

    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {}

    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {}

    override fun getCount(): Int = bookmarks.size

    override fun onPageDidLoad() {
        super.onPageDidLoad()

        val recyclerView = findViewById(R.id.recycleView) as RecyclerView
        bindRecyclerView(recyclerView)

        headerItemView = findViewById(R.id.BoardExtendOptionalPage_headerView) as TelnetHeaderItemView?
        headerItemView?.setMenuButtonClickListener {
            showMenu()
        }

        bookmarkButton = findViewById(R.id.BoardExtendOptionalPage_bookmarkButton) as Button
        historyButton = findViewById(R.id.BoardExtendOptionalPage_historyButton) as Button
        waterBallButton = findViewById(R.id.BoardExtendOptionalPage_waterBallButton) as Button

        listOf(bookmarkButton, historyButton, waterBallButton).forEach { it.setOnClickListener(buttonClickListener) }
        tabButtons = arrayOf(bookmarkButton, historyButton, waterBallButton)

        if (currentMode == 0) bookmarkButton.performClick()
        else historyButton.performClick()
    }

    private fun showMenu() {
        ASListDialog.createDialog()
            .addItem(getContextString(R.string.drag_reorder))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    if (index == 0) {
                        isReorderMode = !isReorderMode
                        safeNotifyDataSetChanged()
                    }
                }

                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean = false
            })
            .scheduleDismissOnPageDisappear(this)
            .show()
    }

    private fun reloadList() {
        val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
        if (currentMode == 1) bookmarkList?.loadHistoryList(bookmarks)
        else bookmarkList?.loadBookmarkList(bookmarks)
    }

    var buttonClickListener: View.OnClickListener = View.OnClickListener { aView ->
        when (aView) {
            bookmarkButton -> {
                headerItemView?.setData("我的書籤", boardName, "長按可刪除項目")
                currentMode = 0
                reloadList()
                safeNotifyDataSetChanged()
            }
            historyButton -> {
                headerItemView?.setData("瀏覽紀錄", boardName, "長按可刪除項目")
                currentMode = 1
                reloadList()
                safeNotifyDataSetChanged()
            }
        }

        val selectedBgRes = CommonFunctions.getThemeResourceId(R.attr.bahamut_tabSelectedBackground)
        val unselectedBgRes = CommonFunctions.getThemeResourceId(R.attr.bahamut_tabUnselectedBackground)
        val selectedTextRes = CommonFunctions.getThemeResourceId(R.attr.bahamut_tabSelectedTextColor)
        val unselectedTextRes = CommonFunctions.getThemeResourceId(R.attr.bahamut_tabUnselectedTextColor)

        tabButtons.forEach { btn ->
            val isSelected = (btn === aView)
            btn.setBackgroundResource(if (isSelected) selectedBgRes else unselectedBgRes)
            btn.setTextColor(ContextCompat.getColorStateList(btn.context, if (isSelected) selectedTextRes else unselectedTextRes))
        }
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        val bookmark = bookmarks.getOrNull(i)
        var itemView = view

        if (currentMode == 0) {
            if (itemView == null || itemView.findViewById<View>(R.id.BoardExtendOptionalPage_bookmarkItemView_Title) == null) {
                itemView = LayoutInflater.from(context).inflate(
                    R.layout.board_extend_optional_page_bookmark_item_view,
                    viewGroup,
                    false
                )
            }
            bindBookmarkView(itemView, bookmark, i)
        } else {
            if (itemView == null || itemView.findViewById<View>(R.id.BoardExtendOptionalPage_historyItemView_Title) == null) {
                itemView = LayoutInflater.from(context).inflate(
                    R.layout.board_extend_optional_page_history_item_view,
                    viewGroup,
                    false
                )
            }
            bindHistoryView(itemView, bookmark, i)
        }

        return itemView
    }

    private fun bindBookmarkView(itemView: View, bookmark: Bookmark?, position: Int) {
        val titleLabel = itemView.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_Title)
        val authorLabel = itemView.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_Author)
        val markLabel = itemView.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark)
        val gyLabel = itemView.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_GY)
        val buttonBlock = itemView.findViewById<View>(R.id.BoardExtendOptionalPage_bookmarkItemView_ButtonBlock)
        val btnEdit = itemView.findViewById<Button>(R.id.BoardExtendOptionalPage_bookmarkItemView_Edit)
        val dragHandle = itemView.findViewById<View>(R.id.ClassPage_ItemView_DragHandle)

        if (bookmark != null) {
            titleLabel?.text = if (bookmark.keyword.isNullOrEmpty()) "未輸入" else bookmark.keyword
            authorLabel?.text = if (bookmark.author.isNullOrEmpty()) "未輸入" else bookmark.author
            markLabel?.visibility = if (bookmark.mark == "y") View.VISIBLE else View.INVISIBLE
            gyLabel?.text = if (bookmark.gy.isNullOrEmpty()) Bookmark.OPTIONAL_BOOKMARK else bookmark.gy
        } else {
            titleLabel?.text = "未輸入"
            authorLabel?.text = "未輸入"
            markLabel?.visibility = View.INVISIBLE
            gyLabel?.text = Bookmark.OPTIONAL_BOOKMARK
        }

        if (isReorderMode) {
            dragHandle?.visibility = View.VISIBLE
            buttonBlock?.visibility = View.GONE
        } else {
            dragHandle?.visibility = View.GONE
            buttonBlock?.visibility = View.VISIBLE
        }

        btnEdit?.setOnClickListener { onEditClick(itemView, position) }
    }

    private fun bindHistoryView(itemView: View, bookmark: Bookmark?, position: Int) {
        val titleLabel = itemView.findViewById<TextView>(R.id.BoardExtendOptionalPage_historyItemView_Title)
        val dragHandle = itemView.findViewById<View>(R.id.ClassPage_ItemView_DragHandle)

        if (bookmark != null) {
            titleLabel?.text = if (bookmark.keyword.isNullOrEmpty()) "未輸入" else bookmark.keyword
        } else {
            titleLabel?.text = "未輸入"
        }

        if (isReorderMode) {
            dragHandle?.visibility = View.VISIBLE
        } else {
            dragHandle?.visibility = View.GONE
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition in bookmarks.indices && toPosition in bookmarks.indices) {
            Collections.swap(bookmarks, fromPosition, toPosition)
            recyclerViewAdapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }
        return false
    }

    override fun onItemDrop(fromPosition: Int, toPosition: Int) {
        val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
        if (currentMode == 0) {
            bookmarkList?.setBookmarks(bookmarks)
        } else {
            bookmarkList?.setHistoryBookmarks(bookmarks)
        }
        bookmarkStore?.store()
    }

    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        if (index in bookmarks.indices) {
            onDeleteClick(itemView, index)
            return true
        }
        return false
    }

    override fun onItemClick(view: View?, position: Int) {
        val bookmark = bookmarks.getOrNull(position) ?: return
        val page = PageContainer.instance!!.boardSearchPage
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
        val bookmark = bookmarks.getOrNull(position) ?: return
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
                    safeNotifyDataSetChanged()
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        return true
    }

    private var editBookmarkIndex = -1

    private fun showSearchArticleDialog() {
        if (editBookmarkIndex > -1) {
            val bookmark = bookmarks.getOrNull(editBookmarkIndex) ?: return
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
        val bookmark = bookmarks.getOrNull(editBookmarkIndex) ?: return
        bookmark.keyword = vector[0]; bookmark.author = vector[1]
        bookmark.mark = if (vector[2] == "YES") "y" else "n"
        bookmark.gy = vector[3]; bookmark.title = bookmark.generateTitle()
        bookmarkStore?.getBookmarkList(boardName)?.updateBookmark(editBookmarkIndex, bookmark)
        bookmarkStore?.store()
        reloadList()
        safeNotifyDataSetChanged()
        editBookmarkIndex = -1
    }

    override fun onSearchDialogCancelButtonClicked() {
        safeNotifyDataSetChanged()
        editBookmarkIndex = -1
    }

    override fun clear() {
        super.clear()
        isReorderMode = false
    }
}
