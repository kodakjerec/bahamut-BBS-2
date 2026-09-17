package com.kota.Bahamut.pages.essencePage

import android.content.Context
import android.content.Intent
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.command.BahamutCommandFSendMail
import com.kota.Bahamut.pages.articlePage.ArticleTelnetModeContent
import com.kota.Bahamut.pages.articlePage.ArticleTextModeContent
import com.kota.Bahamut.pages.articlePage.ArticleViewMode
import com.kota.Bahamut.pages.mailPage.SendMailPage
import com.kota.Bahamut.pages.mailPage.SendMailPageListener
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView

class ArticleEssencePage : TelnetPage(), SendMailPageListener {
    private var telnetArticle: TelnetArticle? = null
    var telnetView: TelnetView? = null
    private var boardEssencePage: BoardEssencePage? = null

    // Compose state
    var currentArticle by mutableStateOf<TelnetArticle?>(null)
    var viewModeState by mutableIntStateOf(ArticleViewMode.MODE_TEXT)

    override val pageLayout: Int
        get() = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_ARTICLE_ESSENCE

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                ArticleEssencePageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        clear()
        val result = super.onBackPressed()
        PageContainer.instance!!.cleanArticleEssencePage()
        return result
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    fun setBoardEssencePage(page: BoardEssencePage) {
        boardEssencePage = page
    }

    fun setArticle(aArticle: TelnetArticle?) {
        clear()
        telnetArticle = aArticle
        currentArticle = aArticle
        if (telnetView != null && aArticle?.frame != null) {
            telnetView?.frame = aArticle.frame!!
        }
        ASProcessingDialog.dismissProcessingDialog()
    }

    fun changeLoadingPercentage(percentage: String?) {
        ASProcessingDialog.showProcessingDialog(CommonFunctions.getContextString(R.string.loading_) + "\n" + percentage)
    }

    fun fSendMail() {
        boardEssencePage?.pushCommand(BahamutCommandFSendMail(UserSettings.propertiesUsername))
    }

    override fun clear() {
        telnetArticle = null
        currentArticle = null
    }

    private fun onPageUpButtonClicked() {
        if (TelnetClient.myInstance?.telnetConnector?.isConnecting == true) {
            boardEssencePage?.loadPreviousArticle()
                ?: PageContainer.instance!!.boardEssencePage.loadPreviousArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    private fun onPageDownButtonClicked() {
        if (TelnetClient.myInstance?.telnetConnector?.isConnecting == true) {
            boardEssencePage?.loadNextArticle()
                ?: PageContainer.instance!!.boardEssencePage.loadNextArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    private fun showConnectionClosedToast() {
        ASToast.showShortToast("連線已中斷")
    }

    private fun toggleViewMode() {
        viewModeState = if (viewModeState == ArticleViewMode.MODE_TEXT) {
            ArticleViewMode.MODE_TELNET
        } else {
            ArticleViewMode.MODE_TEXT
        }
    }

    // 選單: 開啟文章連結
    private fun onOpenUrlClicked() {
        val article = currentArticle ?: telnetArticle ?: return
        val textView = TextView(context).apply {
            text = article.fullText
        }
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        val urls = textView.urls
        if (urls.isNullOrEmpty()) {
            ASToast.showShortToast("本頁面沒有超連結")
            return
        }

        val urlTitles = urls.map { it.url }.toTypedArray()
        ASListDialog.createDialog()
            .setTitle("連結")
            .addItems(urlTitles)
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemClicked(paramASListDialog: ASListDialog?, index: Int, title: String?) {
                    val url = urls[index].url
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(intent)
                }

                override fun onListDialogItemLongClicked(paramASListDialog: ASListDialog?, index: Int, title: String?): Boolean = true
            }).show()
    }

    // 選單: 寄信給原作者
    private fun onSendMailClicked() {
        val article = currentArticle ?: telnetArticle ?: return
        val sendMailPage = SendMailPage().apply {
            setReceiver(article.author)
            setPostTitle("Re: " + article.title)
            setListener(this@ArticleEssencePage)
        }
        navigationController.pushViewController(sendMailPage)
        fSendMail()
    }

    override fun onSendMailDialogSendButtonClicked(
        sendMailPage: SendMailPage,
        receiver: String,
        title: String,
        content: String
    ) {
        PageContainer.instance!!.mailBoxPage.onSendMailDialogSendButtonClicked(
            sendMailPage,
            receiver,
            title,
            content
        )
        onBackPressed()
    }

    // 選單: 加入黑名單
    private fun onAddBlockListClicked() {
        val article = currentArticle ?: telnetArticle ?: return
        val author = article.author
        if (author.isEmpty()) return

        ASAlertDialog.createDialog()
            .setTitle("加入黑名單")
            .setMessage("是否要將 \"$author\" 加入黑名單?")
            .addButton("取消")
            .addButton("加入")
            .setListener { _, index ->
                if (index == 1) {
                    val newList = UserSettings.blockList
                    if (!newList.contains(author)) {
                        newList.add(author)
                        UserSettings.blockList = newList
                        UserSettings.notifyDataUpdated()
                        ASToast.showShortToast("已加入黑名單")
                    } else {
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.already_have_item))
                    }
                }
            }.scheduleDismissOnPageDisappear(this).show()
    }

