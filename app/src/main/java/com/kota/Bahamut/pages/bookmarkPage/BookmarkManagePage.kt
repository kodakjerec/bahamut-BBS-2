package com.kota.Bahamut.pages.bookmarkPage

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
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
import com.kota.Bahamut.pages.theme.ThemeStore.getSelectTheme
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.rgbToInt
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetHeaderItemView
import com.kota.telnetUI.TelnetPage
import java.util.Collections
import java.util.Vector
import kotlin.math.abs

open class BookmarkManagePage(
    aBoardName: String?,
    private val boardExtendOptionalPageListener: BoardExtendOptionalPageListener?
) : TelnetPage(), BookmarkClickListener, DialogSearchArticleListener {
    var boardName: String? = null
    private val bookmarks: MutableList<Bookmark> = MutableList<Bookmark>()
    protected var headerItemView: TelnetHeaderItemView? = null
    private var selectedButton: Button? = null
    private var bookmarkButton: Button? = null
    private var historyButton: Button? = null
    private var waterBallButton: Button? = null
    private lateinit var tabButtons: Array<Button>
    private var currentMode = 0
    var bookmarkAdapter: BookmarkAdapter? = null
    var historyAdapter: HistoryAdapter? = null
    var bookmarkStore: BookmarkStore? = TempSettings.bookmarkStore
    private var isUnderRecycleView = false
    private var scale: Float? = 0f
    override val pageLayout: Int
        get() = R.layout.bookmark_manage_page
    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOOKMARK

    var itemTouchHelper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        var isSwiped: Boolean = false
        var isDragged: Boolean = false
        var start: Int = -1
        var end: Int = -1
        private var dragView: View? = null

        // 上下移動
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            start = viewHolder.adapterPosition
            end = target.adapterPosition
            if (this@BookmarkManagePage.currentMode == 0) {
                if (propertiesVIP) {
                    Collections.swap(bookmarks, start, end)
                    bookmarkAdapter?.notifyItemMoved(start, end)
                } else {
                    showShortToast(getContextString(R.string.vip_only_message))
                }
            }
            return true
        }

        // 左右移動
        @SuppressLint("NotifyDataSetChanged")
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val bookmarkIndex = viewHolder.adapterPosition
            if (this@BookmarkManagePage.currentMode == 0) {
                if (direction == ItemTouchHelper.LEFT) {
                    // 左滑刪除
                    createDialog()
                        .setTitle(getContextString(R.string.delete) + getContextString(R.string.bookmark))
                        .setMessage(
                            getContextString(R.string.delete_this_bookmark) + "\n\"" + this@BookmarkManagePage.bookmarkAdapter?.getItem(
                                bookmarkIndex
                            )?.title + "\""
                        )
                        .addButton(getContextString(R.string.cancel))
                        .addButton(getContextString(R.string.delete))
                        .setListener { aDialog: ASAlertDialog?, index1: Int ->
                            if (index1 == 1) {
                                bookmarkStore?.getBookmarkList(this@BookmarkManagePage.boardName)
                                    ?.removeBookmark(bookmarkIndex)
                                bookmarkStore?.store()
                                reloadList()
                                bookmarkAdapter?.notifyDataSetChanged()
                            } else {
                                // 還原
                                bookmarkAdapter?.notifyItemChanged(viewHolder.adapterPosition)
                            }
                        }
                        .scheduleDismissOnPageDisappear(this@BookmarkManagePage).show()
                } else if (direction == ItemTouchHelper.RIGHT) {
                    if (propertiesVIP) {
                        // 右滑修改
                        editBookmarkIndex = bookmarkIndex
                        showSearchArticleDialog()
                    } else {
                        showShortToast(getContextString(R.string.vip_only_message))
                        // 還原
                        bookmarkAdapter?.notifyItemChanged(viewHolder.adapterPosition)
                    }
                }
            } else if (this@BookmarkManagePage.currentMode == 1) {
                if (direction == ItemTouchHelper.LEFT) {
                    bookmarkStore?.getBookmarkList(this@BookmarkManagePage.boardName)
                        .removeHistoryBookmark(bookmarkIndex)
                    bookmarkStore?.store()
                    reloadList()
                    historyAdapter?.notifyDataSetChanged()
                } else {
                    // 還原
                    historyAdapter?.notifyItemChanged(viewHolder.adapterPosition)
                }
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            isUnderRecycleView = true
            if (this@BookmarkManagePage.currentMode == 0) {
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {
                        // the user is dragging an item and didn't lift their finger off yet
                        isSwiped = false
                        isDragged = true
                        if (viewHolder != null) { // 選取變色
                            dragView = viewHolder.itemView
                            dragView?.setBackgroundResource(R.color.ripple_material)
                        }
                    }

                    ItemTouchHelper.ACTION_STATE_SWIPE -> {
                        // the user is swiping an item and didn't lift their finger off yet
                        isSwiped = true
                        isDragged = false
                    }

                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        // the user just dropped the item (after dragging it), and lift their finger off.
                        //
                        if (isSwiped) { // The user used onSwiped()
                            Log.e("swipe", "swipe is over")
                        }
                        if (!isSwiped && isDragged) { // The user used onMove()
                            if (dragView != null) { // 解除 選取變色
                                dragView?.setBackgroundResource(R.color.transparent)
                                dragView = null
                            }
                            val bookmarkList =
                                bookmarkStore?.getBookmarkList(this@BookmarkManagePage.boardName)
                            bookmarkList?.clear()
                            for (bookmark in bookmarks) {
                                if (bookmark.index == start) bookmark.index = end
                                else if (bookmark.index == end) bookmark.index = start
                                bookmarkList?.addBookmark(bookmark)
                            }
                            bookmarkStore?.store()
                        }
                        isSwiped = false
                        isDragged = false
                        isUnderRecycleView = false
                    }
                }
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            val fontWidth = 18 * scale
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val offset = abs(dX)
                val right = viewHolder.itemView.right
                val left = viewHolder.itemView.left
                val top = viewHolder.itemView.top
                if (dX < 0) {
                    // Draw button at the right edge of the item
                    val paint = Paint()
                    paint.color = getContextColor(R.color.tab_item_text_color_selected)
                    paint.textSize = fontWidth
                    // Calculate the top-left corner of the item
                    val x = right - offset
                    val y = top + fontWidth * 2

                    // Draw the text line by line
                    val lines: Array<String?> =
                        "◀左滑刪除".split("".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    for (i in lines.indices) {
                        c.drawText(lines[i]!!, x + i * (paint.textSize + 10), y, paint)
                    }
                } else {
                    // Draw button at the right edge of the item
                    val paint = Paint()
                    paint.color = getContextColor(R.color.tab_item_text_color_selected)
                    paint.textSize = fontWidth
                    // Calculate the top-left corner of the item
                    val x = offset - left - fontWidth
                    val y = top + fontWidth * 2

                    // Draw the text line by line
                    val lines: Array<String?> =
                        "▶右滑修改".split("".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    for (i in lines.indices) {
                        c.drawText(lines[i]!!, x - i * (paint.textSize + 10), y, paint)
                    }
                }
            }
        }
    })

    override fun onPageDidLoad() {
        reloadList()

        val recyclerView = findViewById(R.id.recycleView) as RecyclerView?
        recyclerView?.setLayoutManager(LinearLayoutManager(context))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        bookmarkAdapter = BookmarkAdapter(bookmarks)
        recyclerView.setAdapter(bookmarkAdapter)
        bookmarkAdapter?.setOnItemClickListener(this)

        headerItemView =
            findViewById(R.id.BoardExtendOptionalPage_headerView) as TelnetHeaderItemView?
        headerItemView?.setData("我的書籤", boardName, "左滑刪除,右滑修改")
        bookmarkButton = findViewById(R.id.BoardExtendOptionalPage_bookmarkButton) as Button?
        historyButton = findViewById(R.id.BoardExtendOptionalPage_historyButton) as Button?
        waterBallButton = findViewById(R.id.BoardExtendOptionalPage_waterBallButton) as Button?
        bookmarkButton?.setOnClickListener(buttonClickListener)
        historyButton?.setOnClickListener(buttonClickListener)
        waterBallButton?.setOnClickListener(buttonClickListener)
        selectedButton = bookmarkButton
        tabButtons = arrayOf<Button>(bookmarkButton!!, historyButton!!, waterBallButton!!)
        scale = resource?.displayMetrics?.scaledDensity

        if (currentMode == 0) bookmarkButton?.performClick()
        else historyButton?.performClick()
    }

    private fun reloadList() {
        val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
        if (currentMode == 1) {
            bookmarkList?.loadHistoryList(bookmarks)
        } else {
            bookmarkList?.loadBookmarkList(bookmarks)
        }
    }

    fun setBoardName(aBoardName: String?) {
        boardName = aBoardName
    }

    var buttonClickListener: View.OnClickListener = View.OnClickListener { aView ->
        val recyclerView = findViewById(R.id.recycleView) as RecyclerView?
        if (aView === this@BookmarkManagePage.bookmarkButton) {
            this@BookmarkManagePage.headerItemView?.setTitle("我的書籤")
            this@BookmarkManagePage.currentMode = 0
            reloadList()
            bookmarkAdapter = BookmarkAdapter(bookmarks)
            recyclerView?.setAdapter(bookmarkAdapter)
            bookmarkAdapter?.setOnItemClickListener(this@BookmarkManagePage)
        } else if (aView === this@BookmarkManagePage.waterBallButton) {
            this@BookmarkManagePage.headerItemView?.setTitle("訊息紀錄")
            this@BookmarkManagePage.currentMode = 2
            reloadList()
        } else if (aView === this@BookmarkManagePage.historyButton) {
            this@BookmarkManagePage.headerItemView?.setTitle("瀏覽紀錄")
            this@BookmarkManagePage.currentMode = 1
            reloadList()
            historyAdapter = HistoryAdapter(bookmarks)
            recyclerView?.setAdapter(historyAdapter)
            historyAdapter?.setOnItemClickListener(this@BookmarkManagePage)
        }

        // 切換頁籤
        val theme = getSelectTheme()
        for (tabButton in this@BookmarkManagePage.tabButtons) {
            if (tabButton === aView) {
                tabButton.setTextColor(rgbToInt(theme.textColor))
                tabButton.setBackgroundColor(rgbToInt(theme.backgroundColor))
            } else {
                tabButton.setTextColor(rgbToInt(theme.textColorDisabled))
                tabButton.setBackgroundColor(rgbToInt(theme.backgroundColorDisabled))
            }
        }
    }

    // 書籤管理->按下書籤
    // 實際動作由看板頁去實作
    override fun onItemClick(view: View?, position: Int) {
        val bookmark = this@BookmarkManagePage.bookmarkAdapter?.getItem(position)
        val page = PageContainer.instance?.boardSearchPage
        if (bookmark == null || page == null) return
        page.clear()
        val state =
            instance.getState(page.getListIdFromListName(this@BookmarkManagePage.boardName))
        state.top = 0
        state.position = 0
        page.setKeyword(bookmark.keyword)
        page.setAuthor(bookmark.author)
        page.setMark(bookmark.mark)
        page.setGy(bookmark.gy)
        val controllers = navigationController.viewControllers
        controllers.removeAt(controllers.size - 1)
        controllers.add(page)
        navigationController.setViewControllers(controllers, true)
        this@BookmarkManagePage.boardExtendOptionalPageListener?.onBoardExtendOptionalPageDidSelectBookmark(bookmark)
    }

    override fun onReceivedGestureRight(): Boolean {
        if (!isUnderRecycleView) {
            onBackPressed()
            return true
        }
        return false
    }

    private var editBookmarkIndex = -1

    init {
        setBoardName(aBoardName)
    }

    // 修改書籤
    private fun showSearchArticleDialog() {
        if (editBookmarkIndex > -1) {
            val bookmark = bookmarkAdapter?.getItem(editBookmarkIndex)
            if (bookmark == null) return
            val searchOptions = Vector<String?>()
            searchOptions.add(bookmark.keyword)
            searchOptions.add(bookmark.author)
            searchOptions.add(bookmark.mark)
            searchOptions.add(bookmark.gy)
            val dialogSearchArticle = DialogSearchArticle()
            dialogSearchArticle.setListener(this)
            dialogSearchArticle.editContent(searchOptions)
            dialogSearchArticle.show()
        }
    }

    // 搜尋文章完畢
    // 此處修改原本書籤內容
    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String?>) {
        val bookmark = bookmarkAdapter?.getItem(editBookmarkIndex)
        if (bookmark == null) return
        bookmark.keyword = vector[0]
        bookmark.author = vector[1]
        if (vector[2] == "YES") bookmark.mark = "y"
        else bookmark.mark = "n"
        bookmark.gy = vector[3]
        bookmark.title = bookmark.generateTitle()
        bookmarkStore?.getBookmarkList(boardName)?.updateBookmark(editBookmarkIndex, bookmark)
        bookmarkStore?.store()
        reloadList()
        // 還原
        bookmarkAdapter?.notifyItemChanged(editBookmarkIndex)
        editBookmarkIndex = -1
    }

    override fun onSearchDialogCancelButtonClicked() {
        // 還原
        bookmarkAdapter?.notifyItemChanged(editBookmarkIndex)
        editBookmarkIndex = -1
    }
}
