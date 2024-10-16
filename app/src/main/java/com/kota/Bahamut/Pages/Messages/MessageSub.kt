package com.kota.Bahamut.Pages.Messages

import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings
import com.kota.TelnetUI.TelnetPage

class MessageSub: TelnetPage() {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var scrollViewLayout: LinearLayout

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
    }

    /** 設定內容 */
    fun setSenderName(name: String) {
        val senderName = name

        val txtSenderName: TextView = mainLayout.findViewById(R.id.Message_Sub_Sender)
        txtSenderName.text = senderName

        // 讀取訊息
        val db = MessageDatabase(context)
        try {
            scrollViewLayout.removeAllViews()

            val messageList = db.getIdMessage(senderName)
            messageList.forEach { item: BahaMessage ->
                if (item.type==0) {
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
            val scrollview:ScrollView = scrollViewLayout.parent as ScrollView
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
}