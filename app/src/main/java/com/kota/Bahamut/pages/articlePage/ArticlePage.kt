package com.kota.Bahamut.pages.articlePage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandDeleteArticle
import com.kota.Bahamut.command.BahamutCommandLocateArticle
import com.kota.Bahamut.command.TelnetCommand
import com.kota.Bahamut.dialogs.DialogQueryHero
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.EditFromLinkedStep
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
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppColors
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetArticleItem
import com.kota.telnet.TelnetArticlePush
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView
import java.util.Locale
import java.util.Vector
import java.util.regex.Pattern

/**
 * 文章閱讀頁面 (純 Jetpack Compose 實作)
 */
class ArticlePage : TelnetPage() {

    var telnetArticle: TelnetArticle? = null
    var telnetView: TelnetView? = null
    var boardMainPage: BoardMainPage? = null
    var isFullScreen: Boolean = false

    // Compose 響應式狀態
    var currentArticle by mutableStateOf<TelnetArticle?>(null)
    var viewModeState by mutableIntStateOf(propertiesArticleViewMode) // 0-Text 1-Telnet
    var isExtToolbarOpenState by mutableStateOf(propertiesExternalToolbarEnable)
    var toolbarLocationState by mutableIntStateOf(0)
    var toolbarOrderState by mutableIntStateOf(0)

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_ARTICLE

    override val pageLayout: Int
        get() = 0

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                ArticlePageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageDidLoad() {
        showNotification()
        changeToolbarLocation()
        changeToolbarOrder()
        refreshExternalToolbar()
    }

    override fun onPageWillAppear() {
        super.onPageWillAppear()
        reloadViewMode()
        refreshExternalToolbar()
        changeToolbarLocation()
        changeToolbarOrder()
    }

    override fun onPageDidDisappear() {
        telnetView = null
        super.onPageDidDisappear()
    }

    override fun clear() {
        super.clear()
        telnetArticle = null
        currentArticle = null
        telnetView = null
    }

    /** 第一次進入的提示訊息 */
    fun showNotification() {
        val showTopBottomFunction = getShowTopBottomButton()
        if (!showTopBottomFunction) {
            showLongToast(getContextString(R.string.notification_article_top_bottom_function))
            setShowTopBottomButton(true)
        }
    }

    override fun onBackPressed(): Boolean {
        clear()
        navigationController.popViewController()
        PageContainer.instance!!.cleanArticlePage()
        return true
    }

    override fun onMenuButtonClicked(): Boolean {
        onMenuClicked()
        return true
    }

    override fun onReceivedGestureRight(): Boolean {
        if (propertiesArticleViewMode == ArticleViewMode.Companion.MODE_TEXT || isFullScreen) {
            if (propertiesGestureOnBoardEnable) onBackPressed()
            return true
        }
        return true
    }

    fun changeToolbarLocation() {
        toolbarLocationState = propertiesToolbarLocation
    }

    fun changeToolbarOrder() {
        toolbarOrderState = propertiesToolbarOrder
    }

    fun changeViewMode() {
        exchangeArticleViewMode()
        notifyDataUpdated()
        viewModeState = propertiesArticleViewMode
    }

    fun reloadViewMode() {
        viewModeState = propertiesArticleViewMode
    }

    fun onExternalToolbarClicked() {
        val enable = propertiesExternalToolbarEnable
        propertiesExternalToolbarEnable = !enable
        isExtToolbarOpenState = !enable
    }

    fun refreshExternalToolbar() {
        var enable = propertiesExternalToolbarEnable
        val articleMode = propertiesArticleViewMode
        if (articleMode == ArticleViewMode.Companion.MODE_TELNET) {
            enable = true
        }
        isExtToolbarOpenState = enable
    }

    /** 變更telnetView大小 */
    fun reloadTelnetLayout() {}

    /** 載入全部圖片 (保持接口相容) */
    fun onLoadAllImageClicked() {}

    /** 最前篇 */
    var actionDelay: Long = 500L
    var topAction: ASCoroutine? = object : ASCoroutine() {
        override suspend fun run() {
            moveToTopArticle()
        }
    }
    var bottomAction: ASCoroutine? = object : ASCoroutine() {
        override suspend fun run() {
            moveToBottomArticle()
        }
    }

