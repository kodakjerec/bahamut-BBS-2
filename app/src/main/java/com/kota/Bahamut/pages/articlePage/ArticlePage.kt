package com.kota.Bahamut.pages.articlePage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.util.Linkify
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASScrollView
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.command.BahamutCommandDeleteArticle
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.dialogs.DialogQueryHero
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.pages.model.ToolBarFloating
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getShowTopBottomButton
import com.kota.Bahamut.service.NotificationSettings.setShowTopBottomButton
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.blockList
import com.kota.Bahamut.service.UserSettings.Companion.exchangeArticleViewMode
import com.kota.Bahamut.service.UserSettings.Companion.isBlockListContains
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesArticleMoveEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesArticleViewMode
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesExternalToolbarEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesGestureOnBoardEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarOrder
import com.kota.Bahamut.service.UserSettings.Companion.propertiesUsername
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetArticleItem
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetClient.connector
import com.kota.telnet.TelnetConnector.isConnecting
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView
import java.util.Locale
import java.util.Vector

class ArticlePage : TelnetPage() {
    var mainLayout: RelativeLayout? = null
    var telnetArticle: TelnetArticle? = null
    var telnetView: TelnetView? = null
    var listEmptyView: TextView? = null
    var _board_page: BoardMainPage? = null
    var isFullScreen: Boolean = false
    var _action_delay: Long = 500
    var _top_action: Runnable? = null
    var _bottom_action: Runnable? = null
    var listAdapter: BaseAdapter = object : BaseAdapter() {
        private var pushLength = 0 // 推文長度

        // android.widget.Adapter
        override fun getCount(): Int {
            if (telnetArticle != null) {
                pushLength = telnetArticle?.pushSize
                // 內文個數 + header + PostTime + push
                return telnetArticle?.itemSize + 2 + pushLength
            }
            return 0
        }

        // android.widget.Adapter
        override fun getItem(itemIndex: Int): TelnetArticleItem? {
            if (telnetArticle == null) {
                return null
            }
            return telnetArticle?.getItem(itemIndex - 1)
        }

        // android.widget.Adapter
        override fun getItemId(itemIndex: Int): Long {
            return itemIndex.toLong()
        }

        override fun getItemViewType(itemIndex: Int): Int {
            if (itemIndex == 0) {
                // header
                return ArticlePageItemType.Companion.Header
            } else if (itemIndex == getCount() - 1 - pushLength) {
                // postTime
                return ArticlePageItemType.Companion.PostTime
            } else if (itemIndex >= getCount() - pushLength) {
                // push
                return ArticlePageItemType.Companion.Push
            }
            // content
            val returnItem = getItem(itemIndex)
            if (returnItem != null) return returnItem.type
            else return ArticlePageItemType.Companion.Content
        }

        // android.widget.Adapter
        override fun getView(itemIndex: Int, itemViewFrom: View?, parentView: ViewGroup?): View {
            var type = getItemViewType(itemIndex)
            val item = getItem(itemIndex)
            // 2-標題 0-本文 1-簽名檔 3-發文時間 4-推文
            var itemViewOrigin = itemViewFrom

            if (itemViewOrigin == null) {
                when (type) {
                    ArticlePageItemType.Companion.Sign -> itemViewOrigin =
                        ArticlePage_TelnetItemView(context)

                    ArticlePageItemType.Companion.Header -> itemViewOrigin =
                        ArticlePage_HeaderItemView(context)

                    ArticlePageItemType.Companion.PostTime -> itemViewOrigin =
                        ArticlePage_TimeTimeView(context)

                    ArticlePageItemType.Companion.Push -> itemViewOrigin =
                        ArticlePagePushItemView(context)

                    else -> {
                        type = ArticlePageItemType.Companion.Content
                        itemViewOrigin = ArticlePage_TextItemView(context)
                    }
                }
            } else if (type == ArticlePageItemType.Companion.Content) {
                itemViewOrigin = ArticlePage_TextItemView(context)
            }

            if (itemViewOrigin is ArticlePage_TextItemView) {
                if (item != null) {
                    itemViewOrigin.setAuthor(item.author, item.nickname)
                    itemViewOrigin.setQuote(item.quoteLevel)
                    itemViewOrigin.setContent(item.content, item.frame?.rows)
                    // 分隔線
                    itemViewOrigin.setDividerHidden(itemIndex >= getCount() - 2)
                    // 黑名單檢查
                    itemViewOrigin.setVisible(
                        !propertiesBlockListEnable || !isBlockListContains(
                            item.author
                        )
                    )
                }
            } else if (itemViewOrigin is ArticlePage_TelnetItemView) {
                if (item != null) itemViewOrigin.setFrame(item.frame)
                // 分隔線
                itemViewOrigin.setDividerHidden(itemIndex >= getCount() - 2)
            } else if (itemViewOrigin is ArticlePage_HeaderItemView) {
                var author: String? = null
                var title: String? = null
                var board_name: String? = null
                if (telnetArticle != null) {
                    author = telnetArticle?.author
                    title = telnetArticle?.title
                    board_name = telnetArticle?.boardName
                    if (telnetArticle?.nickName != null) {
                        author = author + "(" + telnetArticle?.nickName + ")"
                    }
                }
                itemViewOrigin.setData(title, author, board_name)
                itemViewOrigin.setMenuButtonClickListener(mMenuListener)
            } else if (itemViewOrigin is ArticlePage_TimeTimeView) {
                itemViewOrigin.setTime("《" + telnetArticle?.dateTime + "》")
                itemViewOrigin.setIP(telnetArticle?.fromIP)
            } else if (itemViewOrigin is ArticlePagePushItemView) {
                val tempIndex = itemIndex - (getCount() - pushLength) // itemIndex - 本文長度
                val itemPush = telnetArticle?.getPush(tempIndex)
                if (itemPush != null) {
                    itemViewOrigin.setContent(itemPush)
                    itemViewOrigin.setFloor(tempIndex + 1)
                }
            }
            return itemViewOrigin
        }

        /** 一共有多少种不同的视图类型  */
        override fun getViewTypeCount(): Int {
            return 5
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun isEmpty(): Boolean {
            return getCount() == 0
        }

        override fun areAllItemsEnabled(): Boolean {
            return false
        }

        override fun isEnabled(itemIndex: Int): Boolean {
            val type = getItemViewType(itemIndex)
            return type == 0 || type == 1
        }
    }

    /** 長按內文  */
    var listLongClickListener: OnItemLongClickListener =
        OnItemLongClickListener { var1: AdapterView<*>?, view: View?, itemIndex: Int, pressTime: Long ->
            if (view?.javaClass == ArticlePage_TelnetItemView::class.java) {
                // 開啟切換模式
                val item = telnetArticle?.getItem(itemIndex - 1)

                val viewMode = item?.type
                if (viewMode == 0) {
                    item.type = 1
                    listAdapter.notifyDataSetChanged()
                    return@OnItemLongClickListener true
                } else if (viewMode == 1) {
                    item.type = 0
                    listAdapter.notifyDataSetChanged()
                    return@OnItemLongClickListener true
                } else {
                    return@OnItemLongClickListener true
                }
            }
            false
        }

    /** 最前篇  */
    var pageTopListener: OnLongClickListener = OnLongClickListener { v: View? ->
        if (propertiesArticleMoveEnable) {
            if (_top_action != null) {
                v?.removeCallbacks(_top_action)
            }

            _top_action = Runnable? {
                _top_action = null
                moveToTopArticle()
            }
            v?.postDelayed(_top_action, _action_delay)
        }
        true
    }

    /** 上一篇  */
    var pageUpListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (_top_action != null) {
            v?.removeCallbacks(_top_action)
            _top_action = null
        }
        if (!TelnetClient.connector.isConnecting || _board_page == null) {
            showConnectionClosedToast()
        } else {
            _board_page?.loadTheSameTitleUp()
        }
    }

