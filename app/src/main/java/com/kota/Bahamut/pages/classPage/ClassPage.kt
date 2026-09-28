package com.kota.Bahamut.pages.classPage

import android.content.res.Configuration
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dialogs.DialogSearchBoard
import com.kota.Bahamut.dialogs.DialogSearchBoardListener
import com.kota.Bahamut.pages.model.ClassPageBlock
import com.kota.Bahamut.pages.model.ClassPageHandler
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.logic.SearchBoardHandler
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetHeaderItemView
import com.kota.telnetUI.TelnetPage
import java.util.Collections
import java.util.Vector

/**
 * 看板列表頁面控制器 (包含「分組討論區」、「佈告討論區」與「我的最愛」)。
 *
 * 主要架構與職責：
 * 1. 介面呈現：基於 [RecyclerView] 現代化架構。
 * 2. 我的最愛拖曳排序：透過 [ItemTouchHelper] 實現長按拖曳排序 (M)，拖曳放開時精準發送 BBS `$fromIndex\nM$toIndex\n` 指令。
 * 3. 移出我的最愛：右側配置獨立刪除按鈕 (d)，避免與長按手勢衝突，點擊觸發確認彈窗並發送 `$itemIndex\nd` 指令。
 * 4. 滾動自動加載：[RecyclerView.OnScrollListener] 滑動至底部時自動發送 `PAGE_DOWN` 鍵加載新區塊項目。
 * 5. BBS 通訊與畫面同步：預載入 [onPagePreload] 負責解析 BBS 終端封包，並將新項目平滑更新至 [boardItems]。
 */
class ClassPage : TelnetPage(), View.OnClickListener, ClassPageClickListener, DialogSearchBoardListener {

    /** 頁面根佈局 */
    lateinit var mainLayout: RelativeLayout

    /** 看板列表 RecyclerView */
    lateinit var recyclerView: RecyclerView

    /** RecyclerView 適配器 */
    var adapter: ClassPageAdapter? = null

    /** 當前已載入的看板清單集合 */
    val boardItems: MutableList<ClassPageItem> = Vector()

    /** 列表識別名稱（當為 "Favorite" 時啟用「我的最愛」模式，包含刪除按鈕與拖曳排序） */
    var listName: String = ""

    /** 頁面標題名稱 */
    private var title: String? = ""

    /** 拖曳起始與結束索引位置暫存 */
    private var startDragPos: Int = -1
    private var endDragPos: Int = -1
    private var dragView: View? = null

    /** 是否正在載入下一頁區塊 */
    private var isLoadingMore = false

    /** 是否強制重新刷洗全頁資料 */
    private var isForceRefresh = false

    /** 是否正在載入最後區塊資料 */
    private var isCommandLoadingLastBlock = false

    /** BBS 伺服器是否還有更多看板項目 */
    private var hasMoreOnBbs = true

    /** 離開頁面時暫存的滾動位置與偏移量 */
    private var savedPosition: Int = -1
    private var savedOffset: Int = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_CLASS

    override val pageLayout: Int
        get() = R.layout.class_page

