package com.kota.Bahamut.pages.boardPage

import android.content.Context
import android.content.res.Configuration
import android.database.DataSetObserver
import android.util.Log
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandEditArticle
import com.kota.Bahamut.command.BahamutCommandFSendMail
import com.kota.Bahamut.command.BahamutCommandGoodArticle
import com.kota.Bahamut.command.BahamutCommandListArticle
import com.kota.Bahamut.command.BahamutCommandPostArticle
import com.kota.Bahamut.command.BahamutCommandPushArticle
import com.kota.Bahamut.command.BahamutCommandSearchArticle
import com.kota.Bahamut.command.BahamutCommandTheSameTitleBottom
import com.kota.Bahamut.command.BahamutCommandTheSameTitleDown
import com.kota.Bahamut.command.BahamutCommandTheSameTitleTop
import com.kota.Bahamut.command.BahamutCommandTheSameTitleUp
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.dialogs.DialogPushArticle
import com.kota.Bahamut.dialogs.DialogSearchArticle
import com.kota.Bahamut.dialogs.DialogSearchArticleListener
import com.kota.Bahamut.dialogs.DialogSelectArticle
import com.kota.Bahamut.dialogs.DialogSelectArticleListener
import com.kota.Bahamut.listPage.ListStateStore.Companion.instance
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.listPage.TelnetListPageItem
import com.kota.Bahamut.pages.ClassPage
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.PostArticlePageListener
import com.kota.Bahamut.pages.blockListPage.BlockListPage
import com.kota.Bahamut.pages.bookmarkPage.BoardExtendOptionalPageListener
import com.kota.Bahamut.pages.bookmarkPage.BookmarkManagePage
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.Bahamut.pages.model.BoardPageHandler
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.isBlockListContains
import com.kota.Bahamut.service.UserSettings.Companion.isBlockListContainsFuzzy
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListForTitle
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBoardMoveEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesDrawerLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesGestureOnBoardEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarOrder
import com.kota.Bahamut.service.UserSettings.Companion.propertiesUsername
import com.kota.Bahamut.ui.components.BBSToolbarDivider
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaCheckbox
import com.kota.Bahamut.ui.components.BahaFloatingToolbar
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.components.FloatingToolbarButtonItem
import com.kota.Bahamut.ui.components.RightArrow
import com.kota.Bahamut.ui.components.rememberDrawablePainter
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppColors
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.setMessage
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASListViewExtentOptionalDelegate
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard
import kotlinx.coroutines.launch
import java.util.Vector
import kotlin.math.max

