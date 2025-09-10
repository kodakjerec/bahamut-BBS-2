package com.kota.Bahamut.Pages.Messages

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASListView
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.Dialogs.DialogShortenImage
import com.kota.Bahamut.Dialogs.DialogShortenUrl
import com.kota.Bahamut.Dialogs.DialogShortenUrlListener
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression
import com.kota.Bahamut.Dialogs.Dialog_InsertExpression_Listener
import com.kota.Bahamut.Dialogs.Dialog_InsertSymbol
import com.kota.Bahamut.Pages.BlockListPage.ArticleExpressionListPage
import com.kota.Bahamut.Pages.Model.PostEditText
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.TelnetUI.TelnetPage
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MessageSub: TelnetPage(), View.OnClickListener {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var listView: ASListView
    private lateinit var senderNameField: TextView
    private lateinit var contentField:PostEditText
    private var isPostDelayedSuccess = false

    override fun getPageLayout(): Int {
        return R.layout.message_sub
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    override fun isTopPage(): Boolean {
        return true
    }

    override fun isPopupPage(): Boolean {
        return true
    }

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout

        listView = mainLayout.findViewById(R.id.Message_Sub_Scroll)

        val txtEsc = mainLayout.findViewById<TextView>(R.id.Message_Sub_Back)
        txtEsc.setOnClickListener{ _-> onBackPressed() }
        senderNameField = mainLayout.findViewById(R.id.Message_Sub_Sender)
        contentField = mainLayout.findViewById(R.id.Message_Sub_EditField)

        mainLayout.findViewById<TextView>(R.id.Message_Sub_Cancel).setOnClickListener(this)
        mainLayout.findViewById<TextView>(R.id.Message_Sub_Symbol).setOnClickListener(this)
        mainLayout.findViewById<LinearLayout>(R.id.Message_Sub_ShortenUrl).setOnClickListener(this)
        mainLayout.findViewById<TextView>(R.id.Message_Sub_ShortenImage).setOnClickListener(this)
        mainLayout.findViewById<TextView>(R.id.Message_Sub_Post).setOnClickListener(this)
    }

    /** 設定內容 */
    fun setSenderName(senderName: String) {
        senderNameField.text = senderName

        // 讀取訊息
        val db = MessageDatabase(context)
        try {
            listView.adapter = null

            val messageList = db.getIdMessage(senderName)
            val myAdapter = MessageSubAdapter(messageList)
            listView.adapter = myAdapter
            // 移到最下方
            listView.setSelection(myAdapter.count-1)
        } finally {
            db.close()
        }
    }

    /** 臨時插入一個訊息 */
    fun insertMessage(item:BahaMessage) {
        if (item.senderName == senderNameField.text) {
            // 更新為已讀取
            val db = MessageDatabase(context)
            try {
                db.updateReceiveMessage(item.senderName)
            } finally {
                db.close()
            }
            object: ASRunner(){
                override fun run() {
                    val myAdapter: MessageSubAdapter = listView.adapter as MessageSubAdapter
                    myAdapter.addItem(item)
                }
            }.runInMainThread()
        }
    }

    // 按下畫面上的按鈕
    override fun onClick(view: View) {
        when (view.id) {
            R.id.Message_Sub_Cancel -> {
                // 符號
                val dialog = Dialog_InsertSymbol()
                dialog.setListener { str: String -> this.insertString(str) }
                dialog.show()
            }
            R.id.Message_Sub_Symbol -> {
                // 表情符號
                val items = UserSettings.getArticleExpressions()
                Dialog_InsertExpression.createDialog().setTitle("表情符號").addItems(items)
                    .setListener(object : Dialog_InsertExpression_Listener {
                        override fun onListDialogItemClicked(
                            paramASListDialog: Dialog_InsertExpression,
                            index: Int,
                            aTitle: String
                        ) {
                            val symbol = items[index]
                            insertString(symbol)
                        }

                        override fun onListDialogSettingClicked() {
                            navigationController.pushViewController(ArticleExpressionListPage())
                        }
                    }).scheduleDismissOnPageDisappear(this).show()
            }
            R.id.Message_Sub_ShortenUrl -> {
                // 短網址
                val dialog = DialogShortenUrl()
                dialog.setListener(object: DialogShortenUrlListener {
                    override fun onShortenUrlDone(str: String?) {
                        insertString(str!!)
                    }
                })
                dialog.show()
            }
            R.id.Message_Sub_ShortenImage -> {
                // 縮圖
                var shortenTimes: Int = UserSettings.getPropertiesNoVipShortenTimes()
                if (!UserSettings.getPropertiesVIP() && shortenTimes>30) {
                    ASToast.showLongToast(getContextString(R.string.vip_only_message))
                    return
                }
                val intent = Intent(TempSettings.myActivity, DialogShortenImage::class.java)
                startActivity(intent)
            }
            R.id.Message_Sub_Post -> {
                // 發表
                if (contentField.text!!.isNotEmpty()) {
                    // 送出訊息指令
                    sendMessagePart1()


                    // 預設送出完畢
                    contentField.setText("")
                }
            }
        }
    }

    var tempMessage: BahaMessage? = null
    /** 送出訊息-1 試著啟動訊息 */
    private fun sendMessagePart1() {
        val aSenderName = senderNameField.text.toString().trim()
        val aMessage = contentField.text.toString().trim()
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.CTRL_S)

        // 更新db
        val db = MessageDatabase(context)
        try {
            val bahaMessage = db.sendMessage(aSenderName, aMessage)
            if (bahaMessage!=null) {
                tempMessage = bahaMessage
                // 更新畫面
                insertMessage(bahaMessage)
            }
        } finally {
            db.close()
        }

        messageAsRunner.cancel()
        messageAsRunner.postDelayed(3000)
        isPostDelayedSuccess = false
    }
    /** 送出訊息-2 送出對方id */
    fun sendMessagePart2() {
        messageAsRunner.cancel()

        if (tempMessage!=null) {
            val aSenderName = tempMessage!!.senderName

            val builder = TelnetOutputBuilder.create()
                .pushString("$aSenderName\n")
                .build()
            TelnetClient.getClient().sendDataToServer(builder)
        }

        messageAsRunner.postDelayed(3000)
        // telnet會連續觸發這段兩次
        if (!isPostDelayedSuccess)
        isPostDelayedSuccess = false
    }
    /** 送出訊息-3 更新訊息 */
    fun sendMessagePart3() {
        messageAsRunner.cancel()
        isPostDelayedSuccess = true

        if (tempMessage!=null) {
            tempMessage!!.status = MessageStatus.Success
            val aMessage = tempMessage!!.message

            val builder = TelnetOutputBuilder.create()
                .pushString("$aMessage\n")
                .build()
            TelnetClient.getClient().sendDataToServer(builder)

            // 更新db
            val db = MessageDatabase(context)
            try {
                db.updateSendMessage(tempMessage!!)
                // 更新畫面
                for (i in listView.childCount - 1 downTo 0) {
                    val view = listView.getChildAt(i)
                    if (view.javaClass == MessageSubSend::class.java) {
                        val item:MessageSubSend = view as MessageSubSend
                        if (item.myBahaMessage.id == tempMessage!!.id) {
                            item.setStatus(tempMessage!!.status)
                            break
                        }
                    }
                }
            } finally {
                db.close()
            }
            tempMessage = null
        }
    }
    fun sendMessageFail(status:MessageStatus) {
        messageAsRunner.cancel()
        isPostDelayedSuccess = false

        if (tempMessage!=null) {
            tempMessage!!.status = status
            // 更新db
            val db = MessageDatabase(context)
            try {
                db.updateSendMessage(tempMessage!!)
                // 更新畫面
                for (i in listView.childCount - 1 downTo 0) {
                    val view = listView.getChildAt(i)
                    if (view.javaClass == MessageSubSend::class.java) {
                        val item:MessageSubSend = view as MessageSubSend
                        if (item.myBahaMessage.id == tempMessage!!.id) {
                            item.setStatus(tempMessage!!.status)
                            break
                        }
                    }
                }
            } finally {
                db.close()
            }
            tempMessage = null
        }
    }

    fun insertString(str: String) {
        contentField.editableText.insert(contentField.selectionStart, str)
    }

    /** 強制發送訊息進入失敗 */
    private var messageAsRunner: ASRunner = object : ASRunner() {
        override fun run() {
            if (!isPostDelayedSuccess) {
                sendMessageFail(MessageStatus.Offline)
                ASToast.showLongToast("私訊無反應，對方可能不在線上")
            }
        }
    }
}