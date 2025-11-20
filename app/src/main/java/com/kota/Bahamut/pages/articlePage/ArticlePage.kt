package com.kota.Bahamut.pages.articlePage

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.core.net.toUri
import androidx.core.view.size
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandDeleteArticle
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.dialogs.DialogQueryHero
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.pages.model.ToolBarFloating
import com.kota.Bahamut.pages.theme.ThemeFunctions
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
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASListView
import com.kota.asFramework.ui.ASScrollView
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetArticleItem
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView
import java.util.Locale
import java.util.Vector

class ArticlePage : TelnetPage() {
    var mainLayout: RelativeLayout? = null
    var telnetArticle: TelnetArticle? = null
    var telnetView: TelnetView? = null
    var listEmptyView: TextView? = null
    var boardMainPage: BoardMainPage? = null
    var isFullScreen: Boolean = false
    var actionDelay: Long = 500
    var topAction: Runnable? = null
    var bottomAction: Runnable? = null
    var listAdapter: BaseAdapter = object : BaseAdapter() {
        private var pushLength = 0 // 推文長度

        // android.widget.Adapter
        override fun getCount(): Int {
            if (telnetArticle != null) {
                pushLength = telnetArticle!!.pushSize
                // 內文個數 + header + PostTime + push
                return telnetArticle!!.itemSize.plus(2) + pushLength
            }
            return 0
        }

        // android.widget.Adapter
        override fun getItem(itemIndex: Int): TelnetArticleItem? {
            if (telnetArticle == null) {
                return null
            }
            return telnetArticle!!.getItem(itemIndex - 1)
        }

        // android.widget.Adapter
        override fun getItemId(itemIndex: Int): Long {
            return itemIndex.toLong()
        }

        override fun getItemViewType(itemIndex: Int): Int {
            if (itemIndex == 0) {
                // header
                return ArticlePageItemType.Companion.HEADER
            } else if (itemIndex == getCount() - 1 - pushLength) {
                // postTime
                return ArticlePageItemType.Companion.POST_TIME
            } else if (itemIndex >= getCount() - pushLength) {
                // push
                return ArticlePageItemType.Companion.PUSH
            }
            // content
            val returnItem = getItem(itemIndex)
            return returnItem?.type ?: ArticlePageItemType.Companion.CONTENT
        }

        // android.widget.Adapter
        override fun getView(itemIndex: Int, itemViewFrom: View?, parentView: ViewGroup?): View {
            var type = getItemViewType(itemIndex)
            val item = getItem(itemIndex)
            // 2-標題 0-本文 1-簽名檔 3-發文時間 4-推文
            var itemViewOrigin = itemViewFrom

            if (itemViewOrigin == null) {
                when (type) {
                    ArticlePageItemType.Companion.SIGN -> itemViewOrigin =
                        ArticlePageTelnetItemView(context)

                    ArticlePageItemType.Companion.HEADER -> itemViewOrigin =
                        ArticlePageHeaderItemView(context)

                    ArticlePageItemType.Companion.POST_TIME -> itemViewOrigin =
                        ArticlePageTimeTimeView(context)

                    ArticlePageItemType.Companion.PUSH -> itemViewOrigin =
                        ArticlePagePushItemView(context!!)

                    else -> {
                        type = ArticlePageItemType.Companion.CONTENT
                        itemViewOrigin = ArticlePageTextItemView(context)
                    }
                }
            } else if (type == ArticlePageItemType.Companion.CONTENT) {
                itemViewOrigin = ArticlePageTextItemView(context)
            }

            if (itemViewOrigin is ArticlePageTextItemView) {
                if (item != null) {
                    itemViewOrigin.setAuthor(item.author, item.nickname)
                    itemViewOrigin.setQuote(item.quoteLevel)
                    itemViewOrigin.setContent(item.content, item.frame!!.rows)
                    // 分隔線
                    itemViewOrigin.setDividerHidden(itemIndex >= getCount() - 2)
                    // 黑名單檢查
                    itemViewOrigin.setVisible(
                        !propertiesBlockListEnable || !isBlockListContains(
                            item.author
                        )
                    )
                }
            } else if (itemViewOrigin is ArticlePageTelnetItemView) {
                if (item != null) itemViewOrigin.setFrame(item.frame!!)
                // 分隔線
                itemViewOrigin.setDividerHidden(itemIndex >= getCount() - 2)
            } else if (itemViewOrigin is ArticlePageHeaderItemView) {
                var author: String? = null
                var title: String? = null
                var boardName: String? = null
                if (telnetArticle != null) {
                    author = telnetArticle!!.author
                    title = telnetArticle!!.title
                    boardName = telnetArticle!!.boardName
                    if (telnetArticle!!.nickName != null) {
                        author = author + "(" + telnetArticle!!.nickName + ")"
                    }
                }
                itemViewOrigin.setData(title, author, boardName)
                itemViewOrigin.setMenuButtonClickListener(mMenuListener)
            } else if (itemViewOrigin is ArticlePageTimeTimeView) {
                itemViewOrigin.setTime("《" + telnetArticle!!.dateTime + "》")
                itemViewOrigin.setIP(telnetArticle!!.fromIP)
            } else if (itemViewOrigin is ArticlePagePushItemView) {
                val tempIndex = itemIndex - (getCount() - pushLength) // itemIndex - 本文長度
                val itemPush = telnetArticle!!.getPush(tempIndex)
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
            if (view?.javaClass == ArticlePageTelnetItemView::class.java) {
                // 開啟切換模式
                val item = telnetArticle!!.getItem(itemIndex - 1)

                val viewMode = item?.type
                when (viewMode) {
                    0 -> {
                        item.type = 1
                        listAdapter.notifyDataSetChanged()
                        return@OnItemLongClickListener true
                    }
                    1 -> {
                        item.type = 0
                        listAdapter.notifyDataSetChanged()
                        return@OnItemLongClickListener true
                    }
                    else -> {
                        return@OnItemLongClickListener true
                    }
                }
            }
            false
        }

    /** 最前篇  */
    var pageTopListener: OnLongClickListener = OnLongClickListener { v: View? ->
        if (propertiesArticleMoveEnable) {
            if (topAction != null) {
                v?.removeCallbacks(topAction)
            }

            topAction = Runnable {
                topAction = null
                moveToTopArticle()
            }
            v?.postDelayed(topAction, actionDelay)
        }
        true
    }

    /** 上一篇  */
    var pageUpListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (topAction != null) {
            v?.removeCallbacks(topAction)
            topAction = null
        }
        if (!TelnetClient.myInstance!!.telnetConnector!!.isConnecting || boardMainPage == null) {
            showConnectionClosedToast()
        } else {
            boardMainPage?.loadTheSameTitleUp()
        }
    }

    /** 最後篇  */
    var pageBottomListener: OnLongClickListener = OnLongClickListener { v: View? ->
        if (propertiesArticleMoveEnable) {
            if (bottomAction != null) {
                v?.removeCallbacks(bottomAction)
            }

            bottomAction = Runnable {
                bottomAction = null
                moveToBottomArticle()
            }
            v?.postDelayed(bottomAction, actionDelay)
        }
        true
    }

    /** 下一篇  */
    var pageDownListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (bottomAction != null) {
            v?.removeCallbacks(bottomAction)
            bottomAction = null
        }
        if (!TelnetClient.myInstance!!.telnetConnector!!.isConnecting || boardMainPage == null) {
            showConnectionClosedToast()
        } else {
            boardMainPage?.loadTheSameTitleDown()
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
    var btnLLListener: View.OnClickListener = View.OnClickListener { view: View? ->
        propertiesToolbarLocation = 1
        this@ArticlePage.changeToolbarLocation()
    }

    /** 靠右對其  */
    var btnRRListener: View.OnClickListener = View.OnClickListener { view: View? ->
        propertiesToolbarLocation = 2
        this@ArticlePage.changeToolbarLocation()
    }

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_ARTICLE

    override val pageLayout: Int
        get() = R.layout.article_page

    override val isPopupPage: Boolean
        get() = true

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout?

        telnetView = mainLayout!!.findViewById(R.id.Article_contentTelnetView)
        reloadTelnetLayout()
        val listView = mainLayout!!.findViewById<ASListView>(R.id.Article_contentList)
        listEmptyView = mainLayout!!.findViewById(R.id.Article_listEmptyView)
        listView.adapter = listAdapter
        listView.emptyView = listEmptyView
        listView.onItemLongClickListener = listLongClickListener

        val backButton = mainLayout!!.findViewById<Button>(R.id.Article_backButton)
        backButton.setOnClickListener(replyListener)

        val pageUpButton = mainLayout!!.findViewById<Button>(R.id.Article_pageUpButton)!!
        pageUpButton.setOnClickListener(pageUpListener)
        pageUpButton.setOnLongClickListener(pageTopListener)

        val pageDownButton = mainLayout!!.findViewById<Button>(R.id.Article_pageDownButton)!!
        pageDownButton.setOnClickListener(pageDownListener)
        pageDownButton.setOnLongClickListener(pageBottomListener)

        val doGyButton = mainLayout!!.findViewById<Button?>(R.id.do_gy)!!
        doGyButton.setOnClickListener(mDoGyListener)
        val changeModeButton = mainLayout!!.findViewById<Button?>(R.id.change_mode)!!
        changeModeButton.setOnClickListener(mChangeModeListener)
        val showLinkButton = mainLayout!!.findViewById<Button?>(R.id.show_link)!!
        showLinkButton.setOnClickListener(mShowLinkListener)

        mainLayout!!.findViewById<View>(R.id.BoardPageLLButton).setOnClickListener(btnLLListener)
        mainLayout!!.findViewById<View>(R.id.BoardPageRRButton).setOnClickListener(btnRRListener)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.ext_toolbar) as LinearLayout?)
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)

        refreshExternalToolbar()
        showNotification()

        // 工具列位置
        changeToolbarLocation()
        changeToolbarOrder()
    }

    /** 變更工具列位置  */
    fun changeToolbarLocation() {
        val toolbar = mainLayout!!.findViewById<LinearLayout>(R.id.toolbar)
        val toolbarBlock = mainLayout!!.findViewById<LinearLayout>(R.id.toolbar_block)
        val toolBarFloating =
            mainLayout!!.findViewById<ToolBarFloating>(R.id.ToolbarFloatingComponent)
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

        val layoutParams = toolbar!!.layoutParams as RelativeLayout.LayoutParams
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

        toolbar.layoutParams = layoutParams
    }

    /** 反轉按鈕順序  */
    fun changeToolbarOrder() {
        val toolbar = mainLayout!!.findViewById<LinearLayout>(R.id.toolbar)

        val choiceToolbarOrder = propertiesToolbarOrder
        if (choiceToolbarOrder == 1) {
            // 最左邊最右邊
            val btnLL = toolbar.findViewById<Button?>(R.id.BoardPageLLButton)
            val btnLLDivider = toolbar.findViewById<View>(R.id.toolbar_divider_0)
            val btnRR = toolbar.findViewById<Button?>(R.id.BoardPageRRButton)
            val btnRRDivider = toolbar.findViewById<View>(R.id.toolbar_divider_3)

            // 擷取中間的元素
            val allViews = ArrayList<View?>()
            for (i in toolbar.size - 3 downTo 2) {
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

    /** 第一次進入的提示訊息  */
    fun showNotification() {
        val showTopBottomFunction = getShowTopBottomButton()
        if (!showTopBottomFunction) {
            showLongToast(getContextString(R.string.notification_article_top_bottom_function))
            setShowTopBottomButton(true)
        }
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onPageWillAppear() {
        reloadViewMode()
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onPageDidDisappear() {
        telnetView = null
        super.onPageDidDisappear()
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onBackPressed(): Boolean {
        navigationController.popViewController()
        PageContainer.instance!!.cleanArticlePage()
        return true
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onMenuButtonClicked(): Boolean {
        onMenuClicked()
        return true
    }

    fun onMenuClicked() {
        if (telnetArticle != null) {
            val author = telnetArticle!!.author.lowercase(Locale.getDefault())
            val logonUser = propertiesUsername!!.trim().lowercase(Locale.getDefault())
            val isBoard = boardMainPage?.pageType == BahamutPage.BAHAMUT_BOARD
            val extToolbarEnable = propertiesExternalToolbarEnable
            val externalToolbarEnableTitle =
                if (extToolbarEnable) getContextString(R.string.hide_toolbar) else getContextString(
                    R.string.open_toolbar
                )
            ASListDialog.createDialog()
                .addItem(getContextString(R.string.do_gy))
                .addItem(getContextString(R.string.do_push))
                .addItem(getContextString(R.string.change_mode))
                .addItem(if (isBoard && author == logonUser) getContextString(R.string.edit_article) else null)
                .addItem(if (author == logonUser) getContextString(R.string.delete_article) else null)
                .addItem(externalToolbarEnableTitle)
                .addItem(getContextString(R.string.insert) + getContextString(R.string.system_setting_page_chapter_blocklist))
                .addItem(getContextString(R.string.open_url))
                .addItem(getContextString(R.string.board_page_item_long_click_1))
                .addItem(getContextString(R.string.board_page_item_load_all_image))
                .setListener(object : ASListDialogItemClickListener {
                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemClicked(
                        paramASListDialog: ASListDialog?,
                        index: Int,
                        title: String?
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
                            8 -> boardMainPage?.funSendMail()
                            9 -> onLoadAllImageClicked()
                            else -> {}
                        }
                    }

                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemLongClicked(
                        paramASListDialog: ASListDialog?,
                        index: Int,
                        title: String?
                    ): Boolean {
                        return true
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        }
    }

    /** 載入全部圖片  */
    fun onLoadAllImageClicked() {
        val listView = mainLayout!!.findViewById<ASListView>(R.id.Article_contentList)
        val childCount = listView.size
        for (childIndex in 0..<childCount) {
            val view = listView.getChildAt(childIndex)
            if (view.javaClass == ArticlePageTextItemView::class.java) {
                val firstLLayout = (view as ArticlePageTextItemView).getChildAt(0) as LinearLayout
                val secondLLayout = firstLLayout.getChildAt(0) as LinearLayout
                val childCount2 = secondLLayout.childCount
                for (childIndex2 in 0..<childCount2) {
                    val view1 = secondLLayout.getChildAt(childIndex2)
                    if (view1.javaClass == ThumbnailItemView::class.java) {
                        (view1 as ThumbnailItemView).prepareLoadImage()
                    }
                }
            }
        }
    }

    /** 變更telnetView大小  */
    fun reloadTelnetLayout() {
        val textWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            20.0f,
            context!!.resources.displayMetrics
        ).toInt()
        var telnetViewWidth = (textWidth / 2) * 80
        val screenWidth: Int = if (navigationController.currentOrientation == 2) {
            navigationController.screenHeight
        } else {
            navigationController.screenWidth
        }
        if (telnetViewWidth <= screenWidth) {
            telnetViewWidth = -1
            isFullScreen = true
        } else {
            isFullScreen = false
        }
        val layoutParams = telnetView!!.layoutParams!!
        layoutParams.width = telnetViewWidth
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        telnetView?.layoutParams = layoutParams
    }

    fun moveToTopArticle() {
        if (TelnetClient.myInstance!!.telnetConnector!!.isConnecting && boardMainPage != null) {
            boardMainPage?.loadTheSameTitleTop()
        } else {
            showConnectionClosedToast()
        }
    }

    fun moveToBottomArticle() {
        if (TelnetClient.myInstance!!.telnetConnector!!.isConnecting && boardMainPage != null) {
            boardMainPage?.loadTheSameTitleBottom()
        } else {
            showConnectionClosedToast()
        }
    }

    fun showConnectionClosedToast() {
        showShortToast("連線已中斷")
    }

    /** 推薦  */
    fun onGYButtonClicked() {
        if (boardMainPage != null) {
            boardMainPage?.goodLoadingArticle()
        }
    }

    /** 推文  */
    fun onPushArticleButtonClicked() {
        if (boardMainPage != null) {
            boardMainPage?.pushArticle()
        }
    }

    /** 刪除文章  */
    fun onDeleteButtonClicked() {
        if (telnetArticle != null && boardMainPage != null) {
            val itemNumber = telnetArticle!!.myNumber
            ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.delete))
                .setMessage(getContextString(R.string.del_this_article))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.delete))
                .setListener { aDialog: ASAlertDialog?, index: Int ->
                    if (index == 1) {
                        val command: TelnetCommand = BahamutCommandDeleteArticle(itemNumber)
                        boardMainPage?.pushCommand(command)
                        onBackPressed()
                    }
                }.scheduleDismissOnPageDisappear(this).show()
        }
    }

    /** 回覆文章  */
    var replyListener: View.OnClickListener = View.OnClickListener { v: View? ->
        if (TelnetClient.myInstance!!.telnetConnector!!.isConnecting) {
            if (telnetArticle != null) {
                val page = PageContainer.instance!!.postArticlePage
                val replyTitle = telnetArticle!!.generateReplyTitle()
                val replyContent = telnetArticle!!.generateReplyContent()
                page.setBoardPage(boardMainPage)
                page.setOperationMode(PostArticlePage.OperationMode.Reply)
                page.setArticleNumber(telnetArticle!!.myNumber.toString())
                page.setPostTitle(replyTitle)
                page.setPostContent(replyContent + "\n\n\n")
                page.setListener(boardMainPage)
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
            val page = PageContainer.instance!!.postArticlePage
            val editTitle = telnetArticle!!.generateEditTitle()
            val editContent = telnetArticle!!.generateEditContent()
            val editFormat = telnetArticle!!.generateEditFormat()
            page.setBoardPage(boardMainPage)
            page.setArticleNumber(telnetArticle!!.myNumber.toString())
            page.setOperationMode(PostArticlePage.OperationMode.Edit)
            page.setPostTitle(editTitle)
            page.setPostContent(editContent)
            page.setEditFormat(editFormat)
            page.setListener(boardMainPage)
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
        val textContentView = mainLayout!!.findViewById<ViewGroup?>(R.id.Article_TextContentView)
        val telnetViewBlock =
            mainLayout!!.findViewById<ASScrollView?>(R.id.Article_contentTelnetViewBlock)
        // 文字模式
        if (propertiesArticleViewMode == ArticleViewMode.Companion.MODE_TEXT) {
            if (textContentView != null) {
                textContentView.visibility = View.VISIBLE
            }
            if (telnetViewBlock != null) {
                telnetViewBlock.visibility = View.GONE
                return
            }
            return
        }

        // telnet模式
        if (textContentView != null) {
            textContentView.visibility = View.GONE
        }
        if (telnetViewBlock != null) {
            telnetViewBlock.visibility = View.VISIBLE
            telnetViewBlock.invalidate()
        }
    }

    // com.kota.asFramework.pageController.ASViewController
    override fun onReceivedGestureRight(): Boolean {
        if (propertiesArticleViewMode == ArticleViewMode.Companion.MODE_TEXT || isFullScreen) {
            if (propertiesGestureOnBoardEnable) onBackPressed()
            return true
        }
        return true
    }

    override val isKeepOnOffline: Boolean
        get() = true

    fun onExternalToolbarClicked() {
        val enable = propertiesExternalToolbarEnable
        propertiesExternalToolbarEnable = !enable
        refreshExternalToolbar()
    }

    fun refreshExternalToolbar() {
        var enable = propertiesExternalToolbarEnable
        val articleMode = propertiesArticleViewMode
        if (articleMode == ArticleViewMode.Companion.MODE_TELNET) {
            enable = true
        }

        val toolbarView = mainLayout!!.findViewById<View>(R.id.ext_toolbar)
        if (toolbarView != null) {
            toolbarView.visibility = if (enable) View.VISIBLE else View.GONE
        }
    }

    fun onOpenLinkClicked() {
        if (telnetArticle != null) {
            // 擷取文章內的所有連結
            val textView = TextView(context)
            textView.text = telnetArticle!!.fullText
            Linkify.addLinks(textView, Linkify.WEB_URLS)

            val urls = textView.urls
            if (urls.size == 0) {
                showShortToast(getContextString(R.string.no_url))
                return
            }
            val listDialog = ASListDialog.createDialog()
            for (url in urls) {
                listDialog.addItem(url.url)
            }
            listDialog.setListener(object : ASListDialogItemClickListener {
                // com.kota.asFramework.dialog.ASListDialogItemClickListener
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }

                // com.kota.asFramework.dialog.ASListDialogItemClickListener
                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    val url2 = urls[index].url
                    val intent = Intent(Intent.ACTION_VIEW, url2.toUri())
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
            })
            listDialog.show()
        }
    }

    /** 加入黑名單  */
    fun onAddBlockListClicked() {
        val article = telnetArticle
        if (article != null) {
            val buffer: MutableSet<String> = HashSet()
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
            if (buffer.isEmpty()) {
                showShortToast("無可加入黑名單的ID")
                return
            }
            val names = buffer.toTypedArray<String>()
            ASListDialog.createDialog().addItems(names)
                .setListener(object : ASListDialogItemClickListener {
                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemLongClicked(
                        paramASListDialog: ASListDialog?,
                        index: Int,
                        title: String?
                    ): Boolean {
                        return true
                    }

                    // com.kota.asFramework.dialog.ASListDialogItemClickListener
                    override fun onListDialogItemClicked(
                        paramASListDialog: ASListDialog?,
                        index: Int,
                        title: String?
                    ) {
                        onBlockButtonClicked(names[index])
                    }
                }).show()
        }
    }

    fun onBlockButtonClicked(aBlockName: String) {
        ASAlertDialog.createDialog()
            .setTitle("加入黑名單")
            .setMessage("是否要將\"$aBlockName\"加入黑名單?")
            .addButton("取消")
            .addButton("加入")
            .setListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    val newList: MutableList<String> = blockList
                    if (newList.contains(aBlockName)) {
                        showShortToast(getContextString(R.string.already_have_item))
                    } else {
                        newList.add(aBlockName)
                    }

                    blockList = newList
                    notifyDataUpdated()

                    if (propertiesBlockListEnable) {
                        if (aBlockName == telnetArticle!!.author) {
                            onBackPressed()
                        } else {
                            listAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    /** 給其他頁面託付使用  */
    fun setBoardPage(aBoardMainPage: BoardMainPage) {
        boardMainPage = aBoardMainPage
    }

    /** 給其他網頁顯示文章使用  */
    fun setArticle(aArticle: TelnetArticle) {
        telnetArticle = aArticle
        if (telnetArticle != null) {
            val boardName = boardMainPage?.listName
            // 加入歷史紀錄
            val store = TempSettings.bookmarkStore
            if (store != null) {
                val bookmarkList = store.getBookmarkList(boardName)
                bookmarkList.addHistoryBookmark(telnetArticle!!.title)
                store.storeWithoutCloud()
            }

            // 關係到 telnetView
            telnetView!!.frame = telnetArticle!!.frame!!

            reloadTelnetLayout()
            val telnetContentView =
                mainLayout!!.findViewById<ASScrollView?>(R.id.Article_contentTelnetViewBlock)
            telnetContentView?.scrollTo(0, 0)
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
    fun ctrlQUser(fromStrings: Vector<String>) {
        try {
            object : ASRunner() {
                override fun run() {
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
