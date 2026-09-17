package com.kota.Bahamut.pages.mailPage

import android.content.Context
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.ArticleTempStore
import com.kota.Bahamut.dialogs.DialogInsertExpression
import com.kota.Bahamut.dialogs.DialogInsertExpressionListener
import com.kota.Bahamut.dialogs.DialogInsertSymbol
import com.kota.Bahamut.dialogs.DialogInsertSymbolListener
import com.kota.Bahamut.dialogs.DialogPaintColor
import com.kota.Bahamut.dialogs.DialogPaintColorListener
import com.kota.Bahamut.pages.blockListPage.ArticleExpressionListPage
import com.kota.Bahamut.service.UserSettings.Companion.articleExpressions
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.telnetUI.TelnetPage
import java.util.Vector

/**
 * 寫信畫面, 有 標題、收件人 (純 Jetpack Compose 實作)
 */
class SendMailPage : TelnetPage(), DialogInsertSymbolListener, DialogPaintColorListener {

    var sendMailPageListener: SendMailPageListener? = null
    var recover: Boolean = false

    // Compose state
    var receiverState by mutableStateOf(TextFieldValue(""))
    var titleState by mutableStateOf(TextFieldValue(""))
    var contentState by mutableStateOf(TextFieldValue(""))
    var isTitleBlockHidden by mutableStateOf(false)

    val name: String
        get() = "BahamutSendMailDialog"

    fun setListener(aListener: SendMailPageListener?) {
        sendMailPageListener = aListener
    }

    override val pageLayout: Int
        get() = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_SEND_MAIL

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                SendMailPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageDidLoad() {
        if (recover) {
            loadTempArticle(8)
            recover = false
        }
    }

    fun setPostTitle(aTitle: String) {
        titleState = TextFieldValue(aTitle, TextRange(aTitle.length))
    }

    fun setPostContent(aContent: String) {
        contentState = TextFieldValue(aContent, TextRange(aContent.length))
    }

    fun setReceiver(aReceiver: String) {
        receiverState = TextFieldValue(aReceiver, TextRange(aReceiver.length))
    }

    override fun clear() {
        receiverState = TextFieldValue("")
        titleState = TextFieldValue("")
        contentState = TextFieldValue("")
        sendMailPageListener = null
    }

    fun setRecover() {
        recover = true
        saveTempArticle(8)
    }

    fun changeViewMode() {
        isTitleBlockHidden = !isTitleBlockHidden
    }

    private fun insertStringAtCursor(str: String) {
        val current = contentState
        val start = current.selection.start.coerceIn(0, current.text.length)
        val end = current.selection.end.coerceIn(0, current.text.length)
        val newText = current.text.replaceRange(start, end, str)
        val newCursor = start + str.length
        contentState = TextFieldValue(newText, TextRange(newCursor))
    }

    private fun onPostClicked() {
        if (sendMailPageListener == null) return

        val receiver = receiverState.text.replace("\n", "").trim()
        val title = titleState.text.replace("\n", "").trim()
        val content = contentState.text

        val empty = Vector<String?>()
        if (receiver.isEmpty()) empty.add("收件人")
        if (title.isEmpty()) empty.add("標題")
        if (content.isEmpty()) empty.add("內文")

        if (empty.isNotEmpty()) {
            val errMsg = StringBuilder()
            for (i in empty.indices) {
                errMsg.append(empty[i])
                if (i == empty.size - 2) {
                    errMsg.append("與")
                } else if (i < empty.size - 2) {
                    errMsg.append("、")
                }
            }
            errMsg.append("不可為空")
            ASAlertDialog.createDialog().setTitle("錯誤").setMessage(errMsg.toString())
                .addButton("確定").show()
            return
        }

        ASAlertDialog.createDialog().addButton("取消").addButton("送出").setTitle("確認")
            .setMessage("您是否確定要送出此信件?")
            .setListener { _, index ->
                if (index == 1) {
                    sendMailPageListener?.onSendMailDialogSendButtonClicked(
                        this@SendMailPage,
                        receiver,
                        title,
                        content
                    )
                    navigationController.popViewController()
                    clear()
                }
            }.show()
    }

    private fun onInsertExpressionClicked() {
        val items: Array<String> = articleExpressions
        DialogInsertExpression.createDialog().setTitle("表情符號").addItems(items)
            .setListener(object : DialogInsertExpressionListener {
                override fun onListDialogItemClicked(
                    paramASListDialog: DialogInsertExpression,
                    paramInt: Int,
                    paramString: String
                ) {
                    insertStringAtCursor(items[paramInt])
                }

                override fun onListDialogSettingClicked() {
                    setRecover()
                    navigationController.pushViewController(ArticleExpressionListPage())
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    private fun onInsertSymbolClicked() {
        val dialog = DialogInsertSymbol()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSymbolDialogDismissWithSymbol(str: String) {
        insertStringAtCursor(str)
    }

    private fun onPaintColorClicked() {
        val dialog = DialogPaintColor()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onPaintColorDone(str: String) {
        insertStringAtCursor(str)
    }

    private fun loadTempArticle(index: Int) {
        val articleTemp = ArticleTempStore(context).articles[index]
        receiverState = TextFieldValue(articleTemp.header ?: "")
        titleState = TextFieldValue(articleTemp.title ?: "")
        contentState = TextFieldValue(articleTemp.content ?: "")
    }

    private fun saveTempArticle(index: Int) {
        val store = ArticleTempStore(context)
        val articleTemp = store.articles[index]
        articleTemp.header = receiverState.text
        articleTemp.title = titleState.text
        articleTemp.content = contentState.text
        store.store()
    }

    @Composable
    fun SendMailPageContent() {
        val colors = AppTheme.colors

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 標題與收件人區塊
            AnimatedVisibility(visible = !isTitleBlockHidden) {
                Column {
                    // 標題列
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BahaText(
                            text = stringResource(R.string.title_),
                            size = BahaTextSize.SUBTITLE,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        BahaInputField(
                            value = titleState,
                            onValueChange = { titleState = it },
                            placeholder = stringResource(R.string.input_title_here),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    HorizontalDivider(color = colors.divider, thickness = 1.dp)

                    // 收件人列
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BahaText(
                            text = stringResource(R.string.receiver_),
                            size = BahaTextSize.SUBTITLE,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        BahaInputField(
                            value = receiverState,
                            onValueChange = { receiverState = it },
                            placeholder = stringResource(R.string.input_receiver_here),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    HorizontalDivider(color = colors.divider, thickness = 1.dp)
                }
            }

            // 內文編輯區塊
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                BasicTextField(
                    value = contentState,
                    onValueChange = { contentState = it },
                    textStyle = TextStyle(
                        color = colors.textPrimary,
                        fontSize = AppTheme.fontSize.base
                    ),
                    cursorBrush = SolidColor(colors.textPrimary),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    decorationBox = { innerTextField ->
                        if (contentState.text.isEmpty()) {
                            BahaText(
                                text = stringResource(R.string.input_content_here),
                                color = colors.textSecondary,
                                size = BahaTextSize.BASE
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // 底部工具列 (上色, 符號, 表情, 切換, 送出) (滿版無縫 50dp)
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
                BahaButton(
                    text = stringResource(R.string.post_article_page_paint_color),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onPaintColorClicked() }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.symbol),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onInsertSymbolClicked() }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.face),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onInsertExpressionClicked() }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.change_mode_short),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { changeViewMode() }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.send),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onPostClicked() }
                )
            }
        }
    }
}
