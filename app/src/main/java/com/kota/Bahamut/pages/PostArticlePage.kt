package com.kota.Bahamut.pages

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.ArticleTempStore
import com.kota.Bahamut.dataModels.ReferenceAuthor
import com.kota.Bahamut.dialogs.DialogInsertExpression
import com.kota.Bahamut.dialogs.DialogInsertExpressionListener
import com.kota.Bahamut.dialogs.DialogInsertSymbol
import com.kota.Bahamut.dialogs.DialogPaintColor
import com.kota.Bahamut.dialogs.DialogPostArticle
import com.kota.Bahamut.dialogs.DialogReference
import com.kota.Bahamut.dialogs.DialogShortenImage
import com.kota.Bahamut.dialogs.DialogShortenUrl
import com.kota.Bahamut.pages.blockListPage.ArticleExpressionListPage
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.judgeDoubleWord
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.articleExpressions
import com.kota.Bahamut.service.UserSettings.Companion.articleHeaders
import com.kota.Bahamut.service.UserSettings.Companion.propertiesNoVipShortenTimes
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage

class PostArticlePage : TelnetPage() {
    private var articleNumber: String? = null
    private var boardMainPage: BoardMainPage? = null
    private var editFormat: String? = null
    private var isHeaderHidden = false
    private var postArticlePageListener: PostArticlePageListener? = null
    private var operationMode: OperationMode? = OperationMode.New
    var recover: Boolean = false
    private var telnetArticle: TelnetArticle? = null

    // Compose states
    var headers: Array<String> = emptyArray()
    var headerSelectedState by mutableIntStateOf(0)
    var titleState by mutableStateOf(TextFieldValue(""))
    var contentState by mutableStateOf(TextFieldValue(""))
    var isToolbarExpanded by mutableStateOf(false)

    enum class OperationMode {
        New,
        Reply,
        Edit
    }

    fun setListener(aListener: PostArticlePageListener?) {
        postArticlePageListener = aListener
    }

    override val pageLayout: Int
        get() = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_POST_ARTICLE

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                PostArticlePageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageDidLoad() {
        headers = articleHeaders
        if (recover) {
            loadTempArticle(9)
            recover = false
        }
    }

    fun setPostTitle(aTitle: String?) {
        val t = aTitle ?: ""
        titleState = TextFieldValue(t, TextRange(t.length))
    }

    fun setPostContent(aContent: String?) {
        val c = aContent ?: ""
        contentState = TextFieldValue(c, TextRange(c.length))
    }

    fun setTelnetArticle(article: TelnetArticle?) {
        telnetArticle = article
    }

    fun setHeaderHidden(hidden: Boolean) {
        isHeaderHidden = hidden
    }

    fun setOperationMode(aMode: OperationMode?) {
        operationMode = aMode
    }

    fun setArticleNumber(aNumber: String?) {
        articleNumber = aNumber
    }

    fun setEditFormat(aFormat: String?) {
        editFormat = aFormat
    }

    fun setBoardPage(aBoardMainPage: BoardMainPage?) {
        boardMainPage = aBoardMainPage
    }

    fun setRecover() {
        recover = true
        saveTempArticle(9)
    }

    val editContent: String?
        get() {
            if (editFormat == null) return null
            val editTitle = judgeDoubleWord(
                titleState.text,
                TelnetFrame.DEFAULT_COLUMN - 9
            ).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val editContentStr = contentState.text
            return String.format(editFormat!!, editTitle, editContentStr)
        }

    override fun clear() {
        titleState = TextFieldValue("")
        contentState = TextFieldValue("")
        headerSelectedState = 0
        postArticlePageListener = null
        recover = false
    }

    fun closeArticle() {
        if (recover) {
            val data = create()
                .pushKey(TelnetKeyboard.CTRL_X)
                .pushString("a\n")
                .build()
            TelnetClient.myInstance?.sendDataToServer(data)
            recover = false
        }
        clear()
        try {
            navigationController.popToViewController(boardMainPage)
        } catch (_: UninitializedPropertyAccessException) {
        }
        PageContainer.instance?.cleanPostArticlePage()
    }