    var pageTopListener: OnLongClickListener = OnLongClickListener {
        if (propertiesArticleMoveEnable) {
            topAction?.cancel()
            topAction?.postDelayed(actionDelay)
        }
        true
    }

    /** 上一篇 */
    var pageUpListener: View.OnClickListener = View.OnClickListener {
        topAction?.cancel()
        if (!TelnetClient.myInstance!!.telnetConnector!!.isConnecting || boardMainPage == null) {
            showConnectionClosedToast()
        } else {
            boardMainPage?.loadTheSameTitleUp()
        }
    }

    /** 最後篇 */
    var pageBottomListener: OnLongClickListener = OnLongClickListener {
        if (propertiesArticleMoveEnable) {
            bottomAction?.cancel()
            bottomAction?.postDelayed(actionDelay)
        }
        true
    }

    /** 下一篇 */
    var pageDownListener: View.OnClickListener = View.OnClickListener {
        bottomAction?.cancel()
        if (!TelnetClient.myInstance!!.telnetConnector!!.isConnecting || boardMainPage == null) {
            showConnectionClosedToast()
        } else {
            boardMainPage?.loadTheSameTitleDown()
        }
    }

    /** 選單 */
    val mMenuListener: View.OnClickListener = View.OnClickListener { onMenuClicked() }

    /** 推薦 */
    val mDoGyListener: View.OnClickListener = View.OnClickListener { onGYButtonClicked() }

    /** 切換模式 */
    val mChangeModeListener: View.OnClickListener = View.OnClickListener {
        changeViewMode()
        refreshExternalToolbar()
    }

    /** 開啟連結 */
    val mShowLinkListener: View.OnClickListener = View.OnClickListener { onOpenLinkClicked() }

    /** 靠左對齊 */
    var btnLLListener: View.OnClickListener = View.OnClickListener {
        propertiesToolbarLocation = 1
        this@ArticlePage.changeToolbarLocation()
    }

