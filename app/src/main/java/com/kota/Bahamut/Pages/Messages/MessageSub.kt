package com.kota.Bahamut.Pages.Messages

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kota.ASFramework.Thread.ASRunner
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
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.TempSettings.setImgurAlbum
import com.kota.Bahamut.Service.TempSettings.setImgurToken
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
    private lateinit var scrollViewLayout: LinearLayout
    private lateinit var contentField:PostEditText

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

        scrollViewLayout = mainLayout.findViewById(R.id.Message_Sub_Scroll)

        val txtEsc = mainLayout.findViewById<TextView>(R.id.Message_Sub_Back)
        txtEsc.setOnClickListener{ _-> onBackPressed() }
        contentField = mainLayout.findViewById(R.id.Message_Sub_EditField)

        mainLayout.findViewById<TextView>(R.id.Message_Sub_Cancel).setOnClickListener(this)
        mainLayout.findViewById<TextView>(R.id.Message_Sub_Symbol).setOnClickListener(this)
        mainLayout.findViewById<LinearLayout>(R.id.Message_Sub_ShortenUrl).setOnClickListener(this)
        mainLayout.findViewById<TextView>(R.id.Message_Sub_ShortenImage).setOnClickListener(this)
        mainLayout.findViewById<TextView>(R.id.Message_Sub_Post).setOnClickListener(this)
    }

    /** 設定內容 */
    fun setSenderName(senderName: String) {

        val txtSenderName: TextView = mainLayout.findViewById(R.id.Message_Sub_Sender)
        txtSenderName.text = senderName

        // 讀取訊息
        val db = MessageDatabase(context)
        try {
            scrollViewLayout.removeAllViews()

            val messageList = db.getIdMessage(senderName)
            messageList.forEach { item: BahaMessage ->
                if (item.type == 0) {
                    val messageSubReceive = MessageSubReceive(context)
                    messageSubReceive.setContent(item)
                    scrollViewLayout.addView(messageSubReceive)
                } else {
                    val messageSend = MessageSubSend(context)
                    messageSend.setContent(item)
                    scrollViewLayout.addView(messageSend)
                }
            }
        } finally {
            db.close()
            // 滾動到最下方
            val scrollview: ScrollView = scrollViewLayout.parent as ScrollView
            scrollview.post {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    /** 臨時插入一個訊息 */
    fun insertMessage(item:BahaMessage) {
        val txtSenderName: TextView = mainLayout.findViewById(R.id.Message_Sub_Sender)
        if (item.senderName == txtSenderName.text) {
            // 更新為已讀取
            val db = MessageDatabase(context)
            try {
                db.updateReceiveMessage(item.senderName)
            } finally {
                db.close()
            }

            // 插入到Scrollview
            object: ASRunner(){
                override fun run() {
                    if (item.type == 0) {
                        val messageSubReceive = MessageSubReceive(context)
                        messageSubReceive.setContent(item)
                        scrollViewLayout.addView(messageSubReceive)
                    } else {
                        val messageSend = MessageSubSend(context)
                        messageSend.setContent(item)
                        scrollViewLayout.addView(messageSend)
                    }
                    // 滾動到最下方
                    val scrollview:ScrollView = scrollViewLayout.parent as ScrollView
                    scrollview.post {
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
            }.runInMainThread()
        }
    }

    // 按下畫面上的按鈕
    override fun onClick(view: View) {
        val viewId = view.id
        when (viewId) {
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
                        insertString(str)
                    }
                })
                dialog.show()
            }
            R.id.Message_Sub_ShortenImage -> {
                // 縮圖
                getUrlToken()
                val intent = Intent(TempSettings.myActivity, DialogShortenImage::class.java)
                startActivity(intent)
            }
            R.id.Message_Sub_Post -> {
                // 發表
                if (contentField.text!!.isNotEmpty()) {
                    val txtSenderName: TextView = mainLayout.findViewById(R.id.Message_Sub_Sender)
                    val aSenderName = txtSenderName.text.toString().trim()
                    val aMessage = contentField.text.toString().trim()
                    // 送出水球指令
                    val builder = TelnetOutputBuilder.create()
                        .pushKey(TelnetKeyboard.CTRL_S) // 準備送水球
                        .pushString("$aSenderName\n")
                        .pushString("$aMessage\n")
                        .build()
                    TelnetClient.getClient().sendDataToServer(builder)

                    // 更新畫面上的送出
                    val db = MessageDatabase(context)
                    try {
                        val bahaMessage = db.sendMessage(aSenderName, aMessage)
                        insertMessage(bahaMessage)
                    } finally {
                        db.close()
                    }

                    // 預設送出完畢
                    contentField.setText("")
                }
            }
        }
    }

    private fun insertString(str: String?) {
        contentField.editableText.insert(contentField.selectionStart, str)
    }

    /** 取得imgur token  */
    private fun getUrlToken() {
        val apiUrl = "https://worker-get-imgur-token.kodakjerec.workers.dev/"
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .get()
            .build()
        ASRunner.runInNewThread {
            try {
                val response = client.newCall(request).execute()
                assert(response.body != null)
                val data = response.body!!.string()
                val jsonObject = JSONObject(data)
                val accessToken = jsonObject.getString("accessToken")
                val albumHash = jsonObject.getString("albumHash")
                if (accessToken.isNotEmpty()) {
                    setImgurToken(accessToken)
                    setImgurAlbum(albumHash)
                }
            } catch (e: Exception) {
//                ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error01));
                Log.e("ShortenImage", e.toString())
            }
        }
    }
}