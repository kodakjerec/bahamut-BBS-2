package com.kota.Bahamut.pages.boardPage

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BuildConfig
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
import com.kota.Bahamut.pages.model.ToolBarFloating
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.pages.theme.ThemeStore.getSelectTheme
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.rgbToInt
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.isBlockListContains
import com.kota.Bahamut.service.UserSettings.Companion.isBlockListContainsFuzzy
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAnimationEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListForTitle
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBoardMoveEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesDrawerLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesGestureOnBoardEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarOrder
import com.kota.Bahamut.service.UserSettings.Companion.propertiesUsername
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
import com.kota.telnetUI.textView.TelnetTextViewLarge
import java.util.Vector
import kotlin.math.abs

open class BoardMainPage : TelnetListPage(),
    DialogSearchArticleListener,
    DialogSelectArticleListener,
    PostArticlePageListener,
    BoardExtendOptionalPageListener,
    ASListViewExtentOptionalDelegate {
    var mainDrawerLayout: DrawerLayout? = null
    lateinit var mainLayout: RelativeLayout
    var boardTitle: String = ""
    var boardManager: String = ""
    var lastListAction: Int = BoardPageAction.Companion.LIST

    // com.kota.Bahamut.ListPage.TelnetListPage
    override var isItemBlockEnable: Boolean = false // 是否啟用黑名單
    var blockListForTitle: Boolean = false // 是否啟用黑名單套用至標題
    var isDrawerOpening: Boolean = false // 側邊選單正在開啟中
    val myBookmarkList: MutableList<Bookmark> = ArrayList<Bookmark>()
    lateinit var drawerListView: ListView
    lateinit var drawerListViewNone: TextView
    var myMode: Int = 0 // 現在開啟的是 0-書籤 1-紀錄
    lateinit var tabButtons: Array<Button>
    var showBookmarkButton: Button? = null // 顯示書籤按鈕
    var showHistoryButton: Button? = null // 顯示記錄按鈕
    var drawerLocation: Int = GravityCompat.END // 抽屜最後位置
    private var isPostDelayedSuccess = false

    /** 發文  */
    val mPostListener: View.OnClickListener =
        View.OnClickListener { view: View? -> this@BoardMainPage.onPostButtonClicked() }

    /** 最前頁  */
    val mFirstPageClickListener: OnLongClickListener = OnLongClickListener { view: View? ->
        this@BoardMainPage.moveToFirstPosition()
        true
    }

    /** 上一頁  */
    val mPrevPageClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        var firstIndex = listView?.firstVisiblePosition!!
        val endIndex = listView?.lastVisiblePosition!!
        val moveIndex = abs(endIndex - firstIndex)
        firstIndex -= moveIndex
        if (firstIndex < 0) firstIndex = 0
        setListViewSelection(firstIndex)
    }

    /** 下一頁  */
    private val lastEndIndexes = IntArray(3) // 最後頁的結束位置
    private var endIndexCheckCount = 0 // 結束位置檢查次數
    val mNextPageClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        var firstIndex = listView?.firstVisiblePosition!!
        val endIndex = listView?.lastVisiblePosition!!
        val moveIndex = abs(endIndex - firstIndex)
        firstIndex += moveIndex

        if (endIndexCheckCount > 0 && endIndex == lastEndIndexes[endIndexCheckCount - 1]) {
            lastEndIndexes[endIndexCheckCount] = endIndex
            endIndexCheckCount++

            // 連按超過三次
            if (endIndexCheckCount >= 3) {
                // Reset counter
                endIndexCheckCount = 0
                // Move to last position
                this@BoardMainPage.setManualLoadPage()
                this@BoardMainPage.moveToLastPosition()
                return@OnClickListener
            }
        } else {
            // Reset counter if endIndex changed
            endIndexCheckCount = 1
        }

        // Store current endIndex
        lastEndIndexes[0] = endIndex
        setListViewSelection(firstIndex)
    }

    /** 最後頁  */
    val mLastPageClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.setManualLoadPage()
        this@BoardMainPage.moveToLastPosition()
    }
    val mLastPageLongClickListener: OnLongClickListener = OnLongClickListener { view: View? ->
        this@BoardMainPage.setManualLoadPage()
        this@BoardMainPage.moveToLastPosition()
        true
    }

    /** 彈出側邊選單  */
    val mMenuButtonListener: View.OnClickListener = View.OnClickListener { view: View? ->
        if (mainDrawerLayout != null) {
            if (mainDrawerLayout!!.isDrawerOpen(drawerLocation)) {
                mainDrawerLayout!!.closeDrawer(drawerLocation)
            } else {
                drawerLocation = if (propertiesDrawerLocation == 0) {
                    GravityCompat.END
                } else {
                    GravityCompat.START
                }
                val menuView = mainDrawerLayout!!.findViewById<LinearLayout>(R.id.menu_view)!!
                val layoutParamsDrawer = menuView.layoutParams as DrawerLayout.LayoutParams
                layoutParamsDrawer.gravity = drawerLocation
                menuView.layoutParams = layoutParamsDrawer
                mainDrawerLayout!!.openDrawer(drawerLocation, propertiesAnimationEnable)
            }
            reloadBookmark()
        }
    }

    /** 跳出小視窗 全部已讀/全部未讀  */
    val mReadAllListener: View.OnClickListener = View.OnClickListener { view: View? ->
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
                        val data = create()
                            .pushString("vV\n")
                            .build()
                        TelnetClient.myInstance!!.sendDataToServer(data)
                        showShortToast(getContextString(R.string.board_main_read_all_msg01))
                    } else if (title == getContextString(R.string.board_main_unread_all)) {
                        val data = create()
                            .pushString("vU\n")
                            .build()
                        TelnetClient.myInstance!!.sendDataToServer(data)
                        showShortToast(getContextString(R.string.board_main_unread_all_msg01))
                    }
                }

                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }
            }).show()
    }

    var bookmarkAdapter: BaseAdapter = object : BaseAdapter() {
        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getCount(): Int {
            return this@BoardMainPage.myBookmarkList.size
        }

        override fun getItem(i: Int): Bookmark? {
            return this@BoardMainPage.myBookmarkList[i]
        }

        /** 顯示側邊選單書籤  */
        override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
            var view = view
            if (view == null) {
                view = BoardExtendBookmarkItemView(this@BoardMainPage.context)
            }
            val boardExtendBookmarkItemView = view as BoardExtendBookmarkItemView
            boardExtendBookmarkItemView.setBookmark(getItem(i))
            boardExtendBookmarkItemView.setDividerTopVisible(i == 0)
            return view
        }
    }

    var historyAdapter: BaseAdapter = object : BaseAdapter() {
        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getCount(): Int {
            return this@BoardMainPage.myBookmarkList.size
        }

        override fun getItem(i: Int): Bookmark? {
            return this@BoardMainPage.myBookmarkList[i]
        }

        /** 顯示側邊選單書籤  */
        override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
            var view = view
            if (view == null) {
                view = BoardExtendHistoryItemView(this@BoardMainPage.context)
            }
            val boardExtendHistoryItemView = view as BoardExtendHistoryItemView
            boardExtendHistoryItemView.setBookmark(getItem(i))
            boardExtendHistoryItemView.setDividerTopVisible(i == 0)
            return view
        }
    }

    /** 側邊選單 mListener  */
    var drawerListener: DrawerListener = object : DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

        override fun onDrawerOpened(drawerView: View) {}

        override fun onDrawerClosed(drawerView: View) {}

        override fun onDrawerStateChanged(newState: Int) {
            isDrawerOpening = newState != DrawerLayout.STATE_IDLE

            // 側邊選單未完全開啟 or 正要啟動狀態
            if (isDrawerOpening) {
                this@BoardMainPage.reloadBookmark()
            }
        }
    }

    /** 點書籤  */
    var bookmarkListener: OnItemClickListener =
        OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, j: Long ->
            this@BoardMainPage.closeDrawer()
            val bookmark = this@BoardMainPage.myBookmarkList[i]
            searchArticle(bookmark.keyword, bookmark.author, bookmark.mark, bookmark.gy)
        }

    /** 切換成書籤清單  */
    var buttonClickListener: View.OnClickListener = View.OnClickListener { aView ->
        myMode = if (aView === showBookmarkButton) {
            0
        } else {
            1
        }
        reloadBookmark(aView)
    }

    /** 搜尋文章  */
    var searchListener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.closeDrawer()
        this@BoardMainPage.showSearchArticleDialog()
    }

    /** 選擇文章  */
    var selectListener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.closeDrawer()
        this@BoardMainPage.showSelectArticleDialog()
    }

    /** 啟用/停用 黑名單  */
    var enableBlockListener: View.OnClickListener =
        View.OnClickListener { view: View? -> this@BoardMainPage.onChangeBlockStateButtonClicked() }

    /** 修改黑名單  */
    var editBlockListener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.closeDrawer()
        this@BoardMainPage.onEditBlockListButtonClicked()
    }

    /** 開啟書籤管理  */
    var editBookmarkListener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.closeDrawer()
        this@BoardMainPage.onBookmarkButtonClicked()
    }

    /** 靠左對其  */
    var btnLLListener: View.OnClickListener = View.OnClickListener { view: View? ->
        propertiesToolbarLocation = 1
        this@BoardMainPage.changeToolbarLocation()
    }

    /** 靠右對其  */
    var btnRRListener: View.OnClickListener = View.OnClickListener { view: View? ->
        propertiesToolbarLocation = 2
        this@BoardMainPage.changeToolbarLocation()
    }

    override val pageLayout: Int
        get() = R.layout.board_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BOARD

    override val isAutoLoadEnable: Boolean
        get() = true
    
    override fun onASListViewHandleExtentOptional(paramASListView: ASListView?, paramInt: Int): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")  // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.asFramework.pageController.ASViewController
    override fun onPageDidLoad() {
        super.onPageDidLoad()

        mainDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout?
        mainLayout = findViewById(R.id.content_view) as RelativeLayout

        val aSListView = mainLayout.findViewById<ASListView>(R.id.BoardPageListView)
        aSListView.extendOptionalDelegate = this
        aSListView.emptyView = mainLayout.findViewById<View>(R.id.BoardPageListEmptyView)
        bindListView(aSListView)

        mainLayout.findViewById<View>(R.id.BoardPagePostButton).setOnClickListener(mPostListener)
        mainLayout.findViewById<View>(R.id.BoardPageFirstPageButton)
            .setOnClickListener(mPrevPageClickListener)
        mainLayout.findViewById<View>(R.id.BoardPageFirstPageButton)
            .setOnLongClickListener(mFirstPageClickListener)
        // 下一頁
        val boardPageLatestPageButton =
            mainLayout.findViewById<Button>(R.id.BoardPageLatestPageButton)
        if (propertiesBoardMoveEnable > 0) {
            boardPageLatestPageButton.text = getContextString(R.string.next_page)
            boardPageLatestPageButton.setOnClickListener(mNextPageClickListener)
            boardPageLatestPageButton.setOnLongClickListener(mLastPageLongClickListener)
        } else {
            boardPageLatestPageButton.setOnClickListener(mLastPageClickListener)
        }
        mainLayout.findViewById<View>(R.id.BoardPageLLButton).setOnClickListener(btnLLListener)
        mainLayout.findViewById<View>(R.id.BoardPageRRButton).setOnClickListener(btnRRListener)

        // 側邊選單
        if (mainDrawerLayout != null) {
            // 替換外觀
            ThemeFunctions().layoutReplaceTheme(mainDrawerLayout?.findViewById<ViewGroup?>(R.id.menu_view))

            val drawerLayout = mainDrawerLayout?.findViewById<DrawerLayout?>(R.id.drawer_layout)
            if (drawerLayout != null) {
                val menuView = mainDrawerLayout?.findViewById<LinearLayout>(R.id.menu_view)!!
                val layoutParamsDrawer = menuView.layoutParams as DrawerLayout.LayoutParams
                layoutParamsDrawer.gravity = drawerLocation
                menuView.layoutParams = layoutParamsDrawer
                drawerLayout.addDrawerListener(drawerListener)
                // 根據手指位置設定側邊選單位置
                aSListView.setOnTouchListener { view: View?, motionEvent: MotionEvent? ->
                    if (isDrawerOpening) return@setOnTouchListener false
                    val screenWidth: Int =
                        context!!.resources.displayMetrics.widthPixels / 2
                    if (motionEvent!!.x < screenWidth) layoutParamsDrawer.gravity =
                        GravityCompat.START
                    else layoutParamsDrawer.gravity = GravityCompat.END
                    drawerLocation = layoutParamsDrawer.gravity
                    menuView.layoutParams = layoutParamsDrawer
                    false
                }
            }
            val searchArticleButton =
                mainDrawerLayout?.findViewById<View>(R.id.search_article_button)
            searchArticleButton?.setOnClickListener(searchListener)
            val selectArticleButton =
                mainDrawerLayout?.findViewById<View>(R.id.select_article_button)
            selectArticleButton?.setOnClickListener(selectListener)
            val blockEnableCheckbox =
                mainDrawerLayout?.findViewById<CheckBox?>(R.id.block_enable_button_checkbox)
            if (blockEnableCheckbox != null) {
                blockEnableCheckbox.isChecked = propertiesBlockListEnable
                blockEnableCheckbox.setOnClickListener(enableBlockListener)

                val textCheckbox =
                    mainDrawerLayout?.findViewById<TelnetTextViewLarge>(R.id.block_enable_button_checkbox_label)!!
                textCheckbox.setOnClickListener(enableBlockListener)
            }
            val blockSettingButton =
                mainDrawerLayout?.findViewById<View>(R.id.block_setting_button)
            blockSettingButton?.setOnClickListener(editBlockListener)
            val bookmarkEditButton =
                mainDrawerLayout?.findViewById<View>(R.id.bookmark_edit_button)
            bookmarkEditButton?.setOnClickListener(editBookmarkListener)
            // 側邊選單內的書籤
            drawerListView = mainDrawerLayout?.findViewById<ListView>(R.id.bookmark_list_view)!!
            drawerListViewNone =
                mainDrawerLayout?.findViewById<TextView>(R.id.bookmark_list_view_none)!!
            showBookmarkButton =
                mainDrawerLayout?.findViewById<Button>(R.id.show_bookmark_button)
            showHistoryButton =
                mainDrawerLayout?.findViewById<Button>(R.id.show_history_button)
            tabButtons = arrayOf<Button>(showBookmarkButton!!, showHistoryButton!!)
            showBookmarkButton?.setOnClickListener(buttonClickListener)
            showHistoryButton?.setOnClickListener(buttonClickListener)
            mainDrawerLayout?.findViewById<View>(R.id.bookmark_tab_button)!!
                .setOnClickListener(toEssencePageClickListener)
        }

        // 標題
        val boardPageHeaderView =
            mainLayout.findViewById<BoardHeaderView>(R.id.BoardPage_HeaderView)
        if (pageType == BahamutPage.BAHAMUT_BOARD) {
            boardPageHeaderView.setMenuButtonClickListener(mMenuButtonListener)
            boardPageHeaderView.setDetail1ClickListener(mReadAllListener)
        } else {
            boardPageHeaderView.setMenuButtonClickListener(null)
        }
        refreshHeaderView()

        this.isItemBlockEnable = propertiesBlockListEnable
        blockListForTitle = propertiesBlockListForTitle

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)

        // 解決android 14跳出軟鍵盤
        // 先把 focus 設定到其他目標物, 避免系統在回收過程一個個去 focus
        // keyword: clearFocusInternal
        mainLayout.requestFocus()

        // 工具列位置
        changeToolbarLocation()
        changeToolbarOrder()

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            // 任務完成
            // 關閉"正在自動登入"

            TempSettings.isUnderAutoToChat = false

            object: ASCoroutine() {
                override suspend fun run() {
                    dismissProcessingDialog()
                }
            }.postDelayed(500L)
        }

        // 跳到指定文章編號
        // 指定 boardMainPage 才能用
        if (this::class == BoardMainPage::class && TempSettings.lastVisitArticleNumber > 0) {
            // 從 classPage 進入到 boardMainPage
            if (ASNavigationController.currentController!!.lastViewController!!::class == ClassPage::class) {
                if (BuildConfig.DEBUG)
                    showShortToast(TempSettings.lastVisitArticleNumber.toString())

                object :ASCoroutine() {
                    override suspend fun run() {
                        // 置中顯示
                        val firstIndex = listView?.firstVisiblePosition!!
                        val endIndex = listView?.lastVisiblePosition!!
                        val moveIndex = abs(endIndex - firstIndex)
                        TempSettings.lastVisitArticleNumber -= moveIndex / 2

                        onSelectDialogDismissWIthIndex(TempSettings.lastVisitArticleNumber.toString())
                    }
                }.postDelayed(300L)
            }
        }
    }

    /** 按下精華區  */
    var toEssencePageClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        this.lastListAction = BoardPageAction.Companion.ESSENCE
        PageContainer.instance!!.pushBoardEssencePage(listName, boardTitle)
        navigationController.pushViewController(PageContainer.instance!!.boardEssencePage)
        TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.TAB)
    }

    /** 變更工具列位置  */
    fun changeToolbarLocation() {
        val toolbar = mainLayout.findViewById<LinearLayout>(R.id.toolbar)
        val toolbarBlock = mainLayout.findViewById<LinearLayout>(R.id.toolbar_block)
        val toolBarFloating =
            mainLayout.findViewById<ToolBarFloating>(R.id.ToolbarFloatingComponent)
        toolBarFloating.visibility = View.GONE

        // 最左邊最右邊
        val btnLL = toolbar.findViewById<Button>(R.id.BoardPageLLButton)
        val btnLLDivider = toolbar.findViewById<View>(R.id.toolbar_divider_0)
        val btnRR = toolbar.findViewById<Button>(R.id.BoardPageRRButton)
        val btnRRDivider = toolbar.findViewById<View>(R.id.toolbar_divider_3)
        btnLL.visibility = View.GONE
        btnLLDivider.visibility = View.GONE
        btnRR.visibility = View.GONE
        btnRRDivider.visibility = View.GONE

        val layoutParams = toolbar.layoutParams as RelativeLayout.LayoutParams
        val choiceToolbarLocation = propertiesToolbarLocation // 0-中間 1-靠左 2-靠右 3-浮動
        when (choiceToolbarLocation) {
            1 -> {
                // 底部-最左邊
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
                layoutParams.addRule(RelativeLayout.ALIGN_START)
                btnRR.visibility = View.VISIBLE
                btnRRDivider.visibility = View.VISIBLE
            }

            2 -> {
                // 底部-最右邊
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.removeRule(RelativeLayout.ALIGN_START)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
                btnLL.visibility = View.VISIBLE
                btnLLDivider.visibility = View.VISIBLE
            }

            3 -> {
                // 浮動
                // 去除 原本工具列
                toolbar.visibility = View.GONE
                // 去除底部卡位用view
                toolbarBlock.visibility = View.GONE
                // 浮動工具列
                toolBarFloating.visibility = View.VISIBLE
                // button setting
                toolBarFloating.setOnClickListenerSetting(mPostListener)
                val originalBtn = mainLayout.findViewById<Button>(R.id.BoardPagePostButton)
                toolBarFloating.setTextSetting(originalBtn.text.toString())
                // button 1
                toolBarFloating.setOnClickListener1(mPrevPageClickListener)
                toolBarFloating.setOnLongClickListener1(mFirstPageClickListener)
                toolBarFloating.setText1(getContextString(R.string.prev_page))
                // button 2
                if (propertiesBoardMoveEnable > 0) {
                    toolBarFloating.setOnClickListener2(mNextPageClickListener)
                    toolBarFloating.setOnLongClickListener2(mLastPageLongClickListener)
                    toolBarFloating.setText2(getContextString(R.string.next_page))
                } else {
                    toolBarFloating.setOnClickListener2(mLastPageClickListener)
                    toolBarFloating.setText2(getContextString(R.string.last_page))
                }
            }

            else -> {
                // 底部-中間
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        toolbar.layoutParams = layoutParams
    }

    /** 反轉按鈕順序  */
    fun changeToolbarOrder() {
        val toolbar = mainLayout.findViewById<LinearLayout>(R.id.toolbar)

        val choiceToolbarOrder = propertiesToolbarOrder
        if (choiceToolbarOrder == 1) {
            // 最左邊最右邊
            val btnLL = toolbar.findViewById<Button?>(R.id.BoardPageLLButton)
            val btnLLDivider = toolbar.findViewById<View>(R.id.toolbar_divider_0)
            val btnRR = toolbar.findViewById<Button?>(R.id.BoardPageRRButton)
            val btnRRDivider = toolbar.findViewById<View>(R.id.toolbar_divider_3)

            // 擷取中間的元素
            val allViews = ArrayList<View?>()
            for (i in toolbar.childCount - 3 downTo 2) {
                val view = toolbar.getChildAt(i)
                allViews.add(view)
            }

            // 清空
            toolbar.removeAllViews()

            // 插入
            toolbar.addView(btnLL)
            toolbar.addView(btnLLDivider)
            for (j in allViews.indices) {
                toolbar.addView(allViews[j])
            }
            toolbar.addView(btnRRDivider)
            toolbar.addView(btnRR)
        }
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onMenuButtonClicked(): Boolean {
        mMenuButtonListener.onClick(null)
        return true
    }

    /** 更新headerView  */
    fun refreshHeaderView() {
        var boardTitle1 = boardTitle
        boardTitle1 = boardTitle1.ifEmpty { getContextString(R.string.loading) }
        var boardManager1 = boardManager
        boardManager1 = boardManager1.ifEmpty { getContextString(R.string.loading) }
        val boardName = listName
        val headerView = mainLayout.findViewById<BoardHeaderView>(R.id.BoardPage_HeaderView)
        headerView.setData(boardTitle1, boardName, boardManager1)
    }

    override fun getListIdFromListName(aName: String?): String? {
        return "$aName[Board]"
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun loadPage(): TelnetListPageBlock {
        val load = BoardPageHandler.instance.load()
        if (!isInitialed) {
            val visitBoard = TempSettings.lastVisitBoard
            if (visitBoard != load.boardName) {
                // 紀錄最後瀏覽的看板
                TempSettings.lastVisitBoard = load.boardName
                clear()
                if (load.boardType == BoardPageAction.Companion.SEARCH) {
                    pushRefreshCommand(0)
                }
            }
            boardManager = load.boardManager
            boardTitle = load.boardTitle
            listName = load.boardName
        }
        return load
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun isItemCanLoadAtIndex(index: Int): Boolean {
        val boardPageItem = getItem(index) as BoardPageItem?
        if (boardPageItem != null) {
            // 紀錄正在看的討論串標題
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

    @Synchronized  // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.asFramework.pageController.ASViewController
    override fun onPageRefresh() {
        refreshHeaderView()
        super.onPageRefresh()
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onBackPressed(): Boolean {
        val drawerLayout = mainLayout.findViewById<DrawerLayout?>(R.id.drawer_layout)
        if (drawerLayout != null && drawerLayout.isDrawerOpen(drawerLocation)) {
            drawerLayout.closeDrawer(drawerLocation)
            return true
        }
        clear()
        navigationController.popViewController()
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        PageContainer.instance!!.cleanBoardPage()
        return true
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun onListViewItemLongClicked(itemView: View?, index: Int): Boolean {
        onListArticle(index + 1)
        return true
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onSearchButtonClicked(): Boolean {
        showSearchArticleDialog()
        return true
    }

    fun showSearchArticleDialog() {
        val dialogSearchArticle = DialogSearchArticle()
        dialogSearchArticle.setListener(this)
        dialogSearchArticle.show()
    }

    protected fun showSelectArticleDialog() {
        val dialogSelectArticle = DialogSelectArticle()
        dialogSelectArticle.setListener(this)
        dialogSelectArticle.show()
    }

    // com.kota.Bahamut.Dialogs.Dialog_SearchArticle_Listener
    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String>) {
        searchArticle(
            vector[0]!!,
            vector[1],
            if (vector[2] == "YES") "y" else "n",
            vector[3]
        )
    }

    /** 搜尋文章  */
    fun searchArticle(keyword: String, author: String, mark: String, myGY: String) {
        this.lastListAction = BoardPageAction.Companion.SEARCH
        val boardSearchPage = PageContainer.instance!!.boardSearchPage
        boardSearchPage.clear()
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

    /** 選擇文章  */
    // com.kota.Bahamut.Dialogs.Dialog_SelectArticle_Listener
    override fun onSelectDialogDismissWIthIndex(str: String) {
        var i: Int
        try {
            i = str.toInt() - 1
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
            i = -1
        }
        if (i >= 0) {
            setListViewSelection(i)
        }
    }

    fun onBookmarkButtonClicked() {
        navigationController.pushViewController(BookmarkManagePage(listName, this))
    }

    /** 長按串接文章  */
    fun onListArticle(i: Int) {
        this.lastListAction = BoardPageAction.Companion.LINK_TITLE
        val boardLinkedTitlePage = PageContainer.instance!!.boardLinkedTitlePage
        boardLinkedTitlePage.clear()
        navigationController.pushViewController(boardLinkedTitlePage)
        val state = instance.getState(boardLinkedTitlePage.getListIdFromListName(listName))
        state.top = 0
        state.position = 0
        pushCommand(BahamutCommandListArticle(i))
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onReceivedGestureRight(): Boolean {
        if (propertiesGestureOnBoardEnable) {
            if (this.isDrawerOpen || isDrawerOpening) {
                return false
            }
            onBackPressed()
            return true
        }
        return true
    }

    /** 發文  */
    protected open fun onPostButtonClicked() {
        val postArticlePage = PageContainer.instance!!.postArticlePage
        postArticlePage.setBoardPage(this)
        postArticlePage.setListener(this)
        navigationController.pushViewController(postArticlePage)
    }

    /** 按下推薦文章  */
    fun goodLoadingArticle() {
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string.do_gy))
            .setMessage(getContextString(R.string.gy_this_article))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string.do_gy))
            .setListener { aSAlertDialog: ASAlertDialog?, i2: Int ->
                if (i2 == 1) {
                    this@BoardMainPage.pushCommand(BahamutCommandGoodArticle(loadingItemNumber))
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    /** 按下推文  */
    fun pushArticle() {
        this@BoardMainPage.pushCommand(BahamutCommandPushArticle(loadingItemNumber))

        pushArticleASCoroutine?.cancel()
        pushArticleASCoroutine?.postDelayed(2000L)
        isPostDelayedSuccess = false
    }

    /** 開啟推文小視窗  */
    fun openPushArticleDialog() {
        pushArticleASCoroutine?.cancel()
        isPostDelayedSuccess = true

        val dialog = DialogPushArticle()
        dialog.show()
    }

    /** 沒有開啟推文小視窗, 視為沒開放功能  */
    var pushArticleASCoroutine: ASCoroutine? = object : ASCoroutine() {
        override suspend fun run() {
            if (!isPostDelayedSuccess) {
                onPagePreload()
                showLongToast("沒反應，看板未開放推文")
            }
        }
    }

    /** 提供給 stateHandler 的取消介面  */
    fun cancelRunner() {
        pushArticleASCoroutine?.cancel()
        isPostDelayedSuccess = true
    }

    /** 轉寄至信箱  */
    fun funSendMail() {
        pushCommand(BahamutCommandFSendMail(propertiesUsername))
    }

    /** 最前篇  */
    fun loadTheSameTitleTop() {
        onLoadItemStart()
        pushCommand(BahamutCommandTheSameTitleTop(loadingItemNumber))
    }

    /** 最後篇  */
    fun loadTheSameTitleBottom() {
        onLoadItemStart()
        pushCommand(BahamutCommandTheSameTitleBottom(loadingItemNumber))
    }

    /** 上一篇  */
    fun loadTheSameTitleUp() {
        onLoadItemStart()
        pushCommand(BahamutCommandTheSameTitleUp(loadingItemNumber))
    }

    /** 下一篇  */
    fun loadTheSameTitleDown() {
        onLoadItemStart()
        pushCommand(BahamutCommandTheSameTitleDown(loadingItemNumber))
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun isItemBlocked(aItem: TelnetListPageItem?): Boolean {
        if (aItem != null) {
            return this.isItemBlockEnable && isBlockListContains((aItem as BoardPageItem).author)
        }
        return false
    }

    /** 啟用/停用 黑名單  */
    fun onChangeBlockStateButtonClicked() {
        propertiesBlockListEnable = !this.isItemBlockEnable
        notifyDataUpdated()
        this.isItemBlockEnable = propertiesBlockListEnable
        if (mainDrawerLayout != null) {
            val blockEnableCheckbox =
                mainDrawerLayout?.findViewById<CheckBox>(R.id.block_enable_button_checkbox)!!
            blockEnableCheckbox.isChecked = this.isItemBlockEnable
        }
        reloadListView()
    }

    fun onEditBlockListButtonClicked() {
        navigationController.pushViewController(BlockListPage())
    }

    /** 點下文章  */
    override fun loadItemAtIndex(index: Int) {
        if (isItemCanLoadAtIndex(index)) {
            val articlePage = PageContainer.instance!!.articlePage
            articlePage.setBoardPage(this)
            articlePage.clear()
            navigationController.pushViewController(articlePage)

            super.loadItemAtIndex(index)
        }
    }

    fun prepareInitial() {
        isInitialed = false
    }

    /** 書籤管理->按下書籤  */
    // com.kota.Bahamut.BookmarkPage.BoardExtendOptionalPageListener
    override fun onBoardExtendOptionalPageDidSelectBookmark(bookmark: Bookmark?) {
        if (bookmark != null) {
            this.lastListAction = BoardPageAction.Companion.SEARCH
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

    // android.widget.Adapter
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View? {
        var myView = view
        val itemIndex = i + 1
        val block = ItemUtils.getBlock(itemIndex)
        val boardPageItem = getItem(i) as BoardPageItem?
        if (boardPageItem == null && currentBlock != block && !isLoadingBlock(itemIndex)) {
            loadBoardBlock(block)
        }
        if (myView == null) {
            myView = BoardPageItemView(context)
            myView.layoutParams = AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val boardPageItemView = myView as BoardPageItemView
        boardPageItemView.setItem(boardPageItem)
        boardPageItemView.setNumber(itemIndex)

        if (boardPageItem != null && this.isItemBlockEnable) {
            if (isBlockListContains(boardPageItem.author)) {
                boardPageItem.isBlocked = true
            } else if (blockListForTitle && isBlockListContainsFuzzy(boardPageItem.title)) {
                boardPageItem.isBlocked = true
            } else {
                boardPageItem.isBlocked = false
            }

            if (boardPageItem.isBlocked) boardPageItemView.visible = false
        } else {
            boardPageItemView.visible = true
        }

        return myView
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock) {
        BoardPageBlock.recycle(telnetListPageBlock as BoardPageBlock)
    }

    //
    override fun recycleItem(telnetListPageItem: TelnetListPageItem) {
        BoardPageItem.recycle(telnetListPageItem as BoardPageItem?)
    }

    /** 修改文章 訊息發出 */
    override fun onPostDialogEditButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String?,
        str2: String?,
        str3: String?
    ) {
        pushCommand(BahamutCommandEditArticle(str, str2!!, str3!!))
        if (TRACE_LOG_ENABLE) {
            try {
                Log.i(
                    "BoardMainPageTrace",
                    "time=${java.time.Instant.now()} thread=${Thread.currentThread().name} isMain=${ASCoroutine.isMainThread} caller=${traceCaller()} action=onPostDialogSendButtonClicked isPageAppeared=${isPageAppeared}"
                )
            } catch (_: Exception) {
            }
        }
        // 強制刷新列表 UI（執行於主執行緒）
        safeNotifyDataSetChanged()
    }

    /** 發表文章/回覆文章 訊息發出  */
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

        // 回應到作者信箱
        if (str3 != null && str3 == "M") {
            return
        }
        // 發文中等待視窗
        showProcessingDialog(getContextString(R.string.board_page_post_waiting_message_1))
        postWaitingDialog1?.postDelayed(3000L)
        postWaitingDialog2?.postDelayed(6000L)
    }
    // 3秒後跳出
    val postWaitingDialog1: ASCoroutine? = object: ASCoroutine() {
        override suspend fun run() {
            setMessage(getContextString(R.string.board_page_post_waiting_message_2))
        }
    }
    // 6秒後跳出
    val postWaitingDialog2: ASCoroutine? = object: ASCoroutine() {
        override suspend fun run() {
            setMessage(getContextString(R.string.board_page_post_waiting_message_3))
        }
    }

    /** 引言過多, 回逤發文時的設定  */
    fun recoverPost() {
        ASCoroutine.runOnMain {
                cleanCommand() // 清除引言過多留下的command buffer
                val page = PageContainer.instance!!.postArticlePage
                page.setRecover()
        }

        postWaitingDialog1?.cancel()
        postWaitingDialog2?.cancel()
        dismissProcessingDialog()
    }

    /** 完成發文  */
    fun finishPost() {
        ASCoroutine.runOnMain {
            val page = PageContainer. instance!!.postArticlePage
            page.closeArticle()
        }

        postWaitingDialog1?.cancel()
        postWaitingDialog2?.cancel()
        dismissProcessingDialog()
    }

    fun reloadBookmark(aView: View? = null) {
        val store = TempSettings.bookmarkStore
        if (store != null) {
            if (myMode == 0) {
                store.getBookmarkList(listName).loadBookmarkList(myBookmarkList)

            } else {
                store.getBookmarkList(listName).loadHistoryList(myBookmarkList)
            }
        }
        if (TRACE_LOG_ENABLE) {
            try { Log.i("BoardMainPageTrace", "time=${java.time.Instant.now()} thread=${Thread.currentThread().name} isMain=${ASCoroutine.isMainThread} caller=${traceCaller()} action=reloadBookmark myBookmarkListSize=${myBookmarkList.size}") } catch (_: Exception) { }
        }
        if (myBookmarkList.isEmpty()) {
            drawerListView.visibility = View.GONE
            drawerListViewNone.visibility = View.VISIBLE
        } else {
            drawerListView.visibility = View.VISIBLE
            drawerListViewNone.visibility = View.GONE
        }

        // 切換View本體
        if (myMode == 0)
            drawerListView.adapter = bookmarkAdapter
        else
            drawerListView.adapter = historyAdapter
            
        if (isPageAppeared) {
            // 只更新當前顯示的 adapter
            if (myMode == 0)
                bookmarkAdapter.notifyDataSetChanged()
            else
                historyAdapter.notifyDataSetChanged()
        }
        if (drawerListView.onItemClickListener == null)
            drawerListView.onItemClickListener = bookmarkListener

        // 填上顏色: 書籤. 紀錄
        var selectedView = aView
        if(selectedView == null) selectedView = tabButtons[myMode]
        val theme = getSelectTheme()
        for (tabButton in this@BoardMainPage.tabButtons) {
            if (tabButton === selectedView) {
                tabButton.setTextColor(rgbToInt(theme.textColor))
                tabButton.setBackgroundColor(rgbToInt(theme.backgroundColor))
            } else {
                tabButton.setTextColor(rgbToInt(theme.textColorDisabled))
                tabButton.setBackgroundColor(rgbToInt(theme.backgroundColorDisabled))
            }
        }
    }

    fun closeDrawer() {
        val drawerLayout = mainLayout.findViewById<DrawerLayout?>(R.id.drawer_layout)
        drawerLayout?.closeDrawers()
    }

    val isDrawerOpen: Boolean
        /** 側邊選單已開啟 或 正在開啟中  */
        get() {
            val drawerLayout =
                mainLayout.findViewById<DrawerLayout?>(R.id.drawer_layout)
            if (drawerLayout != null) {
                return drawerLayout.isDrawerOpen(drawerLocation)
            }
            return false
        }

    override fun onSearchDialogCancelButtonClicked() {
    }
}