package com.kota.Bahamut.pages.boardPage

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
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
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.setMessage
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASListViewExtentOptionalDelegate
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
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
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.blockListPage.BlockListPage
import com.kota.Bahamut.pages.bookmarkPage.BoardExtendOptionalPageListener
import com.kota.Bahamut.pages.bookmarkPage.BookmarkManagePage
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.Bahamut.pages.model.BoardPageHandler
import com.kota.Bahamut.pages.model.BoardPageItem
import com.kota.Bahamut.pages.model.ToolBarFloating
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.PostArticlePageListener
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.pages.theme.ThemeStore.getSelectTheme
import com.kota.Bahamut.R
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
import com.kota.telnet.logic.ItemUtils
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnetUI.textView.TelnetTextViewLarge
import java.util.Timer
import java.util.TimerTask
import java.util.Vector
import kotlin.math.abs

open class BoardMainPage : TelnetListPage(), DialogSearchArticleListener,
    DialogSelectArticleListener, PostArticlePageListener, BoardExtendOptionalPageListener,
    ASListViewExtentOptionalDelegate {
    var mainDrawerLayout: DrawerLayout? = null
    var mainLayout: RelativeLayout? = null
    protected var _board_title: String? = null
    protected var _board_manager: String? = null
    var lastListAction: Int = BoardPageAction.Companion.LIST
    var _initialed: Boolean = false
    var _refresh_header_view: Boolean = false // 正在更新標題列

    // com.kota.Bahamut.ListPage.TelnetListPage
    var isItemBlockEnable: Boolean = false // 是否啟用黑名單
    var blockListForTitle: Boolean = false // 是否啟用黑名單套用至標題
    var _isDrawerOpening: Boolean = false // 側邊選單正在開啟中
    val _bookmarkList: MutableList<Bookmark> = ArrayList<Bookmark>()
    var _drawerListView: ListView? = null
    var _drawerListViewNone: TextView? = null
    var _mode: Int = 0 // 現在開啟的是 0-書籤 1-紀錄
    var _tab_buttons: Array<Button>
    var _show_bookmark_button: Button? = null // 顯示書籤按鈕
    var _show_history_button: Button? = null // 顯示記錄按鈕
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

    /** 前一頁  */
    val mPrevPageClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        var firstIndex = listViewWidget?.getFirstVisiblePosition()
        val endIndex = listViewWidget?.getLastVisiblePosition()
        val moveIndex = abs(endIndex - firstIndex)
        firstIndex -= moveIndex
        if (firstIndex < 0) firstIndex = 0
        setListViewSelection(firstIndex)
    }

    /** 下一頁  */
    private val lastEndIndexes = IntArray(3) // 最後頁的結束位置
    private var endIndexCheckCount = 0 // 結束位置檢查次數
    val mNextPageClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        var firstIndex = listViewWidget?.getFirstVisiblePosition()
        val endIndex = listViewWidget?.getLastVisiblePosition()
        val moveIndex = abs(endIndex - firstIndex)
        firstIndex += moveIndex

        if (endIndexCheckCount > 0 && endIndex == lastEndIndexes[endIndexCheckCount - 1]) {
            lastEndIndexes[endIndexCheckCount] = endIndex
            endIndexCheckCount++

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
        val drawerLayout = this@BoardMainPage.findViewById(R.id.drawer_layout) as DrawerLayout?
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(drawerLocation)) {
                drawerLayout.closeDrawer(drawerLocation)
            } else {
                if (propertiesDrawerLocation == 0) {
                    drawerLocation = GravityCompat.END
                } else {
                    drawerLocation = GravityCompat.START
                }
                val menu_view = mainDrawerLayout?.findViewById<LinearLayout>(R.id.menu_view)
                val layoutParams_drawer = menu_view.getLayoutParams() as DrawerLayout.LayoutParams
                layoutParams_drawer.gravity = drawerLocation
                menu_view.setLayoutParams(layoutParams_drawer)
                drawerLayout.openDrawer(drawerLocation, propertiesAnimationEnable)
            }
        }
    }

    /** 跳出小視窗 全部已讀/全部未讀  */
    val mReadAllListener: View.OnClickListener = View.OnClickListener { view: View? ->
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem(getContextString(R.string.board_main_read_all))
            .addItem(getContextString(R.string.board_main_unread_all))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ): Boolean {
                    return@OnClickListener true
                }

                override fun onListDialogItemClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ) {
                    if (aTitle == getContextString(R.string.board_main_read_all)) {
                        val data = create()
                            .pushString("vV\n")
                            .build()
                        TelnetClient.client?.sendDataToServer(data)
                        showShortToast(getContextString(R.string.board_main_read_all_msg01))
                    } else if (aTitle == getContextString(R.string.board_main_unread_all)) {
                        val data = create()
                            .pushString("vU\n")
                            .build()
                        TelnetClient.client?.sendDataToServer(data)
                        showShortToast(getContextString(R.string.board_main_unread_all_msg01))
                    }
                }
            }).show()
    }

    var _bookmark_adapter: BaseAdapter = object : BaseAdapter() {
        // android.widget.Adapter
        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        // android.widget.Adapter
        override fun getCount(): Int {
            return this@BoardMainPage._bookmarkList.size
        }

        // android.widget.Adapter
        override fun getItem(i: Int): Bookmark? {
            return this@BoardMainPage._bookmarkList.get(i)
        }

        /** 顯示側邊選單書籤  */
        // android.widget.Adapter
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

    var _history_adapter: BaseAdapter = object : BaseAdapter() {
        // android.widget.Adapter
        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        // android.widget.Adapter
        override fun getCount(): Int {
            return this@BoardMainPage._bookmarkList.size
        }

        // android.widget.Adapter
        override fun getItem(i: Int): Bookmark? {
            return this@BoardMainPage._bookmarkList.get(i)
        }

        /** 顯示側邊選單書籤  */
        // android.widget.Adapter
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
    var _drawer_listener: DrawerListener = object : DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

        override fun onDrawerOpened(drawerView: View) {}

        override fun onDrawerClosed(drawerView: View) {}

        override fun onDrawerStateChanged(newState: Int) {
            _isDrawerOpening = newState != DrawerLayout.STATE_IDLE

            // 側邊選單未完全開啟 or 正要啟動狀態
            if (!this.isDrawerOpen || _isDrawerOpening) {
                this@BoardMainPage.reloadBookmark()
            }
        }
    }

    /** 點書籤  */
    var _bookmark_listener: OnItemClickListener =
        OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, j: Long ->
            this@BoardMainPage.closeDrawer()
            val bookmark = this@BoardMainPage._bookmarkList.get(i)
            searchArticle(bookmark.keyword!!, bookmark.author, bookmark.mark, bookmark.gy)
        }

    /** 切換成書籤清單  */
    var buttonClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(aView: View?) {
            if (aView === _show_bookmark_button) {
                _mode = 0
            } else {
                _mode = 1
            }
            if (_drawerListView != null) {
                if (_mode == 0) _drawerListView?.setAdapter(_bookmark_adapter)
                else _drawerListView?.setAdapter(_history_adapter)
                if (_drawerListView?.getOnItemClickListener() == null) _drawerListView?.setOnItemClickListener(
                    _bookmark_listener
                )
            }
            reloadBookmark()

            // 切換頁籤
            val theme = getSelectTheme()
            for (tab_button in this@BoardMainPage._tab_buttons) {
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

    /** 搜尋文章  */
    var _search_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.closeDrawer()
        this@BoardMainPage.showSearchArticleDialog()
    }

    /** 選擇文章  */
    var _select_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.closeDrawer()
        this@BoardMainPage.showSelectArticleDialog()
    }

    /** 啟用/停用 黑名單  */
    var _enable_block_listener: View.OnClickListener =
        View.OnClickListener { view: View? -> this@BoardMainPage.onChangeBlockStateButtonClicked() }

    /** 修改黑名單  */
    var _edit_block_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.closeDrawer()
        this@BoardMainPage.onEditBlockListButtonClicked()
    }

    /** 開啟書籤管理  */
    var _edit_bookmark_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        this@BoardMainPage.closeDrawer()
        this@BoardMainPage.onBookmarkButtonClicked()
    }

    /** 靠左對其  */
    var _btnLL_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        propertiesToolbarLocation = 1
        this@BoardMainPage.changeToolbarLocation()
    }

    /** 靠右對其  */
    var _btnRR_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        propertiesToolbarLocation = 2
        this@BoardMainPage.changeToolbarLocation()
    }

    open val pageLayout: Int
        // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.asFramework.pageController.ASViewController
        get() = R.layout.board_page

    open val pageType: Int
        // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.asFramework.pageController.ASViewController
        get() = BahamutPage.BAHAMUT_BOARD

    open val isAutoLoadEnable: Boolean
        // com.kota.Bahamut.ListPage.TelnetListPage
        get() = true

    // com.kota.asFramework.ui.ASListViewExtentOptionalDelegate
    override fun onASListViewHandleExtentOptional(aSListView: ASListView?, i: Int): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")  // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.asFramework.pageController.ASViewController
    override fun onPageDidLoad() {
        super.onPageDidLoad()

        mainDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout?
        mainLayout = findViewById(R.id.content_view) as RelativeLayout?

        val aSListView = mainLayout?.findViewById<ASListView>(R.id.BoardPageListView)
        aSListView.extendOptionalDelegate = this
        aSListView.setEmptyView(mainLayout?.findViewById<View>(R.id.BoardPageListEmptyView))
        listView = aSListView

        mainLayout?.findViewById<View>(R.id.BoardPagePostButton).setOnClickListener(mPostListener)
        mainLayout?.findViewById<View>(R.id.BoardPageFirstPageButton)
            .setOnClickListener(mPrevPageClickListener)
        mainLayout?.findViewById<View>(R.id.BoardPageFirstPageButton)
            .setOnLongClickListener(mFirstPageClickListener)
        // 下一頁
        val boardPageLatestPageButton =
            mainLayout?.findViewById<Button>(R.id.BoardPageLatestPageButton)
        if (propertiesBoardMoveEnable > 0) {
            boardPageLatestPageButton.setText(getContextString(R.string.next_page))
            boardPageLatestPageButton.setOnClickListener(mNextPageClickListener)
            boardPageLatestPageButton.setOnLongClickListener(mLastPageLongClickListener)
        } else {
            boardPageLatestPageButton.setOnClickListener(mLastPageClickListener)
        }
        mainLayout?.findViewById<View>(R.id.BoardPageLLButton).setOnClickListener(_btnLL_listener)
        mainLayout?.findViewById<View>(R.id.BoardPageRRButton).setOnClickListener(_btnRR_listener)

        // 側邊選單
        if (mainDrawerLayout != null) {
            // 替換外觀
            ThemeFunctions().layoutReplaceTheme(mainDrawerLayout?.findViewById<ViewGroup?>(R.id.menu_view))

            val drawerLayout = mainDrawerLayout?.findViewById<DrawerLayout?>(R.id.drawer_layout)
            if (drawerLayout != null) {
                val menu_view = mainDrawerLayout?.findViewById<LinearLayout>(R.id.menu_view)
                val layoutParams_drawer = menu_view.getLayoutParams() as DrawerLayout.LayoutParams
                layoutParams_drawer.gravity = drawerLocation
                menu_view.setLayoutParams(layoutParams_drawer)
                drawerLayout.addDrawerListener(_drawer_listener)
                // 根據手指位置設定側邊選單位置
                aSListView.setOnTouchListener(OnTouchListener { view: View?, motionEvent: MotionEvent? ->
                    if (_isDrawerOpening) return@setOnTouchListener false
                    val screenWidth: Int =
                        context.getResources().getDisplayMetrics().widthPixels / 2
                    if (motionEvent?.getX() < screenWidth) layoutParams_drawer.gravity =
                        GravityCompat.START
                    else layoutParams_drawer.gravity = GravityCompat.END
                    drawerLocation = layoutParams_drawer.gravity
                    menu_view.setLayoutParams(layoutParams_drawer)
                    false
                })
            }
            val search_article_button =
                mainDrawerLayout?.findViewById<View>(R.id.search_article_button)
            if (search_article_button != null) {
                search_article_button.setOnClickListener(_search_listener)
            }
            val select_article_button =
                mainDrawerLayout?.findViewById<View>(R.id.select_article_button)
            if (select_article_button != null) {
                select_article_button.setOnClickListener(_select_listener)
            }
            val block_enable_checkbox =
                mainDrawerLayout?.findViewById<CheckBox?>(R.id.block_enable_button_checkbox)
            if (block_enable_checkbox != null) {
                block_enable_checkbox.setChecked(propertiesBlockListEnable)
                block_enable_checkbox.setOnClickListener(_enable_block_listener)

                val text_checkbox =
                    mainDrawerLayout?.findViewById<TelnetTextViewLarge>(R.id.block_enable_button_checkbox_label)
                text_checkbox.setOnClickListener(_enable_block_listener)
            }
            val block_setting_button =
                mainDrawerLayout?.findViewById<View>(R.id.block_setting_button)
            if (block_setting_button != null) {
                block_setting_button.setOnClickListener(_edit_block_listener)
            }
            val bookmark_edit_button =
                mainDrawerLayout?.findViewById<View>(R.id.bookmark_edit_button)
            if (bookmark_edit_button != null) {
                bookmark_edit_button.setOnClickListener(_edit_bookmark_listener)
            }
            // 側邊選單內的書籤
            _drawerListView = mainDrawerLayout?.findViewById<ListView?>(R.id.bookmark_list_view)
            _drawerListViewNone =
                mainDrawerLayout?.findViewById<TextView>(R.id.bookmark_list_view_none)
            if (_drawerListView != null) {
                _show_bookmark_button =
                    mainDrawerLayout?.findViewById<Button>(R.id.show_bookmark_button)
                _show_history_button =
                    mainDrawerLayout?.findViewById<Button>(R.id.show_history_button)
                _tab_buttons = arrayOf<Button>(_show_bookmark_button!!, _show_history_button!!)
                _show_bookmark_button?.setOnClickListener(buttonClickListener)
                _show_history_button?.setOnClickListener(buttonClickListener)
                if (_mode == 0) _show_bookmark_button?.performClick()
                else _show_history_button?.performClick()
            }
            mainDrawerLayout?.findViewById<View>(R.id.bookmark_tab_button)
                .setOnClickListener(toEssencePageClickListener)
        }

        // 標題
        val boardPageHeaderView =
            mainLayout?.findViewById<BoardHeaderView>(R.id.BoardPage_HeaderView)
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
        if (mainDrawerLayout != null) mainDrawerLayout?.requestFocus()

        // 工具列位置
        changeToolbarLocation()
        changeToolbarOrder()

        // 自動登入洽特
        if (TempSettings.isUnderAutoToChat) {
            // 任務完成
            // 關閉"正在自動登入"

            TempSettings.isUnderAutoToChat = false

            val timer = Timer()
            val task1: TimerTask = object : TimerTask() {
                override fun run() {
                    dismissProcessingDialog()
                }
            }
            val task2: TimerTask = object : TimerTask() {
                override fun run() {
                    // 跳到指定文章編號
                    if (TempSettings.lastVisitArticleNumber > 0) {
                        onSelectDialogDismissWIthIndex(TempSettings.lastVisitArticleNumber.toString())
                    }
                }
            }
            timer.schedule(task1, 500)
            timer.schedule(task2, 500)
        }
    }

    /** 按下精華區  */
    var toEssencePageClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        this.lastListAction = BoardPageAction.Companion.ESSENCE
        PageContainer.instance?.pushBoardEssencePage(name, _board_title)
        navigationController.pushViewController(PageContainer.instance?.getBoardEssencePage())
        TelnetClient.client?.sendKeyboardInputToServer(TelnetKeyboard.TAB)
    }

    /** 變更工具列位置  */
    fun changeToolbarLocation() {
        val toolbar = mainLayout?.findViewById<LinearLayout>(R.id.toolbar)
        val toolbarBlock = mainLayout?.findViewById<LinearLayout>(R.id.toolbar_block)
        val toolBarFloating =
            mainLayout?.findViewById<ToolBarFloating>(R.id.ToolbarFloatingComponent)
        toolBarFloating.setVisibility(View.GONE)

        // 最左邊最右邊
        val _btnLL = toolbar.findViewById<Button>(R.id.BoardPageLLButton)
        val _btnLLDivider = toolbar.findViewById<View>(R.id.toolbar_divider_0)
        val _btnRR = toolbar.findViewById<Button>(R.id.BoardPageRRButton)
        val _btnRRDivider = toolbar.findViewById<View>(R.id.toolbar_divider_3)
        _btnLL.setVisibility(View.GONE)
        _btnLLDivider.setVisibility(View.GONE)
        _btnRR.setVisibility(View.GONE)
        _btnRRDivider.setVisibility(View.GONE)

        val layoutParams = toolbar.getLayoutParams() as RelativeLayout.LayoutParams
        val choice_toolbar_location = propertiesToolbarLocation // 0-中間 1-靠左 2-靠右 3-浮動
        when (choice_toolbar_location) {
            1 -> {
                // 底部-最左邊
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
                layoutParams.addRule(RelativeLayout.ALIGN_START)
                _btnRR.setVisibility(View.VISIBLE)
                _btnRRDivider.setVisibility(View.VISIBLE)
            }

            2 -> {
                // 底部-最右邊
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.removeRule(RelativeLayout.ALIGN_START)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
                _btnLL.setVisibility(View.VISIBLE)
                _btnLLDivider.setVisibility(View.VISIBLE)
            }

            3 -> {
                // 浮動
                // 去除 原本工具列
                toolbar.setVisibility(View.GONE)
                // 去除底部卡位用view
                toolbarBlock.setVisibility(View.GONE)
                // 浮動工具列
                toolBarFloating.setVisibility(View.VISIBLE)
                // button setting
                toolBarFloating.setOnClickListenerSetting(mPostListener)
                val OriginalBtn = mainLayout?.findViewById<Button>(R.id.BoardPagePostButton)
                toolBarFloating.setTextSetting(OriginalBtn.getText().toString())
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

        toolbar.setLayoutParams(layoutParams)
    }

    /** 反轉按鈕順序  */
    fun changeToolbarOrder() {
        val toolbar = mainLayout?.findViewById<LinearLayout>(R.id.toolbar)

        val choice_toolbar_order = propertiesToolbarOrder
        if (choice_toolbar_order == 1) {
            // 最左邊最右邊
            val _btnLL = toolbar.findViewById<Button?>(R.id.BoardPageLLButton)
            val _btnLLDivider = toolbar.findViewById<View>(R.id.toolbar_divider_0)
            val _btnRR = toolbar.findViewById<Button?>(R.id.BoardPageRRButton)
            val _btnRRDivider = toolbar.findViewById<View>(R.id.toolbar_divider_3)

            // 擷取中間的元素
            val allViews = ArrayList<View?>()
            for (i in toolbar.getChildCount() - 3 downTo 2) {
                val view = toolbar.getChildAt(i)
                allViews.add(view)
            }

            // 清空
            toolbar.removeAllViews()

            // 插入
            toolbar.addView(_btnLL)
            toolbar.addView(_btnLLDivider)
            for (j in allViews.indices) {
                toolbar.addView(allViews.get(j))
            }
            toolbar.addView(_btnRRDivider)
            toolbar.addView(_btnRR)
        }
    }

    // com.kota.asFramework.pageController.ASViewController
    protected override fun onMenuButtonClicked(): Boolean {
        mMenuButtonListener.onClick(null)
        return true
    }

    /** 更新headerView  */
    fun refreshHeaderView() {
        var board_title = _board_title
        board_title =
            if (board_title == null || board_title.isEmpty()) getContextString(R.string.loading) else board_title
        var board_manager = _board_manager
        board_manager =
            if (board_manager == null || board_manager.isEmpty()) getContextString(R.string.loading) else board_manager
        val board_name = name
        val header_view = mainLayout?.findViewById<BoardHeaderView?>(R.id.BoardPage_HeaderView)
        if (header_view != null) {
            header_view.setData(board_title, board_name, board_manager)
        }
    }

    public override fun getListIdFromListName(str: String?): String? {
        return str + "[Board]"
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    public override fun loadPage(): TelnetListPageBlock {
        val load = BoardPageHandler.instance?.load()
        if (!_initialed) {
            val _lastVisitBoard = TempSettings.lastVisitBoard
            if (_lastVisitBoard != load.boardName) {
                // 紀錄最後瀏覽的看板
                TempSettings.lastVisitBoard = load.boardName
                clear()
                if (load.boardType == BoardPageAction.Companion.SEARCH) {
                    pushRefreshCommand(0)
                }
            }
            _board_manager = load.boardManager
            _board_title = load.boardTitle
            name = load.boardName
            _refresh_header_view = true
            _initialed = true
        }
        return load
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    public override fun isItemCanLoadAtIndex(i: Int): Boolean {
        val boardPageItem = getItem(i) as BoardPageItem?
        if (boardPageItem != null) {
            // 紀錄正在看的討論串標題
            TempSettings.boardFollowTitle = boardPageItem.title
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
        super.onPageRefresh()
        if (_refresh_header_view) {
            refreshHeaderView()
            _refresh_header_view = false
        }
    }

    // com.kota.asFramework.pageController.ASViewController
    protected override fun onBackPressed(): Boolean {
        val drawerLayout = mainLayout?.findViewById<DrawerLayout?>(R.id.drawer_layout)
        if (drawerLayout != null && drawerLayout.isDrawerOpen(drawerLocation)) {
            drawerLayout.closeDrawer(drawerLocation)
            return true
        }
        clear()
        navigationController.popViewController()
        TelnetClient.client?.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        PageContainer.instance?.cleanBoardPage()
        return true
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    override fun onListViewItemLongClicked(view: View?, i: Int): Boolean {
        onListArticle(i + 1)
        return true
    }

    // com.kota.asFramework.pageController.ASViewController
    protected override fun onSearchButtonClicked(): Boolean {
        showSearchArticleDialog()
        return true
    }

    fun showSearchArticleDialog() {
        val dialog_SearchArticle = DialogSearchArticle()
        dialog_SearchArticle.setListener(this)
        dialog_SearchArticle.show()
    }

    protected fun showSelectArticleDialog() {
        val dialog_SelectArticle = DialogSelectArticle()
        dialog_SelectArticle.setListener(this)
        dialog_SelectArticle.show()
    }

    // com.kota.Bahamut.Dialogs.Dialog_SearchArticle_Listener
    override fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String?>) {
        searchArticle(
            vector.get(0)!!,
            vector.get(1),
            if (vector.get(2) == "YES") "y" else "n",
            vector.get(3)
        )
    }

    /** 搜尋文章  */
    fun searchArticle(_keyword: String, _author: String?, _mark: String?, _gy: String?) {
        this.lastListAction = BoardPageAction.Companion.SEARCH
        val board_Search_Page = PageContainer.instance?.getBoardSearchPage()
        board_Search_Page.clear()
        navigationController.pushViewController(board_Search_Page)
        val state = instance.getState(board_Search_Page.getListIdFromListName(name))
        if (state != null) {
            state.top = 0
            state.position = 0
        }
        board_Search_Page.setKeyword(_keyword)
        board_Search_Page.setAuthor(_author)
        board_Search_Page.setMark(_mark)
        board_Search_Page.setGy(_gy)
        pushCommand(BahamutCommandSearchArticle(_keyword, _author, _mark, _gy))
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
        navigationController.pushViewController(BookmarkManagePage(name, this))
    }

    /** 長按串接文章  */
    fun onListArticle(i: Int) {
        this.lastListAction = BoardPageAction.Companion.LINK_TITLE
        val board_Linked_Title_Page = PageContainer.instance?.getBoardLinkedTitlePage()
        board_Linked_Title_Page.clear()
        navigationController.pushViewController(board_Linked_Title_Page)
        val state = instance.getState(board_Linked_Title_Page.getListIdFromListName(name))
        if (state != null) {
            state.top = 0
            state.position = 0
        }
        pushCommand(BahamutCommandListArticle(i))
    }

    // com.kota.asFramework.pageController.ASViewController
    public override fun onReceivedGestureRight(): Boolean {
        if (propertiesGestureOnBoardEnable) {
            if (this.isDrawerOpen || _isDrawerOpening) {
                return false
            }
            onBackPressed()
            return true
        }
        return true
    }

    /** 發文  */
    protected open fun onPostButtonClicked() {
        val postArticlePage = PageContainer.instance?.getPostArticlePage()
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
            .setListener(ASAlertDialogListener { aSAlertDialog: ASAlertDialog?, i2: Int ->
                if (i2 == 1) {
                    this@BoardMainPage.pushCommand(BahamutCommandGoodArticle(loadingItemNumber))
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    /** 按下推文  */
    fun pushArticle() {
        this@BoardMainPage.pushCommand(BahamutCommandPushArticle(loadingItemNumber))

        pushArticleAsRunner.cancel()
        pushArticleAsRunner.postDelayed(2000)
        isPostDelayedSuccess = false
    }

    /** 開啟推文小視窗  */
    fun openPushArticleDialog() {
        pushArticleAsRunner.cancel()
        isPostDelayedSuccess = true

        val dialog = DialogPushArticle()
        dialog.show()
    }

    /** 沒有開啟推文小視窗, 視為沒開放功能  */
    var pushArticleAsRunner: ASRunner = object : ASRunner() {
        public override fun run() {
            if (!isPostDelayedSuccess) {
                onPagePreload()
                showLongToast("沒反應，看板未開放推文")
            }
        }
    }

    /** 提供給 stateHandler 的取消介面  */
    fun cancelRunner() {
        pushArticleAsRunner.cancel()
        isPostDelayedSuccess = true
    }

    /** 轉寄至信箱  */
    fun FSendMail() {
        pushCommand(BahamutCommandFSendMail(propertiesUsername!!))
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
    public override fun isItemBlocked(telnetListPageItem: TelnetListPageItem?): Boolean {
        if (telnetListPageItem != null) {
            return this.isItemBlockEnable && isBlockListContains((telnetListPageItem as BoardPageItem).author)
        }
        return false
    }

    /** 啟用/停用 黑名單  */
    fun onChangeBlockStateButtonClicked() {
        propertiesBlockListEnable = !this.isItemBlockEnable
        notifyDataUpdated()
        this.isItemBlockEnable = propertiesBlockListEnable
        if (mainDrawerLayout != null) {
            val block_enable_checkbox =
                mainDrawerLayout?.findViewById<CheckBox>(R.id.block_enable_button_checkbox)
            block_enable_checkbox.setChecked(this.isItemBlockEnable)
        }
        reloadListView()
    }

    fun onEditBlockListButtonClicked() {
        navigationController.pushViewController(BlockListPage())
    }

    /** 點下文章  */
    public override fun loadItemAtIndex(index: Int) {
        if (isItemCanLoadAtIndex(index)) {
            val articlePage = PageContainer.instance?.getArticlePage()
            articlePage.setBoardPage(this)
            articlePage.clear()
            navigationController.pushViewController(articlePage)

            super.loadItemAtIndex(index)
        }
    }

    fun prepareInitial() {
        _initialed = false
    }

    /** 書籤管理->按下書籤  */
    // com.kota.Bahamut.BookmarkPage.BoardExtendOptionalPageListener
    override fun onBoardExtendOptionalPageDidSelectBookmark(bookmark: Bookmark?) {
        if (bookmark != null) {
            this.lastListAction = BoardPageAction.Companion.SEARCH
            pushCommand(
                BahamutCommandSearchArticle(
                    bookmark.keyword!!,
                    bookmark.author,
                    bookmark.mark,
                    bookmark.gy
                )
            )
        }
    }

    // com.kota.Bahamut.ListPage.TelnetListPage, android.widget.Adapter
    public override fun getView(index: Int, view: View?, viewGroup: ViewGroup?): View? {
        var view = view
        val item_index = index + 1
        val block = ItemUtils.getBlock(item_index)
        val boardPageItem = getItem(index) as BoardPageItem?
        if (boardPageItem == null && currentBlock != block && !isLoadingBlock(item_index)) {
            loadBoardBlock(block)
        }
        if (view == null) {
            view = BoardPageItemView(context)
            view.setLayoutParams(
                AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val boardPageItemView = view as BoardPageItemView
        boardPageItemView.setItem(boardPageItem)
        boardPageItemView.setNumber(item_index)

        if (boardPageItem != null && this.isItemBlockEnable) {
            if (isBlockListContains(boardPageItem.author)) {
                boardPageItem.isBlocked = true
            } else if (blockListForTitle && isBlockListContainsFuzzy(boardPageItem.title)) {
                boardPageItem.isBlocked = true
            } else {
                boardPageItem.isBlocked = false
            }

            if (boardPageItem.isBlocked) boardPageItemView.setVisible(false)
        } else {
            boardPageItemView.setVisible(true)
        }

        return view
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    public override fun recycleBlock(telnetListPageBlock: TelnetListPageBlock?) {
        BoardPageBlock.recycle(telnetListPageBlock as BoardPageBlock?)
    }

    // com.kota.Bahamut.ListPage.TelnetListPage
    public override fun recycleItem(telnetListPageItem: TelnetListPageItem?) {
        BoardPageItem.recycle(telnetListPageItem as BoardPageItem?)
    }

    // 修改文章 訊息發出
    // com.kota.Bahamut.pages.PostArticlePage_Listener
    override fun onPostDialogEditButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String?,
        str2: String,
        str3: String
    ) {
        pushCommand(BahamutCommandEditArticle(str, str2, str3))
    }

    var timer: Timer? = null

    /** 發表文章/回覆文章 訊息發出  */
    // com.kota.Bahamut.pages.PostArticlePage_Listener
    override fun onPostDialogSendButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String,
        str2: String,
        str3: String?,
        str4: String?,
        str5: String?,
        boolean6: Boolean
    ) {
        pushCommand(BahamutCommandPostArticle(this, str, str2, str3, str4, str5, boolean6))
        // 回應到作者信箱
        if (str3 != null && str3 == "M") {
            return
        }
        // 發文中等待視窗
        timer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                setMessage(getContextString(R.string.board_page_post_waiting_message_2))
            }
        }
        val timerTask2: TimerTask = object : TimerTask() {
            override fun run() {
                setMessage(getContextString(R.string.board_page_post_waiting_message_3))
            }
        }
        timer?.schedule(timerTask, 3000)
        timer?.schedule(timerTask2, 6000)

        showProcessingDialog(getContextString(R.string.board_page_post_waiting_message_1))
    }

    /** 引言過多, 回逤發文時的設定  */
    fun recoverPost() {
        object : ASRunner() {
            public override fun run() {
                cleanCommand() // 清除引言過多留下的command buffer
                val page = PageContainer.instance?.getPostArticlePage()
                page.setRecover()
            }
        }.runInMainThread()
        if (timer != null) {
            timer?.cancel()
            timer?.purge()
            timer = null
        }
        dismissProcessingDialog()
    }

    /** 完成發文  */
    fun finishPost() {
        object : ASRunner() {
            public override fun run() {
                val page = PageContainer.instance?.getPostArticlePage()
                if (page != null) page.closeArticle()
            }
        }.runInMainThread()
        if (timer != null) {
            timer?.cancel()
            timer?.purge()
            timer = null
        }
        dismissProcessingDialog()
    }

    // com.kota.Bahamut.ListPage.TelnetListPage, com.kota.asFramework.pageController.ASViewController
    public override fun onPageDidAppear() {
        super.onPageDidAppear()
        _bookmark_adapter.notifyDataSetChanged()
        _history_adapter.notifyDataSetChanged()
    }

    fun reloadBookmark() {
        val listName = name
        val context: Context = context
        if (context == null) {
            return
        }
        val store = TempSettings.bookmarkStore
        if (store != null) {
            if (_mode == 0) {
                store.getBookmarkList(listName).loadBookmarkList(_bookmarkList)
                if (isPageAppeared) {
                    _bookmark_adapter.notifyDataSetChanged()
                }
            } else {
                store.getBookmarkList(listName).loadHistoryList(_bookmarkList)
                if (isPageAppeared) {
                    _history_adapter.notifyDataSetChanged()
                }
            }
        }
        if (_bookmarkList.size == 0) {
            _drawerListView?.setVisibility(View.GONE)
            _drawerListViewNone?.setVisibility(View.VISIBLE)
        } else {
            _drawerListView?.setVisibility(View.VISIBLE)
            _drawerListViewNone?.setVisibility(View.GONE)
        }
    }

    fun closeDrawer() {
        val drawerLayout = mainLayout?.findViewById<DrawerLayout?>(R.id.drawer_layout)
        if (drawerLayout != null) {
            drawerLayout.closeDrawers()
        }
    }

    val isDrawerOpen: Boolean
        /** 側邊選單已開啟 或 正在開啟中  */
        get() {
            val drawerLayout =
                mainLayout?.findViewById<DrawerLayout?>(R.id.drawer_layout)
            if (drawerLayout != null) {
                return drawerLayout.isDrawerOpen(drawerLocation)
            }
            return false
        }

    override fun onSearchDialogCancelButtonClicked() {
    }
}