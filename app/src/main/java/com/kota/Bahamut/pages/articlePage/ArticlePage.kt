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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
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
import com.kota.Bahamut.service.UserSettings
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
import com.kota.Bahamut.ui.components.rememberDrawablePainter
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
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView
import java.util.Locale
import java.util.Vector
import java.util.regex.Pattern
import kotlin.math.min

/**
 * 文章閱讀頁面 (純 Jetpack Compose 實作)
 *
 * 具備雙模式：文字閱讀模式 (TextMode) 與 原始 Telnet 畫面模式 (TelnetMode)。
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
    var loadAllImagesTrigger by mutableIntStateOf(0)

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
        if (propertiesArticleViewMode == ArticleViewMode.MODE_TEXT || isFullScreen) {
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
        if (articleMode == ArticleViewMode.MODE_TELNET) {
            enable = true
        }
        isExtToolbarOpenState = enable
    }

    /** 載入全部圖片 */
    fun onLoadAllImageClicked() {
        loadAllImagesTrigger++
    }

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
                .addItem(getContextString(R.string.board_page_item_load_all_image))
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
                            9 -> onLoadAllImageClicked()
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
        val isSuccess: Boolean = verifyAndEditFromLinked(aArticle)
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
            val authorText = article?.let {
                val nick = if (!it.nickName.isNullOrEmpty()) "(${it.nickName})" else ""
                "${it.author}$nick"
            } ?: ""
            val boardText = article?.boardName ?: ""

            // 0. 外部快捷工具列 (推/噓, 切換模式, 開啟連結)
            if (isExtToolbarOpenState || viewModeState == ArticleViewMode.MODE_TELNET) {
                ArticleExtToolbar(
                    onDoGy = { onGYButtonClicked() },
                    onChangeMode = {
                        changeViewMode()
                        refreshExternalToolbar()
                    },
                    onOpenLink = { onOpenLinkClicked() }
                )
            }

            // 1. 頂部導覽列
            ArticleTopBar(
                title = article?.title ?: stringResource(R.string.loading_),
                author = authorText,
                boardName = boardText,
                onAuthorClick = {
                    if (article != null) {
                        showAuthorActionDialog(authorText)
                    }
                },
                onMenuClick = { onMenuClicked() }
            )

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
                            fontSize = BahaTextSize.BODY
                        )
                    }
                } else {
                    if (viewModeState == ArticleViewMode.MODE_TEXT) {
                        // 文字模式
                        ArticleTextModeContent(
                            article = article,
                            colors = colors,
                            loadAllTrigger = loadAllImagesTrigger,
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
            .setTitle(author)
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
 * 頂部導覽列 (經典 BBS 深海藍底，雙行文字，無返回鍵，右側選單按鈕)
 */
@Composable
fun ArticleTopBar(
    title: String,
    author: String,
    boardName: String,
    onAuthorClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val colors = AppTheme.colors
    var isExpanded by remember { mutableStateOf(true) }

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
            // 左側文章資訊區塊 (雙行)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            ) {
                // 第一列：文章標題
                BahaText(
                    text = title,
                    color = colors.titleBarTitle,
                    fontSize = BahaTextSize.TITLE,
                    maxLines = if (isExpanded) 3 else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 第二列：作者 (白色，可點擊呼叫選單) 與 看板 (青色)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = author,
                        color = colors.titleBarDetail,
                        fontSize = BahaTextSize.BODY,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable { onAuthorClick() }
                    )

                    if (boardName.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        BahaText(
                            text = boardName,
                            color = colors.titleBarDetail2,
                            fontSize = BahaTextSize.BODY,
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
                .height(50.dp)
                .background(colors.toolbarBackground)
                .padding(horizontal = 6.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BahaButton(
                text = stringResource(R.string.do_gy),
                type = ButtonType.NORMAL,
                fontSize = BahaTextSize.TITLE,
                onClick = onDoGy,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.toolbarDivider)
            )
            BahaButton(
                text = stringResource(R.string.change_mode),
                type = ButtonType.NORMAL,
                fontSize = BahaTextSize.TITLE,
                onClick = onChangeMode,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.toolbarDivider)
            )
            BahaButton(
                text = stringResource(R.string.open_url),
                type = ButtonType.NORMAL,
                fontSize = BahaTextSize.TITLE,
                onClick = onOpenLink,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 文字模式內容主體
 */
@Composable
fun ArticleTextModeContent(
    article: TelnetArticle,
    colors: AppColors,
    loadAllTrigger: Int = 0,
    onAuthorClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. 內文區塊 (還原原生 BBS 文字項目，無額外 Material 卡片)
        if (article.itemSize == 0 && article.frame != null) {
            item {
                ArticleTelnetBlockItem(frame = article.frame!!)
            }
        } else {
            items(count = article.itemSize) { i ->
                val item = article.getItem(i)
                if (item != null) {
                    val isBlocked = propertiesBlockListEnable && isBlockListContains(item.author)
                    if (!isBlocked) {
                        if (item.type == 1 && item.frame != null) {
                            ArticleTelnetBlockItem(frame = item.frame!!)
                        } else {
                            ArticleContentBlockItem(
                                item = item,
                                colors = colors,
                                loadAllTrigger = loadAllTrigger,
                                onAuthorClick = onAuthorClick
                            )
                        }
                    }
                }
            }
        }

        // 2. 發文來源 IP 與時間長條 (原生 ArticlePageTimeTimeView 樣式：深藍色全寬橫條)
        item {
            ArticlePostTimeBar(
                ip = article.fromIP,
                time = article.dateTime,
                colors = colors
            )
        }

        // 3. 修改紀錄列表
        if (article.editRecordSize > 0) {
            items(count = article.editRecordSize) { eIndex ->
                val editRec = article.getEditRecord(eIndex)
                if (editRec != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        BahaText(
                            text = "※ 修改: ${editRec.author} 於 ${editRec.dateTime}",
                            color = colors.dialogBorder,
                            fontSize = BahaTextSize.BODY
                        )
                    }
                }
            }
        }

        // 4. 推文列表
        if (article.pushSize > 0) {
            items(count = article.pushSize) { pIndex ->
                val push = article.getPush(pIndex)
                if (push != null) {
                    ArticlePushRowItem(
                        push = push,
                        floor = pIndex + 1,
                        colors = colors,
                        loadAllTrigger = loadAllTrigger,
                        onAuthorClick = onAuthorClick
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
 * 簽名檔或 ANSI 圖形區塊元件
 */
@Composable
fun ArticleTelnetBlockItem(frame: TelnetFrame) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        factory = { ctx ->
            TelnetView(ctx).apply {
                this.frame = frame
            }
        },
        update = { view ->
            view.frame = frame
        }
    )
}

/**
 * 原生發文 IP 與時間長條元件 (深藍底全寬，左側為 IP，右側為日期時間)
 */
@Composable
fun ArticlePostTimeBar(
    ip: String,
    time: String,
    colors: AppColors
) {
    if (ip.isEmpty() && time.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.titleBarBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BahaText(
            text = ip,
            color = colors.titleBarDetail,
            fontSize = BahaTextSize.BODY,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (time.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            BahaText(
                text = "《$time》",
                color = colors.titleBarDetail,
                fontSize = BahaTextSize.BODY,
                maxLines = 1
            )
        }
    }
}

/**
 * 內文區塊元件 (原生格式：作者名說: + 內文，支援引言顏色與超連結，並支援預覽縮圖)
 */
@Composable
fun ArticleContentBlockItem(
    item: TelnetArticleItem,
    colors: AppColors,
    loadAllTrigger: Int = 0,
    onAuthorClick: (String) -> Unit
) {
    val quoteLevel = item.quoteLevel
    val isQuote = quoteLevel > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // 作者列: 例如 "syo0093("順番に殴るね") 說:"
        if (!item.author.isNullOrEmpty()) {
            val nick = if (!item.nickname.isNullOrEmpty()) "(${item.nickname})" else ""
            val authorColor = if (isQuote) colors.bbsAuthor1 else colors.bbsAuthor0
            BahaText(
                text = "${item.author}$nick 說:",
                color = authorColor,
                fontSize = BahaTextSize.TITLE,
                modifier = Modifier
                    .clickable { onAuthorClick(item.author ?: "") }
                    .padding(bottom = if (isQuote) 2.dp else 10.dp)
            )
        }

        // 正文內容 (依據超連結分割，將 ThumbnailView 精確插入至連結正下方)
        if (item.content.isNotEmpty()) {
            val fixedText = remember(item.content) { fixUrlNewlines(item.content) }
            val textColor = if (isQuote) colors.bbsContent1 else colors.bbsContent0
            val segments = remember(fixedText) { parseContentSegments(fixedText) }

            for (segment in segments) {
                if (segment.text.isNotEmpty()) {
                    LinkableText(
                        text = segment.text,
                        defaultColor = textColor,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                if (UserSettings.linkAutoShow && segment.url != null) {
                    ArticleThumbnail(
                        url = segment.url,
                        loadAllTrigger = loadAllTrigger
                    )
                }
            }
        } else if (item.frame != null) {
            ArticleTelnetBlockItem(frame = item.frame!!)
        }

        // 底部微弱分隔線
        HorizontalDivider(
            color = colors.divider.copy(alpha = 0.25f),
            thickness = 0.5.dp
        )
    }
}

/**
 * 推文項目 Row (還原原生 ArticlePagePushItemView 佈局，支援網址與預覽縮圖)
 */
@Composable
fun ArticlePushRowItem(
    push: TelnetArticlePush,
    floor: Int,
    colors: AppColors,
    loadAllTrigger: Int = 0,
    onAuthorClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 推文作者
            BahaText(
                text = push.author,
                color = colors.textSecondary,
                fontSize = BahaTextSize.BODY,
                modifier = Modifier.clickable { onAuthorClick(push.author) }
            )

            Spacer(modifier = Modifier.width(4.dp))

            // 樓層編號
            BahaText(
                text = " [ $floor 樓]",
                color = colors.textSecondary,
                fontSize = BahaTextSize.BODY
            )

            Spacer(modifier = Modifier.weight(1f))

            // 推文時間
            if (push.date.isNotEmpty() || push.time.isNotEmpty()) {
                BahaText(
                    text = "${push.date} ${push.time}".trim(),
                    color = colors.textSecondary,
                    fontSize = BahaTextSize.BODY
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 推文內容 (依據超連結分割，將 ThumbnailView 精確插入至連結正下方)
        if (push.content.isNotEmpty()) {
            val fixedContent = remember(push.content) { fixUrlNewlines(push.content) }
            val segments = remember(fixedContent) { parseContentSegments(fixedContent) }

            for (segment in segments) {
                if (segment.text.isNotEmpty()) {
                    LinkableText(
                        text = segment.text,
                        defaultColor = colors.bbsBoardFollowOtherRead,
                        fontSize = AppTheme.fontSize.body
                    )
                }

                if (UserSettings.linkAutoShow && segment.url != null) {
                    ArticleThumbnail(
                        url = segment.url,
                        loadAllTrigger = loadAllTrigger
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = colors.divider.copy(alpha = 0.25f),
            thickness = 0.5.dp
        )
    }
}

/**
 * 連結預覽縮圖元件 (Compose)
 */
@Composable
fun ArticleThumbnail(
    url: String,
    loadAllTrigger: Int,
    modifier: Modifier = Modifier
) {
    ThumbnailItem(
        url = url,
        loadAllTrigger = loadAllTrigger,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}


/** 修正 BBS 每行 78 字元導致的網址換行 */
fun fixUrlNewlines(text: String): String {
    val result = StringBuilder()
    val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val urlBuffer = StringBuilder()
    var inUrl = false

    for (line in lines) {
        if (inUrl) {
            if (line.length < 78) {
                urlBuffer.append(line)
                result.append(urlBuffer).append("\n")
                urlBuffer.setLength(0)
                inUrl = false
            } else {
                urlBuffer.append(line)
            }
        } else {
            if (line.contains("http://") || line.contains("https://")) {
                inUrl = true
                if (line.startsWith("http://") || line.startsWith("https://")) {
                    urlBuffer.append(line)
                    if (line.length < 78) {
                        result.append(urlBuffer).append("\n")
                        urlBuffer.setLength(0)
                        inUrl = false
                    }
                } else {
                    val httpIndex = line.indexOf("http://")
                    val httpsIndex = line.indexOf("https://")
                    var urlStartIndex = -1

                    if (httpIndex != -1 && httpsIndex != -1) {
                        urlStartIndex = min(httpIndex, httpsIndex)
                    } else if (httpIndex != -1) {
                        urlStartIndex = httpIndex
                    } else if (httpsIndex != -1) {
                        urlStartIndex = httpsIndex
                    }

                    if (urlStartIndex > 0) {
                        result.append(line.substring(0, urlStartIndex)).append("\n")
                        urlBuffer.append(line.substring(urlStartIndex))
                    } else {
                        urlBuffer.append(line)
                    }
                }
            } else {
                result.append(line).append("\n")
            }
        }
    }
    if (urlBuffer.isNotEmpty()) {
        result.append(urlBuffer)
    }
    return result.toString()
}

/**
 * 文章內容段落結構
 * @param text 本段顯示之文字（若包含超連結，則到該超連結結尾為止）
 * @param url 若此段末尾包含超連結，則為該超連結 URL，供下方精確插入預覽圖
 */
data class ContentSegment(
    val text: String,
    val url: String? = null
)

/**
 * 依據超連結分割文章文字內容，使縮圖預覽可以直接插入至各超連結正下方，隨後再接續後續內文
 */
fun parseContentSegments(rawText: String): List<ContentSegment> {
    val urlPattern = Pattern.compile(
        "https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    )
    val matcher = urlPattern.matcher(rawText)
    val segments = mutableListOf<ContentSegment>()
    var lastIndex = 0

    while (matcher.find()) {
        val url = matcher.group().trim()
        val urlEnd = matcher.end()
        // 本段文字為從上一段結尾到當前網址結尾 (包含網址)
        val partText = rawText.substring(lastIndex, urlEnd)
        segments.add(ContentSegment(text = partText, url = url))

        // 若網址後方緊接換行符號 (\r\n 或 \n)，略過該換行符，避免圖片下方產生多餘空行
        var nextStart = urlEnd
        if (nextStart < rawText.length && rawText[nextStart] == '\r') {
            nextStart++
        }
        if (nextStart < rawText.length && rawText[nextStart] == '\n') {
            nextStart++
        }
        lastIndex = nextStart
    }

    // 若最後一個網址後方還有剩餘文字
    if (lastIndex < rawText.length) {
        val remainingText = rawText.substring(lastIndex)
        if (remainingText.isNotEmpty()) {
            segments.add(ContentSegment(text = remainingText, url = null))
        }
    }

    // 若全文完全沒有任何網址，直接返回完整文字區段
    if (segments.isEmpty() && rawText.isNotEmpty()) {
        segments.add(ContentSegment(text = rawText, url = null))
    }

    return segments
}

/**
 * 支援點擊超連結的文字元件
 */
@Suppress("DEPRECATION")
@Composable
fun LinkableText(
    text: String,
    defaultColor: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = AppTheme.fontSize.title
) {
    val uriHandler = LocalUriHandler.current
    val linkColor = AppTheme.colors.textLink
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
                    color = linkColor,
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

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = TextStyle(
            color = defaultColor,
            fontSize = fontSize,
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
    val verticalScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.toolbarDivider)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colors.toolbarBackground),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 按鈕文字大小
            var newFontSize = BahaTextSize.BASE
            if (toolbarLocation == 1 || toolbarLocation == 2)
                newFontSize = BahaTextSize.TITLE

            // 靠右對齊時左側切換按鈕 (LL)
            if (toolbarLocation == 2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors.pageBackground)
                        .clickable(onClick = onLLClick),
                    contentAlignment = Alignment.Center
                ) {
                    BahaText(
                        text = stringResource(R.string.toolbar_item_ll),
                        color = colors.dialogSelectArticleFocused.copy(alpha = 0.5f),
                        fontSize = BahaTextSize.TITLE
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
                        text = stringResource(R.string.reply),
                        type = ButtonType.NORMAL,
                        fontSize = newFontSize,
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
                        fontSize = newFontSize,
                        onClick = onPrevClick,
                        onLongClick = onFirstClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                },
                {
                    BahaButton(
                        text = stringResource(R.string.next_article),
                        type = ButtonType.NORMAL,
                        fontSize = newFontSize,
                        onClick = onNextClick,
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
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors.pageBackground)
                        .clickable(onClick = onRRClick),
                    contentAlignment = Alignment.Center
                ) {
                    BahaText(
                        text = stringResource(R.string.toolbar_item_rr),
                        color = colors.dialogSelectArticleFocused.copy(alpha = 0.5f),
                        fontSize = BahaTextSize.TITLE
                    )
                }
            }
        }
    }
}