    /**
     * 手勢拖曳排序處理器 (ItemTouchHelper)
     *
     * 僅在「我的最愛」模式 (listName == "Favorite") 允許上下拖曳。
     * - onMove：更新在地清單資料順序並刷新畫面動畫。
     * - onSelectedChanged：高亮當前拖曳列，並在手勢結束 (IDLE) 時送出 BBS 大 M 排序指令。
     */
    val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        override fun isLongPressDragEnabled(): Boolean {
            return listName == "Favorite"
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            endDragPos = to
            if (listName == "Favorite") {
                if (from in 0 until boardItems.size && to in 0 until boardItems.size) {
                    Collections.swap(boardItems, from, to)
                    adapter?.notifyItemMoved(from, to)
                }
            }
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (listName == "Favorite") {
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {
                        if (viewHolder != null) {
                            startDragPos = viewHolder.bindingAdapterPosition
                            dragView = viewHolder.itemView
                            dragView?.setBackgroundResource(R.color.ripple_material)
                        }
                    }
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        if (dragView != null) {
                            dragView?.setBackgroundResource(R.color.transparent)
                            dragView = null
                        }
                        // 若發生有效位置移動，向 BBS 發送次序移動指令 (大 M)
                        if (startDragPos != -1 && endDragPos != -1 && startDragPos != endDragPos) {
                            val fromIndex = startDragPos + 1
                            val toIndex = endDragPos + 1
                            sendBbsMoveOrderCommand(fromIndex, toIndex)
                        }
                        startDragPos = -1
                        endDragPos = -1
                    }
                }
            }
        }
    })

    /**
     * 頁面首次載入與 UI 視圖初始化
     */
    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout

        // 初始化 RecyclerView 與 Adapter
        recyclerView = mainLayout.findViewById(R.id.ClassPage_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ClassPageAdapter(boardItems)
        adapter?.isFavoriteMode = (listName == "Favorite")
        adapter?.setOnItemClickListener(this)
        recyclerView.adapter = adapter

        // 加入滾動監聽器：接近底部時自動向 BBS 請求下一頁
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (dy > 0 && lastVisibleItem >= totalItemCount - 3 && !isLoadingMore && hasMoreOnBbs) {
                    loadNextPageFromBbs()
                }
            }
        })

        // 綁定手勢拖曳功能
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // 底部工具列按鈕監聽
        mainLayout.findViewById<View>(R.id.ClassPage_SearchButton).setOnClickListener(this)
        mainLayout.findViewById<View>(R.id.ClassPage_FirstPageButton).setOnClickListener(this)
        mainLayout.findViewById<View>(R.id.ClassPage_LastestPageButton).setOnClickListener(this)

        updateToolbarColors()

        // 自動登入洽特處理
        if (TempSettings.isUnderAutoToChat) {
            object : ASCoroutine() {
                override suspend fun run() {
                    TelnetClient.myInstance!!.sendStringToServer("sChat")
                }
            }.postDelayed(500L)
        }
    }

    /**
     * 向 BBS 伺服器發送 PageDown 鍵，請求加載下一頁項目
     */
    private fun loadNextPageFromBbs() {
        isLoadingMore = true
        TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.PAGE_DOWN, 1)
    }

    /**
     * 頁面 UI 狀態重繪與標題更新
     */
    @Synchronized
    override fun onPageRefresh() {
        updateToolbarColors()
        val hasNotification = com.kota.Bahamut.BahamutStateHandler.bahamutStateHandler?.hasSystemNotification == true
        var displayTitle = if (hasNotification) "系統精靈送信來了" else this.title
        if (displayTitle.isNullOrEmpty()) {
            displayTitle = getContextString(R.string.loading)
        }

        val headerView = mainLayout.findViewById<TelnetHeaderItemView>(R.id.ClassPage_headerView)
        if (headerView != null) {
            if (TempSettings.lastVisitBoard.isNotEmpty()) {
                val finalLastVisitBoard = TempSettings.lastVisitBoard
                val lastVisitBoard = finalLastVisitBoard + getContextString(R.string.toolbar_item_rr)

                val detail2 = mainLayout.findViewById<TextView>(R.id.ClassPage_lastVisit)
                if (detail2 != null) {
                    detail2.visibility = View.VISIBLE
                    detail2.bringToFront()
                    detail2.text = lastVisitBoard
                    detail2.setOnClickListener {
                        TelnetClient.myInstance!!.sendStringToServer("s$finalLastVisitBoard")
                    }
                }
            }
            val detail = "看板列表"
            val headerDetail2 = if (listName == "Favorite") "長按可移動位置" else ""
            headerView.setData(displayTitle, detail, headerDetail2)
        }

        adapter?.isFavoriteMode = (listName == "Favorite")
        adapter?.notifyDataSetChanged()
        updateEmptyViewVisibility()
    }

    /**
     * 解析 BBS 接收到的資料封包並平滑更新/合併至本地清單
     *
     * @return 處理成功傳回 true，若封包無有效資料則傳回 false
     */
    override fun onPagePreload(): Boolean {
        // 若已載入過清單資料，且非主動加載更多或強制刷新，則直接傳回 true，不重複讀取/更新列表
        if (boardItems.isNotEmpty() && !isLoadingMore && !isForceRefresh) {
            ASCoroutine.ensureMainThread {
                dismissProcessingDialog()
            }
            return true
        }

        val block: ClassPageBlock = ClassPageHandler.instance.load()
        val newItems = mutableListOf<ClassPageItem>()
        var i = 0
        while (i < 20) {
            val item = block.getItem(i) as ClassPageItem? ?: break
            if (item.itemNumber > 0) {
                newItems.add(item)
            }
            i++
        }

        // 若資料未準備好則傳回 false
        if (newItems.isEmpty()) {
            return false
        }

        // 若當前區塊不足 20 項，表示已無更多 BBS 項目
        if (newItems.size < 20) {
            hasMoreOnBbs = false
        }

        val minNumber = block.minimumItemNumber
        val isInitialLoad = boardItems.isEmpty() || isForceRefresh

        if (isInitialLoad) {
            boardItems.clear()
            boardItems.addAll(newItems)
        } else {
            for (item in newItems) {
                val pos = item.itemNumber - 1
                if (pos in 0 until boardItems.size) {
                    boardItems[pos] = item
                } else if (pos == boardItems.size) {
                    boardItems.add(item)
                } else if (pos > boardItems.size) {
                    while (boardItems.size < pos) {
                        val dummy = ClassPageItem.create()
                        dummy.itemNumber = boardItems.size + 1
                        boardItems.add(dummy)
                    }
                    boardItems.add(item)
                }
            }
        }

        isLoadingMore = false
        isForceRefresh = false
        isCommandLoadingLastBlock = false

        ASCoroutine.ensureMainThread {
            dismissProcessingDialog()
            if (isInitialLoad) {
                adapter?.notifyDataSetChanged()
            } else {
                val startPos = (minNumber - 1).coerceAtLeast(0)
                adapter?.notifyItemRangeChanged(startPos, newItems.size)
            }
            updateEmptyViewVisibility()
        }
        return true
    }

    /**
     * 切換空清單提示元件的可見度
     */
    private fun updateEmptyViewVisibility() {
        val emptyView = mainLayout.findViewById<View>(R.id.ClassPage_listEmptyView)
        if (emptyView != null) {
            emptyView.visibility = if (boardItems.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    /**
     * 向 BBS 伺服器發送「我的最愛」次序移動指令 (大 M)
     *
     * 格式：`$fromIndex\nM$toIndex\n`
     */
    private fun sendBbsMoveOrderCommand(fromIndex: Int, toIndex: Int) {
        TelnetClient.myInstance!!.sendStringToServer("$fromIndex\nM$toIndex")
    }

    /**
     * 處理看板列點擊事件 (進入看板或子目錄)
     */
    override fun onItemClick(view: View?, position: Int) {
        val item = adapter?.getItem(position) ?: return
        if (item.isDirectory) {
            showProcessingDialog(getContextString(R.string.loading))
            PageContainer.instance!!.pushClassPage(item.name, item.title)
            navigationController.pushViewController(PageContainer.instance!!.classPage)
            TelnetClient.myInstance!!.sendStringToServer("${position + 1}\n")
        } else {
            if (TempSettings.lastVisitBoard != item.name) {
                TempSettings.lastVisitArticleNumber = 0
            }
            TelnetClient.myInstance!!.sendStringToServer("${position + 1}\n")
        }
    }

    /**
     * 處理移出「我的最愛」按鈕點擊事件 (小 d)
     */
    override fun onDeleteClick(view: View?, position: Int) {
        val item = adapter?.getItem(position) ?: return
        val message = if (item.title.isNotEmpty()) "確定要將「${item.title.trim()}」移出我的最愛?" else "確定要將此看板移出我的最愛?"
        ASAlertDialog.createDialog()
            .setMessage(message)
            .addButton("取消")
            .addButton("確定")
            .setListener { _, index ->
                if (index == 1) {
                    val itemIndex = position + 1
                    // 向 BBS 送出刪除指令 (跳到該項 + 按 d)
                    TelnetClient.myInstance!!.sendStringToServer("$itemIndex")
                    TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SMALL_D)

                    // 本地同步刪除項目並更新 RecyclerView
                    if (position in 0 until boardItems.size) {
                        boardItems.removeAt(position)
                        for (i in position until boardItems.size) {
                            boardItems[i].itemNumber = i + 1
                        }
                        adapter?.notifyItemRemoved(position)
                        adapter?.notifyItemRangeChanged(position, boardItems.size - position)
                        updateEmptyViewVisibility()
                    }
                }
            }
            .scheduleDismissOnPageDisappear(this)
            .show()
    }

    /**
     * 處理看板列長按事件 (非「我的最愛」模式下長按看板將其加入「我的最愛」)
     */
    override fun onItemLongClick(view: View?, position: Int): Boolean {
        if (listName == "Favorite") {
            // 我的最愛模式下長按由 ItemTouchHelper 處理拖曳排序 (大 M)
            return false
        }

        val item = adapter?.getItem(position) ?: return false
        // 目錄項目長按不處理
        if (item.isDirectory) {
            return false
        }

        val itemIndex = position + 1
        val message = if (item.title.isNotEmpty()) "確定要將「${item.title.trim()}」加入我的最愛?" else "確定要將此看板加入我的最愛?"
        ASAlertDialog.createDialog()
            .setMessage(message)
            .addButton("取消")
            .addButton("確定")
            .setListener { _, index ->
                if (index == 1) {
                    TelnetClient.myInstance!!.sendStringToServer("$itemIndex")
                    TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SMALL_A)
                }
            }
            .scheduleDismissOnPageDisappear(this)
            .show()

        return true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ClassPage_FirstPageButton -> moveToFirstPosition()
            R.id.ClassPage_LastestPageButton -> moveToLastPosition()
            R.id.ClassPage_SearchButton -> onSearchButtonClicked()
        }
    }

    /**
     * 移動至清單第一頁/最頂端
     */
    fun moveToFirstPosition() {
        if (boardItems.isNotEmpty()) {
            recyclerView.scrollToPosition(0)
        }
    }

    /**
     * 移動至清單最後一頁/最末端
     */
    fun moveToLastPosition() {
        isLoadingMore = true
        TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.END, 1)
    }

    /**
     * 處理實體返回鍵事件
     */
    override fun onBackPressed(): Boolean {
        val isExitingClassSystem = PageContainer.instance!!.classPageStackSize <= 1
        if (isExitingClassSystem) {
            restoreCountModeIfNeeded()
        }

        clear()
        PageContainer.instance!!.popClassPage()
        navigationController.popViewController()
        TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW, 1)
        return true
    }

    /**
     * 離開看板系統時還原使用者原始計數模式
     */
    private fun restoreCountModeIfNeeded() {
        val stateHandler = com.kota.Bahamut.BahamutStateHandler.bahamutStateHandler ?: return
        if (stateHandler.isUserOriginalCountMode && !stateHandler.isRestoringToCountMode) {
            stateHandler.isRestoringToCountMode = true
            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SMALL_C)
        }
    }

    /**
     * 設定頁面標題
     */
    fun setClassTitle(aTitle: String?) {
        this.title = aTitle
    }

    /**
     * 處理向右滑動返回手勢
     */
    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onPageRefresh()
        updateToolbarColors()
    }

    /**
     * 更新底部工具列主題配色
     */
    fun updateToolbarColors() {
        val buttonTextColor = CommonFunctions.getThemeColorStateList(R.attr.bahamut_buttonTextColor)
        if (buttonTextColor != null) {
            mainLayout.findViewById<Button>(R.id.ClassPage_SearchButton)?.setTextColor(buttonTextColor)
            mainLayout.findViewById<Button>(R.id.ClassPage_FirstPageButton)?.setTextColor(buttonTextColor)
            mainLayout.findViewById<Button>(R.id.ClassPage_LastestPageButton)?.setTextColor(buttonTextColor)
        }

        val bgRes = CommonFunctions.getThemeResourceId(R.attr.bahamut_toolbarItemBackground)
        if (bgRes != 0) {
            mainLayout.findViewById<Button>(R.id.ClassPage_SearchButton)?.setBackgroundResource(bgRes)
            mainLayout.findViewById<Button>(R.id.ClassPage_FirstPageButton)?.setBackgroundResource(bgRes)
            mainLayout.findViewById<Button>(R.id.ClassPage_LastestPageButton)?.setBackgroundResource(bgRes)
        }

        val pageBg = CommonFunctions.getThemeColor(R.attr.bahamut_pageBackground)
        mainLayout.setBackgroundColor(pageBg)
    }

    /**
     * 觸發搜尋看板對話框
     */
    override fun onSearchButtonClicked(): Boolean {
        showSearchBoardDialog()
        return true
    }

    private fun showSearchBoardDialog() {
        val dialog = DialogSearchBoard()
        dialog.setListener(this)
        dialog.show()
    }

    /**
     * 執行關鍵字搜尋看板
     */
    override fun onSearchButtonClickedWithKeyword(str: String) {
        SearchBoardHandler.instance.clear()
        showProcessingDialog("搜尋中")
        create().pushString("s$str ").sendToServer()
    }

    /**
     * 搜尋看板完成回呼，彈出搜尋結果對話框
     */
    fun onSearchBoardFinished() {
        dismissProcessingDialog()
        ASListDialog.createDialog().addItems(SearchBoardHandler.instance.boards)
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    val board = SearchBoardHandler.instance.getBoard(index)
                    if (this@ClassPage.listName == "Favorite") {
                        this@ClassPage.showAddBoardToFavoriteDialog(board)
                        return
                    }
                    if (TempSettings.lastVisitBoard != board) {
                        TempSettings.lastVisitArticleNumber = 0
                    }
                    TelnetClient.myInstance!!.sendStringToServer("s$board")

                    SearchBoardHandler.instance.clear()
                }

                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return false
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    /**
     * 彈出將看板加入「我的最愛」對話框
     */
    fun showAddBoardToFavoriteDialog(boardName: String?) {
        ASAlertDialog.createDialog().setMessage("是否將看板" + boardName + "加入我的最愛?")
            .addButton("取消").addButton("加入")
            .setListener { _, index ->
                if (index == 1) {
                    create().pushKey(TelnetKeyboard.LEFT_ARROW).pushString("B\n")
                        .pushKey(TelnetKeyboard.HOME).pushString("/$boardName\na ")
                        .pushKey(TelnetKeyboard.LEFT_ARROW).pushString("F\ns$boardName\n")
                        .sendToServer()
                    return@setListener
                }
                if (TempSettings.lastVisitBoard != boardName) {
                    TempSettings.lastVisitArticleNumber = 0
                }
                TelnetClient.myInstance!!.sendStringToServer("s$boardName")
                SearchBoardHandler.instance.clear()
            }.scheduleDismissOnPageDisappear(this).show()
    }

    /**
     * 離開頁面（例如進入看板）前保存當前滾動位置與偏移量
     */
    override fun onPageDidDisappear() {
        super.onPageDidDisappear()
        if (this::recyclerView.isInitialized) {
            val lm = recyclerView.layoutManager as? LinearLayoutManager
            if (lm != null) {
                savedPosition = lm.findFirstVisibleItemPosition()
                val firstView = lm.findViewByPosition(savedPosition)
                savedOffset = firstView?.top ?: 0
            }
        }
    }

    /**
     * 返回頁面時還原先前儲存的滾動位置
     */
    override fun onPageWillAppear() {
        super.onPageWillAppear()
        if (savedPosition >= 0 && this::recyclerView.isInitialized) {
            val lm = recyclerView.layoutManager as? LinearLayoutManager
            lm?.scrollToPositionWithOffset(savedPosition, savedOffset)
        }
    }

    /**
     * 完全離開 ClassPage 時清理頁面狀態與暫存資料
     */
    override fun clear() {
        boardItems.clear()
        adapter?.notifyDataSetChanged()
        listName = ""
        isLoadingMore = false
        hasMoreOnBbs = true
        savedPosition = -1
        savedOffset = 0
    }
}
