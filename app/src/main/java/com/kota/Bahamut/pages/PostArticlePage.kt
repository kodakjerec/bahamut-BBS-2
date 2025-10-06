package com.kota.Bahamut.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Selection
import android.util.TypedValue
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
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
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.judgeDoubleWord
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.articleExpressions
import com.kota.Bahamut.service.UserSettings.Companion.articleHeaders
import com.kota.Bahamut.service.UserSettings.Companion.propertiesNoVipShortenTimes
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage

class PostArticlePage : TelnetPage(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    var mainLayout: RelativeLayout? = null
    private var articleNumber: String? = null
    private var boardMainPage: BoardMainPage? = null
    private var originalContent: String? = null
    private var contentField: EditText? = null
    private var editFormat: String? = null
    private var isHeaderHidden = false
    private var headerSelected = 0
    private var headerSelector: Spinner? = null
    private var postArticlePageListener: PostArticlePageListener? = null
    private var operationMode: OperationMode? = OperationMode.New
    private var originalTitle: String? = null
    private var postButton: TextView? = null
    private var symbolButton: TextView? = null
    private var insertSymbolButton: TextView? = null
    private var paintColorButton: TextView? = null
    private var titleBlock: View? = null
    private var titleField: EditText? = null
    private var titleFieldBackground: TextView? = null
    lateinit var headers: Array<String>
    var recover: Boolean = false
    private var telnetArticle: TelnetArticle? = null

    private var isToolbarShow = false // 是否展開工具列


    enum class OperationMode {
        New,
        Reply,
        Edit
    }

    fun setListener(aListener: PostArticlePageListener?) {
        postArticlePageListener = aListener
    }

    override val pageLayout: Int
        get() = R.layout.post_article_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_POST_ARTICLE

    override val isPopupPage: Boolean
        get() = true

    override fun onPageDidLoad() {
        initial()
        if (recover) {
            loadTempArticle(9)
            recover = false
        }
    }

    override fun onPageDidDisappear() {
        headerSelector = null
        titleField = null
        titleFieldBackground = null
        contentField = null
        titleBlock = null
        symbolButton = null
        postButton = null
        insertSymbolButton = null
        paintColorButton = null
        super.onPageDidDisappear()
    }

    private fun refreshTitleField() {
        if (titleField != null && originalTitle != null) {
            titleField?.setText(originalTitle)
            if (originalTitle?.isNotEmpty() == true) {
                Selection.setSelection(titleField?.text, 1)
            }
            originalTitle = null
        }
    }

    private fun refreshContentField() {
        if (contentField != null && originalContent != null) {
            contentField?.setText(originalContent)
            if (originalContent?.isNotEmpty() == true) {
                Selection.setSelection(contentField?.text, originalContent?.length!!)
            }
            originalContent = null
        }
    }

    private fun refreshHeaderSelector() {
        if (headerSelector == null) {
            return
        }
        if (isHeaderHidden) {
            headerSelector?.visibility = View.GONE
        } else {
            headerSelector?.visibility = View.VISIBLE
        }
    }

    override fun onPageRefresh() {
        refreshTitleField()
        refreshContentField()
        refreshHeaderSelector()
    }

    @SuppressLint("ResourceType")
    private fun initial() {
        headers = articleHeaders

        mainLayout = findViewById(R.id.content_view) as RelativeLayout?

        titleField = mainLayout?.findViewById(R.id.ArticlePostDialog_TitleField)
        // 點標題的時候拉大編輯框
        titleField?.onFocusChangeListener = titleFieldListener
        titleFieldBackground =
            mainLayout?.findViewById(R.id.ArticlePostDialog_TitleFieldBackground)
        contentField = mainLayout?.findViewById(R.id.ArticlePostDialog_EditField)

        postButton = mainLayout?.findViewById(R.id.ArticlePostDialog_Post)
        postButton?.setOnClickListener(this)

        symbolButton = mainLayout?.findViewById(R.id.ArticlePostDialog_Symbol)
        symbolButton?.setOnClickListener(this)

        insertSymbolButton = mainLayout?.findViewById(R.id.ArticlePostDialog_Cancel)
        insertSymbolButton?.setOnClickListener(this)

        paintColorButton = mainLayout?.findViewById(R.id.ArticlePostDialog_Color)
        paintColorButton?.setOnClickListener(this)

        mainLayout?.findViewById<View>(R.id.ArticlePostDialog_File)!!.setOnClickListener(this)
        mainLayout?.findViewById<View>(R.id.ArticlePostDialog_ShortenUrl)!!.setOnClickListener(this)
        mainLayout?.findViewById<View>(R.id.ArticlePostDialog_ShortenImage)!!
            .setOnClickListener(this)
        mainLayout?.findViewById<View>(R.id.ArticlePostDialog_EditButtons)!!
            .setOnClickListener(this)

        headerSelector = mainLayout?.findViewById(R.id.Post_headerSelector)
        val adapter: ArrayAdapter<Any> =
            ArrayAdapter<Any>(context!!, R.layout.simple_spinner_item, headers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        headerSelector?.adapter = adapter
        headerSelector?.onItemSelectedListener = this
        titleBlock = mainLayout?.findViewById(R.id.Post_TitleBlock)
        titleBlock?.requestFocus()

        mainLayout?.findViewById<View>(R.id.Post_Toolbar_Show)!!
            .setOnClickListener(postToolbarShowOnClickListener)
        mainLayout?.findViewById<View>(R.id.ArticlePostDialog_Reference)!!
            .setOnClickListener(referenceClickListener)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    override fun clear() {
        if (titleField != null) {
            titleField?.setText("")
        }
        if (contentField != null) {
            contentField?.setText("")
        }
        postArticlePageListener = null
        recover = false
    }

    /** 引言過多失敗存檔  */
    fun setRecover() {
        recover = true
        saveTempArticle(9)
    }

    /** 設定文章標題  */
    fun setPostTitle(aTitle: String?) {
        originalTitle = aTitle
        refreshTitleField()
    }

    /** 設定內文  */
    fun setPostContent(aContent: String?) {
        originalContent = aContent
        refreshContentField()
    }

    override fun onClick(view: View) {
        if (view === postButton) {
            if (postArticlePageListener != null) {
                val title = getArticleHeader(headerSelected) + titleField?.text.toString()
                    .replace("\n", "")
                val content = contentField?.text.toString()
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
        } else if (view === symbolButton) {
            // 表情符號
            val items: Array<String> = articleExpressions
            DialogInsertExpression.createDialog().setTitle("表情符號").addItems(items)
                .setListener(object : DialogInsertExpressionListener {
                    override fun onListDialogItemClicked(
                        paramASListDialog: DialogInsertExpression,
                        paramInt: Int,
                        paramString: String
                    ) {
                        val symbol = items[paramInt]
                        this@PostArticlePage.insertString(symbol)
                    }

                    override fun onListDialogSettingClicked() {
                        // 將當前內容存檔, pushView會讓當前頁面消失
                        setRecover()
                        navigationController.pushViewController(ArticleExpressionListPage())
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        } else if (view === insertSymbolButton) {
            // 符號
            val dialog = DialogInsertSymbol()
            dialog.setListener { str: String? -> this.insertString(str) }
            dialog.show()
        } else if (view === paintColorButton) {
            // 上色
            val dialog = DialogPaintColor()
            dialog.setListener { str: String? -> this.insertString(str) }
            dialog.show()
        } else if (view.id == R.id.ArticlePostDialog_File) {
            // 檔案
            onFileClicked()
        } else if (view.id == R.id.ArticlePostDialog_ShortenUrl) {
            // 短網址
            val dialog = DialogShortenUrl()
            dialog.setListener { str: String? -> this.insertString(str) }
            dialog.show()
        } else if (view.id == R.id.ArticlePostDialog_ShortenImage) {
            // 縮圖
            val shortenTimes = propertiesNoVipShortenTimes
            if (!propertiesVIP && shortenTimes > 30) {
                showLongToast(getContextString(R.string.vip_only_message))
                return
            }
            val intent = Intent(TempSettings.myActivity, DialogShortenImage::class.java)
            startActivity(intent)
        } else if (view.id == R.id.ArticlePostDialog_EditButtons) {
            showShortToast(getContextString(R.string.error_under_develop))
        }
    }

    /** 發文  */
    private fun post(title: String, content: String?) {

        val sendTitle: String? = if (articleNumber == null || title != originalTitle) {
            title
        } else {
            null
        }
        val sendContent = content
        // 有來源文章編號, 可能為Reply, edit
        if (articleNumber != null) {
            if (operationMode == OperationMode.Reply) {
                // 回覆: 有註記回文
                val dialog = DialogPostArticle(1)
                dialog.setListener { aTarget: String?, aSign: String? ->
                    if (this@PostArticlePage.postArticlePageListener != null) {
                        this@PostArticlePage.postArticlePageListener?.onPostDialogSendButtonClicked(
                            this@PostArticlePage,
                            sendTitle,
                            sendContent,
                            aTarget,
                            this@PostArticlePage.articleNumber,
                            aSign,
                            recover
                        )
                    }
                    // 回應到作者信箱
                    if (aTarget == "M") {
                        closeArticle()
                    }
                }
                dialog.show()
            } else {
                // 修改: 沒有註記回文
                ASAlertDialog.createDialog().addButton(getContextString(R.string.cancel))
                    .addButton("送出").setTitle("確認").setMessage("您是否確定要編輯此文章?")
                    .setListener { aDialog: ASAlertDialog?, index: Int ->
                        if (index == 1) {
                            if (this@PostArticlePage.postArticlePageListener != null) {
                                this@PostArticlePage.postArticlePageListener?.onPostDialogEditButtonClicked(
                                    this@PostArticlePage,
                                    this@PostArticlePage.articleNumber,
                                    sendTitle,
                                    this@PostArticlePage.editContent
                                )
                            }
                        }
                        closeArticle()
                    }.show()
            }
        } else {
            // 新增文章
            val dialog2 = DialogPostArticle(0)
            dialog2.setListener { aTarget: String?, aSign: String? ->
                if (this@PostArticlePage.postArticlePageListener != null) {
                    this@PostArticlePage.postArticlePageListener?.onPostDialogSendButtonClicked(
                        this@PostArticlePage,
                        sendTitle,
                        sendContent,
                        null,
                        null,
                        aSign,
                        recover
                    )
                }
            }
            dialog2.show()
        }
    }

    fun closeArticle() {
        // 引言過多情況下放棄, 要補上放棄存檔command
        if (recover) {
            val data = create()
                .pushKey(TelnetKeyboard.CTRL_X)
                .pushString("a\n")
                .build()
            TelnetClient.myInstance?.sendDataToServer(data)
            recover = false
        }
        clear()
        navigationController.popToViewController(boardMainPage)
        PageContainer.instance?.cleanPostArticlePage()
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, aView: View?, index: Int, id: Long) {
        headerSelected = index
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
    }

    /** 標題欄位取得焦點  */
    var titleFieldListener: OnFocusChangeListener =
        OnFocusChangeListener { view: View?, hasFocus: Boolean ->
            if (view !== titleField) {
                return@OnFocusChangeListener
            }
            if (hasFocus) {
                titleField?.isSingleLine = false
                titleField?.setTextColor(-1)
                titleFieldBackground?.setTextColor(0)
                return@OnFocusChangeListener
            }
            titleField?.isSingleLine = true
            titleField?.setTextColor(0)
            titleFieldBackground?.setTextColor(-1)
            titleFieldBackground?.text = titleField?.text.toString()
        }

    fun setHeaderHidden(hidden: Boolean) {
        isHeaderHidden = hidden
        refreshHeaderSelector()
    }

    /** 讀取暫存檔  */
    fun onFileClicked() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem(getContextString(R.string.load_temp))
            .addItem(getContextString(R.string.save_to_temp))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    if (title == getContextString(R.string.load_temp)) {
                        this@PostArticlePage.ontLoadArticleFromTempButtonClicked()
                    } else if (title == getContextString(R.string.save_to_temp)) {
                        this@PostArticlePage.ontSaveArticleToTempButtonClicked()
                    }
                }
            }).show()
    }

    /** 按下 讀取暫存檔  */
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
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    if (index == 0) {
                        ASAlertDialog.createDialog()
                            .setTitle(getContextString(R.string.load_temp))
                            .setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?")
                            .addButton(getContextString(R.string.cancel))
                            .addButton(getContextString(R.string.sure))
                            .setListener { aDialog12: ASAlertDialog?, buttonIndex: Int ->
                                this@PostArticlePage.loadTempArticle(
                                    9
                                )
                            }.show()
                    } else {
                        ASAlertDialog.createDialog()
                            .setTitle(getContextString(R.string.load_temp))
                            .setMessage("您是否確定要以暫存檔." + index + "的內容取代您現在編輯的內容?")
                            .addButton(getContextString(R.string.cancel))
                            .addButton(getContextString(R.string.sure))
                            .setListener { aDialog1: ASAlertDialog?, buttonIndex: Int ->
                                if (buttonIndex == 1) {
                                    this@PostArticlePage.loadTempArticle(index - 1)
                                }
                            }.show()
                    }
                }
            }).show()
    }

    /** 讀取暫存檔  */
    private fun loadTempArticle(index: Int) {
        val articleTemp = ArticleTempStore(context).articles[index]
        headerSelector?.setSelection(getIndexOfHeader(articleTemp.header))
        titleField?.setText(articleTemp.title)
        contentField?.setText(articleTemp.content)
    }

    /** 儲存暫存檔  */
    private fun saveTempArticle(index: Int) {
        val store = ArticleTempStore(context)
        val articleTemp = store.articles[index]
        // 類別
        articleTemp.header = ""
        if (headerSelector?.selectedItemPosition!! > 0) {
            articleTemp.header = getArticleHeader(headerSelector?.selectedItemPosition!!)
        }
        // 標題
        articleTemp.title = titleField?.text.toString()
        // 內文
        articleTemp.content = contentField?.text.toString()

        // 存檔
        store.store()
        // ArticleTempStore 有暫存檔定義
        if (index < 8) {
            ASAlertDialog.createDialog().setTitle(getContextString(R.string._save))
                .setMessage("存檔完成").addButton(getContextString(R.string.sure)).show()
        }
    }

    /** 從字串去回推標題定位  */
    fun getIndexOfHeader(aHeader: String?): Int {
        if (aHeader == null || aHeader.isEmpty()) {
            return 0
        }
        for (i in 1..<headers.size) {
            if (headers[i] == aHeader) {
                return i
            }
        }
        return -1
    }

    /** 從定位取出特定標題  */
    fun getArticleHeader(index: Int): String? {
        if (index <= 0 || index >= headers.size) {
            return ""
        }
        return headers[index]
    }

    /** 按下 存入暫存檔  */
    private fun ontSaveArticleToTempButtonClicked() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem(getContextString(R.string.save_to_temp) + ".1")
            .addItem(getContextString(R.string.save_to_temp) + ".2")
            .addItem(getContextString(R.string.save_to_temp) + ".3")
            .addItem(getContextString(R.string.save_to_temp) + ".4")
            .addItem(getContextString(R.string.save_to_temp) + ".5")
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.load_temp))
                        .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                        .addButton(getContextString(R.string.cancel))
                        .addButton(getContextString(R.string.sure))
                        .setListener { aDialog1: ASAlertDialog?, buttonIndex: Int ->
                            if (buttonIndex == 1) {
                                this@PostArticlePage.saveTempArticle(index)
                            }
                        }.show()
                }
            }).show()
    }

    /** 存檔時, 存入暫存檔  */
    private fun ontSaveArticleToTempAndLeaveButtonClicked() {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .addItem(getContextString(R.string.save_to_temp) + ".1")
            .addItem(getContextString(R.string.save_to_temp) + ".2")
            .addItem(getContextString(R.string.save_to_temp) + ".3")
            .addItem(getContextString(R.string.save_to_temp) + ".4")
            .addItem(getContextString(R.string.save_to_temp) + ".5")
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.load_temp))
                        .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                        .addButton(getContextString(R.string.cancel))
                        .addButton(getContextString(R.string.sure))
                        .setListener { aDialog1: ASAlertDialog?, buttonIndex: Int ->
                            if (buttonIndex == 0) {
                                this@PostArticlePage.onBackPressed()
                            } else {
                                this@PostArticlePage.saveTempArticle(index)
                                closeArticle()
                            }
                        }.show()
                }
            }).show()
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

    val editContent: String?
        /** 組合修改文章內容  */
        get() {
            if (editFormat == null) {
                return null
            }
            val editTitle = judgeDoubleWord(
                titleField?.text.toString(),
                TelnetFrame.Companion.DEFAULT_COLUMN - 9
            ).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val editContent = contentField?.text.toString()
            return String.format(editFormat!!, editTitle, editContent)
        }

    override val isKeepOnOffline: Boolean
        get() = true

    fun setBoardPage(aBoardMainPage: BoardMainPage?) {
        boardMainPage = aBoardMainPage
    }

    /** 按下 返回  */
    override fun onBackPressed(): Boolean {
        if (titleField?.text.toString().isEmpty() && contentField?.text
                .toString().isEmpty()
        ) {
            return super.onBackPressed()
        }
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .setMessage(getContextString(R.string.give_up_post_article))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string._giveUp))
            .addButton(getContextString(R.string._save))
            .setListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    closeArticle()
                } else if (index == 2) {
                    this@PostArticlePage.ontSaveArticleToTempAndLeaveButtonClicked()
                }
            }.show()
        return true
    }

    /** 按下 展開/摺疊  */
    var postToolbarShowOnClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        val thisBtn = view as TextView
        val toolBar = mainLayout?.findViewById<LinearLayout>(R.id.toolbar)!!
        val layoutParams = toolBar.layoutParams as RelativeLayout.LayoutParams
        if (isToolbarShow) {
            // 從展開->摺疊
            isToolbarShow = false
            thisBtn.text = getContextString(R.string.post_toolbar_show)
            layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                60f,
                resource?.displayMetrics
            ).toInt()
        } else {
            isToolbarShow = true
            thisBtn.text = getContextString(R.string.post_toolbar_collapse)
            layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                120f,
                resource?.displayMetrics
            ).toInt()
        }
        toolBar.layoutParams = layoutParams
    }

    fun insertString(str: String?) {
        if (contentField != null) {
            contentField?.editableText!!.insert(contentField?.selectionStart!!, str)
        }
    }

    /** 設定TelnetArticle, 格式按鈕會使用  */
    fun setTelnetArticle(article: TelnetArticle?) {
        telnetArticle = article
    }

    /** 按下格式  */
    var referenceClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        val authors: MutableList<ReferenceAuthor> = ArrayList()
        if (telnetArticle == null) {
            showErrorDialog(getContextString(R.string.dialog_reference_error_1), this)
            return@OnClickListener
        }

        if (editFormat == null) {
            // 回覆
            // 回應作者
            val replyAuthor = ReferenceAuthor()
            replyAuthor.enabled = true
            replyAuthor.authorName = telnetArticle?.author!!
            authors.add(replyAuthor)

            // 更上一層
            val newAuthor = ReferenceAuthor()
            if (telnetArticle?.infoSize!! > 0) {
                val item = telnetArticle?.getInfo(0)
                newAuthor.enabled = true
                newAuthor.authorName = item?.author!!
            }
            authors.add(newAuthor)
        } else {
            // 修改
            if (telnetArticle?.infoSize!! > 0) {
                for (i in 0..<telnetArticle?.infoSize!!) {
                    val newAuthor = ReferenceAuthor()
                    val item = telnetArticle?.getInfo(i)
                    newAuthor.enabled = true
                    newAuthor.authorName = item?.author!!
                    authors.add(newAuthor)
                }
            }
        }

        val dialog = DialogReference()
        dialog.setAuthors(authors)
        dialog.setListener { authors ->
            this.referenceBack(
                authors
            )
        }
        dialog.show()
    }

    fun referenceBack(authors: MutableList<ReferenceAuthor>) {
        // 找出父層內容
        val originParentContent = listOf(
            *telnetArticle?.generateReplyContent()!!.split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        )
        val tempParentContent: MutableList<String> = ArrayList()
        val finalParentContent: MutableList<String?> = ArrayList()


        // 開始篩選
        val author0 = authors[0]
        val author1 = authors[1]
        var author0InsertRows = 0
        var author1InsertRows = 0

        // 如果有選到後三行, 計算兩個作者總行數
        var author0TotalRows = 0
        var author1TotalRows = 0
        for (i in originParentContent.indices) {
            val rowString = originParentContent[i]
            if (rowString.startsWith("> ※ ") || rowString.startsWith("> > ")) {
                if (!(author1.removeBlank && rowString.replace("> > ".toRegex(), "").isEmpty())) {
                    tempParentContent.add(rowString)
                    author1TotalRows++
                }
            } else if (rowString.startsWith("※ ") || rowString.startsWith("> ")) {
                if (!(author0.removeBlank && rowString.replace("> ".toRegex(), "").isEmpty())) {
                    tempParentContent.add(rowString)
                    author0TotalRows++
                }
            }
        }

        var needInsert: Boolean
        for (i in tempParentContent.indices) {
            val rowString = tempParentContent[i]
            needInsert = false
            if (rowString.startsWith("> ※ ")) {
                // 前二
                if (author1.enabled) needInsert = true
            } else if (rowString.startsWith("> > ")) {
                // 前二
                if (author1.enabled) {
                    when (author1.reservedType) {
                        0 ->  // 全部
                            needInsert = true

                        1 -> { // 前三
                            if (author1InsertRows < 3) {
                                author1InsertRows++
                                needInsert = true
                            }
                        }

                        2 -> {  // 後三
                            author1InsertRows++
                            if (author1InsertRows + 3 >= author1TotalRows) needInsert = true
                        }
                    }
                }
            } else if (rowString.startsWith("※ ")) {
                // 前一
                if (author0.enabled) needInsert = true
            } else if (rowString.startsWith("> ")) {
                // 前一
                if (author0.enabled) {
                    when (author0.reservedType) {
                        0 ->  // 保留
                            needInsert = true

                        1 -> { // 前三
                            if (author0InsertRows < 3) {
                                author0InsertRows++
                                needInsert = true
                            }
                        }

                        2 -> { // 後三
                            author0InsertRows++
                            if (author0InsertRows + 3 >= author0TotalRows) needInsert = true
                        }
                    }
                }
            }
            if (needInsert) {
                finalParentContent.add(rowString)
            }
        }
        val joinedParentContent = java.lang.String.join("\n", finalParentContent)

        // 找出自己打的內容
        val originFromContent = listOf(
            *contentField?.text.toString().split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        )
        val selfContent: MutableList<String?> = ArrayList()
        for (i in originFromContent.indices) {
            if (!originFromContent[i].startsWith("※ 引述") && !originFromContent[i]
                    .startsWith("> ")
            ) {
                selfContent.add(originFromContent[i])
            }
        }
        // final Result
        val joinedSelfContent = java.lang.String.join("\n", selfContent)

        val rev2 = java.lang.String.join("", joinedParentContent, "\n", joinedSelfContent)
        contentField?.setText(rev2)
    }
}