open class BoardMainPage : TelnetListPage(),
    DialogSearchArticleListener,
    DialogSelectArticleListener,
    PostArticlePageListener,
    BoardExtendOptionalPageListener,
    ASListViewExtentOptionalDelegate {

    // 狀態驅動變數
    var boardTitle: String = ""
    var boardManager: String = ""
    var lastListAction: Int = BoardPageAction.LIST

    // Compose 狀態
    var boardTitleState by mutableStateOf("")
    var boardManagerState by mutableStateOf("")
    var isDrawerOpenState by mutableStateOf(false)
    var isDrawerLeftState by mutableStateOf(propertiesDrawerLocation != 0)
    var myModeState by mutableIntStateOf(0) // 0-書籤 1-紀錄
    var isItemBlockEnableState by mutableStateOf(false)
    val bookmarkListState = mutableStateListOf<Bookmark>()
    var scrollToItemTrigger by mutableStateOf<Int?>(null)
    var toolbarLocationState by mutableIntStateOf(0)
    var toolbarOrderState by mutableIntStateOf(0)

    override var isItemBlockEnable: Boolean = false
    var blockListForTitle: Boolean = false
    val myBookmarkList: MutableList<Bookmark> = ArrayList()
    var myMode: Int = 0
    private var isPostDelayedSuccess = false

    override val pageLayout: Int
        get() = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOARD

    override val isAutoLoadEnable: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                BoardMainPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    /** 發文 / 書籤點擊 */
    val mPostListener: View.OnClickListener =
        View.OnClickListener { this@BoardMainPage.onPostButtonClicked() }

    /** 彈出側邊選單 */
    val mMenuButtonListener: View.OnClickListener = View.OnClickListener {
        isDrawerOpenState = !isDrawerOpenState
        if (isDrawerOpenState) {
            reloadBookmark()
        }
    }

    /** 跳出小視窗 全部已讀/全部未讀 */
    val mReadAllListener: View.OnClickListener = View.OnClickListener {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem(getContextString(R.string.board_main_read_all))
            .addItem(getContextString(R.string.board_main_unread_all))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    if (title == getContextString(R.string.board_main_read_all)) {
                        val data = create().pushString("vV\n").build()
                        TelnetClient.myInstance!!.sendDataToServer(data)
                        showShortToast(getContextString(R.string.board_main_read_all_msg01))
                    } else if (title == getContextString(R.string.board_main_unread_all)) {
                        val data = create().pushString("vU\n").build()
                        TelnetClient.myInstance!!.sendDataToServer(data)
                        showShortToast(getContextString(R.string.board_main_unread_all_msg01))
                    }
                }

                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean = true
            }).show()
    }

    var btnLLListener: View.OnClickListener = View.OnClickListener {
        propertiesToolbarLocation = 1
        this@BoardMainPage.changeToolbarLocation()
    }

    var btnRRListener: View.OnClickListener = View.OnClickListener {
        propertiesToolbarLocation = 2
        this@BoardMainPage.changeToolbarLocation()
    }

    override fun onASListViewHandleExtentOptional(paramASListView: ASListView?, paramInt: Int): Boolean = false

    override fun onPageDidLoad() {
        super.onPageDidLoad()

        this.isItemBlockEnable = propertiesBlockListEnable
        this.isItemBlockEnableState = propertiesBlockListEnable
        blockListForTitle = propertiesBlockListForTitle

        refreshHeaderView()
        changeToolbarLocation()
        changeToolbarOrder()
        reloadBookmark()

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            TempSettings.isUnderAutoToChat = false
            object : ASCoroutine() {
                override suspend fun run() {
                    dismissProcessingDialog()
                }
            }.postDelayed(500L)
        }

        // 跳到指定文章編號
        if (this::class == BoardMainPage::class) {
            val isFromClassPage = ASNavigationController.currentController?.lastViewController is ClassPage
            if (isFromClassPage) {
                if (TempSettings.lastVisitArticleNumber > 0) {
                    object : ASCoroutine() {
                        override suspend fun run() {
                            val findIndex = max(1, TempSettings.lastVisitArticleNumber - 10)
                            onSelectDialogDismissWIthIndex(findIndex.toString())
                        }
                    }.postDelayed(100L)
                } else {
                    object : ASCoroutine() {
                        override suspend fun run() {
                            setManualLoadPage()
                            moveToLastPosition()
                            scrollToPosition(count - 1)
                        }
                    }.postDelayed(100L)
                }
            }
        }
    }

    var toEssencePageClickListener: View.OnClickListener = View.OnClickListener {
        this.lastListAction = BoardPageAction.ESSENCE
        PageContainer.instance!!.pushBoardEssencePage(listName, boardTitle)
        navigationController.pushViewController(PageContainer.instance!!.boardEssencePage)
        TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.TAB)
    }

    /** 變更工具列位置狀態 */
    fun changeToolbarLocation() {
        toolbarLocationState = propertiesToolbarLocation
    }

    /** 變更工具列按鈕排序狀態 */
    fun changeToolbarOrder() {
        toolbarOrderState = propertiesToolbarOrder
    }

    /** 實作實體選單鍵或標題列選單點擊 */
    override fun onMenuButtonClicked(): Boolean {
        mMenuButtonListener.onClick(null)
        return true
    }

    /** 重新整理看板抬頭標題與板主資訊 */
    fun refreshHeaderView() {
        var boardTitle1 = boardTitle
        boardTitle1 = boardTitle1.ifEmpty { getContextString(R.string.loading) }
        var boardManager1 = boardManager
        boardManager1 = boardManager1.ifEmpty { getContextString(R.string.loading) }
        boardTitleState = boardTitle1
        boardManagerState = boardManager1
    }

    /** 取得看板快取識別碼 */
    override fun getListIdFromListName(aName: String?): String? = "$aName[Board]"

    /** 從資料處理器載入當前板面區塊資料 */
    override fun loadPage(): TelnetListPageBlock {
        val load = BoardPageHandler.instance.load()
        if (!isInitialed) {
            val visitBoard = TempSettings.lastVisitBoard
            if (visitBoard != load.boardName) {
                TempSettings.lastVisitBoard = load.boardName
                TempSettings.lastVisitArticleNumber = 0
                clear()
                if (load.boardType == BoardPageAction.SEARCH) {
                    pushRefreshCommand(0)
                }
            }
            boardManager = load.boardManager
            boardTitle = load.boardTitle
            listName = load.boardName
        }
        return load
    }

    /** 檢查指定索引文章項目是否可點擊與載入 */
    override fun isItemCanLoadAtIndex(index: Int): Boolean {
        val boardPageItem = getItem(index) as BoardPageItem?
        if (boardPageItem != null) {
            BahamutStateHandler.bahamutStateHandler?.myCursorRow = getIndexInBlock(index) + 3
            TempSettings.boardFollowTitle = boardPageItem.title
            if (this::class == BoardMainPage::class)
                TempSettings.lastVisitArticleNumber = boardPageItem.itemNumber
        }
        if (boardPageItem == null || !boardPageItem.isDeleted) {
            return true
        }
        showShortToast("此文章已被刪除")
        return false
    }

    /** 頁面刷新生命週期 */
    @Synchronized
    override fun onPageRefresh() {
        refreshHeaderView()
        super.onPageRefresh()
    }

    /** 裝置旋轉與組態變更回呼 */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshHeaderView()
        safeNotifyDataSetChanged()
    }

    /** 列表項目長按事件 (開啟同標題主題串連) */
    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        onListArticle(index + 1)
        return true
    }

    /** 搜尋按鈕點擊觸發 */
    override fun onSearchButtonClicked(): Boolean {
        showSearchArticleDialog()
        return true
    }

    /** 顯示文章搜尋條件設定對話框 */
    fun showSearchArticleDialog() {
        val dialogSearchArticle = DialogSearchArticle()
        dialogSearchArticle.setListener(this)
        dialogSearchArticle.show()
    }

    /** 顯示跳轉指定文章編號對話框 */
    fun showSelectArticleDialog() {
        val dialogSelectArticle = DialogSelectArticle()
        dialogSelectArticle.setListener(this)
        dialogSelectArticle.show()
    }

    /** 搜尋對話框確定搜尋按鈕回呼 */
    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String>) {
        searchArticle(
            vector[0]!!,
            vector[1],
            if (vector[2] == "YES") "y" else "n",
            vector[3]
        )
    }

    /** 執行文章關鍵字/作者/標記搜尋並跳轉頁面 */
    fun searchArticle(keyword: String, author: String, mark: String, myGY: String) {
        this.lastListAction = BoardPageAction.SEARCH
        val boardSearchPage = PageContainer.instance!!.boardSearchPage
        boardSearchPage.clear()
        boardSearchPage.listName = this.listName
        boardSearchPage.boardTitle = this.boardTitle
        boardSearchPage.boardManager = "文章搜尋"
        navigationController.pushViewController(boardSearchPage)
        val state = instance.getState(boardSearchPage.getListIdFromListName(listName))
        state.top = 0
        state.position = 0
        boardSearchPage.setKeyword(keyword)
        boardSearchPage.setAuthor(author)
        boardSearchPage.setMark(mark)
        boardSearchPage.setGy(myGY)
        pushCommand(BahamutCommandSearchArticle(keyword, author, mark, myGY))
    }

    /** 處理跳轉文章對話框關閉並移動滾動位置 */
    override fun onSelectDialogDismissWIthIndex(str: String) {
        var i: Int
        try {
            i = str.toInt() - 1
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
            i = -1
        }
        if (i >= 0) {
            setListViewSelection(i)
        }
    }

    /** 觸發列表平滑滾動至指定項目索引 */
    fun scrollToPosition(index: Int) {
        scrollToItemTrigger = index
    }

    /** 設定列表選擇位置 */
    override fun setListViewSelection(selection: Int) {
        super.setListViewSelection(selection)
        scrollToPosition(selection)
    }

    /** 開啟書籤管理頁面 */
    fun onBookmarkButtonClicked() {
        navigationController.pushViewController(BookmarkManagePage(listName, this))
    }

    /** 開啟主題串列頁面 */
    fun onListArticle(i: Int) {
        this.lastListAction = BoardPageAction.LINK_TITLE
        val boardLinkedTitlePage = PageContainer.instance!!.boardLinkedTitlePage
        boardLinkedTitlePage.clear()
        boardLinkedTitlePage.listName = this.listName
        boardLinkedTitlePage.boardTitle = this.boardTitle
        boardLinkedTitlePage.boardManager = "主題串列"
        navigationController.pushViewController(boardLinkedTitlePage)
        val state = instance.getState(boardLinkedTitlePage.getListIdFromListName(listName))
        state.top = 0
        state.position = 0
        pushCommand(BahamutCommandListArticle(i))
    }

    /** 開啟發表文章頁面 */
    protected open fun onPostButtonClicked() {
        val postArticlePage = PageContainer.instance!!.postArticlePage
        postArticlePage.setBoardPage(this)
        postArticlePage.setListener(this)
        navigationController.pushViewController(postArticlePage)
    }

    /** 對當前載入的文章點讚/給 GY */
    fun goodLoadingArticle() {
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string.do_gy))
            .setMessage(getContextString(R.string.gy_this_article))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.do_gy))
            .setListener { _, i2 ->
                if (i2 == 1) {
                    this@BoardMainPage.pushCommand(BahamutCommandGoodArticle(loadingItemNumber))
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    /** 發送推文指令 */
    fun pushArticle() {
        this@BoardMainPage.pushCommand(BahamutCommandPushArticle(loadingItemNumber))
        pushArticleASCoroutine?.cancel()
        pushArticleASCoroutine?.postDelayed(2000L)
        isPostDelayedSuccess = false
    }

    /** 開啟推文輸入對話框 */
    fun openPushArticleDialog() {
        pushArticleASCoroutine?.cancel()
        isPostDelayedSuccess = true
        val dialog = DialogPushArticle()
        dialog.show()
    }

    /** 推文發送逾時檢查協程 */
    var pushArticleASCoroutine: ASCoroutine? = object : ASCoroutine() {
        override suspend fun run() {
            if (!isPostDelayedSuccess) {
                onPagePreload()
                showLongToast("沒反應，看板未開放推文")
            }
        }
    }

    /** 取消推文逾時檢查 */
    fun cancelRunner() {
        pushArticleASCoroutine?.cancel()
        isPostDelayedSuccess = true
    }

    /** 發送站內信給板主或作者 */
    fun funSendMail() {
        pushCommand(BahamutCommandFSendMail(propertiesUsername))
    }

    /** 載入同標題最舊一篇 (頂端) */
    fun loadTheSameTitleTop() {
        onLoadItemStart()
        pushCommand(BahamutCommandTheSameTitleTop(loadingItemNumber))
    }

    /** 載入同標題最新一篇 (底端) */
    fun loadTheSameTitleBottom() {
        onLoadItemStart()
        pushCommand(BahamutCommandTheSameTitleBottom(loadingItemNumber))
    }

    /** 載入同標題上一篇 */
    fun loadTheSameTitleUp() {
        onLoadItemStart()
        pushCommand(BahamutCommandTheSameTitleUp(loadingItemNumber))
    }

    /** 載入同標題下一篇 */
    fun loadTheSameTitleDown() {
        onLoadItemStart()
        pushCommand(BahamutCommandTheSameTitleDown(loadingItemNumber))
    }

    /** 判定項目作者是否地位於黑名單中 */
    override fun isItemBlocked(aItem: TelnetListPageItem?): Boolean {
        if (aItem != null) {
            return this.isItemBlockEnable && isBlockListContains((aItem as BoardPageItem).author)
        }
        return false
    }

    /** 切換黑名單過濾啟用狀態 */
    fun onChangeBlockStateButtonClicked() {
        propertiesBlockListEnable = !this.isItemBlockEnable
        notifyDataUpdated()
        this.isItemBlockEnable = propertiesBlockListEnable
        this.isItemBlockEnableState = propertiesBlockListEnable
        safeNotifyDataSetChanged()
    }

    /** 開啟黑名單編輯頁面 */
    fun onEditBlockListButtonClicked() {
        navigationController.pushViewController(BlockListPage())
    }

    /** 點擊並開啟指定索引的文章頁面 */
    override fun loadItemAtIndex(index: Int) {
        if (isItemCanLoadAtIndex(index)) {
            lastLoadItemIndex = index
            listId?.let { id ->
                val state = instance.getState(id)
                state.position = index
            }
            val articlePage = PageContainer.instance!!.articlePage
            articlePage.setBoardPage(this)
            articlePage.clear()
            navigationController.pushViewController(articlePage)

            super.loadItemAtIndex(index)
        }
    }

    /** 保存當前列表狀態與位置 */
    override fun saveListState() {
        super.saveListState()
        listId?.let { id ->
            val state = instance.getState(id)
            if (state.position < 0 && lastLoadItemIndex >= 0) {
                state.position = lastLoadItemIndex
            }
        }
    }

    /** 載入已保存的列表狀態與位置 */
    override fun loadListState() {
        super.loadListState()
        listId?.let { id ->
            val state = instance.getState(id)
            if (state.position >= 0) {
                scrollToPosition(state.position)
            } else if (lastLoadItemIndex >= 0) {
                scrollToPosition(lastLoadItemIndex)
            }
        }
    }

    /** 準備重置初始化狀態 */
    fun prepareInitial() {
        isInitialed = false
    }

    /** 處理擴充選單選取書籤項目回呼 */
    override fun onBoardExtendOptionalPageDidSelectBookmark(bookmark: Bookmark?) {
        if (bookmark != null) {
            this.lastListAction = BoardPageAction.SEARCH
            pushCommand(
                BahamutCommandSearchArticle(
                    bookmark.keyword,
                    bookmark.author,
                    bookmark.mark,
                    bookmark.gy
                )
            )
        }
    }

    /** 回收板面 Block */
    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {
        BoardPageBlock.recycle(telnetListPageBlock as BoardPageBlock)
    }

    /** 回收板面 Item */
    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {
        BoardPageItem.recycle(telnetListPageItem as BoardPageItem?)
    }

    /** 處理文章編輯對話框發送 */
    override fun onPostDialogEditButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String?,
        str2: String?,
        str3: String?
    ) {
        pushCommand(BahamutCommandEditArticle(str, str2!!, str3!!))
        safeNotifyDataSetChanged()
    }

    /** 處理文章發布對話框發送 */
    override fun onPostDialogSendButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String?,
        str2: String?,
        str3: String?,
        str4: String?,
        str5: String?,
        boolean6: Boolean?
    ) {
        pushCommand(BahamutCommandPostArticle(this, str!!, str2!!, str3, str4, str5, boolean6!!))
        if (str3 != null && str3 == "M") return

        showProcessingDialog(getContextString(R.string.board_page_post_waiting_message_1))
        postWaitingDialog1.postDelayed(3000L)
        postWaitingDialog2.postDelayed(6000L)
    }

    val postWaitingDialog1: ASCoroutine = object : ASCoroutine() {
        override suspend fun run() {
            setMessage(getContextString(R.string.board_page_post_waiting_message_2))
        }
    }

    val postWaitingDialog2: ASCoroutine = object : ASCoroutine() {
        override suspend fun run() {
            setMessage(getContextString(R.string.board_page_post_waiting_message_3))
        }
    }

    /** 恢復發文草稿 */
    fun recoverPost() {
        ASCoroutine.ensureMainThread {
            cleanCommand()
            val page = PageContainer.instance!!.postArticlePage
            page.setRecover()
        }
        postWaitingDialog1.cancel()
        postWaitingDialog2.cancel()
        dismissProcessingDialog()
    }

    /** 完成貼文並關閉發文頁 */
    fun finishPost() {
        ASCoroutine.ensureMainThread {
            val page = PageContainer.instance!!.postArticlePage
            page.closeArticle()
        }
        postWaitingDialog1.cancel()
        postWaitingDialog2.cancel()
        dismissProcessingDialog()
    }

    /** 重新載入書籤與歷史紀錄清單 */
    fun reloadBookmark(aView: View? = null) {
        val store = TempSettings.bookmarkStore
        myBookmarkList.clear()
        if (store != null) {
            if (myMode == 0) {
                store.getBookmarkList(listName).loadBookmarkList(myBookmarkList)
            } else {
                store.getBookmarkList(listName).loadHistoryList(myBookmarkList)
            }
        }
        bookmarkListState.clear()
        bookmarkListState.addAll(myBookmarkList)
        myModeState = myMode
    }

    /** 開啟側邊選單 Drawer (可指定方向: true 表示由左向右滑出，false 表示由右向左滑出) */
    fun openDrawer(isLeft: Boolean = propertiesDrawerLocation != 0) {
        isDrawerLeftState = isLeft
        isDrawerOpenState = true
        reloadBookmark()
    }

    /** 關閉側邊選單 Drawer */
    fun closeDrawer() {
        isDrawerOpenState = false
    }

    /** 接收向左滑動手勢 (從右向左滑動) */
    override fun onReceivedGestureLeft(): Boolean {
        if (pageType == BahamutPage.BAHAMUT_BOARD) {
            if (!isDrawerOpenState) {
                openDrawer(isLeft = false)
                return true
            } else if (!isDrawerLeftState) {
                closeDrawer()
                return true
            }
        }
        return super.onReceivedGestureLeft()
    }

    /** 接收向右滑動手勢 (從左向右滑動) */
    override fun onReceivedGestureRight(): Boolean {
        if (pageType == BahamutPage.BAHAMUT_BOARD) {
            if (!isDrawerOpenState) {
                openDrawer(isLeft = true)
                return true
            } else if (isDrawerLeftState) {
                closeDrawer()
                return true
            }
        }
        if (propertiesGestureOnBoardEnable) {
            if (isDrawerOpenState) {
                closeDrawer()
                return true
            }
            onBackPressed()
            return true
        }
        return false
    }

    /** 處理系統返回鍵事件 */
    override fun onBackPressed(): Boolean {
        if (isDrawerOpenState) {
            closeDrawer()
            return true
        }
        clear()
        navigationController.popViewController()
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        PageContainer.instance!!.cleanBoardPage()
        return true
    }

    /** 搜尋對話框取消事件回呼 */
    override fun onSearchDialogCancelButtonClicked() {}

    // -------------------------------------------------------------
    // Compose 畫面主體
    // -------------------------------------------------------------

    @Composable
    fun BoardMainPageContent() {
        val colors = AppTheme.colors
        val coroutineScope = rememberCoroutineScope()
        val haptic = LocalHapticFeedback.current

        val savedPosition = remember {
            val s = listId?.let { instance.getState(it) }
            when {
                s != null && s.position >= 0 -> s.position
                lastLoadItemIndex >= 0 -> lastLoadItemIndex
                TempSettings.lastVisitArticleNumber > 0 -> max(0, TempSettings.lastVisitArticleNumber - 1)
                else -> -1
            }
        }
        val savedOffset = remember {
            val s = listId?.let { instance.getState(it) }
            if (s != null && s.top > 0) s.top else 0
        }
        val initialIndex = if (savedPosition >= 0) savedPosition else 0
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = initialIndex,
            initialFirstVisibleItemScrollOffset = savedOffset
        )

        // 即時同步滾動位置至 ListStateStore
        LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            listId?.let { id ->
                val state = instance.getState(id)
                state.position = listState.firstVisibleItemIndex
                state.top = listState.firstVisibleItemScrollOffset
            }
        }

        // 監聽 TelnetListPage 資料變更
        var dataVersion by remember { mutableIntStateOf(0) }
        DisposableEffect(Unit) {
            val observer = object : DataSetObserver() {
                override fun onChanged() {
                    dataVersion++
                }

                override fun onInvalidated() {
                    dataVersion++
                }
            }
            registerDataSetObserver(observer)
            onDispose {
                unregisterDataSetObserver(observer)
            }
        }

        val currentCount = if (dataVersion >= 0) count else 0

        // 當列表資料載入或返回時，若初始在 0 且有保存的位置，確保滾動到保存的位置
        var hasRestoredSavedPosition by remember { mutableStateOf(false) }
        LaunchedEffect(currentCount) {
            if (!hasRestoredSavedPosition && currentCount > 0 && savedPosition >= 0) {
                val target = savedPosition.coerceIn(0, currentCount - 1)
                listState.scrollToItem(target, savedOffset)
                hasRestoredSavedPosition = true
            }
        }

        // 監聽平移或滾動觸發
        LaunchedEffect(scrollToItemTrigger) {
            scrollToItemTrigger?.let { target ->
                val safeTarget = if (target == -1) {
                    max(0, currentCount - 1)
                } else {
                    target.coerceIn(0, max(0, currentCount - 1))
                }
                if (currentCount > 0) {
                    listState.scrollToItem(safeTarget)
                }
                scrollToItemTrigger = null
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 頁面主佈局
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. 頂部標題列
                BoardMainTopBar(
                    title = boardTitleState.ifEmpty { stringResource(R.string.loading) },
                    listName = listName,
                    manager = boardManagerState,
                    isBoard = pageType == BahamutPage.BAHAMUT_BOARD,
                    onReadAllClick = { mReadAllListener.onClick(null) },
                    onMenuClick = {
                        if (pageType == BahamutPage.BAHAMUT_BOARD) {
                            mMenuButtonListener.onClick(null)
                        } else {
                            onMenuButtonClicked()
                        }
                    }
                )

                // 2. 文章列表主體
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
                                fontSize = AppTheme.fontSize.body
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                count = currentCount,
                                key = { index -> index }
                            ) { index ->
                                @Suppress("UNUSED_VARIABLE")
                                val version = dataVersion
                                val itemIndex = index + 1
                                val itemBlock = ItemUtils.getBlock(itemIndex)
                                val item = getItem(index) as? BoardPageItem

                                // 按需載入區塊
                                if (item == null && currentBlock != itemBlock && !isLoadingBlock(itemIndex)) {
                                    loadBoardBlock(itemBlock)
                                }

                                // 黑名單過濾判定
                                if (item != null && isItemBlockEnable) {
                                    item.isBlocked = isBlockListContains(item.author) ||
                                            (blockListForTitle && isBlockListContainsFuzzy(item.title))
                                }

                                if (item?.isBlocked != true) {
                                    BoardPageRowItem(
                                        item = item,
                                        itemIndex = itemIndex,
                                        colors = colors,
                                        onClick = {
                                            coroutineScope.launch {
                                                loadItemAtIndex(index)
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            coroutineScope.launch {
                                                onListViewItemLongClicked(null, index)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 側邊選單 Drawer 手勢觸發區 (限定在文章列表極邊緣 16dp，未開啟時不擋 Header)
                    if (pageType == BahamutPage.BAHAMUT_BOARD && !isDrawerOpenState) {
                        // 左側極邊緣觸發區 (16dp 寬) - 從最左邊緣向內 (右) 滑動開啟左側選單
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(16.dp)
                                .align(Alignment.CenterStart)
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        if (dragAmount > 15f) {
                                            openDrawer(isLeft = true)
                                        }
                                    }
                                }
                        )
                        // 右側極邊緣觸發區 (16dp 寬) - 從最右邊緣向內 (左) 滑動開啟右側選單
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(16.dp)
                                .align(Alignment.CenterEnd)
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        if (dragAmount < -15f) {
                                            openDrawer(isLeft = false)
                                        }
                                    }
                                }
                        )
                    }
                }

                // 3. 底部操作工具列 (僅在 toolbarLocationState <= 2 時顯示)
                if (toolbarLocationState <= 2) {
                    BoardMainToolbar(
                        colors = colors,
                        pageType = pageType,
                        toolbarLocation = toolbarLocationState,
                        toolbarOrder = toolbarOrderState,
                        onPostClick = { mPostListener.onClick(null) },
                        onPrevClick = {
                            val firstVisible = listState.firstVisibleItemIndex
                            val visibleCount = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(10)
                            val target = max(0, firstVisible - visibleCount)
                            if (this@BoardMainPage::class == BoardMainPage::class) {
                                TempSettings.lastVisitArticleNumber = target
                            }
                            coroutineScope.launch { listState.scrollToItem(target) }
                        },
                        onFirstClick = {
                            moveToFirstPosition()
                            coroutineScope.launch { listState.scrollToItem(0) }
                        },
                        onNextClick = {
                            val firstVisible = listState.firstVisibleItemIndex
                            val visibleCount = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(10)
                            val target = (firstVisible + visibleCount).coerceAtMost(max(0, currentCount - 1))
                            if (this@BoardMainPage::class == BoardMainPage::class) {
                                TempSettings.lastVisitArticleNumber = target
                            }
                            coroutineScope.launch { listState.scrollToItem(target) }
                        },
                        onLastClick = {
                            setManualLoadPage()
                            moveToLastPosition()
                            coroutineScope.launch { listState.scrollToItem(max(0, currentCount - 1)) }
                        },
                        onLLClick = { btnLLListener.onClick(null) },
                        onRRClick = { btnRRListener.onClick(null) }
                    )
                }
            }

            // 5. 浮動工具列 (toolbarLocationState == 3)
            if (toolbarLocationState == 3) {
                val isMoveEnable = propertiesBoardMoveEnable > 0
                val postText = stringResource(
                    if (pageType == BahamutPage.BAHAMUT_BOARD) R.string.post else R.string.bookmark
                )
                val floatingButtons = listOf(
                    FloatingToolbarButtonItem(
                        text = postText,
                        onClick = { mPostListener.onClick(null) }
                    ),
                    FloatingToolbarButtonItem(
                        text = stringResource(R.string.prev_page),
                        onClick = {
                            val firstVisible = listState.firstVisibleItemIndex
                            val visibleCount = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(10)
                            val target = max(0, firstVisible - visibleCount)
                            if (this@BoardMainPage::class == BoardMainPage::class) {
                                TempSettings.lastVisitArticleNumber = target
                            }
                            coroutineScope.launch { listState.scrollToItem(target) }
                        },
                        onLongClick = {
                            moveToFirstPosition()
                            coroutineScope.launch { listState.scrollToItem(0) }
                        }
                    ),
                    FloatingToolbarButtonItem(
                        text = stringResource(if (isMoveEnable) R.string.next_page else R.string.last_page),
                        onClick = {
                            if (isMoveEnable) {
                                val firstVisible = listState.firstVisibleItemIndex
                                val visibleCount = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(10)
                                val target = (firstVisible + visibleCount).coerceAtMost(max(0, currentCount - 1))
                                if (this@BoardMainPage::class == BoardMainPage::class) {
                                    TempSettings.lastVisitArticleNumber = target
                                }
                                coroutineScope.launch { listState.scrollToItem(target) }
                            } else {
                                setManualLoadPage()
                                moveToLastPosition()
                                coroutineScope.launch { listState.scrollToItem(max(0, currentCount - 1)) }
                            }
                        },
                        onLongClick = {
                            setManualLoadPage()
                            moveToLastPosition()
                            coroutineScope.launch { listState.scrollToItem(max(0, currentCount - 1)) }
                        }
                    )
                )

                BahaFloatingToolbar(
                    buttons = floatingButtons,
                    toolbarOrder = toolbarOrderState
                )
            }

            // 6. 側邊選單 Drawer (若 pageType == BAHAMUT_BOARD)
            // 放置於最外層 Box，開啟時覆蓋畫面的整個邊側 (全螢幕高度，涵蓋 Header 與 Toolbar)
            if (pageType == BahamutPage.BAHAMUT_BOARD) {
                BoardEndDrawer(
                    isOpen = isDrawerOpenState,
                    isLeft = isDrawerLeftState,
                    mode = myModeState,
                    bookmarks = bookmarkListState,
                    isBlockEnabled = isItemBlockEnableState,
                    colors = colors,
                    onClose = { closeDrawer() },
                    onEssenceClick = {
                        closeDrawer()
                        toEssencePageClickListener.onClick(null)
                    },
                    onBookmarkManageClick = {
                        closeDrawer()
                        onBookmarkButtonClicked()
                    },
                    onTabClick = { mode ->
                        myMode = mode
                        reloadBookmark()
                    },
                    onBookmarkItemClick = { bookmark ->
                        closeDrawer()
                        searchArticle(bookmark.keyword, bookmark.author, bookmark.mark, bookmark.gy)
                    },
                    onSearchClick = {
                        closeDrawer()
                        showSearchArticleDialog()
                    },
                    onSelectClick = {
                        closeDrawer()
                        showSelectArticleDialog()
                    },
                    onToggleBlock = { onChangeBlockStateButtonClicked() },
                    onBlockSettingClick = {
                        closeDrawer()
                        onEditBlockListButtonClicked()
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 子元件定義
// -------------------------------------------------------------

/**
 * 看板頂部導覽列 (經典 BBS 深海藍底，雙行文字，無返回鍵，右側選單按鈕)
 */
@Composable
fun BoardMainTopBar(
    title: String,
    listName: String,
    manager: String,
    isBoard: Boolean,
    onReadAllClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.titleBarBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側看板文字資訊區塊 (雙行)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            ) {
                // 第一列：看板中文大標題 (黃字)
                BahaText(
                    text = title,
                    color = colors.titleBarTitle,
                    fontSize = AppTheme.fontSize.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 第二列：看板英文名 (白字) + vV (青色) 與 看板板主/在線人數 (青色)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        BahaText(
                            text = listName,
                            color = colors.titleBarDetail,
                            fontSize = AppTheme.fontSize.body,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isBoard) {
                            BahaText(
                                text = stringResource(R.string.board_main_vV),
                                color = colors.textSecondary,
                                fontSize = AppTheme.fontSize.body,
                                modifier = Modifier
                                    .clickable { onReadAllClick() }
                                    .padding(start = 2.dp)
                            )
                        }
                    }

                    if (manager.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        BahaText(
                            text = manager,
                            color = colors.titleBarDetail2,
                            fontSize = AppTheme.fontSize.body,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 右側選單按鈕 (經典 BBS 選單圖示，60dp 寬，背景為 titleBarMenu #101090，無垂直分隔線)
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(colors.titleBarMenu)
                    .clickable { onMenuClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = rememberDrawablePainter(resId = R.drawable.menu_icon),
                    contentDescription = stringResource(R.string.zero_word),
                    tint = colors.textPrimary
                )
            }
        }
    }
}

/**
 * 看板文章項目 Row Item
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardPageRowItem(
    item: BoardPageItem?,
    itemIndex: Int,
    colors: AppColors,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val titleText = item?.title ?: stringResource(R.string.loading_)
    val authorText = item?.author ?: ""
    val dateText = item?.date ?: ""
    val gyCount = item?.gy ?: 0
    val isReply = item?.isReply == true
    val isMarked = item?.isMarked == true
    val isRead = (item?.isDeleted == true) || (item?.isRead == true)
    val isFollowed = TempSettings.isBoardFollowTitle(titleText)

    // 決定標題顏色
    val titleColor = if (isFollowed) {
        if (!isReply) {
            if (isRead) colors.bbsBoardFollowFirstRead else colors.bbsBoardFollowFirst
        } else {
            if (isRead) colors.bbsBoardFollowOtherRead else colors.bbsBoardFollowOther
        }
    } else {
        if (isRead) colors.bbsBoardNormalRead else colors.bbsBoardNormal
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.pageBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側文章資訊區塊
            Column(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // 第一列：狀態與標題
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 狀態 (◆ 或 Re)
                    BahaText(
                        text = if (isReply) "Re" else "◆",
                        color = colors.bbsMailStatus,
                        fontSize = AppTheme.fontSize.title,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // 標題
                    BahaText(
                        text = titleText,
                        color = titleColor,
                        fontSize = AppTheme.fontSize.title
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 第二列：編號、標記、日期、GY、作者
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 編號 (5 碼)
                    BahaText(
                        text = String.format("%05d", item?.itemNumber ?: itemIndex),
                        color = colors.bbsMailNumber,
                        fontSize = AppTheme.fontSize.body
                    )

                    // 標記 M
                    if (isMarked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        BahaText(
                            text = "M",
                            color = colors.bbsMailMark,
                            fontSize = AppTheme.fontSize.body
                        )
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    // 日期
                    Spacer(modifier = Modifier.width(12.dp))
                    BahaText(
                        text = dateText,
                        color = colors.bbsMailDate,
                        fontSize = AppTheme.fontSize.body
                    )

                    // GY 推文數
                    if (gyCount > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        BahaText(
                            text = stringResource(R.string.gy_),
                            color = colors.bbsContent0,
                            fontSize = AppTheme.fontSize.body
                        )
                        BahaText(
                            text = gyCount.toString(),
                            color = colors.bbsBoardGy,
                            fontSize = AppTheme.fontSize.body
                        )
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 作者 (靠右對齊)
                    BahaText(
                        text = authorText,
                        color = colors.bbsMailAuthor,
                        fontSize = AppTheme.fontSize.body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 右箭頭按鈕區塊
            RightArrow { onClick() }
        }

        // 底部分隔線
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider)
        )
    }
}

/**
 * 底部操作工具列
 */
@Composable
fun BoardMainToolbar(
    colors: AppColors,
    pageType: Int,
    toolbarLocation: Int,
    toolbarOrder: Int,
    onPostClick: () -> Unit,
    onPrevClick: () -> Unit,
    onFirstClick: () -> Unit,
    onNextClick: () -> Unit,
    onLastClick: () -> Unit,
    onLLClick: () -> Unit,
    onRRClick: () -> Unit
) {
    val isMoveEnable = propertiesBoardMoveEnable > 0
    val postText = stringResource(
        if (pageType == BahamutPage.BAHAMUT_BOARD) R.string.post else R.string.bookmark
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(colors.toolbarBackground),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 靠右對齊時左側切換按鈕 (LL)
            if (toolbarLocation == 2) {
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .background(colors.toolbarBackgroundDisabled)
                        .clickable(onClick = onLLClick),
                    contentAlignment = Alignment.Center
                ) {
                    BahaText(
                        text = stringResource(R.string.toolbar_item_ll),
                        color = colors.dialogSelectArticleFocused.copy(alpha = 0.5f)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
            }

            val buttons: List<@Composable () -> Unit> = listOf(
                {
                    BahaButton(
                        text = postText,
                        type = ButtonType.NORMAL,
                        onClick = onPostClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                },
                {
                    BahaButton(
                        text = stringResource(R.string.prev_page),
                        type = ButtonType.NORMAL,
                        onClick = onPrevClick,
                        onLongClick = onFirstClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                },
                {
                    BahaButton(
                        text = stringResource(if (isMoveEnable) R.string.next_page else R.string.last_page),
                        type = ButtonType.NORMAL,
                        onClick = if (isMoveEnable) onNextClick else onLastClick,
                        onLongClick = onLastClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            )

            // 反轉順序判定
            val orderedButtons = if (toolbarOrder == 1) buttons.reversed() else buttons
            orderedButtons.forEachIndexed { index, btn ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colors.toolbarDivider)
                    )
                }
                btn()
            }

            // 靠左對齊時右側切換按鈕 (RR)
            if (toolbarLocation == 1) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .background(colors.toolbarBackgroundDisabled)
                        .clickable(onClick = onRRClick),
                    contentAlignment = Alignment.Center
                ) {
                    BahaText(
                        text = stringResource(R.string.toolbar_item_rr),
                        color = colors.dialogSelectArticleFocused.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * 側邊選單 Drawer
 */
@Composable
fun BoardEndDrawer(
    isOpen: Boolean,
    isLeft: Boolean,
    mode: Int,
    bookmarks: List<Bookmark>,
    isBlockEnabled: Boolean,
    colors: AppColors,
    onClose: () -> Unit,
    onEssenceClick: () -> Unit,
    onBookmarkManageClick: () -> Unit,
    onTabClick: (Int) -> Unit,
    onBookmarkItemClick: (Bookmark) -> Unit,
    onSearchClick: () -> Unit,
    onSelectClick: () -> Unit,
    onToggleBlock: () -> Unit,
    onBlockSettingClick: () -> Unit
) {
    if (isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground.copy(alpha = 0.5f))
                .clickable { onClose() }
        )
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(
            initialOffsetX = { if (isLeft) -it else it }
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { if (isLeft) -it else it }
        ) + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (isLeft) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.7f)
                    .widthIn(max = 280.dp)
                    .background(colors.pageBackground)
                    .clickable(enabled = false) {} // 阻止點擊穿透到遮罩
                    .pointerInput(isLeft) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if ((!isLeft && dragAmount > 20f) || (isLeft && dragAmount < -20f)) {
                                onClose()
                            }
                        }
                    }
            ) {
                // 1. 頂部工具列：精華區 / 書籤管理 / 關閉按鈕 (>)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(colors.toolbarBackground),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaButton(
                        text = stringResource(R.string.essence_page),
                        type = ButtonType.NORMAL,
                        onClick = onEssenceClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    BBSToolbarDivider()
                    BahaButton(
                        text = stringResource(R.string.bookmark_manager),
                        type = ButtonType.NORMAL,
                        onClick = onBookmarkManageClick,
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                    )
                    RightArrow { onBookmarkManageClick }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.toolbarDivider)
                )

                // 2. 中間清單：書籤 / 紀錄 清單
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
                                text = stringResource(R.string.list_empty),
                                color = colors.textSecondary
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(bookmarks.size) { bIndex ->
                                val bItem = bookmarks[bIndex]
                                val gyValue = bItem.gy.trim().ifEmpty { "0" }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onBookmarkItemClick(bItem) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        if (mode == 0) {
                                            // 第一列：關鍵字 / 標題
                                            BahaText(
                                                text = bItem.keyword.ifEmpty { stringResource(R.string.un_input) },
                                                color = colors.textPrimary,
                                                fontSize = AppTheme.fontSize.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            // 第二列：作者、標記 M、GY值
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                BahaText(
                                                    text = stringResource(R.string.author_),
                                                    color = colors.textSecondary,
                                                    fontSize = AppTheme.fontSize.caption
                                                )
                                                BahaText(
                                                    text = bItem.author.ifEmpty { stringResource(R.string.un_input) },
                                                    color = colors.bbsMailAuthor,
                                                    fontSize = AppTheme.fontSize.caption,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (bItem.mark == "y") {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    BahaText(
                                                        text = stringResource(R.string.word_m),
                                                        color = colors.bbsMailMark,
                                                        fontSize = AppTheme.fontSize.caption
                                                    )
                                                }
                                                Spacer(modifier = Modifier.weight(1f))
                                                BahaText(
                                                    text = stringResource(R.string.gy_),
                                                    color = colors.textSecondary,
                                                    fontSize = AppTheme.fontSize.caption
                                                )
                                                BahaText(
                                                    text = gyValue,
                                                    color = colors.bbsBoardGy,
                                                    fontSize = AppTheme.fontSize.caption
                                                )
                                            }
                                        } else {
                                            // 第一列：關鍵字 / 標題
                                            BahaText(
                                                text = bItem.keyword.ifEmpty { stringResource(R.string.un_input) },
                                                color = colors.textPrimary,
                                                fontSize = AppTheme.fontSize.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(colors.divider)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. 底部第一列：切換分頁 [書籤] | [紀錄]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.toolbarDivider)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(colors.toolbarBackground),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaButton(
                        text = stringResource(R.string.bookmark),
                        type = if (mode == 0) ButtonType.NORMAL else ButtonType.SECONDARY,
                        onClick = { onTabClick(0) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    BBSToolbarDivider()
                    BahaButton(
                        text = stringResource(R.string.record),
                        type = if (mode == 1) ButtonType.NORMAL else ButtonType.SECONDARY,
                        onClick = { onTabClick(1) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                // 4. 底部第二列：操作 [搜尋] | [選擇]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.toolbarDivider)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(colors.toolbarBackground),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaButton(
                        text = stringResource(R.string.search),
                        type = ButtonType.NORMAL,
                        onClick = onSearchClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    BBSToolbarDivider()
                    BahaButton(
                        text = stringResource(R.string.select),
                        type = ButtonType.NORMAL,
                        onClick = onSelectClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                // 5. 底部第三列：黑名單 [☑ 啟用] | [黑名單 >]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.toolbarDivider)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(colors.toolbarBackground),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .clickable { onToggleBlock() }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        BahaCheckbox(
                            checked = isBlockEnabled,
                            onCheckedChange = { onToggleBlock() }
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        BahaText(
                            text = stringResource(R.string.on),
                            color = colors.buttonText
                        )
                    }
                    BBSToolbarDivider()
                    BahaButton(
                        text = stringResource(R.string.system_setting_page_chapter_blocklist),
                        type = ButtonType.NORMAL,
                        onClick = onBlockSettingClick,
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight()
                    )
                    RightArrow { onBlockSettingClick }
                }
            }
        }
    }
}