    fun insertString(str: String?) {
        if (str.isNullOrEmpty()) return
        val current = contentState
        val start = current.selection.start.coerceIn(0, current.text.length)
        val end = current.selection.end.coerceIn(0, current.text.length)
        val newText = current.text.replaceRange(start, end, str)
        val newCursor = start + str.length
        contentState = TextFieldValue(newText, TextRange(newCursor))
    }

    private fun post(title: String, content: String?) {
        val sendTitle: String? = if (articleNumber == null) {
            title
        } else {
            title
        }

        if (articleNumber != null) {
            if (operationMode == OperationMode.Reply) {
                val dialog = DialogPostArticle(1)
                dialog.setListener { aTarget: String?, aSign: String? ->
                    postArticlePageListener?.onPostDialogSendButtonClicked(
                        this@PostArticlePage,
                        sendTitle,
                        content,
                        aTarget,
                        articleNumber,
                        aSign,
                        recover
                    )
                    if (aTarget == "M") {
                        closeArticle()
                    }
                }
                dialog.show()
            } else {
                ASAlertDialog.createDialog().addButton(getContextString(R.string.cancel))
                    .addButton("送出").setTitle("確認").setMessage("您是否確定要編輯此文章?")
                    .setListener { _, index ->
                        if (index == 1) {
                            postArticlePageListener?.onPostDialogEditButtonClicked(
                                this@PostArticlePage,
                                articleNumber,
                                sendTitle,
                                editContent
                            )
                        }
                        closeArticle()
                    }.show()
            }
        } else {
            val dialog2 = DialogPostArticle(0)
            dialog2.setListener { aTarget: String?, aSign: String? ->
                postArticlePageListener?.onPostDialogSendButtonClicked(
                    this@PostArticlePage,
                    sendTitle,
                    content,
                    null,
                    null,
                    aSign,
                    recover
                )
            }
            dialog2.show()
        }
    }

    private fun onPostButtonClicked() {
        if (postArticlePageListener == null) return
        val title = getArticleHeader(headerSelectedState) + titleState.text.replace("\n", "")
        val content = contentState.text
        var errMsg: String? = null
        if (title.isEmpty() && content.isEmpty()) {
            errMsg = "標題與內文不可為空"
        } else if (title.isEmpty()) {
            errMsg = "標題不可為空"
        } else if (content.isEmpty()) {
            errMsg = "內文不可為空"
        }
        if (errMsg == null) {
            post(title, content)
        } else {
            ASAlertDialog.createDialog().setTitle("錯誤").setMessage(errMsg)
                .addButton(getContextString(R.string.sure)).show()
        }
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
                    insertString(items[paramInt])
                }

