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
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialog.Companion.showErrorDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.dataModels.ArticleTempStore
import com.kota.Bahamut.dataModels.ReferenceAuthor
import com.kota.Bahamut.dialogs.DialogReference
import com.kota.Bahamut.dialogs.DialogReferenceListener
import com.kota.Bahamut.dialogs.DialogShortenImage
import com.kota.Bahamut.dialogs.DialogShortenUrl
import com.kota.Bahamut.dialogs.DialogShortenUrlListener
import com.kota.Bahamut.dialogs.Dialog_InsertExpression
import com.kota.Bahamut.dialogs.Dialog_InsertExpression_Listener
import com.kota.Bahamut.dialogs.Dialog_InsertSymbol
import com.kota.Bahamut.dialogs.Dialog_InsertSymbol_Listener
import com.kota.Bahamut.dialogs.Dialog_PaintColor
import com.kota.Bahamut.dialogs.Dialog_PaintColor_Listener
import com.kota.Bahamut.dialogs.Dialog_PostArticle
import com.kota.Bahamut.dialogs.Dialog_PostArticle_Listener
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.blockListPage.ArticleExpressionListPage
import com.kota.Bahamut.pages.boardPage.BoardMainPage
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.judgeDoubleWord
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.articleExpressions
import com.kota.Bahamut.service.UserSettings.Companion.articleHeaders
import com.kota.Bahamut.service.UserSettings.Companion.propertiesNoVipShortenTimes
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnetUI.TelnetPage
import java.util.Arrays