    /** 靠右對齊 */
    var btnRRListener: View.OnClickListener = View.OnClickListener {
        propertiesToolbarLocation = 2
        this@ArticlePage.changeToolbarLocation()
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

    /** 推薦 */
    fun onGYButtonClicked() {
        boardMainPage?.goodLoadingArticle()
    }

    /** 推文 */
    fun onPushArticleButtonClicked() {
        boardMainPage?.pushArticle()
    }

    /** 刪除文章 */
    fun onDeleteButtonClicked() {
        val article = telnetArticle ?: currentArticle
        if (article != null && boardMainPage != null) {
            val itemNumber = article.articleNumber
            ASAlertDialog.createDialog()
                .setTitle(getContextString(R.string.delete))
                .setMessage(getContextString(R.string.del_this_article))
                .addButton(getContextString(R.string.cancel))
                .addButton(getContextString(R.string.delete))
                .setListener { _, index ->
                    if (index == 1) {
                        val command: TelnetCommand = BahamutCommandDeleteArticle(itemNumber)
                        boardMainPage?.pushCommand(command)
                        onBackPressed()
                    }
                }.scheduleDismissOnPageDisappear(this).show()
        }
    }

    /** 回覆文章 */
    var replyListener: View.OnClickListener = View.OnClickListener {
        if (TelnetClient.myInstance!!.telnetConnector!!.isConnecting) {
            val article = telnetArticle ?: currentArticle
            if (article != null) {
                val page = PageContainer.instance!!.postArticlePage
                val replyTitle = article.generateReplyTitle()
                val replyContent = article.generateReplyContent()
                page.setBoardPage(boardMainPage)
                page.setOperationMode(PostArticlePage.OperationMode.Reply)
                page.setArticleNumber(article.articleNumber.toString())
                page.setPostTitle(replyTitle)
                page.setPostContent(replyContent + "\n\n\n")
                page.setListener(boardMainPage)
                page.setHeaderHidden(true)
                page.setTelnetArticle(article)
                navigationController.pushViewController(page)
                return@OnClickListener
            }
            return@OnClickListener
        }
        showConnectionClosedToast()
    }

    /** 修改文章 */
    fun onEditButtonClicked() {
        val isBoard = boardMainPage?.pageType == BahamutPage.BAHAMUT_BOARD
        val article = telnetArticle ?: currentArticle

        if (isBoard && article != null) {
            val page = PageContainer.instance!!.postArticlePage
            val editTitle = article.generateEditTitle()
            val editContent = article.generateEditContent()
            val editFormat = article.generateEditFormat()
            page.setBoardPage(boardMainPage)
            page.setArticleNumber(article.articleNumber.toString())
            page.setOperationMode(PostArticlePage.OperationMode.Edit)
            page.setPostTitle(editTitle)
            page.setPostContent(editContent)
            page.setEditFormat(editFormat)
            page.setListener(boardMainPage)
            page.setHeaderHidden(true)
            page.setTelnetArticle(article)
            navigationController.pushViewController(page)
        } else if (article != null) {
            val selectedIndex = boardMainPage?.selectedIndex ?: 0
            var isFirstInPage = selectedIndex % 20 == 1
            val totalLength = boardMainPage?.getItemSize() ?: 0
            if (totalLength == 1) isFirstInPage = false
            boardMainPage?.pushCommand(BahamutCommandLocateArticle(article, isFirstInPage))
        }
    }

    /** 開啟選單 */
    fun onMenuClicked() {
        val article = telnetArticle ?: currentArticle
        if (article != null) {
            val author = article.author.lowercase(Locale.getDefault())
            val logonUser = propertiesUsername.lowercase(Locale.getDefault())
            val extToolbarEnable = propertiesExternalToolbarEnable
            val externalToolbarEnableTitle =
                if (extToolbarEnable) getContextString(R.string.hide_toolbar)
                else getContextString(R.string.open_toolbar)

            ASListDialog.createDialog()
                .addItem(getContextString(R.string.do_gy))
                .addItem(getContextString(R.string.do_push))
                .addItem(getContextString(R.string.change_mode))
                .addItem(if (author == logonUser) getContextString(R.string.edit_article) else null)
                .addItem(if (author == logonUser) getContextString(R.string.delete_article) else null, true)
                .addItem(externalToolbarEnableTitle)
                .addItem(getContextString(R.string.insert) + getContextString(R.string.system_setting_page_chapter_blocklist))
                .addItem(getContextString(R.string.open_url))
                .addItem(getContextString(R.string.board_page_item_long_click_1))
                .setListener(object : ASListDialogItemClickListener {
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
                            else -> {}
                        }
                    }

                    override fun onListDialogItemLongClicked(
                        paramASListDialog: ASListDialog?,
                        index: Int,
                        title: String?
                    ): Boolean = true
                }).scheduleDismissOnPageDisappear(this).show()
        }
    }

    /** 開啟連結列表 */
    fun onOpenLinkClicked() {
        val article = telnetArticle ?: currentArticle
        if (article != null) {
            val textView = TextView(context)
            textView.text = article.fullText
            Linkify.addLinks(textView, Linkify.WEB_URLS)

            val urls = textView.urls
            if (urls.isEmpty()) {
                showShortToast(getContextString(R.string.no_url))
                return
            }
            val listDialog = ASListDialog.createDialog()
            for (url in urls) {
                listDialog.addItem(url.url)
            }
            listDialog.setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean = true

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

    /** 加入黑名單 */
    fun onAddBlockListClicked() {
        val article = telnetArticle ?: currentArticle
        if (article != null) {
            val buffer: MutableSet<String> = HashSet()
            buffer.add(article.author)
            val len = article.itemSize
            for (i in 0 until len) {
                val item = article.getItem(i)
                val author = item?.author
                if (!author.isNullOrEmpty() && !isBlockListContains(author)) {
                    buffer.add(author)
                }
            }
            if (buffer.isEmpty()) {
                showShortToast("無可加入黑名單的ID")
                return
            }
            val names = buffer.toTypedArray()
            ASListDialog.createDialog().addItems(names)
                .setListener(object : ASListDialogItemClickListener {
                    override fun onListDialogItemLongClicked(
                        paramASListDialog: ASListDialog?,
                        index: Int,
                        title: String?
                    ): Boolean = true

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
            .setListener { _, index ->
                if (index == 1) {
                    val newList: MutableList<String> = blockList
                    if (newList.contains(aBlockName)) {
                        showShortToast(getContextString(R.string.already_have_item))
                    } else {
                        newList.add(aBlockName)
                    }

                    blockList = newList
                    notifyDataUpdated()

                    val article = telnetArticle ?: currentArticle
                    if (propertiesBlockListEnable && article != null) {
                        if (aBlockName == article.author) {
                            onBackPressed()
                        }
                    }
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    fun setBoardPage(aBoardMainPage: BoardMainPage) {
        boardMainPage = aBoardMainPage
    }

    fun setArticle(aArticle: TelnetArticle): Boolean {
        var isSuccess = true
        telnetArticle = aArticle
        currentArticle = aArticle

        val boardName = boardMainPage?.listName ?: ""
        if (boardName.isNotEmpty()) {
            val store = TempSettings.bookmarkStore
            if (store != null) {
                val bookmarkList = store.getBookmarkList(boardName)
                bookmarkList.addHistoryBookmark(aArticle.title)
                store.store()
            }
        }

        if (telnetView != null && aArticle.frame != null) {
            telnetView?.frame = aArticle.frame!!
        }

        dismissProcessingDialog()
        isSuccess = verifyAndEditFromLinked(aArticle)
        return isSuccess
    }

    private fun verifyAndEditFromLinked(article: TelnetArticle): Boolean {
        val state = TempSettings.editFromLinkedState ?: return true
        if (state.step != EditFromLinkedStep.READING_ARTICLE &&
            state.step != EditFromLinkedStep.SEARCH_NEXT &&
            state.step != EditFromLinkedStep.SEARCH_PREV
        ) {
            return true
        }

        if (state.matchesTarget(article)) {
            state.step = EditFromLinkedStep.DONE
            TempSettings.editFromLinkedState = null
            onEditButtonClicked()
        } else {
            state.retryCount++
            state.step = EditFromLinkedStep.FAILED
            TempSettings.editFromLinkedState = null
            showShortToast("找不到文章")
            onBackPressed()
            return false
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    fun changeLoadingPercentage(percentage: String?) {
        showProcessingDialog(getContextString(R.string.loading_) + "\n" + percentage)
    }

    fun ctrlQUser(fromStrings: Vector<String>) {
        try {
            ASCoroutine.ensureMainThread {
                val dialogQueryHero = DialogQueryHero()
                dialogQueryHero.show()
                dialogQueryHero.getData(fromStrings)
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
    }

    // -------------------------------------------------------------
    // Compose 畫面主體
    // -------------------------------------------------------------

    @Composable
    fun ArticlePageContent() {
        val colors = AppTheme.colors
        val article = currentArticle

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 1. 頂部導覽列
            ArticleTopBar(
                title = article?.title ?: stringResource(R.string.loading_),
                subtitle = article?.let {
                    val nick = if (!it.nickName.isNullOrEmpty()) " (${it.nickName})" else ""
                    "${it.boardName}  ${it.author}$nick"
                } ?: "",
                onBackClick = { onBackPressed() },
                onMenuClick = { onMenuClicked() }
            )

            // 2. 外部快捷工具列 (推/噓, 切換模式, 開啟連結)
            if (isExtToolbarOpenState || viewModeState == ArticleViewMode.Companion.MODE_TELNET) {
                ArticleExtToolbar(
                    onDoGy = { onGYButtonClicked() },
                    onChangeMode = {
                        changeViewMode()
                        refreshExternalToolbar()
                    },
                    onOpenLink = { onOpenLinkClicked() }
                )
            }

            // 3. 文章內容主體
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (article == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        BahaText(
                            text = stringResource(R.string.loading_),
                            color = colors.textSecondary,
                            size = BahaTextSize.BODY
                        )
                    }
                } else {
                    if (viewModeState == ArticleViewMode.Companion.MODE_TEXT) {
                        // 文字模式
                        ArticleTextModeContent(
                            article = article,
                            colors = colors,
                            onAuthorClick = { author ->
                                showAuthorActionDialog(author)
                            }
                        )
                    } else {
                        // Telnet 畫面模式
                        ArticleTelnetModeContent(
                            article = article,
                            onTelnetViewCreated = { view ->
                                this@ArticlePage.telnetView = view
                            }
                        )
                    }
                }
            }

            // 4. 底部工具列 (回覆, 上一篇, 下一篇)
            ArticleBottomToolbar(
                colors = colors,
                toolbarLocation = toolbarLocationState,
                toolbarOrder = toolbarOrderState,
                onReplyClick = { replyListener.onClick(null) },
                onPrevClick = { pageUpListener.onClick(null) },
                onFirstClick = { pageTopListener.onLongClick(null) },
                onNextClick = { pageDownListener.onClick(null) },
                onLastClick = { pageBottomListener.onLongClick(null) },
                onLLClick = { btnLLListener.onClick(null) },
                onRRClick = { btnRRListener.onClick(null) }
            )
        }
    }

    /** 點擊作者動作選單 (查詢勇者 / 寄信) */
    private fun showAuthorActionDialog(author: String) {
        var cleanAuthor = author
        if (cleanAuthor.contains("(")) {
            cleanAuthor = cleanAuthor.substring(0, cleanAuthor.indexOf("("))
        }
        ASListDialog.createDialog()
            .setTitle(cleanAuthor)
            .addItem(getContextString(R.string.dialog_query_hero))
            .addItem(getContextString(R.string.message_sub_send_hero))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean = true

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    if (title == getContextString(R.string.dialog_query_hero)) {
                        TelnetClient.myInstance!!.sendDataToServer(
                            create().pushKey(TelnetKeyboard.CTRL_Q).build()
                        )
                    } else if (title == getContextString(R.string.message_sub_send_hero)) {
                        val aPage = PageContainer.instance!!.getMessageSub()
                        ASNavigationController.currentController!!.pushViewController(aPage)
                        aPage.setSenderName(cleanAuthor)
                    }
                }
            }).show()
    }
}

// -------------------------------------------------------------
// 子元件定義
// -------------------------------------------------------------

/**
 * 頂部導覽列
 */
@Composable
fun ArticleTopBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.toolbarBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.titleBarTitle
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                BahaText(
                    text = title,
                    color = colors.titleBarTitle,
                    size = BahaTextSize.BODY,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotEmpty()) {
                    BahaText(
                        text = subtitle,
                        color = colors.titleBarDetail,
                        size = BahaTextSize.TINY,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.zero_word),
                    tint = colors.titleBarTitle
                )
            }
        }
        HorizontalDivider(color = colors.divider, thickness = 1.dp)
    }
}

/**
 * 外部快捷操作工具列 (推/噓, 切換模式, 開啟連結)
 */
@Composable
fun ArticleExtToolbar(
    onDoGy: () -> Unit,
    onChangeMode: () -> Unit,
    onOpenLink: () -> Unit
) {
    val colors = AppTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.toolbarBackground)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BahaButton(
                text = stringResource(R.string.do_gy),
                type = ButtonType.NORMAL,
                onClick = onDoGy,
                modifier = Modifier.weight(1f)
            )
            BahaButton(
                text = stringResource(R.string.change_mode),
                type = ButtonType.NORMAL,
                onClick = onChangeMode,
                modifier = Modifier.weight(1f)
            )
            BahaButton(
                text = stringResource(R.string.open_url),
                type = ButtonType.NORMAL,
                onClick = onOpenLink,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = colors.divider, thickness = 1.dp)
    }
}

