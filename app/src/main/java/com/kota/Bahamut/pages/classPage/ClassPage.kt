package com.kota.Bahamut.pages.classPage

import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandDeleteFavoriteBoard
import com.kota.Bahamut.dialogs.DialogSearchBoard
import com.kota.Bahamut.dialogs.DialogSearchBoardListener
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.listPage.TelnetListPage2
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.model.ClassPageBlock
import com.kota.Bahamut.pages.model.ClassPageBlock.Companion.recycle
import com.kota.Bahamut.pages.model.ClassPageHandler
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.pages.model.ClassPageItem.Companion.recycle
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
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.logic.SearchBoardHandler
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetHeaderItemView

class ClassPage : TelnetListPage2(), View.OnClickListener, DialogSearchBoardListener {
    lateinit var mainLayout: RelativeLayout
    private var title: String? = ""
    var isReorderMode: Boolean = false

    /**
     * 當使用者放開拖曳項目 (完成排序) 時觸發，向 Telnet BBS 發送移動指令
     */
    override fun onItemDrop(fromPosition: Int, toPosition: Int) {
        val fromIndex = fromPosition + 1
        val toIndex = toPosition + 1
        create().pushString("$fromIndex\nM$toIndex\n").sendToServer()
        cleanAllItem()
        loadLastBlock()
    }

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_CLASS

    override val pageLayout: Int
        get() = R.layout.class_page