    @Composable
    fun ArticleEssencePageContent() {
        val colors = AppTheme.colors
        val article = currentArticle
        var menuExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 頂部導覽列
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.titleBarBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onBackPressed() }) {
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
                            text = article?.title ?: stringResource(R.string.loading_),
                            color = colors.titleBarTitle,
                            size = BahaTextSize.BODY,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (article != null) {
                            val nick = if (!article.nickName.isNullOrEmpty()) " (${article.nickName})" else ""
                            BahaText(
                                text = "${article.boardName}  ${article.author}$nick",
                                color = colors.titleBarDetail,
                                size = BahaTextSize.TINY,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.zero_word),
                                tint = colors.titleBarTitle
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(colors.surface)
                        ) {
                            DropdownMenuItem(
                                text = { BahaText(stringResource(R.string.change_mode), color = colors.textPrimary, size = BahaTextSize.SUBTITLE) },
                                onClick = {
                                    menuExpanded = false
                                    toggleViewMode()
                                }
                            )
                            DropdownMenuItem(
                                text = { BahaText(stringResource(R.string.open_url), color = colors.textPrimary, size = BahaTextSize.SUBTITLE) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenUrlClicked()
                                }
                            )
                            DropdownMenuItem(
                                text = { BahaText("寄信給原作者", color = colors.textPrimary, size = BahaTextSize.SUBTITLE) },
                                onClick = {
                                    menuExpanded = false
                                    onSendMailClicked()
                                }
                            )
                            DropdownMenuItem(
                                text = { BahaText("加入黑名單", color = colors.textPrimary, size = BahaTextSize.SUBTITLE) },
                                onClick = {
                                    menuExpanded = false
                                    onAddBlockListClicked()
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(color = colors.divider, thickness = 1.dp)
            }

            // 文章內容主體
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
                    if (viewModeState == ArticleViewMode.MODE_TEXT) {
                        ArticleTextModeContent(
                            article = article,
                            colors = colors,
                            onAuthorClick = { }
                        )
                    } else {
                        ArticleTelnetModeContent(
                            article = article,
                            onTelnetViewCreated = { view ->
                                this@ArticleEssencePage.telnetView = view
                            }
                        )
                    }
                }
            }

            // 底部工具列 (切換模式, 上一篇, 下一篇)
            HorizontalDivider(color = colors.toolbarDivider, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.toolbarBackground)
            ) {
                BahaButton(
                    text = stringResource(R.string.change_mode_short),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { toggleViewMode() }
                )
                BahaButton(
                    text = stringResource(R.string.prev_article),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onPageUpButtonClicked() }
                )
                BahaButton(
                    text = stringResource(R.string.next_article),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onPageDownButtonClicked() }
                )
            }
        }
    }
}
