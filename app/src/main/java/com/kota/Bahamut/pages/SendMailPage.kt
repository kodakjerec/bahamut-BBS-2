package com.kota.Bahamut.pages

import android.text.Selection
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.dataModels.ArticleTempStore
import com.kota.Bahamut.dialogs.Dialog_InsertExpression
import com.kota.Bahamut.dialogs.Dialog_InsertExpression_Listener
import com.kota.Bahamut.dialogs.Dialog_InsertSymbol
import com.kota.Bahamut.dialogs.Dialog_InsertSymbol_Listener
import com.kota.Bahamut.dialogs.Dialog_PaintColor
import com.kota.Bahamut.dialogs.Dialog_PaintColor_Listener
import com.kota.Bahamut.pages.blockListPage.ArticleExpressionListPage
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.UserSettings.Companion.articleExpressions
import com.kota.telnetUI.TelnetPage
import java.util.Vector

class SendMailPage : TelnetPage(), View.OnClickListener, OnFocusChangeListener,
    Dialog_InsertSymbol_Listener, Dialog_PaintColor_Listener {
    var _content: String? = null
    var _content_field: EditText? = null
    var _hide_title_button: Button? = null
    var _paint_color_button: Button? = null
    var _listener: SendMailPage_Listener? = null
    var _post_button: Button? = null
    var _receiver: String? = null
    var _receiver_field: EditText? = null
    var _receiver_field_background: TextView? = null
    var _symbol_button: Button? = null
    var _title: String? = null
    var _title_block: View? = null
    var _title_block_hidden: Boolean = false
    var _title_field: EditText? = null
    var _title_field_background: TextView? = null
    var recover: Boolean = false

    val name: String
        get() = "BahamutSendMailDialog"

    fun setListener(aListener: SendMailPage_Listener?) {
        _listener = aListener
    }

    val pageLayout: Int
        get() = R.layout.send_mail_page

    val pageType: Int
        get() = BahamutPage.BAHAMUT_SEND_MAIL

    val isPopupPage: Boolean
        get() = true

    public override fun onPageDidLoad() {
        initial()
        if (recover) {
            loadTempArticle(8)
            recover = false
        }
    }

    public override fun onPageDidDisappear() {
        _title_field = null
        _title_field_background = null
        _receiver_field = null
        _receiver_field_background = null
        _content_field = null
        _post_button = null
        _symbol_button = null
        _hide_title_button = null
        _title_block = null
        super.onPageDidDisappear()
    }

    fun refreshTitleField() {
        if (_title_field != null && _title != null) {
            _title_field!!.setText(_title)
            if (_title!!.length > 0) {
                Selection.setSelection(_title_field!!.getText(), 1)
            }
            _title = null
        }
    }

    fun refreshReceiverField() {
        if (_receiver_field != null && _receiver != null) {
            _receiver_field!!.setText(_receiver)
            _receiver = null
        }
    }

    fun refreshContentField() {
        if (_content_field != null && _content != null) {
            _content_field!!.setText(_content)
            if (_content!!.length > 0) {
                Selection.setSelection(_content_field!!.getText(), _content!!.length)
            }
            _content = null
        }
    }

    public override fun onPageRefresh() {
        refreshTitleField()
        refreshContentField()
        refreshReceiverField()
    }

    fun initial() {
        _title_field = findViewById(R.id.SendMail_TitleField) as EditText?
        _title_field!!.setOnFocusChangeListener(this)
        _title_field_background = findViewById(R.id.SendMail_TitleFieldBackground) as TextView?

        _receiver_field = findViewById(R.id.SendMail_ReceiverField) as EditText?
        _receiver_field!!.setOnFocusChangeListener(this)
        _receiver_field_background =
            findViewById(R.id.SendMail_ReceiverFieldBackground) as TextView?

        _content_field = findViewById(R.id.SendMailDialog_EditField) as EditText?

        _post_button = findViewById(R.id.SendMailDialog_Post) as Button?
        _post_button!!.setOnClickListener(this)

        _symbol_button = findViewById(R.id.SendMailDialog_Symbol) as Button?
        _symbol_button!!.setOnClickListener(this)

        _hide_title_button = findViewById(R.id.SendMailDialog_Cancel) as Button?
        _hide_title_button!!.setOnClickListener(this)

        _paint_color_button = findViewById(R.id.ArticlePostDialog_Color) as Button?
        _paint_color_button!!.setOnClickListener(this)

        findViewById(R.id.SendMailDialog_change)!!.setOnClickListener(this)
        _title_block = findViewById(R.id.SendMail_TitleBlock)
        refresh()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    public override fun clear() {
        if (_receiver_field != null) {
            _receiver_field!!.setText("")
        }
        if (_title_field != null) {
            _title_field!!.setText("")
        }
        if (_content_field != null) {
            _content_field!!.setText("")
        }
        _listener = null
    }

    fun setPostTitle(aTitle: String?) {
        _title = aTitle
        refreshTitleField()
    }

    fun setPostContent(aContent: String?) {
        _content = aContent
        refreshContentField()
    }

    override fun onClick(view: View) {
        if (view === _post_button) {
            if (_listener != null) {
                val receiver = _receiver_field!!.getText().toString().replace("\n", "")
                val title = _title_field!!.getText().toString().replace("\n", "")
                val content = _content_field!!.getText().toString()
                val err_msg = StringBuilder()
                val empty = Vector<String?>()
                if (receiver.length == 0) {
                    empty.add("收件人")
                }
                if (title.length == 0) {
                    empty.add("標題")
                }
                if (content.length == 0) {
                    empty.add("內文")
                }
                if (empty.size > 0) {
                    for (i in empty.indices) {
                        err_msg.append(empty.get(i))
                        if (i == empty.size - 2) {
                            err_msg.append("與")
                        } else if (i < empty.size - 2) {
                            err_msg.append("、")
                        }
                    }
                    err_msg.append("不可為空")
                }
                if (err_msg.length > 0) {
                    ASAlertDialog.createDialog().setTitle("錯誤").setMessage(err_msg.toString())
                        .addButton("確定").show()
                    return
                }
                val send_receiver = receiver
                val send_title = title
                val send_content = content
                ASAlertDialog.createDialog().addButton("取消").addButton("送出").setTitle("確認")
                    .setMessage("您是否確定要送出此信件?")
                    .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int ->
                        if (index == 1) {
                            _listener!!.onSendMailDialogSendButtonClicked(
                                this@SendMailPage,
                                send_receiver,
                                send_title,
                                send_content
                            )
                            navigationController!!.popViewController()
                            clear()
                        }
                    }).show()
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
                        _content_field!!.getEditableText()
                            .insert(_content_field!!.getSelectionStart(), symbol)
                    }

                    override fun onListDialogSettingClicked() {
                        // 將當前內容存檔, pushView會讓當前頁面消失
                        setRecover()
                        navigationController!!.pushViewController(ArticleExpressionListPage())
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        } else if (view === _hide_title_button) {
            onInsertSymbolButtonClicked()
        } else if (view === _paint_color_button) {
            val dialog = Dialog_PaintColor()
            dialog.setListener(this)
            dialog.show()
        } else if (view.getId() == R.id.SendMailDialog_change) {
            changeViewMode()
        }
    }

    fun setRecover() {
        recover = true
        saveTempArticle(8)
    }

    fun refresh() {
        if (_title_block_hidden) {
            _title_block!!.setVisibility(View.GONE)
        } else {
            _title_block!!.setVisibility(View.VISIBLE)
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v === _receiver_field) {
            if (hasFocus) {
                _receiver_field!!.setSingleLine(false)
                _receiver_field_background!!.setTextColor(0)
                _receiver_field!!.setTextColor(-1)
            } else {
                _receiver_field!!.setSingleLine(true)
                _receiver_field!!.setTextColor(0)
                _receiver_field_background!!.setTextColor(-1)
                _receiver_field_background!!.setText(_receiver_field!!.getText().toString())
            }
        }
        if (v !== _title_field) {
            return
        }
        if (hasFocus) {
            _title_field!!.setSingleLine(false)
            _title_field!!.setTextColor(-1)
            _title_field_background!!.setTextColor(0)
            return
        }
        _title_field!!.setSingleLine(true)
        _title_field!!.setTextColor(0)
        _title_field_background!!.setTextColor(-1)
        _title_field_background!!.setText(_title_field!!.getText().toString())
    }

    fun changeViewMode() {
        _title_block_hidden = !_title_block_hidden
        refresh()
    }

    fun onInsertSymbolButtonClicked() {
        val dialog = Dialog_InsertSymbol()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSymbolDialogDismissWithSymbol(symbol: String?) {
        _content_field!!.getEditableText().insert(_content_field!!.getSelectionStart(), symbol)
    }

    fun setReceiver(a_receiver: String?) {
        _receiver = a_receiver
        refreshReceiverField()
    }

    val isKeepOnOffline: Boolean
        get() = true

    override fun onPaintColorDone(str: String?) {
        _content_field!!.getEditableText().insert(_content_field!!.getSelectionStart(), str)
    }

    // 讀取暫存檔
    private fun loadTempArticle(index: Int) {
        val article_temp = ArticleTempStore(context).articles.get(index)
        _receiver_field!!.setText(article_temp.header)
        _title_field!!.setText(article_temp.title)
        _content_field!!.setText(article_temp.content)
    }

    // 儲存暫存檔
    private fun saveTempArticle(index: Int) {
        val store = ArticleTempStore(context)
        val article_temp = store.articles.get(index)
        // 收信者
        article_temp.header = _receiver_field!!.getText().toString()
        // 標題
        article_temp.title = _title_field!!.getText().toString()
        // 內文
        article_temp.content = _content_field!!.getText().toString()

        // 存檔
        store.store()
    }
}
