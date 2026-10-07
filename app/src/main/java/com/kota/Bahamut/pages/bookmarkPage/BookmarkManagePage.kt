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
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.dataModels.BookmarkStore
import com.kota.Bahamut.dialogs.DialogSearchArticle
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
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

/**
 * 看板書籤與瀏覽紀錄管理頁面。
 * 提供「我的書籤」與「瀏覽紀錄」的檢視、點擊搜尋、編輯（VIP專屬）、刪除及拖曳排序功能。
 *
 * @param aBoardName 看板名稱
 * @param boardExtendOptionalPageListener 書籤選取事件監聽器
 */
open class BookmarkManagePage(
    aBoardName: String,
    private val boardExtendOptionalPageListener: BoardExtendOptionalPageListener?
) : TelnetListPage2(), BookmarkClickListener, DialogSearchArticleListener {

    /** 當前看板名稱 */
    var boardName: String = aBoardName

    /** 當前顯示的書籤/歷史紀錄清單 */
    private val bookmarks: MutableList<Bookmark> = Vector()

    /** 頂部標題 View */
    protected var headerItemView: TelnetHeaderItemView? = null

    /** 「我的書籤」頁籤按鈕 */
    lateinit var bookmarkButton: Button

    /** 「瀏覽紀錄」頁籤按鈕 */
    lateinit var historyButton: Button

    /** 「水球/訊息紀錄」頁籤按鈕 */
    lateinit var waterBallButton: Button

    /** 頁籤按鈕集合 */
    private lateinit var tabButtons: Array<Button>

    /** 當前頁籤模式 (0: 我的書籤, 1: 瀏覽紀錄, 2: 訊息紀錄) */
    private var currentMode = 0

    /** 書籤資料儲存區 */
    var bookmarkStore: BookmarkStore? = TempSettings.bookmarkStore

    /** 是否處於拖曳排序模式 */
    var isReorderMode: Boolean = false

    /** 頁面 Layout 資源 ID */
    override val pageLayout: Int
        get() = R.layout.bookmark_manage_page

    /** 頁面類型 ID */
    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOOKMARK

    /** 是否開啟自動分頁載入（本頁停用） */
    override val isAutoLoadEnable: Boolean
        get() = false

    /** 載入頁面區塊資料（無非同步區塊載入） */
    override fun loadPage(): TelnetListPageBlock? = null

    /** 回收區塊 View */
    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {}

    /** 回收項目 View */
    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {}

    /** 取得當前清單總筆數 */
    override fun getCount(): Int = bookmarks.size

    /**
     * 頁面載入完成時的初始化流程：
     * 綁定 RecyclerView、設定頂部選單、按鈕事件及初始載入「我的書籤」。
     */
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

    /**
     * 顯示右上角選單，提供切換「拖曳排序」功能。
     */
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

    /**
     * 依據當前模式 ([currentMode]) 從 [bookmarkStore] 重新載入資料至 [bookmarks] 清單。
     */
    private fun reloadList() {
        val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
        if (currentMode == 1) bookmarkList?.loadHistoryList(bookmarks)
        else bookmarkList?.loadBookmarkList(bookmarks)
    }

    /**
     * 頁籤按鈕點擊監聽器，負責切換模式並更新頁籤樣式。
     */
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

    /**
     * 取得指定索引位置的 Item View，根據當前模式填充對應版面。
     *
     * @param i 索引位置
     * @param view 可重複使用的 View
     * @param viewGroup 父 View 容器
     * @return 渲染後的 Item View
     */
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

    /**
     * 綁定「我的書籤」項目的 UI 數據與元件狀態。
     *
     * @param itemView 項目 View
     * @param bookmark 書籤數據
     * @param position 列表位置
     */
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

    /**
     * 綁定「瀏覽紀錄」項目的 UI 數據與元件狀態。
     *
     * @param itemView 項目 View
     * @param bookmark 歷史紀錄數據
     * @param position 列表位置
     */
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

    /**
     * 拖曳排序移動中的回呼，交換記憶體清單中的項目位置。
     *
     * @param fromPosition 起始位置
     * @param toPosition 目標位置
     * @return 是否成功移動
     */
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition in bookmarks.indices && toPosition in bookmarks.indices) {
            Collections.swap(bookmarks, fromPosition, toPosition)
            recyclerViewAdapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }
        return false
    }

    /**
     * 拖曳排序放開時的回呼，將最終排序結果寫回 [bookmarkStore] 並持久化。
     *
     * @param fromPosition 起始位置
     * @param toPosition 最終位置
     */
    override fun onItemDrop(fromPosition: Int, toPosition: Int) {
        val bookmarkList = bookmarkStore?.getBookmarkList(boardName)
        if (currentMode == 0) {
            bookmarkList?.setBookmarks(bookmarks)
        } else {
            bookmarkList?.setHistoryBookmarks(bookmarks)
        }
        bookmarkStore?.store()
    }

    /**
     * 列表項目長按事件，觸發刪除流程。
     *
     * @param itemView 長按的 View
     * @param index 項目索引
     * @return 是否已消費該事件
     */
    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        if (index in bookmarks.indices) {
            onDeleteClick(itemView, index)
            return true
        }
        return false
    }

    /**
     * 點擊項目事件，退出當前頁面並委派給 [boardExtendOptionalPageListener] 執行文章搜尋。
     *
     * @param view 點擊的 View
     * @param position 項目位置
     */
    override fun onItemClick(parentView: android.widget.AdapterView<*>?, itemView: View?, index: Int, id: Long) {
        onItemClick(itemView, index)
    }

    override fun onItemClick(view: View?, position: Int) {
        val bookmark = bookmarks.getOrNull(position) ?: return
        navigationController.popViewController()
        boardExtendOptionalPageListener?.onBoardExtendOptionalPageDidSelectBookmark(bookmark)
    }

    /**
     * 點擊編輯按鈕，驗證 VIP 權限後開啟編輯對話框。
     *
     * @param view 點擊的 View
     * @param position 項目位置
     */
    override fun onEditClick(view: View?, position: Int) {
        if (propertiesVIP) {
            editBookmarkIndex = position
            showSearchArticleDialog()
        } else {
            showShortToast(getContextString(R.string.vip_only_message))
        }
    }

    /**
     * 點擊刪除按鈕或長按項目時，顯示刪除確認對話框。
     *
     * @param view 項目 View
     * @param position 項目位置
     */
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

    /**
     * 處理向右滑動手勢，返回上一頁。
     *
     * @return 是否已處理手勢
     */
    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        return true
    }

    /** 當前正在編輯的書籤索引 (-1 表示未在中編輯) */
    private var editBookmarkIndex = -1

    /**
     * 開啟文章搜尋條件編輯對話框 ([DialogSearchArticle])。
     */
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

    /**
     * 編輯對話框按下確認時的回呼，更新書籤內容並儲存。
     *
     * @param vector 包含關鍵字、作者、Mark及GY的搜尋條件向量
     */
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

    /**
     * 編輯對話框按下取消時的回呼。
     */
    override fun onSearchDialogCancelButtonClicked() {
        safeNotifyDataSetChanged()
        editBookmarkIndex = -1
    }

    /**
     * 清除頁面資源，重置排序模式狀態。
     */
    override fun clear() {
        super.clear()
        isReorderMode = false
    }
}
