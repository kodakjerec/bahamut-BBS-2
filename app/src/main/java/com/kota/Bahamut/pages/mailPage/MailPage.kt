package com.kota.Bahamut.pages.mailPage

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.articlePage.ArticleTelnetModeContent
import com.kota.Bahamut.pages.articlePage.ArticleTextModeContent
import com.kota.Bahamut.pages.articlePage.ArticleViewMode
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.TelnetView

class MailPage : TelnetPage(), SendMailPageListener {
    var telnetArticle: TelnetArticle? = null
    var telnetView: TelnetView? = null

    // Compose states
    var currentArticle by mutableStateOf<TelnetArticle?>(null)
    var viewModeState by mutableIntStateOf(ArticleViewMode.MODE_TEXT)

    override val pageLayout: Int
        get() = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_MAIL

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                MailPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        clear()
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        if (viewModeState != ArticleViewMode.MODE_TEXT) {
            return true
        }
        onBackPressed()
        return true
    }

    fun setArticle(aArticle: TelnetArticle) {
        clear()
        telnetArticle = aArticle
        currentArticle = aArticle
        if (telnetView != null && aArticle.frame != null) {
            telnetView?.frame = aArticle.frame!!
        }
        dismissProcessingDialog()
    }

    override fun clear() {
        telnetArticle = null
        currentArticle = null
    }

    override fun onSendMailDialogSendButtonClicked(
        sendMailPage: SendMailPage,
        receiver: String,
        title: String,
        content: String
    ) {
        PageContainer.instance!!.mailBoxPage
            .onSendMailDialogSendButtonClicked(sendMailPage, receiver, title, content)
        onBackPressed()
    }

    fun onPageUpButtonClicked() {
        if (TelnetClient.myInstance?.telnetConnector?.isConnecting == true) {
            PageContainer.instance!!.mailBoxPage.loadPreviousArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    fun onPageDownButtonClicked() {
        if (TelnetClient.myInstance?.telnetConnector?.isConnecting == true) {
            PageContainer.instance!!.mailBoxPage.loadNextArticle()
        } else {
            showConnectionClosedToast()
        }
    }

    private fun showConnectionClosedToast() {
        showShortToast("連線已中斷")
    }

    fun onReplyButtonClicked() {
        val sendMailPage = SendMailPage()
        navigationController.pushViewController(sendMailPage)
        val article = currentArticle ?: telnetArticle
        if (article != null) {
            val replyTitle = article.generateReplyTitle()
            val replyContent = article.generateReplyContent()
            sendMailPage.setPostTitle(replyTitle)
            sendMailPage.setPostContent(replyContent)
            sendMailPage.setReceiver(article.author)
            sendMailPage.setListener(this)
        }
    }

    fun reloadViewMode() {
        viewModeState = if (viewModeState == ArticleViewMode.MODE_TEXT) {
            ArticleViewMode.MODE_TELNET
        } else {
            ArticleViewMode.MODE_TEXT
        }
    }

    @SuppressLint("SetTextI18n")
    fun changeLoadingPercentage(percentage: String?) {
        showProcessingDialog(getContextString(R.string.loading_) + "\n" + percentage)
    }

    @Composable
    fun MailPageContent() {
        val colors = AppTheme.colors
        val article = currentArticle

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 頂部導覽列
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
                        Text(
                            text = article?.title ?: stringResource(R.string.loading_),
                            color = colors.titleBarTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (article != null) {
                            val nick = if (!article.nickName.isNullOrEmpty()) " (${article.nickName})" else ""
                            Text(
                                text = "${article.author}$nick  ${article.dateTime}",
                                color = colors.titleBarDetail,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                HorizontalDivider(color = colors.divider, thickness = 1.dp)
            }

            // 信件內容主體
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
                        Text(
                            text = stringResource(R.string.loading_),
                            color = colors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    if (viewModeState == ArticleViewMode.MODE_TEXT) {
                        ArticleTextModeContent(
                            article = article,
                            colors = colors,
                            onAuthorClick = {}
                        )
                    } else {
                        ArticleTelnetModeContent(
                            article = article,
                            onTelnetViewCreated = { view ->
                                this@MailPage.telnetView = view
                            }
                        )
                    }
                }
            }

            // 底部工具列 (回信, 切換模式, 上一篇, 下一篇)
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            Row(modifier = Modifier.fillMaxWidth()) {
                BahaButton(
                    text = stringResource(R.string.reply_mail),
                    modifier = Modifier.weight(1f),
                    onClick = { onReplyButtonClicked() }
                )
                BahaButton(
                    text = stringResource(R.string.change_mode_short),
                    modifier = Modifier.weight(1f),
                    onClick = { reloadViewMode() }
                )
                BahaButton(
                    text = stringResource(R.string.prev_article),
                    modifier = Modifier.weight(1f),
                    onClick = { onPageUpButtonClicked() }
                )
                BahaButton(
                    text = stringResource(R.string.next_article),
                    modifier = Modifier.weight(1f),
                    onClick = { onPageDownButtonClicked() }
                )
            }
        }
    }
}
