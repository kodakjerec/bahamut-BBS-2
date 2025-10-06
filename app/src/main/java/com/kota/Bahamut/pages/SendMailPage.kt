package com.kota.Bahamut.pages

import android.text.Selection
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.UserSettings.Companion.articleExpressions
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.telnetUI.TelnetPage
import java.util.Vector

class SendMailPage : TelnetPage(), View.OnClickListener, OnFocusChangeListener,
    DialogInsertSymbolListener, DialogPaintColorListener {
    var myContent: String? = null
    var contentField: EditText? = null
    var hideTitleButton: Button? = null
    var paintColorButton: Button? = null
    var sendMailPageListener: SendMailPageListener? = null
    var postButton: Button? = null
    var receiver1: String? = null
    var receiverField: EditText? = null
    var receiverFieldBackground: TextView? = null
    var symbolButton: Button? = null
    var myTitle: String? = null
    var titleBlock: View? = null
    var isTitleBlockHidden: Boolean = false
    var titleField: EditText? = null
    var titleFieldBackground: TextView? = null
    var recover: Boolean = false

    val name: String
        get() = "BahamutSendMailDialog"

    fun setListener(aListener: SendMailPageListener?) {
        sendMailPageListener = aListener
    }

    override val pageLayout: Int
        get() = R.layout.send_mail_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_SEND_MAIL

    override val isPopupPage: Boolean
        get() = true

    override fun onPageDidLoad() {
        initial()
        if (recover) {
            loadTempArticle(8)
            recover = false
        }
    }

    override fun onPageDidDisappear() {
        titleField = null
        titleFieldBackground = null
        receiverField = null
        receiverFieldBackground = null
        contentField = null
        postButton = null
        symbolButton = null
        hideTitleButton = null
        titleBlock = null
        super.onPageDidDisappear()
    }

    fun refreshTitleField() {
        if (titleField != null && myTitle != null) {
            titleField?.setText(myTitle)
            if (myTitle?.isNotEmpty() == true) {
                Selection.setSelection(titleField?.text, 1)
            }
            myTitle = null
        }
    }

    fun refreshReceiverField() {
        if (receiverField != null && receiver1 != null) {
            receiverField?.setText(receiver1)
            receiver1 = null
        }
    }

    fun refreshContentField() {
        if (contentField != null && myContent != null) {
            contentField?.setText(myContent)
            if (myContent?.isNotEmpty() == true) {
                Selection.setSelection(contentField?.text, myContent?.length!!)
            }
            myContent = null
        }
    }

    override fun onPageRefresh() {
        refreshTitleField()
        refreshContentField()
        refreshReceiverField()
    }

    fun initial() {
        titleField = findViewById(R.id.SendMail_TitleField) as EditText?
        titleField?.onFocusChangeListener = this
        titleFieldBackground = findViewById(R.id.SendMail_TitleFieldBackground) as TextView?

        receiverField = findViewById(R.id.SendMail_ReceiverField) as EditText?
        receiverField?.onFocusChangeListener = this
        receiverFieldBackground =
            findViewById(R.id.SendMail_ReceiverFieldBackground) as TextView?

        contentField = findViewById(R.id.SendMailDialog_EditField) as EditText?

        postButton = findViewById(R.id.SendMailDialog_Post) as Button?
        postButton?.setOnClickListener(this)

        symbolButton = findViewById(R.id.SendMailDialog_Symbol) as Button?
        symbolButton?.setOnClickListener(this)

        hideTitleButton = findViewById(R.id.SendMailDialog_Cancel) as Button?
        hideTitleButton?.setOnClickListener(this)

        paintColorButton = findViewById(R.id.ArticlePostDialog_Color) as Button?
        paintColorButton?.setOnClickListener(this)

        findViewById(R.id.SendMailDialog_change)?.setOnClickListener(this)
        titleBlock = findViewById(R.id.SendMail_TitleBlock)
        refresh()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    override fun clear() {
        if (receiverField != null) {
            receiverField?.setText("")
        }
        if (titleField != null) {
            titleField?.setText("")
        }
        if (contentField != null) {
            contentField?.setText("")
        }
        sendMailPageListener = null
    }

    fun setPostTitle(aTitle: String?) {
        myTitle = aTitle
        refreshTitleField()
    }

    fun setPostContent(aContent: String?) {
        myContent = aContent
        refreshContentField()
    }

    override fun onClick(view: View) {
        if (view === postButton) {
            if (sendMailPageListener != null) {
                val receiver = receiverField?.text.toString().replace("\n", "")
                val title = titleField?.text.toString().replace("\n", "")
                val content = contentField?.text.toString()
                val errMsg = StringBuilder()
                val empty = Vector<String?>()
                if (receiver.isEmpty()) {
                    empty.add("收件人")
                }
                if (title.isEmpty()) {
                    empty.add("標題")
                }
                if (content.isEmpty()) {
                    empty.add("內文")
                }
                if (empty.isNotEmpty()) {
                    for (i in empty.indices) {
                        errMsg.append(empty[i])
                        if (i == empty.size - 2) {
                            errMsg.append("與")
                        } else if (i < empty.size - 2) {
                            errMsg.append("、")
                        }
                    }
                    errMsg.append("不可為空")
                }
                if (errMsg.isNotEmpty()) {
                    ASAlertDialog.createDialog().setTitle("錯誤").setMessage(errMsg.toString())
                        .addButton("確定").show()
                    return
                }
                val sendReceiver = receiver
                val sendTitle = title
                val sendContent = content
                ASAlertDialog.createDialog().addButton("取消").addButton("送出").setTitle("確認")
                    .setMessage("您是否確定要送出此信件?")
                    .setListener { aDialog: ASAlertDialog?, index: Int ->
                        if (index == 1) {
                            sendMailPageListener?.onSendMailDialogSendButtonClicked(
                                this@SendMailPage,
                                sendReceiver,
                                sendTitle,
                                sendContent
                            )
                            navigationController.popViewController()
                            clear()
                        }
                    }.show()
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
                        contentField?.editableText!!.insert(contentField?.selectionStart!!, symbol)
                    }

                    override fun onListDialogSettingClicked() {
                        // 將當前內容存檔, pushView會讓當前頁面消失
                        setRecover()
                        navigationController.pushViewController(ArticleExpressionListPage())
                    }
                }).scheduleDismissOnPageDisappear(this).show()
        } else if (view === hideTitleButton) {
            onInsertSymbolButtonClicked()
        } else if (view === paintColorButton) {
            val dialog = DialogPaintColor()
            dialog.setListener(this)
            dialog.show()
        } else if (view.id == R.id.SendMailDialog_change) {
            changeViewMode()
        }
    }

    fun setRecover() {
        recover = true
        saveTempArticle(8)
    }

    fun refresh() {
        if (isTitleBlockHidden) {
            titleBlock?.visibility = View.GONE
        } else {
            titleBlock?.visibility = View.VISIBLE
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v === receiverField) {
            if (hasFocus) {
                receiverField?.isSingleLine = false
                receiverFieldBackground?.setTextColor(0)
                receiverField?.setTextColor(-1)
            } else {
                receiverField?.isSingleLine = true
                receiverField?.setTextColor(0)
                receiverFieldBackground?.setTextColor(-1)
                receiverFieldBackground?.text = receiverField?.text.toString()
            }
        }
        if (v !== titleField) {
            return
        }
        if (hasFocus) {
            titleField?.isSingleLine = false
            titleField?.setTextColor(-1)
            titleFieldBackground?.setTextColor(0)
            return
        }
        titleField?.isSingleLine = true
        titleField?.setTextColor(0)
        titleFieldBackground?.setTextColor(-1)
        titleFieldBackground?.text = titleField?.text.toString()
    }

    fun changeViewMode() {
        isTitleBlockHidden = !isTitleBlockHidden
        refresh()
    }

    fun onInsertSymbolButtonClicked() {
        val dialog = DialogInsertSymbol()
        dialog.setListener(this)
        dialog.show()
    }

    override fun onSymbolDialogDismissWithSymbol(str: String) {
        contentField?.editableText!!.insert(contentField?.selectionStart!!, str)
    }

    fun setReceiver(aReceiver: String?) {
        receiver1 = aReceiver
        refreshReceiverField()
    }

    override val isKeepOnOffline: Boolean
        get() = true

    override fun onPaintColorDone(str: String) {
        contentField?.editableText!!.insert(contentField?.selectionStart!!, str)
    }

    // 讀取暫存檔
    private fun loadTempArticle(index: Int) {
        val articleTemp = ArticleTempStore(context).articles[index]
        receiverField?.setText(articleTemp.header)
        titleField?.setText(articleTemp.title)
        contentField?.setText(articleTemp.content)
    }

    // 儲存暫存檔
    private fun saveTempArticle(index: Int) {
        val store = ArticleTempStore(context)
        val articleTemp = store.articles[index]
        // 收信者
        articleTemp.header = receiverField?.text.toString()
        // 標題
        articleTemp.title = titleField?.text.toString()
        // 內文
        articleTemp.content = contentField?.text.toString()

        // 存檔
        store.store()
    }
}