                override fun onListDialogSettingClicked() {
                    setRecover()
                    navigationController.pushViewController(ArticleExpressionListPage())
                }
            }).scheduleDismissOnPageDisappear(this).show()
    }

    private fun onInsertSymbolClicked() {
        val dialog = DialogInsertSymbol()
        dialog.setListener { str: String? -> insertString(str) }
        dialog.show()
    }

    private fun onPaintColorClicked() {
        val dialog = DialogPaintColor()
        dialog.setListener { str: String? -> insertString(str) }
        dialog.show()
    }

    private fun onFileClicked() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem(getContextString(R.string.load_temp))
            .addItem(getContextString(R.string.save_to_temp))
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
                    if (title == getContextString(R.string.load_temp)) {
                        ontLoadArticleFromTempButtonClicked()
                    } else if (title == getContextString(R.string.save_to_temp)) {
                        ontSaveArticleToTempButtonClicked()
                    }
                }
            }).show()
    }

    private fun ontLoadArticleFromTempButtonClicked() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem("讀取上次送出文章")
            .addItem(getContextString(R.string.load_temp) + ".1")
            .addItem(getContextString(R.string.load_temp) + ".2")
            .addItem(getContextString(R.string.load_temp) + ".3")
            .addItem(getContextString(R.string.load_temp) + ".4")
            .addItem(getContextString(R.string.load_temp) + ".5")
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(paramASListDialog: ASListDialog?, index: Int, title: String?): Boolean = true
                override fun onListDialogItemClicked(paramASListDialog: ASListDialog?, index: Int, title: String?) {
                    if (index == 0) {
                        ASAlertDialog.createDialog()
                            .setTitle(getContextString(R.string.load_temp))
                            .setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?")
                            .addButton(getContextString(R.string.cancel))
                            .addButton(getContextString(R.string.sure))
                            .setListener { _, buttonIndex ->
                                if (buttonIndex == 1) loadTempArticle(9)
                            }.show()
                    } else {
                        ASAlertDialog.createDialog()
                            .setTitle(getContextString(R.string.load_temp))
                            .setMessage("您是否確定要以暫存檔.$index 的內容取代您現在編輯的內容?")
                            .addButton(getContextString(R.string.cancel))
                            .addButton(getContextString(R.string.sure))
                            .setListener { _, buttonIndex ->
                                if (buttonIndex == 1) loadTempArticle(index - 1)
                            }.show()
                    }
                }
            }).show()
    }

    private fun ontSaveArticleToTempButtonClicked() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem(getContextString(R.string.save_to_temp) + ".1")
            .addItem(getContextString(R.string.save_to_temp) + ".2")
            .addItem(getContextString(R.string.save_to_temp) + ".3")
            .addItem(getContextString(R.string.save_to_temp) + ".4")
            .addItem(getContextString(R.string.save_to_temp) + ".5")
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(paramASListDialog: ASListDialog?, index: Int, title: String?): Boolean = true
                override fun onListDialogItemClicked(paramASListDialog: ASListDialog?, index: Int, title: String?) {
                    ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.load_temp))
                        .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                        .addButton(getContextString(R.string.cancel))
                        .addButton(getContextString(R.string.sure))
                        .setListener { _, buttonIndex ->
                            if (buttonIndex == 1) saveTempArticle(index)
                        }.show()
                }
            }).show()
    }

    private fun ontSaveArticleToTempAndLeaveButtonClicked() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem(getContextString(R.string.save_to_temp) + ".1")
            .addItem(getContextString(R.string.save_to_temp) + ".2")
            .addItem(getContextString(R.string.save_to_temp) + ".3")
            .addItem(getContextString(R.string.save_to_temp) + ".4")
            .addItem(getContextString(R.string.save_to_temp) + ".5")
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(paramASListDialog: ASListDialog?, index: Int, title: String?): Boolean = true
                override fun onListDialogItemClicked(paramASListDialog: ASListDialog?, index: Int, title: String?) {
                    ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.load_temp))
                        .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                        .addButton(getContextString(R.string.cancel))
                        .addButton(getContextString(R.string.sure))
                        .setListener { _, buttonIndex ->
                            if (buttonIndex == 0) {
                                onBackPressed()
                            } else {
                                saveTempArticle(index)
                                closeArticle()
                            }
                        }.show()
                }
            }).show()
    }

    private fun loadTempArticle(index: Int) {
        val articleTemp = ArticleTempStore(context).articles[index]
        headerSelectedState = getIndexOfHeader(articleTemp.header).coerceAtLeast(0)
        titleState = TextFieldValue(articleTemp.title ?: "")
        contentState = TextFieldValue(articleTemp.content ?: "")
    }

    private fun saveTempArticle(index: Int) {
        val store = ArticleTempStore(context)
        val articleTemp = store.articles[index]
        articleTemp.header = if (headerSelectedState > 0) getArticleHeader(headerSelectedState) else ""
        articleTemp.title = titleState.text
        articleTemp.content = contentState.text
        store.store()
        if (index < 8) {
            ASAlertDialog.createDialog().setTitle(getContextString(R.string._save))
                .setMessage("存檔完成").addButton(getContextString(R.string.sure)).show()
        }
    }

    fun getIndexOfHeader(aHeader: String?): Int {
        if (aHeader.isNullOrEmpty()) return 0
        for (i in 1 until headers.size) {
            if (headers[i] == aHeader) return i
        }
        return -1
    }

    fun getArticleHeader(index: Int): String {
        if (index <= 0 || index >= headers.size) return ""
        return headers[index]
    }

    private fun onReferenceClicked() {
        val authors: MutableList<ReferenceAuthor> = ArrayList()
        if (telnetArticle == null) {
            ASAlertDialog.showErrorDialog(getContextString(R.string.dialog_reference_error_1), this)
            return
        }

        if (editFormat == null) {
            val replyAuthor = ReferenceAuthor().apply {
                enabled = true
                authorName = telnetArticle?.author ?: ""
            }
            authors.add(replyAuthor)

            val newAuthor = ReferenceAuthor().apply {
                if ((telnetArticle?.infoSize ?: 0) > 0) {
                    val item = telnetArticle?.getInfo(0)
                    enabled = true
                    authorName = item?.author ?: ""
                }
            }
            authors.add(newAuthor)
        } else {
            if ((telnetArticle?.infoSize ?: 0) > 0) {
                for (i in 0 until telnetArticle!!.infoSize) {
                    val newAuthor = ReferenceAuthor().apply {
                        val item = telnetArticle?.getInfo(i)
                        enabled = true
                        authorName = item?.author ?: ""
                    }
                    authors.add(newAuthor)
                }
            }
        }

        val dialog = DialogReference()
        dialog.setAuthors(authors)
        dialog.setListener { resAuthors -> referenceBack(resAuthors) }
        dialog.show()
    }

    private fun referenceBack(authors: MutableList<ReferenceAuthor>) {
        if (telnetArticle == null) return
        val originParentContent = telnetArticle!!.generateReplyContent().split("\n").dropLastWhile { it.isEmpty() }
        val tempParentContent = ArrayList<String>()
        val finalParentContent = ArrayList<String>()

        val author0 = authors.getOrNull(0)
        val author1 = authors.getOrNull(1)
        var author0InsertRows = 0
        var author1InsertRows = 0

        var author0TotalRows = 0
        var author1TotalRows = 0
        for (rowString in originParentContent) {
            if (rowString.startsWith("> ※ ") || rowString.startsWith("> > ")) {
                if (!(author1?.removeBlank == true && rowString.replace("> > ", "").isEmpty())) {
                    tempParentContent.add(rowString)
                    author1TotalRows++
                }
            } else if (rowString.startsWith("※ ") || rowString.startsWith("> ")) {
                if (!(author0?.removeBlank == true && rowString.replace("> ", "").isEmpty())) {
                    tempParentContent.add(rowString)
                    author0TotalRows++
                }
            }
        }

        for (rowString in tempParentContent) {
            var needInsert = false
            if (rowString.startsWith("> ※ ")) {
                if (author1?.enabled == true) needInsert = true
            } else if (rowString.startsWith("> > ")) {
                if (author1?.enabled == true) {
                    when (author1.reservedType) {
                        0 -> needInsert = true
                        1 -> if (author1InsertRows < 3) { author1InsertRows++; needInsert = true }
                        2 -> { author1InsertRows++; if (author1InsertRows + 3 >= author1TotalRows) needInsert = true }
                    }
                }
            } else if (rowString.startsWith("※ ")) {
                if (author0?.enabled == true) needInsert = true
            } else if (rowString.startsWith("> ")) {
                if (author0?.enabled == true) {
                    when (author0.reservedType) {
                        0 -> needInsert = true
                        1 -> if (author0InsertRows < 3) { author0InsertRows++; needInsert = true }
                        2 -> { author0InsertRows++; if (author0InsertRows + 3 >= author0TotalRows) needInsert = true }
                    }
                }
            }
            if (needInsert) finalParentContent.add(rowString)
        }

        val joinedParentContent = finalParentContent.joinToString("\n")
        val originFromContent = contentState.text.split("\n").dropLastWhile { it.isEmpty() }
        val selfContent = originFromContent.filter { !it.startsWith("※ 引述") && !it.startsWith("> ") }
        val joinedSelfContent = selfContent.joinToString("\n")

        val result = if (joinedParentContent.isNotEmpty()) "$joinedParentContent\n$joinedSelfContent" else joinedSelfContent
        contentState = TextFieldValue(result, TextRange(result.length))
    }

    override fun onBackPressed(): Boolean {
        if (titleState.text.isEmpty() && contentState.text.isEmpty()) {
            return super.onBackPressed()
        }
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .setMessage(getContextString(R.string.give_up_post_article))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string._giveUp))
            .addButton(getContextString(R.string._save))
            .setListener { _, index ->
                if (index == 1) {
                    closeArticle()
                } else if (index == 2) {
                    ontSaveArticleToTempAndLeaveButtonClicked()
                }
            }.show()
        return true
    }

    @Composable
    fun PostArticlePageContent() {
        val colors = AppTheme.colors
        var expanded by remember { mutableStateOf(false) }

        if (headers.isEmpty()) {
            headers = articleHeaders
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            // 頂部標題列
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header Selector Dropdown (if not hidden)
                if (!isHeaderHidden && headers.isNotEmpty()) {
                    Box {
                        BahaText(
                            text = headers.getOrNull(headerSelectedState) ?: headers[0],
                            color = colors.titleBarTitle,
                            fontSize = BahaTextSize.TITLE,
                            modifier = Modifier
                                .clickable { expanded = true }
                                .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(colors.surface)
                        ) {
                            headers.forEachIndexed { index, header ->
                                DropdownMenuItem(
                                    text = {
                                        BahaText(
                                            header,
                                            color = colors.textPrimary,
                                            fontSize = BahaTextSize.TITLE) },
                                    onClick = {
                                        headerSelectedState = index
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 標題輸入框
                BahaInputField(
                    value = titleState,
                    onValueChange = { titleState = it },
                    placeholder = stringResource(R.string.input_title_here),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = BahaTextSize.TITLE,
                    fontColor = colors.inputBoxBackground,
                    backgroundColor = colors.inputBoxText
                )
            }
            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            // 內文輸入框
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
                        fontSize = AppTheme.fontSize.title
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
                                fontSize = BahaTextSize.TITLE
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // 工具列 (可展開/摺疊)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )

            AnimatedVisibility(visible = isToolbarExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(colors.toolbarBackground),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BahaButton(
                            text = stringResource(R.string.post_article_page_paint_color),
                            fontSize = BahaTextSize.TITLE,
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
                            text = stringResource(R.string.file),
                            fontSize = BahaTextSize.TITLE,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { onFileClicked() }
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(colors.toolbarDivider)
                        )
                        BahaButton(
                            text = stringResource(R.string.dialog_shorten_url_title),
                            fontSize = BahaTextSize.TITLE,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = {
                                val dialog = DialogShortenUrl()
                                dialog.setListener { str -> insertString(str) }
                                dialog.show()
                            }
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(colors.toolbarDivider)
                        )
                        BahaButton(
                            text = stringResource(R.string.dialog_shorten_img_title),
                            fontSize = BahaTextSize.TITLE,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = {
                                if (!propertiesVIP && propertiesNoVipShortenTimes > 30) {
                                    showLongToast(getContextString(R.string.vip_only_message))
                                } else {
                                    val intent = Intent(TempSettings.myActivity, DialogShortenImage::class.java)
                                    startActivity(intent)
                                }
                            }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.toolbarDivider)
                    )
                }
            }

            // 主要工具列第一行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colors.toolbarBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BahaButton(
                    text = if (isToolbarExpanded) stringResource(R.string.post_toolbar_collapse) else stringResource(R.string.post_toolbar_show),
                    fontSize = BahaTextSize.TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { isToolbarExpanded = !isToolbarExpanded }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.post_article_page_format),
                    fontSize = BahaTextSize.TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onReferenceClicked() }
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.toolbarDivider)
                )
                BahaButton(
                    text = stringResource(R.string.symbol),
                    fontSize = BahaTextSize.TITLE,
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
                    fontSize = BahaTextSize.TITLE,
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
                    text = stringResource(R.string.post),
                    fontSize = BahaTextSize.TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onPostButtonClicked() }
                )
            }
        }
    }
}