/**
 * 文字模式內容主體
 */
@Composable
fun ArticleTextModeContent(
    article: TelnetArticle,
    colors: AppColors,
    onAuthorClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // 1. 標頭資訊卡片
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    BahaText(
                        text = article.title,
                        color = colors.titleBarTitle,
                        size = BahaTextSize.SUBTITLE,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BahaText(
                            text = "作者: ${article.author}${if (!article.nickName.isNullOrEmpty()) " (${article.nickName})" else ""}",
                            color = colors.titleBarDetail2,
                            size = BahaTextSize.CAPTION,
                            modifier = Modifier.clickable { onAuthorClick(article.author) }
                        )
                        BahaText(
                            text = "看板: ${article.boardName}",
                            color = colors.titleBarDetail,
                            size = BahaTextSize.CAPTION
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    BahaText(
                        text = "時間: ${article.dateTime}",
                        color = colors.textSecondary,
                        size = BahaTextSize.TINY
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 2. 內文區塊
        items(count = article.itemSize) { i ->
            val item = article.getItem(i)
            if (item != null) {
                // 黑名單過濾判定
                val isBlocked = propertiesBlockListEnable && isBlockListContains(item.author)
                if (!isBlocked) {
                    ArticleContentBlockItem(item = item, colors = colors)
                }
            }
        }

        // 3. 發文來源 IP 與時間
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (article.fromIP.isNotEmpty()) {
                    BahaText(
                        text = "※ 發文來源: ${article.fromIP}",
                        color = colors.textSecondary,
                        size = BahaTextSize.TINY
                    )
                }
                BahaText(
                    text = "《 ${article.dateTime} 》",
                    color = colors.bbsMailDate,
                    size = BahaTextSize.TINY
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
        }

        // 4. 修改紀錄列表
        if (article.editRecordSize > 0) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    for (eIndex in 0 until article.editRecordSize) {
                        val editRec = article.getEditRecord(eIndex)
                        if (editRec != null) {
                            BahaText(
                                text = "※ 修改: ${editRec.author} 於 ${editRec.dateTime}",
                                color = colors.bbsMailMark,
                                size = BahaTextSize.TINY,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
            }
        }

        // 5. 推文列表
        if (article.pushSize > 0) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                BahaText(
                    text = "── 推文列表 (${article.pushSize}) ──",
                    color = colors.titleBarTitle,
                    size = BahaTextSize.CAPTION,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(count = article.pushSize) { pIndex ->
                val push = article.getPush(pIndex)
                if (push != null) {
                    ArticlePushRowItem(
                        push = push,
                        floor = pIndex + 1,
                        colors = colors
                    )
                }
            }
        }

        // 底部安全留白
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 內文區塊元件 (支援引言顏色與超連結)
 */
@Composable
fun ArticleContentBlockItem(
    item: TelnetArticleItem,
    colors: AppColors
) {
    val quoteLevel = item.quoteLevel
    val isQuote = quoteLevel > 0

    // 作者引述列
    if (!item.author.isNullOrEmpty()) {
        val nick = if (!item.nickname.isNullOrEmpty()) "(${item.nickname})" else ""
        BahaText(
            text = "※ 引述《${item.author}$nick》之銘言：",
            color = if (isQuote) colors.bbsAuthor1 else colors.bbsAuthor0,
            size = BahaTextSize.CAPTION,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
    }

    // 正文內容
    val content = item.content
    if (content.isNotEmpty()) {
        val textColor = if (isQuote) colors.bbsContent1 else colors.bbsContent0
        LinkableText(
            text = content,
            defaultColor = textColor,
            size = BahaTextSize.BODY,
            modifier = Modifier.padding(
                start = if (isQuote) (quoteLevel * 8).dp else 0.dp,
                top = 2.dp,
                bottom = 4.dp
            )
        )
    }
}

/**
 * 推文項目 Row
 */
@Composable
fun ArticlePushRowItem(
    push: TelnetArticlePush,
    floor: Int,
    colors: AppColors
) {
    val tagColor = when (push.type) {
        1 -> Color(0xFF80FF80) // 推 (綠)
        2 -> Color(0xFFFF6060) // 噓 (紅)
        else -> Color(0xFFE0E0E0) // → (白/灰)
    }
    val tagText = when (push.type) {
        1 -> "推"
        2 -> "噓"
        else -> "→"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // 樓層編號
            BahaText(
                text = "[$floor 樓]",
                color = colors.textSecondary,
                size = BahaTextSize.TINY,
                modifier = Modifier.width(44.dp)
            )

            // 推/噓/→ 標籤
            BahaText(
                text = tagText,
                color = tagColor,
                size = BahaTextSize.TINY,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(18.dp)
            )

            // 推文作者
            BahaText(
                text = "${push.author}: ",
                color = Color(0xFF80FFFF),
                size = BahaTextSize.CAPTION,
                fontWeight = FontWeight.Bold
            )

            // 推文內容
            LinkableText(
                text = push.content,
                defaultColor = colors.textPrimary,
                size = BahaTextSize.CAPTION,
                modifier = Modifier.weight(1f)
            )

            // 推文時間
            if (push.date.isNotEmpty() || push.time.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                BahaText(
                    text = "${push.date} ${push.time}",
                    color = colors.textSecondary,
                    size = BahaTextSize.TINY
                )
            }
        }
        HorizontalDivider(color = colors.divider.copy(alpha = 0.2f), thickness = 0.5.dp)
    }
}

/**
 * 支援點擊超連結的文字元件
 */
@Composable
fun LinkableText(
    text: String,
    defaultColor: Color,
    size: BahaTextSize = BahaTextSize.BODY,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val urlPattern = Pattern.compile(
        "https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    )
    val matcher = urlPattern.matcher(text)

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val url = matcher.group()

            // 附加連結前的一般文字
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            // 附加超連結
            pushStringAnnotation(tag = "URL", annotation = url)
            pushStyle(
                SpanStyle(
                    color = Color(0xFF64B5F6),
                    textDecoration = TextDecoration.Underline
                )
            )
            append(url)
            pop()
            pop()

            lastIndex = end
        }

        // 附加結尾文字
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    val resolvedFontSize = when (size) {
        BahaTextSize.ULTRA_LARGE -> AppTheme.fontSize.ultraLarge
        BahaTextSize.LARGE -> AppTheme.fontSize.large
        BahaTextSize.BASE -> AppTheme.fontSize.base
        BahaTextSize.TITLE -> AppTheme.fontSize.title
        BahaTextSize.SUBTITLE -> AppTheme.fontSize.subtitle
        BahaTextSize.BODY -> AppTheme.fontSize.body
        BahaTextSize.CAPTION -> AppTheme.fontSize.caption
        BahaTextSize.TINY -> AppTheme.fontSize.tiny
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = androidx.compose.ui.text.TextStyle(
            color = defaultColor,
            fontSize = resolvedFontSize,
            fontFamily = FontFamily.Default
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        uriHandler.openUri(annotation.item)
                    } catch (_: Exception) {}
                }
        }
    )
}

/**
 * Telnet 原始終端模式主體
 */
@Composable
fun ArticleTelnetModeContent(
    article: TelnetArticle,
    onTelnetViewCreated: (TelnetView) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
    ) {
        AndroidView(
            factory = { ctx ->
                TelnetView(ctx).apply {
                    if (article.frame != null) {
                        frame = article.frame!!
                    }
                    onTelnetViewCreated(this)
                }
            },
            update = { view ->
                if (article.frame != null) {
                    view.frame = article.frame!!
                }
            }
        )
    }
}

/**
 * 底部操作工具列
 */
@Composable
fun ArticleBottomToolbar(
    colors: AppColors,
    toolbarLocation: Int,
    toolbarOrder: Int,
    onReplyClick: () -> Unit,
    onPrevClick: () -> Unit,
    onFirstClick: () -> Unit,
    onNextClick: () -> Unit,
    onLastClick: () -> Unit,
    onLLClick: () -> Unit,
    onRRClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.toolbarDivider, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colors.toolbarBackground),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 靠右對齊時左側切換按鈕
            if (toolbarLocation == 2) {
                BahaButton(
                    text = "<<",
                    type = ButtonType.NORMAL,
                    onClick = onLLClick,
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                )
            }

            val buttons: List<@Composable () -> Unit> = listOf(
                {
                    BahaButton(
                        text = stringResource(R.string.reply),
                        type = ButtonType.NORMAL,
                        onClick = onReplyClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                },
                {
                    BahaButton(
                        text = stringResource(R.string.prev_article),
                        type = ButtonType.NORMAL,
                        onClick = onPrevClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                },
                {
                    BahaButton(
                        text = stringResource(R.string.next_article),
                        type = ButtonType.NORMAL,
                        onClick = onNextClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            )

            // 反轉順序判定
            val orderedButtons = if (toolbarOrder == 1) buttons.reversed() else buttons
            for (btn in orderedButtons) {
                btn()
            }

            // 靠左對齊時右側切換按鈕
            if (toolbarLocation == 1) {
                BahaButton(
                    text = ">>",
                    type = ButtonType.NORMAL,
                    onClick = onRRClick,
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}