class PostArticlePage : TelnetPage(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    var mainLayout: RelativeLayout? = null
    private var _article_number: String? = null
    private var _board_page: BoardMainPage? = null
    private var _ori_content: String? = null
    private var _content_field: EditText? = null
    private var _edit_format: String? = null
    private var _header_hidden = false
    private var _header_selected = 0
    private var _header_selector: Spinner? = null
    private var _listener: PostArticlePage_Listener? = null
    private var _operation_mode: OperationMode? = OperationMode.New
    private var _ori_title: String? = null
    private var _post_button: TextView? = null
    private var _symbol_button: TextView? = null
    private var _insert_symbol_button: TextView? = null
    private var _paint_color_button: TextView? = null
    private var _title_block: View? = null
    private var _title_field: EditText? = null
    private var _title_field_background: TextView? = null
    var _headers: Array<String?>
    var recover: Boolean = false
    private var telnetArticle: TelnetArticle? = null

    private var isToolbarShow = false // 是否展開工具列


    enum class OperationMode {
        New,
        Reply,
        Edit
    }

    fun setListener(aListener: PostArticlePage_Listener?) {
        _listener = aListener
    }

    val pageLayout: Int
        get() = R.layout.post_article_page

    val pageType: Int
        get() = BahamutPage.BAHAMUT_POST_ARTICLE

    val isPopupPage: Boolean
        get() = true

    public override fun onPageDidLoad() {
        initial()
        if (recover) {
            loadTempArticle(9)
            recover = false
        }
    }

    public override fun onPageDidDisappear() {
        _header_selector = null
        _title_field = null
        _title_field_background = null
        _content_field = null
        _title_block = null
        _symbol_button = null
        _post_button = null
        _insert_symbol_button = null
        _paint_color_button = null
        super.onPageDidDisappear()
    }

    private fun refreshTitleField() {
        if (_title_field != null && _ori_title != null) {
            _title_field!!.setText(_ori_title)
            if (_ori_title!!.length > 0) {
                Selection.setSelection(_title_field!!.getText(), 1)
            }
            _ori_title = null
        }
    }

    private fun refreshContentField() {
        if (_content_field != null && _ori_content != null) {
            _content_field!!.setText(_ori_content)
            if (_ori_content!!.length > 0) {
                Selection.setSelection(_content_field!!.getText(), _ori_content!!.length)
            }
            _ori_content = null
        }
    }

    private fun refreshHeaderSelector() {
        if (_header_selector == null) {
            return
        }
        if (_header_hidden) {
            _header_selector!!.setVisibility(View.GONE)
        } else {
            _header_selector!!.setVisibility(View.VISIBLE)
        }
    }

    public override fun onPageRefresh() {
        refreshTitleField()
        refreshContentField()
        refreshHeaderSelector()
    }

    @SuppressLint("ResourceType")
    private fun initial() {
        _headers = articleHeaders

        mainLayout = findViewById(R.id.content_view) as RelativeLayout?

        _title_field = mainLayout!!.findViewById<EditText?>(R.id.ArticlePostDialog_TitleField)
        // 點標題的時候拉大編輯框
        _title_field!!.setOnFocusChangeListener(titleFieldListener)
        _title_field_background =
            mainLayout!!.findViewById<TextView?>(R.id.ArticlePostDialog_TitleFieldBackground)
        _content_field = mainLayout!!.findViewById<EditText?>(R.id.ArticlePostDialog_EditField)

        _post_button = mainLayout!!.findViewById<TextView?>(R.id.ArticlePostDialog_Post)
        _post_button!!.setOnClickListener(this)

        _symbol_button = mainLayout!!.findViewById<TextView?>(R.id.ArticlePostDialog_Symbol)
        _symbol_button!!.setOnClickListener(this)

        _insert_symbol_button = mainLayout!!.findViewById<TextView?>(R.id.ArticlePostDialog_Cancel)
        _insert_symbol_button!!.setOnClickListener(this)

        _paint_color_button = mainLayout!!.findViewById<TextView?>(R.id.ArticlePostDialog_Color)
        _paint_color_button!!.setOnClickListener(this)

        mainLayout!!.findViewById<View?>(R.id.ArticlePostDialog_File).setOnClickListener(this)
        mainLayout!!.findViewById<View?>(R.id.ArticlePostDialog_ShortenUrl).setOnClickListener(this)
        mainLayout!!.findViewById<View?>(R.id.ArticlePostDialog_ShortenImage)
            .setOnClickListener(this)
        mainLayout!!.findViewById<View?>(R.id.ArticlePostDialog_EditButtons)
            .setOnClickListener(this)

        _header_selector = mainLayout!!.findViewById<Spinner?>(R.id.Post_headerSelector)
        val adapter: ArrayAdapter<String?> =
            ArrayAdapter<Any?>(context, R.layout.simple_spinner_item, _headers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        _header_selector!!.setAdapter(adapter)
        _header_selector!!.setOnItemSelectedListener(this)
        _title_block = mainLayout!!.findViewById<View?>(R.id.Post_TitleBlock)
        _title_block!!.requestFocus()

        mainLayout!!.findViewById<View?>(R.id.Post_Toolbar_Show)
            .setOnClickListener(postToolbarShowOnClickListener)
        mainLayout!!.findViewById<View?>(R.id.ArticlePostDialog_Reference)
            .setOnClickListener(referenceClickListener)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    public override fun clear() {
        if (_title_field != null) {
            _title_field!!.setText("")
        }
        if (_content_field != null) {
            _content_field!!.setText("")
        }
        _listener = null
        recover = false
    }

    /** 引言過多失敗存檔  */
    fun setRecover() {
        recover = true
        saveTempArticle(9)
    }

    /** 設定文章標題  */
    fun setPostTitle(aTitle: String?) {
        _ori_title = aTitle
        refreshTitleField()
    }

    /** 設定內文  */
    fun setPostContent(aContent: String?) {
        _ori_content = aContent
        refreshContentField()
    }

    override fun onClick(view: View) {
        if (view === _post_button) {
            if (_listener != null) {
                val title = getArticleHeader(_header_selected) + _title_field!!.getText().toString()
                    .replace("\n", "")
                val content = _content_field!!.getText().toString()
                var err_msg: String? = null
                if (title.length == 0 && content.length == 0) {
                    err_msg = "標題與內文不可為空"
                } else if (title.length == 0) {
                    err_msg = "標題不可為空"
                } else if (content.length == 0) {
                    err_msg = "內文不可為空"
                }
                if (err_msg == null) {
                    post(title, content)
                } else {
                    ASAlertDialog.createDialog().setTitle("錯誤").setMessage(err_msg)
                        .addButton(getContextString(R.string.sure)).show()
                }
            }
        } else if (view === _symbol_button) {
            // 表情符號
            val items: Array<String?> = articleExpressions
            Dialog_InsertExpression.createDialog().setTitle("表情符號").addItems(items)
                .setListener(object : Dialog_InsertExpression_Listener {
                    override fun onListDialogItemClicked(
                        paramASListDialog: Dialog_InsertExpression?,
                        index: Int,
                        aTitle: String?
                    ) {
                        val symbol = items[index]
                        this@PostArticlePage.insertString(symbol)
                    }

                    override fun onListDialogSettingClicked() {
                        // 將當前內容存檔, pushView會讓當前頁面消失
                        setRecover()
                        navigationController!!.pushViewController(ArticleExpressionListPage())
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        } else if (view === _insert_symbol_button) {
            // 符號
            val dialog = Dialog_InsertSymbol()
            dialog.setListener(Dialog_InsertSymbol_Listener { str: String? -> this.insertString(str) })
            dialog.show()
        } else if (view === _paint_color_button) {
            // 上色
            val dialog = Dialog_PaintColor()
            dialog.setListener(Dialog_PaintColor_Listener { str: String? -> this.insertString(str) })
            dialog.show()
        } else if (view.getId() == R.id.ArticlePostDialog_File) {
            // 檔案
            onFileClicked()
        } else if (view.getId() == R.id.ArticlePostDialog_ShortenUrl) {
            // 短網址
            val dialog = DialogShortenUrl()
            dialog.setListener(DialogShortenUrlListener { str: String? -> this.insertString(str) })
            dialog.show()
        } else if (view.getId() == R.id.ArticlePostDialog_ShortenImage) {
            // 縮圖
            val shortenTimes = propertiesNoVipShortenTimes
            if (!propertiesVIP && shortenTimes > 30) {
                showLongToast(getContextString(R.string.vip_only_message))
                return
            }
            val intent = Intent(TempSettings.myActivity, DialogShortenImage::class.java)
            startActivity(intent)
        } else if (view.getId() == R.id.ArticlePostDialog_EditButtons) {
            showShortToast(getContextString(R.string.error_under_develop))
        }
    }

    /** 發文  */
    private fun post(title: String, content: String?) {
        val send_title: String?

        if (_article_number == null || title != _ori_title) {
            send_title = title
        } else {
            send_title = null
        }
        val send_content = content
        // 有來源文章編號, 可能為Reply, edit
        if (_article_number != null) {
            if (_operation_mode == OperationMode.Reply) {
                // 回覆: 有註記回文
                val dialog = Dialog_PostArticle(1)
                dialog.setListener(Dialog_PostArticle_Listener { aTarget: String?, aSign: String? ->
                    if (this@PostArticlePage._listener != null) {
                        this@PostArticlePage._listener!!.onPostDialogSendButtonClicked(
                            this@PostArticlePage,
                            send_title,
                            send_content,
                            aTarget,
                            this@PostArticlePage._article_number,
                            aSign,
                            recover
                        )
                    }
                    // 回應到作者信箱
                    if (aTarget == "M") {
                        closeArticle()
                    }
                })
                dialog.show()
            } else {
                // 修改: 沒有註記回文
                ASAlertDialog.createDialog().addButton(getContextString(R.string.cancel))
                    .addButton("送出").setTitle("確認").setMessage("您是否確定要編輯此文章?")
                    .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                        if (index == 1) {
                            if (this@PostArticlePage._listener != null) {
                                this@PostArticlePage._listener!!.onPostDialogEditButtonClicked(
                                    this@PostArticlePage,
                                    this@PostArticlePage._article_number,
                                    send_title,
                                    this@PostArticlePage.editContent
                                )
                            }
                        }
                        closeArticle()
                    }).show()
            }
        } else {
            // 新增文章
            val dialog2 = Dialog_PostArticle(0)
            dialog2.setListener(Dialog_PostArticle_Listener { aTarget: String?, aSign: String? ->
                if (this@PostArticlePage._listener != null) {
                    this@PostArticlePage._listener!!.onPostDialogSendButtonClicked(
                        this@PostArticlePage,
                        send_title,
                        send_content,
                        null,
                        null,
                        aSign,
                        recover
                    )
                }
            })
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
            TelnetClient.getClient().sendDataToServer(data)
            recover = false
        }
        clear()
        navigationController!!.popToViewController(_board_page)
        PageContainer.getInstance().cleanPostArticlePage()
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, aView: View?, index: Int, id: Long) {
        _header_selected = index
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
    }

    /** 標題欄位取得焦點  */
    var titleFieldListener: OnFocusChangeListener =
        OnFocusChangeListener { view: View?, hasFocus: Boolean ->
            if (view !== _title_field) {
                return@OnFocusChangeListener
            }
            if (hasFocus) {
                _title_field!!.setSingleLine(false)
                _title_field!!.setTextColor(-1)
                _title_field_background!!.setTextColor(0)
                return@OnFocusChangeListener
            }
            _title_field!!.setSingleLine(true)
            _title_field!!.setTextColor(0)
            _title_field_background!!.setTextColor(-1)
            _title_field_background!!.setText(_title_field!!.getText().toString())
        }

    fun setHeaderHidden(hidden: Boolean) {
        _header_hidden = hidden
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
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ) {
                    if (aTitle == getContextString(R.string.load_temp)) {
                        this@PostArticlePage.ontLoadArticleFromTempButtonClicked()
                    } else if (aTitle == getContextString(R.string.save_to_temp)) {
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
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ) {
                    if (index == 0) {
                        ASAlertDialog.createDialog()
                            .setTitle(getContextString(R.string.load_temp))
                            .setMessage("您是否確定要以上次送出文章的內容取代您現在編輯的內容?")
                            .addButton(getContextString(R.string.cancel))
                            .addButton(getContextString(R.string.sure))
                            .setListener(ASAlertDialogListener { aDialog12: ASAlertDialog?, button_index: Int ->
                                this@PostArticlePage.loadTempArticle(
                                    9
                                )
                            }).show()
                    } else {
                        ASAlertDialog.createDialog()
                            .setTitle(getContextString(R.string.load_temp))
                            .setMessage("您是否確定要以暫存檔." + index + "的內容取代您現在編輯的內容?")
                            .addButton(getContextString(R.string.cancel))
                            .addButton(getContextString(R.string.sure))
                            .setListener(ASAlertDialogListener { aDialog1: ASAlertDialog?, button_index: Int ->
                                if (button_index == 1) {
                                    this@PostArticlePage.loadTempArticle(index - 1)
                                }
                            }).show()
                    }
                }
            }).show()
    }

    /** 讀取暫存檔  */
    private fun loadTempArticle(index: Int) {
        val article_temp = ArticleTempStore(context).articles.get(index)
        _header_selector!!.setSelection(getIndexOfHeader(article_temp.header))
        _title_field!!.setText(article_temp.title)
        _content_field!!.setText(article_temp.content)
    }

    /** 儲存暫存檔  */
    private fun saveTempArticle(index: Int) {
        val store = ArticleTempStore(context)
        val article_temp = store.articles.get(index)
        // 類別
        article_temp.header = ""
        if (_header_selector!!.getSelectedItemPosition() > 0) {
            article_temp.header = getArticleHeader(_header_selector!!.getSelectedItemPosition())
        }
        // 標題
        article_temp.title = _title_field!!.getText().toString()
        // 內文
        article_temp.content = _content_field!!.getText().toString()

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
        if (aHeader == null || aHeader.length == 0) {
            return 0
        }
        for (i in 1..<_headers.size) {
            if (_headers[i] == aHeader) {
                return i
            }
        }
        return -1
    }

    /** 從定位取出特定標題  */
    fun getArticleHeader(index: Int): String? {
        if (index <= 0 || index >= _headers.size) {
            return ""
        }
        return _headers[index]
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
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ) {
                    ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.load_temp))
                        .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                        .addButton(getContextString(R.string.cancel))
                        .addButton(getContextString(R.string.sure))
                        .setListener(ASAlertDialogListener { aDialog1: ASAlertDialog?, button_index: Int ->
                            if (button_index == 1) {
                                this@PostArticlePage.saveTempArticle(index)
                            }
                        }).show()
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
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ) {
                    ASAlertDialog.createDialog()
                        .setTitle(getContextString(R.string.load_temp))
                        .setMessage("您是否確定要以現在編輯的內容取代暫存檔." + (index + 1) + "的內容?")
                        .addButton(getContextString(R.string.cancel))
                        .addButton(getContextString(R.string.sure))
                        .setListener(ASAlertDialogListener { aDialog1: ASAlertDialog?, button_index: Int ->
                            if (button_index == 0) {
                                this@PostArticlePage.onBackPressed()
                            } else {
                                this@PostArticlePage.saveTempArticle(index)
                                closeArticle()
                            }
                        }).show()
                }
            }).show()
    }

    fun setOperationMode(aMode: OperationMode?) {
        _operation_mode = aMode
    }

    fun setArticleNumber(aNumber: String?) {
        _article_number = aNumber
    }

    fun setEditFormat(aFormat: String?) {
        _edit_format = aFormat
    }

    val editContent: String?
        /** 組合修改文章內容  */
        get() {
            if (_edit_format == null) {
                return null
            }
            val _title = judgeDoubleWord(
                _title_field!!.getText().toString(),
                TelnetFrame.Companion.DEFAULT_COLUMN - 9
            ).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val _content = _content_field!!.getText().toString()
            return String.format(_edit_format!!, _title, _content)
        }

    val isKeepOnOffline: Boolean
        get() = true

    fun setBoardPage(aBoardMainPage: BoardMainPage?) {
        _board_page = aBoardMainPage
    }

    /** 按下 返回  */
    protected override fun onBackPressed(): Boolean {
        if (_title_field!!.getText().toString().length == 0 && _content_field!!.getText()
                .toString().length == 0
        ) {
            return super.onBackPressed()
        }
        ASAlertDialog.createDialog()
            .setTitle(getContextString(R.string._article))
            .setMessage(getContextString(R.string.give_up_post_article))
            .addButton(getContextString(R.string.cancel))
            .addButton(getContextString(R.string._giveUp))
            .addButton(getContextString(R.string._save))
            .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                if (index == 1) {
                    closeArticle()
                } else if (index == 2) {
                    this@PostArticlePage.ontSaveArticleToTempAndLeaveButtonClicked()
                }
            }).show()
        return true
    }

    /** 按下 展開/摺疊  */
    var postToolbarShowOnClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        val thisBtn = view as TextView
        val toolBar = mainLayout!!.findViewById<LinearLayout>(R.id.toolbar)
        val layoutParams = toolBar.getLayoutParams() as RelativeLayout.LayoutParams
        if (isToolbarShow) {
            // 從展開->摺疊
            isToolbarShow = false
            thisBtn.setText(getContextString(R.string.post_toolbar_show))
            layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                60f,
                resource.getDisplayMetrics()
            ).toInt()
        } else {
            isToolbarShow = true
            thisBtn.setText(getContextString(R.string.post_toolbar_collapse))
            layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                120f,
                resource.getDisplayMetrics()
            ).toInt()
        }
        toolBar.setLayoutParams(layoutParams)
    }

    fun insertString(str: String?) {
        if (_content_field != null) {
            _content_field!!.getEditableText().insert(_content_field!!.getSelectionStart(), str)
        }
    }

    /** 設定TelnetArticle, 格式按鈕會使用  */
    fun setTelnetArticle(article: TelnetArticle?) {
        telnetArticle = article
    }

    /** 按下格式  */
    var referenceClickListener: View.OnClickListener = View.OnClickListener { view: View? ->
        val authors: MutableList<ReferenceAuthor?> = ArrayList<ReferenceAuthor?>()
        if (telnetArticle == null) {
            showErrorDialog(getContextString(R.string.dialog_reference_error_1), this)
            return@OnClickListener
        }

        if (_edit_format == null) {
            // 回覆
            // 回應作者
            val replyAuthor = ReferenceAuthor()
            replyAuthor.enabled = true
            replyAuthor.authorName = telnetArticle!!.author
            authors.add(replyAuthor)

            // 更上一層
            val newAuthor = ReferenceAuthor()
            if (telnetArticle!!.infoSize > 0) {
                val item = telnetArticle!!.getInfo(0)
                newAuthor.enabled = true
                newAuthor.authorName = item!!.author
            }
            authors.add(newAuthor)
        } else {
            // 修改
            if (telnetArticle!!.infoSize > 0) {
                for (i in 0..<telnetArticle!!.infoSize) {
                    val newAuthor = ReferenceAuthor()
                    val item = telnetArticle!!.getInfo(i)
                    newAuthor.enabled = true
                    newAuthor.authorName = item!!.author
                    authors.add(newAuthor)
                }
            }
        }

        val dialog = DialogReference()
        dialog.setAuthors(authors)
        dialog.setListener(DialogReferenceListener { authors: List<ReferenceAuthor> ->
            this.referenceBack(
                authors
            )
        })
        dialog.show()
    }

    fun referenceBack(authors: MutableList<ReferenceAuthor>) {
        // 找出父層內容
        val originParentContent = Arrays.asList<String?>(
            *telnetArticle!!.generateReplyContent().split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        )
        val tempParentContent: MutableList<String> = ArrayList<String>()
        val finalParentContent: MutableList<String?> = ArrayList<String?>()


        // 開始篩選
        val author0 = authors.get(0)
        val author1 = authors.get(1)
        var author0InsertRows = 0
        var author1InsertRows = 0

        // 如果有選到後三行, 計算兩個作者總行數
        var author0TotalRows = 0
        var author1TotalRows = 0
        for (i in originParentContent.indices) {
            val rowString = originParentContent.get(i)
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
            val rowString = tempParentContent.get(i)
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
        val originFromContent = Arrays.asList<String?>(
            *_content_field!!.getText().toString().split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        )
        val selfContent: MutableList<String?> = ArrayList<String?>()
        for (i in originFromContent.indices) {
            if (!originFromContent.get(i)!!.startsWith("※ 引述") && !originFromContent.get(i)!!
                    .startsWith("> ")
            ) {
                selfContent.add(originFromContent.get(i))
            }
        }
        // final Result
        val joinedSelfContent = java.lang.String.join("\n", selfContent)

        val _rev2 = java.lang.String.join("", joinedParentContent, "\n", joinedSelfContent)
        _content_field!!.setText(_rev2)
    }
}
