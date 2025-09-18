package com.kota.Bahamut.pages.bookmarkPage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.createDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.dataModels.BookmarkStore
import com.kota.Bahamut.dialogs.DialogSearchArticle
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
import com.kota.Bahamut.listPage.ListStateStore.Companion.instance
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.theme.ThemeStore.getSelectTheme
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.rgbToInt
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.telnetUI.TelnetHeaderItemView
import com.kota.telnetUI.TelnetPage
import java.util.Collections
import java.util.Vector
import kotlin.math.abs

class BookmarkManagePage(
    aBoardName: String?,
    private val _listener: BoardExtendOptionalPageListener?
) : TelnetPage(), BookmarkClickListener, DialogSearchArticleListener {
    var _board_name: String? = null
    private val _bookmarks: MutableList<Bookmark> = ArrayList<Bookmark>()
    protected var _header_view: TelnetHeaderItemView? = null
    private var _selected_button: Button? = null
    private var _bookmark_button: Button? = null
    private var _history_button: Button? = null
    private var _water_ball_button: Button? = null
    private var _tab_buttons: Array<Button>
    private var _mode = 0
    var bookmarkAdapter: BookmarkAdapter? = null
    var historyAdapter: HistoryAdapter? = null
    var _bookmarkStore: BookmarkStore? = TempSettings.bookmarkStore
    private var isUnderRecycleView = false
    private var scale = 0f
    val pageLayout: Int
        get() = R.layout.bookmark_manage_page
    val pageType: Int
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
            start = viewHolder.getAdapterPosition()
            end = target.getAdapterPosition()
            if (this@BookmarkManagePage._mode == 0) {
                if (propertiesVIP) {
                    Collections.swap(_bookmarks, start, end)
                    bookmarkAdapter!!.notifyItemMoved(start, end)
                } else {
                    showShortToast(getContextString(R.string.vip_only_message))
                }
            }
            return true
        }

        // 左右移動
        @SuppressLint("NotifyDataSetChanged")
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val bookmark_index = viewHolder.getAdapterPosition()
            if (this@BookmarkManagePage._mode == 0) {
                if (direction == ItemTouchHelper.LEFT) {
                    // 左滑刪除
                    createDialog()
                        .setTitle(getContextString(R.string.delete) + getContextString(R.string.bookmark))
                        .setMessage(
                            getContextString(R.string.delete_this_bookmark) + "\n\"" + this@BookmarkManagePage.bookmarkAdapter!!.getItem(
                                bookmark_index
                            ).title + "\""
                        )
                        .addButton(getContextString(R.string.cancel))
                        .addButton(getContextString(R.string.delete))
                        .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index1: Int ->
                            if (index1 == 1) {
                                _bookmarkStore!!.getBookmarkList(this@BookmarkManagePage._board_name)
                                    .removeBookmark(bookmark_index)
                                _bookmarkStore!!.store()
                                reloadList()
                                bookmarkAdapter!!.notifyDataSetChanged()
                            } else {
                                // 還原
                                bookmarkAdapter!!.notifyItemChanged(viewHolder.getAdapterPosition())
                            }
                        })
                        .scheduleDismissOnPageDisappear(this@BookmarkManagePage).show()
                } else if (direction == ItemTouchHelper.RIGHT) {
                    if (propertiesVIP) {
                        // 右滑修改
                        editBookmarkIndex = bookmark_index
                        showSearchArticleDialog()
                    } else {
                        showShortToast(getContextString(R.string.vip_only_message))
                        // 還原
                        bookmarkAdapter!!.notifyItemChanged(viewHolder.getAdapterPosition())
                    }
                }
            } else if (this@BookmarkManagePage._mode == 1) {
                if (direction == ItemTouchHelper.LEFT) {
                    _bookmarkStore!!.getBookmarkList(this@BookmarkManagePage._board_name)
                        .removeHistoryBookmark(bookmark_index)
                    _bookmarkStore!!.store()
                    reloadList()
                    historyAdapter!!.notifyDataSetChanged()
                } else {
                    // 還原
                    historyAdapter!!.notifyItemChanged(viewHolder.getAdapterPosition())
                }
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            isUnderRecycleView = true
            if (this@BookmarkManagePage._mode == 0) {
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {
                        // the user is dragging an item and didn't lift their finger off yet
                        isSwiped = false
                        isDragged = true
                        if (viewHolder != null) { // 選取變色
                            dragView = viewHolder.itemView
                            dragView!!.setBackgroundResource(R.color.ripple_material)
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
                                dragView!!.setBackgroundResource(R.color.transparent)
                                dragView = null
                            }
                            val bookmark_list =
                                _bookmarkStore!!.getBookmarkList(this@BookmarkManagePage._board_name)
                            bookmark_list.clear()
                            for (bookmark in _bookmarks) {
                                if (bookmark.index == start) bookmark.index = end
                                else if (bookmark.index == end) bookmark.index = start
                                bookmark_list.addBookmark(bookmark)
                            }
                            _bookmarkStore!!.store()
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
                val right = viewHolder.itemView.getRight()
                val left = viewHolder.itemView.getLeft()
                val top = viewHolder.itemView.getTop()
                if (dX < 0) {
                    // Draw button at the right edge of the item
                    val paint = Paint()
                    paint.setColor(getContextColor(R.color.tab_item_text_color_selected))
                    paint.setTextSize(fontWidth)
                    // Calculate the top-left corner of the item
                    val x = right - offset
                    val y = top + fontWidth * 2

                    // Draw the text line by line
                    val lines: Array<String?> =
                        "◀左滑刪除".split("".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    for (i in lines.indices) {
                        c.drawText(lines[i]!!, x + i * (paint.getTextSize() + 10), y, paint)
                    }
                } else {
                    // Draw button at the right edge of the item
                    val paint = Paint()
                    paint.setColor(getContextColor(R.color.tab_item_text_color_selected))
                    paint.setTextSize(fontWidth)
                    // Calculate the top-left corner of the item
                    val x = offset - left - fontWidth
                    val y = top + fontWidth * 2

                    // Draw the text line by line
                    val lines: Array<String?> =
                        "▶右滑修改".split("".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    for (i in lines.indices) {
                        c.drawText(lines[i]!!, x - i * (paint.getTextSize() + 10), y, paint)
                    }
                }
            }
        }
    })

    public override fun onPageDidLoad() {
        reloadList()

        val recyclerView = findViewById(R.id.recycleView) as RecyclerView?
        recyclerView!!.setLayoutManager(LinearLayoutManager(context))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        bookmarkAdapter = BookmarkAdapter(_bookmarks)
        recyclerView.setAdapter(bookmarkAdapter)
        bookmarkAdapter!!.setOnItemClickListener(this)

        _header_view =
            findViewById(R.id.BoardExtendOptionalPage_headerView) as TelnetHeaderItemView?
        _header_view!!.setData("我的書籤", _board_name, "左滑刪除,右滑修改")
        _bookmark_button = findViewById(R.id.BoardExtendOptionalPage_bookmarkButton) as Button?
        _history_button = findViewById(R.id.BoardExtendOptionalPage_historyButton) as Button?
        _water_ball_button = findViewById(R.id.BoardExtendOptionalPage_waterBallButton) as Button?
        _bookmark_button!!.setOnClickListener(buttonClickListener)
        _history_button!!.setOnClickListener(buttonClickListener)
        _water_ball_button!!.setOnClickListener(buttonClickListener)
        _selected_button = _bookmark_button
        _tab_buttons = arrayOf<Button>(_bookmark_button!!, _history_button!!, _water_ball_button!!)
        scale = resource.getDisplayMetrics().scaledDensity

        if (_mode == 0) _bookmark_button!!.performClick()
        else _history_button!!.performClick()
    }

    private fun reloadList() {
        val context: Context = context
        if (context != null) {
            val bookmark_list = _bookmarkStore!!.getBookmarkList(_board_name)
            if (_mode == 1) {
                bookmark_list.loadHistoryList(_bookmarks)
            } else {
                bookmark_list.loadBookmarkList(_bookmarks)
            }
        }
    }

    fun setBoardName(aBoardName: String?) {
        _board_name = aBoardName
    }

    var buttonClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(aView: View?) {
            val recyclerView = findViewById(R.id.recycleView) as RecyclerView?
            if (aView === this@BookmarkManagePage._bookmark_button) {
                this@BookmarkManagePage._header_view!!.setTitle("我的書籤")
                this@BookmarkManagePage._mode = 0
                reloadList()
                bookmarkAdapter = BookmarkAdapter(_bookmarks)
                recyclerView!!.setAdapter(bookmarkAdapter)
                bookmarkAdapter!!.setOnItemClickListener(this@BookmarkManagePage)
            } else if (aView === this@BookmarkManagePage._water_ball_button) {
                this@BookmarkManagePage._header_view!!.setTitle("訊息紀錄")
                this@BookmarkManagePage._mode = 2
                reloadList()
            } else if (aView === this@BookmarkManagePage._history_button) {
                this@BookmarkManagePage._header_view!!.setTitle("瀏覽紀錄")
                this@BookmarkManagePage._mode = 1
                reloadList()
                historyAdapter = HistoryAdapter(_bookmarks)
                recyclerView!!.setAdapter(historyAdapter)
                historyAdapter!!.setOnItemClickListener(this@BookmarkManagePage)
            }

            // 切換頁籤
            val theme = getSelectTheme()
            for (tab_button in this@BookmarkManagePage._tab_buttons) {
                if (tab_button === aView) {
                    tab_button.setTextColor(rgbToInt(theme.textColor))
                    tab_button.setBackgroundColor(rgbToInt(theme.backgroundColor))
                } else {
                    tab_button.setTextColor(rgbToInt(theme.textColorDisabled))
                    tab_button.setBackgroundColor(rgbToInt(theme.backgroundColorDisabled))
                }
            }
        }
    }

    // 書籤管理->按下書籤
    // 實際動作由看板頁去實作
    override fun onItemClick(view: View?, position: Int) {
        val bookmark = this@BookmarkManagePage.bookmarkAdapter!!.getItem(position)
        val page = PageContainer.getInstance().getBoardSearchPage()
        page.clear()
        val state =
            instance.getState(page.getListIdFromListName(this@BookmarkManagePage._board_name))
        if (state != null) {
            state.top = 0
            state.position = 0
        }
        page.setKeyword(bookmark.keyword)
        page.setAuthor(bookmark.author)
        page.setMark(bookmark.mark)
        page.setGy(bookmark.gy)
        val controllers = navigationController!!.viewControllers
        controllers.removeAt(controllers.size - 1)
        controllers.add(page)
        navigationController!!.setViewControllers(controllers, true)
        if (this@BookmarkManagePage._listener != null) {
            this@BookmarkManagePage._listener.onBoardExtendOptionalPageDidSelectBookmark(bookmark)
        }
    }

    public override fun onReceivedGestureRight(): Boolean {
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
            val bookmark = bookmarkAdapter!!.getItem(editBookmarkIndex)
            val search_options = Vector<String?>()
            search_options.add(bookmark.keyword)
            search_options.add(bookmark.author)
            search_options.add(bookmark.mark)
            search_options.add(bookmark.gy)
            val dialog_SearchArticle = DialogSearchArticle()
            dialog_SearchArticle.setListener(this)
            dialog_SearchArticle.editContent(search_options)
            dialog_SearchArticle.show()
        }
    }

    // 搜尋文章完畢
    // 此處修改原本書籤內容
    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String?>) {
        val bookmark = bookmarkAdapter!!.getItem(editBookmarkIndex)
        bookmark.keyword = vector.get(0)
        bookmark.author = vector.get(1)
        if (vector.get(2) == "YES") bookmark.mark = "y"
        else bookmark.mark = "n"
        bookmark.gy = vector.get(3)
        bookmark.title = bookmark.generateTitle()
        _bookmarkStore!!.getBookmarkList(_board_name).updateBookmark(editBookmarkIndex, bookmark)
        _bookmarkStore!!.store()
        reloadList()
        // 還原
        bookmarkAdapter!!.notifyItemChanged(editBookmarkIndex)
        editBookmarkIndex = -1
    }

    override fun onSearchDialogCancelButtonClicked() {
        // 還原
        bookmarkAdapter!!.notifyItemChanged(editBookmarkIndex)
        editBookmarkIndex = -1
    }
}