    override fun onPageDidLoad() {
        super.onPageDidLoad()

        mainLayout = findViewById(R.id.content_view) as RelativeLayout

        val recyclerView: RecyclerView = mainLayout.findViewById(R.id.ClassPage_recyclerView)
        emptyView = mainLayout.findViewById(R.id.ClassPage_listEmptyView)
        bindRecyclerView(recyclerView)
        mainLayout.findViewById<View>(R.id.ClassPage_SearchButton).setOnClickListener(this)
        mainLayout.findViewById<View>(R.id.ClassPage_FirstPageButton).setOnClickListener(this)
        mainLayout.findViewById<View>(R.id.ClassPage_LastestPageButton).setOnClickListener(this)

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            // 進入洽特
            // 查詢看板 => Chat => 定位到Chat:Enter
            object: ASCoroutine() {
                override suspend fun run() {
                    // 延遲1秒，確保看板列表載入完成
                    TelnetClient.myInstance!!.sendStringToServer("sChat")
                }
            }.postDelayed(500L)
        }
    }

    @Synchronized
    override fun onPageRefresh() {
        super.onPageRefresh()
        val hasNotification = BahamutStateHandler.bahamutStateHandler?.hasSystemNotification == true
        var title = if (hasNotification) "系統精靈送信來了" else this.title
        if (title == null || title.isEmpty()) {
            title = getContextString(R.string.loading)
        }

        val isFavorite = (listName == "Favorite")
        val headerView =
            mainLayout.findViewById<TelnetHeaderItemView>(R.id.ClassPage_headerView)
        if (headerView != null) {
            if (!TempSettings.lastVisitBoard.isEmpty()) {
                val finalLastVisitBoard = TempSettings.lastVisitBoard
                val lastVisitBoard =
                    finalLastVisitBoard + getContextString(R.string.toolbar_item_rr)

                val detail2 = mainLayout.findViewById<TextView>(R.id.ClassPage_lastVisit)
                if (detail2!==null) {
                    detail2.visibility = View.VISIBLE
                    detail2.bringToFront()
                    detail2.text = lastVisitBoard
                    detail2.setOnClickListener { v: View? ->
                        TelnetClient.myInstance!!.sendStringToServer("s$finalLastVisitBoard")
                    }
                }
            }
            val detail = "看板列表"
            val headerDetail2 = if (isFavorite) "長按移出最愛" else ""
            headerView.setData(title, detail, headerDetail2)

            if (isFavorite) {
                headerView.setMenuButtonClickListener {
                    showFavoriteMenu()
                }
            } else {
                headerView.setMenuButtonClickListener(null)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onPageRefresh()
        safeNotifyDataSetChanged()
    }

    override fun onBackPressed(): Boolean {
        // 檢查是否為最後一層 ClassPage（即即將退回主選單 MainPage）
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
     * 若使用者原本為「總數」模式，在徹底離開看板系統前還原
     */
    private fun restoreCountModeIfNeeded() {
        val stateHandler = BahamutStateHandler.bahamutStateHandler ?: return
        if (stateHandler.isUserOriginalCountMode && !stateHandler.isRestoringToCountMode) {
            stateHandler.isRestoringToCountMode = true
            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SMALL_C)
        }
    }

    override fun onSearchButtonClicked(): Boolean {
        showSearchBoardDialog()
        return true
    }

    private fun showSearchBoardDialog() {
        val dialog = DialogSearchBoard()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSearchButtonClickedWithKeyword(str: String) {
        SearchBoardHandler.instance.clear()
        showProcessingDialog("搜尋中")
        create().pushString("s$str ").sendToServer()
    }

    fun setClassTitle(aTitle: String?) {
        this.title = aTitle
    }

    override fun onClick(aView: View) {
        val getId = aView.id
        when (getId) {
            R.id.ClassPage_FirstPageButton -> {
                moveToFirstPosition()
            }
            R.id.ClassPage_LastestPageButton -> {
                moveToLastPosition()
            }
            R.id.ClassPage_SearchButton -> {
                onSearchButtonClicked()
            }
        }
    }

    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        if (listName == "Favorite") {
            onDeleteFavoriteBoardClicked(index)
            return true
        } else if ((getItem(index) as ClassPageItem).isDirectory) {
            return false
        } else {
            val itemIndex2 = index + 1
            val item = getItem(index) as ClassPageItem?
            val boardTitle = item?.title?.trim() ?: ""
            val message = if (boardTitle.isNotEmpty()) {
                "確定要將「$boardTitle」加入我的最愛？"
            } else {
                "確定要將此看板加入我的最愛？"
            }
            ASAlertDialog.createDialog().setMessage(message)
                .addButton("取消")
                .addButton("確定")
                .setListener { aDialog: ASAlertDialog?, index12: Int ->
                    if (index12 == 1) {
                        TelnetClient.myInstance!!.sendStringToServer("$itemIndex2\na")
                    }
                }.show()
            return true
        }
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }

    fun onSearchBoardFinished() {
        println("onSearchBoardFinished")
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

    fun showAddBoardToFavoriteDialog(boardName: String?) {
        ASAlertDialog.createDialog().setMessage("是否將看板" + boardName + "加入我的最愛?")
            .addButton("取消").addButton("加入")
            .setListener { aDialog: ASAlertDialog?, index: Int ->
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

    override fun loadPage(): TelnetListPageBlock? {
        return ClassPageHandler.instance.load()
    }

    override val isAutoLoadEnable: Boolean
        get() = false

    override fun getListIdFromListName(aName: String?): String? {
        return "$aName[Class]"
    }

    override fun isItemCanLoadAtIndex(index: Int): Boolean {
        val classPageItem = getItem(index) as ClassPageItem?
        if (classPageItem == null || classPageItem.isDeleted) {
            showShortToast("此看板已被刪除")
            return false
        }
        return true
    }

    override fun loadItemAtIndex(index: Int) {
        if (!isItemCanLoadAtIndex(index)) return
        val item = getItem(index) as ClassPageItem

        if (item.isDirectory) {
            PageContainer.instance!!.pushClassPage(item.name, item.title)
            navigationController.pushViewController(PageContainer.instance!!.classPage)
            super.loadItemAtIndex(index)
        } else {
            if (TempSettings.lastVisitBoard != item.name) {
                TempSettings.lastVisitArticleNumber = 0
            }
            TelnetClient.myInstance!!.sendStringToServer((index+1).toString() + "\n")
        }
    }

    override fun clear() {
        super.clear()
        isReorderMode = false
    }

    override fun onMenuButtonClicked(): Boolean {
        if (listName == "Favorite") {
            showFavoriteMenu()
            return true
        }
        return super.onMenuButtonClicked()
    }

    /**
     * 顯示「我的最愛」選單
     */
    private fun showFavoriteMenu() {
        val menuText = "拖曳排序"
        ASListDialog.createDialog()
            .addItem(menuText)
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    if (index == 0) {
                        toggleReorderMode()
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
     * 切換拖曳排序模式（顯示/隱藏拖曳把手）
     */
    private fun toggleReorderMode() {
        isReorderMode = !isReorderMode
        safeNotifyDataSetChanged()
    }

    private fun onDeleteFavoriteBoardClicked(index: Int) {
        val item = getItem(index) as ClassPageItem?
        val itemIndex = index + 1
        val boardTitle = item?.title?.trim() ?: ""
        val message = if (boardTitle.isNotEmpty()) {
            "確定要將「$boardTitle」移出我的最愛？"
        } else {
            "確定要將此看板移出我的最愛？"
        }
        ASAlertDialog.createDialog()
            .setMessage(message)
            .addButton("取消")
            .addButton("確定")
            .setListener { _, buttonIndex ->
                if (buttonIndex == 1) {
                    // 1. 標記本地資料 (資料層，防護二次點擊)
                    item?.isDeleted = true
                    item?.clear()

                    // 2. 推入 Telnet 刪除指令，完成後由 TelnetListPage 自動 cleanAllItem() 並 loadLastBlock() 重新讀取最新清單
                    val command = BahamutCommandDeleteFavoriteBoard(itemIndex)
                    pushCommand(command)
                }
            }
            .scheduleDismissOnPageDisappear(this)
            .show()
    }

    /** 填入看板  */
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        var itemView = view
        val itemIndex = i + 1
        val itemBlock = ItemUtils.getBlock(itemIndex)
        val item = getItem(i) as ClassPageItem?
        if (item == null && currentBlock != itemBlock && !isLoadingBlock(itemIndex)) {
            loadBoardBlock(itemBlock)
        }
        if (itemView == null) {
            itemView = ClassPageItemView(context)
            itemView.layoutParams = ViewGroup.LayoutParams(-1, -2)
        }
        val isFavorite = (listName == "Favorite")
        val pageItemView = itemView as ClassPageItemView
        pageItemView.setItem(item, isFavorite, isReorderMode)
        return itemView
    }


    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {
        recycle(telnetListPageBlock as ClassPageBlock)
    }

    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {
        recycle(telnetListPageItem as ClassPageItem)
    }
}