    /** 最後篇  */
    var pageBottomListener: OnLongClickListener = OnLongClickListener { v: View? ->
        if (propertiesArticleMoveEnable) {
            if (_bottom_action != null) {
                v?.removeCallbacks(_bottom_action)
            }

            _bottom_action = Runnable? {
                _bottom_action = null
                moveToBottomArticle()
            }
            v?.postDelayed(_bottom_action, _action_delay)
        }
        true
    }

    /** 下一篇  */
    var pageDownListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (_bottom_action != null) {
            v?.removeCallbacks(_bottom_action)
            _bottom_action = null
        }
        if (!TelnetClient.connector.isConnecting || _board_page == null) {
            showConnectionClosedToast()
        } else {
            _board_page?.loadTheSameTitleDown()
        }
    }

    /** 選單  */
    val mMenuListener: View.OnClickListener = View.OnClickListener { v: View? -> onMenuClicked() }

    /** 推薦  */
    val mDoGyListener: View.OnClickListener =
        View.OnClickListener { v: View? -> onGYButtonClicked() }

    /** 切換模式  */
    val mChangeModeListener: View.OnClickListener = View.OnClickListener { v: View? ->
        changeViewMode()
        refreshExternalToolbar()
    }

    /** 開啟連結  */
    val mShowLinkListener: View.OnClickListener =
        View.OnClickListener { v: View? -> onOpenLinkClicked() }

    /** 靠左對其  */
    var _btnLL_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        propertiesToolbarLocation = 1
        this@ArticlePage.changeToolbarLocation()
    }

    /** 靠右對其  */
    var _btnRR_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        propertiesToolbarLocation = 2
        this@ArticlePage.changeToolbarLocation()
    }

    val pageType: Int
        // com.kota.asFramework.pageController.ASViewController
        get() = BahamutPage.BAHAMUT_ARTICLE

    val pageLayout: Int
        // com.kota.asFramework.pageController.ASViewController
        get() = R.layout.article_page

    val isPopupPage: Boolean
        // com.kota.telnetUI.TelnetPage
        get() = true

    // com.kota.asFramework.pageController.ASViewController
    public override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout?

        telnetView = mainLayout?.findViewById<TelnetView?>(R.id.Article_contentTelnetView)
        reloadTelnetLayout()
        val listView = mainLayout?.findViewById<ASListView>(R.id.Article_contentList)
        listEmptyView = mainLayout?.findViewById<TextView?>(R.id.Article_listEmptyView)
        listView.setAdapter(listAdapter)
        listView.setEmptyView(listEmptyView)
        listView.setOnItemLongClickListener(listLongClickListener)

        val back_button = mainLayout?.findViewById<Button>(R.id.Article_backButton)
        back_button.setOnClickListener(replyListener)

        val page_up_button = mainLayout?.findViewById<Button>(R.id.Article_pageUpButton)
        page_up_button.setOnClickListener(pageUpListener)
        page_up_button.setOnLongClickListener(pageTopListener)

        val page_down_button = mainLayout?.findViewById<Button>(R.id.Article_pageDownButton)
        page_down_button.setOnClickListener(pageDownListener)
        page_down_button.setOnLongClickListener(pageBottomListener)

        val do_gy_button = mainLayout?.findViewById<Button?>(R.id.do_gy)
        if (do_gy_button != null) {
            do_gy_button.setOnClickListener(mDoGyListener)
        }
        val change_mode_button = mainLayout?.findViewById<Button?>(R.id.change_mode)
        if (change_mode_button != null) {
            change_mode_button.setOnClickListener(mChangeModeListener)
        }
        val show_link_button = mainLayout?.findViewById<Button?>(R.id.show_link)
        if (show_link_button != null) {
            show_link_button.setOnClickListener(mShowLinkListener)
        }

        mainLayout?.findViewById<View>(R.id.BoardPageLLButton).setOnClickListener(_btnLL_listener)
        mainLayout?.findViewById<View>(R.id.BoardPageRRButton).setOnClickListener(_btnRR_listener)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.ext_toolbar) as LinearLayout?)
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)

        if (telnetView?.frame == null && telnetArticle != null) {
            telnetView?.frame = telnetArticle?.frame
        }
        refreshExternalToolbar()
        showNotification()

        // 工具列位置
        changeToolbarLocation()
        changeToolbarOrder()
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
                toolBarFloating.setOnClickListenerSetting(replyListener)
                toolBarFloating.setTextSetting(getContextString(R.string.reply))
                // button 1
                toolBarFloating.setOnClickListener1(pageUpListener)
                toolBarFloating.setOnLongClickListener1(pageTopListener)
                toolBarFloating.setText1(getContextString(R.string.prev_article))
                // button 2
                toolBarFloating.setOnClickListener2(pageDownListener)
                toolBarFloating.setOnLongClickListener2(pageBottomListener)
                toolBarFloating.setText2(getContextString(R.string.next_article))
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

    /** 第一次進入的提示訊息  */
    fun showNotification() {
        val show_top_bottom_function = getShowTopBottomButton()
        if (!show_top_bottom_function) {
            showLongToast(getContextString(R.string.notification_article_top_bottom_function))
            setShowTopBottomButton(true)
        }
    }

    // com.kota.asFramework.pageController.ASViewController
    public override fun onPageWillAppear() {
        reloadViewMode()
    }

    // com.kota.asFramework.pageController.ASViewController
    public override fun onPageDidDisappear() {
        telnetView = null
        super.onPageDidDisappear()
    }

    // com.kota.asFramework.pageController.ASViewController
    protected override fun onBackPressed(): Boolean {
        navigationController.popViewController()
        PageContainer.instance?.cleanArticlePage()
        return true
    }

    // com.kota.asFramework.pageController.ASViewController
    protected override fun onMenuButtonClicked(): Boolean {
        onMenuClicked()
        return true
    }

    fun onMenuClicked() {
        if (telnetArticle != null && telnetArticle?.author != null) {
            val author = telnetArticle?.author.lowercase(Locale.getDefault())
            val logon_user = propertiesUsername?.trim { it <= ' ' }.lowercase(Locale.getDefault())
            val is_board = _board_page?.pageType == BahamutPage.BAHAMUT_BOARD
            val ext_toolbar_enable = propertiesExternalToolbarEnable
            val external_toolbar_enable_title =
                if (ext_toolbar_enable) getContextString(R.string.hide_toolbar) else getContextString(
                    R.string.open_toolbar
                )
            ASListDialog.createDialog()
                .addItem(getContextString(R.string.do_gy))
                .addItem(getContextString(R.string.do_push))
                .addItem(getContextString(R.string.change_mode))
                .addItem(if (is_board && author == logon_user) getContextString(R.string.edit_article) else null)
                .addItem(if (author == logon_user) getContextString(R.string.delete_article) else null)
                .addItem(external_toolbar_enable_title)
                .addItem(getContextString(R.string.insert) + getContextString(R.string.system_setting_page_chapter_blocklist))
                .addItem(getContextString(R.string.open_url))
                .addItem(getContextString(R.string.board_page_item_long_click_1))
                .addItem(getContextString(R.string.board_page_item_load_all_image))
                .setListener(object : ASListDialogItemClickListener {
                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemClicked(
                        aDialog: ASListDialog?,
                        index: Int,
                        aTitle: String?
                    ) {
                        when (index) {
                            0 -> onGYButtonClicked()
                            1 -> onPushArticleButtonClicked()
                            2 -> {
                                changeViewMode()
                                refreshExternalToolbar()
                            }

                            3 -> onEditButtonClicked()
                            4 -> onDeleteButtonClicked()
                            5 -> onExternalToolbarClicked()
                            6 -> onAddBlockListClicked()
                            7 -> onOpenLinkClicked()
                            8 -> _board_page?.FSendMail()
                            9 -> onLoadAllImageClicked()
                            else -> {}
                        }
                    }

                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemLongClicked(
                        aDialog: ASListDialog?,
                        index: Int,
                        aTitle: String?
                    ): Boolean {
                        return true
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        }
    }

    /** 載入全部圖片  */
    fun onLoadAllImageClicked() {
        val list_view = mainLayout?.findViewById<ASListView>(R.id.Article_contentList)
        val childCount = list_view.getChildCount()
        for (childIndex in 0..<childCount) {
            val view = list_view.getChildAt(childIndex)
            if (view.javaClass == ArticlePage_TextItemView::class.java) {
                val firstLLayout = (view as ArticlePage_TextItemView).getChildAt(0) as LinearLayout
                val secondLLayout = firstLLayout.getChildAt(0) as LinearLayout
                val childCount2 = secondLLayout.getChildCount()
                for (childIndex2 in 0..<childCount2) {
                    val view1 = secondLLayout.getChildAt(childIndex2)
                    if (view1.javaClass == Thumbnail_ItemView::class.java) {
                        (view1 as Thumbnail_ItemView).prepare_load_image()
                    }
                }
            }
        }
    }

    /** 變更telnetView大小  */
    fun reloadTelnetLayout() {
        val screenWidth: Int
        val textWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            20.0f,
            context.getResources().getDisplayMetrics()
        ).toInt()
        var telnetViewWidth = (textWidth / 2) * 80
        if (navigationController.currentOrientation == 2) {
            screenWidth = navigationController.screenHeight
        } else {
            screenWidth = navigationController.screenWidth
        }
        if (telnetViewWidth <= screenWidth) {
            telnetViewWidth = -1
            isFullScreen = true
        } else {
            isFullScreen = false
        }
        val layoutParams = telnetView?.getLayoutParams()
        layoutParams.width = telnetViewWidth
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        telnetView?.setLayoutParams(layoutParams)
    }

    fun moveToTopArticle() {
        if (TelnetClient.connector.isConnecting && _board_page != null) {
            _board_page?.loadTheSameTitleTop()
        } else {
            showConnectionClosedToast()
        }
    }

    fun moveToBottomArticle() {
        if (TelnetClient.connector.isConnecting && _board_page != null) {
            _board_page?.loadTheSameTitleBottom()
        } else {
            showConnectionClosedToast()
        }
    }

    fun showConnectionClosedToast() {
        showShortToast("連線已中斷")
    }

    /** 推薦  */
    fun onGYButtonClicked() {
        if (_board_page != null) {
            _board_page?.goodLoadingArticle()
        }
    }

    /** 推文  */
    fun onPushArticleButtonClicked() {
        if (_board_page != null) {
            _board_page?.pushArticle()
        }
    }

    /** 刪除文章  */
    fun onDeleteButtonClicked() {
        if (telnetArticle != null && _board_page != null) {
            val item_number = telnetArticle?.myNumber
            ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.delete))
                .setMessage(getContextString(R.string.del_this_article))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.delete))
                .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                    if (index == 1) {
                        val command: TelnetCommand = BahamutCommandDeleteArticle(item_number)
                        _board_page?.pushCommand(command)
                        onBackPressed()
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        }
    }

    /** 回覆文章  */
    var replyListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (TelnetClient.connector.isConnecting) {
            if (telnetArticle != null) {
                val page = PageContainer.instance?.getPostArticlePage()
                val reply_title = telnetArticle?.generateReplyTitle()
                val reply_content = telnetArticle?.generateReplyContent()
                page.setBoardPage(_board_page)
                page.setOperationMode(PostArticlePage.OperationMode.Reply)
                page.setArticleNumber(telnetArticle?.myNumber.toString())
                page.setPostTitle(reply_title)
                page.setPostContent(reply_content + "\n\n\n")
                page.setListener(_board_page)
                page.setHeaderHidden(true)
                page.setTelnetArticle(telnetArticle)
                navigationController.pushViewController(page)
                return@OnClickListener
            }
            return@OnClickListener
        }
        showConnectionClosedToast()
    }

    /** 修改文章  */
    fun onEditButtonClicked() {
        if (telnetArticle != null) {
            val page = PageContainer.instance?.getPostArticlePage()
            val edit_title = telnetArticle?.generateEditTitle()
            val edit_content = telnetArticle?.generateEditContent()
            val edit_format = telnetArticle?.generateEditFormat()
            page.setBoardPage(_board_page)
            page.setArticleNumber(telnetArticle?.myNumber.toString())
            page.setOperationMode(PostArticlePage.OperationMode.Edit)
            page.setPostTitle(edit_title)
            page.setPostContent(edit_content)
            page.setEditFormat(edit_format)
            page.setListener(_board_page)
            page.setHeaderHidden(true)
            page.setTelnetArticle(telnetArticle)
            navigationController.pushViewController(page)
        }
    }

    /** 切換 text <-> telnet  */
    fun changeViewMode() {
        exchangeArticleViewMode()
        notifyDataUpdated()
        reloadViewMode()
    }

    fun reloadViewMode() {
        val text_content_view = mainLayout?.findViewById<ViewGroup?>(R.id.Article_TextContentView)
        val telnetViewBlock =
            mainLayout?.findViewById<ASScrollView?>(R.id.Article_contentTelnetViewBlock)
        // 文字模式
        if (propertiesArticleViewMode == ArticleViewMode.Companion.MODE_TEXT) {
            if (text_content_view != null) {
                text_content_view.setVisibility(View.VISIBLE)
            }
            if (telnetViewBlock != null) {
                telnetViewBlock.setVisibility(View.GONE)
                return
            }
            return
        }

        // telnet模式
        if (text_content_view != null) {
            text_content_view.setVisibility(View.GONE)
        }
        if (telnetViewBlock != null) {
            telnetViewBlock.setVisibility(View.VISIBLE)
            telnetViewBlock.invalidate()
        }
    }

    // com.kota.asFramework.pageController.ASViewController
    public override fun onReceivedGestureRight(): Boolean {
        if (propertiesArticleViewMode == ArticleViewMode.Companion.MODE_TEXT || isFullScreen) {
            if (propertiesGestureOnBoardEnable) onBackPressed()
            return true
        }
        return true
    }

    val isKeepOnOffline: Boolean
        // com.kota.telnetUI.TelnetPage
        get() = true

    fun onExternalToolbarClicked() {
        val enable = propertiesExternalToolbarEnable
        propertiesExternalToolbarEnable = !enable
        refreshExternalToolbar()
    }

    fun refreshExternalToolbar() {
        var enable = propertiesExternalToolbarEnable
        val article_mode = propertiesArticleViewMode
        if (article_mode == ArticleViewMode.Companion.MODE_TELNET) {
            enable = true
        }
        println("enable:" + enable)
        println("article_mode:" + article_mode)
        val toolbar_view = mainLayout?.findViewById<View>(R.id.ext_toolbar)
        if (toolbar_view != null) {
            toolbar_view.setVisibility(if (enable) View.VISIBLE else View.GONE)
        }
    }

    fun onOpenLinkClicked() {
        if (telnetArticle != null) {
            // 擷取文章內的所有連結
            val textView = TextView(context)
            textView.setText(telnetArticle?.fullText)
            Linkify.addLinks(textView, Linkify.WEB_URLS)

            val urls = textView.getUrls()
            if (urls.size == 0) {
                showShortToast(getContextString(R.string.no_url))
                return
            }
            val list_dialog = ASListDialog.createDialog()
            for (urlspan in urls) {
                list_dialog.addItem(urlspan.getURL())
            }
            list_dialog.setListener(object : ASListDialogItemClickListener {
                // com.kota.asFramework.dialog.ASListDialogItemClickListener
                override fun onListDialogItemLongClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ): Boolean {
                    return true
                }

                // com.kota.asFramework.dialog.ASListDialogItemClickListener
                override fun onListDialogItemClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ) {
                    val url2 = urls[index].getURL()
                    val context2: Context = context
                    if (context2 != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url2))
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }
                }
            })
            list_dialog.show()
        }
    }

    /** 加入黑名單  */
    fun onAddBlockListClicked() {
        val article = telnetArticle
        if (article != null) {
            val buffer: MutableSet<String?> = HashSet<String?>()
            // 作者黑名單
            buffer.add(article.author)
            // 內文黑名單
            val len = article.itemSize
            for (i in 0..<len) {
                val item = article.getItem(i)
                val author = item?.author
                if (author != null && !isBlockListContains(author)) {
                    buffer.add(author)
                }
            }
            if (buffer.size == 0) {
                showShortToast("無可加入黑名單的ID")
                return
            }
            val names = buffer.toTypedArray<String?>()
            ASListDialog.createDialog().addItems(names)
                .setListener(object : ASListDialogItemClickListener {
                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemLongClicked(
                        aDialog: ASListDialog?,
                        index: Int,
                        aTitle: String?
                    ): Boolean {
                        return true
                    }

                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemClicked(
                        aDialog: ASListDialog?,
                        index: Int,
                        aTitle: String?
                    ) {
                        onBlockButtonClicked(names[index]!!)
                    }
                }).show()
        }
    }

    fun onBlockButtonClicked(aBlockName: String) {
        ASAlertDialog.createDialog()
            .setTitle("加入黑名單")
            .setMessage("是否要將\"" + aBlockName + "\"加入黑名單?")
            .addButton("取消")
            .addButton("加入")
            .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    val new_list: MutableList<String?>? = blockList
                    if (new_list?.contains(aBlockName)) {
                        showShortToast(getContextString(R.string.already_have_item))
                    } else {
                        new_list.add(aBlockName)
                    }

                    blockList = new_list
                    notifyDataUpdated()

                    if (propertiesBlockListEnable) {
                        if (aBlockName == telnetArticle?.author) {
                            onBackPressed()
                        } else {
                            listAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    /** 給其他頁面託付使用  */
    fun setBoardPage(aBoardMainPage: BoardMainPage?) {
        _board_page = aBoardMainPage
    }

    /** 給其他網頁顯示文章使用  */
    fun setArticle(aArticle: TelnetArticle?) {
        telnetArticle = aArticle
        if (telnetArticle != null) {
            val board_name = _board_page?.name
            // 加入歷史紀錄
            val store = TempSettings.bookmarkStore
            if (store != null) {
                val bookmark_list = store.getBookmarkList(board_name)
                bookmark_list.addHistoryBookmark(telnetArticle?.title)
                store.storeWithoutCloud()
            }

            // 關係到 telnetView
            telnetView?.frame = telnetArticle?.frame

            reloadTelnetLayout()
            val telnet_content_view =
                mainLayout?.findViewById<ASScrollView?>(R.id.Article_contentTelnetViewBlock)
            if (telnet_content_view != null) {
                telnet_content_view.scrollTo(0, 0)
            }
            listAdapter.notifyDataSetChanged()
        }
        dismissProcessingDialog()
    }

    /** 給 state handler 更改讀取進度  */
    @SuppressLint("SetTextI18n")
    fun changeLoadingPercentage(percentage: String?) {
        showProcessingDialog(getContextString(R.string.loading_) + "\n" + percentage)
    }

    /** 查詢勇者  */
    fun ctrlQUser(fromStrings: Vector<String?>) {
        try {
            object : ASRunner() {
                public override fun run() {
                    val dialogQueryHero = DialogQueryHero()
                    dialogQueryHero.show()
                    dialogQueryHero.getData(fromStrings)
                }
            }.runInMainThread()
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
    }
}
