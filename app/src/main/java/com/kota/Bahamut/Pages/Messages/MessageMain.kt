package com.kota.Bahamut.Pages.Messages

import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.TelnetUI.TelnetPage
import java.util.Vector

class MessageMain:TelnetPage() {
    private lateinit var mainLayout: LinearLayout
    private lateinit var scrollViewLayout: LinearLayout
    private var lastViewPage: Int = 0 // 前端目前畫面
    override fun getPageLayout(): Int {
        return R.layout.message_main
    }

    /** 顯示聊天小視窗  */
    private val showHideFloating = CompoundButton.OnCheckedChangeListener {
        _: CompoundButton?, isChecked: Boolean ->
        NotificationSettings.setShowMessageFloating(isChecked)
        }

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as LinearLayout
        // 切換浮動隱藏
        val checkBox = mainLayout.findViewById<CheckBox>(R.id.Message_Main_Checkbox)
        checkBox.isChecked = NotificationSettings.getShowMessageFloating()
        checkBox.setOnCheckedChangeListener(showHideFloating)
        val checkLayout = mainLayout.findViewById<LinearLayout>(R.id.Message_Main_CheckboxLayout)
        checkLayout.setOnClickListener { _ -> checkBox.isChecked = !checkBox.isChecked }

        val txtEsc = mainLayout.findViewById<TextView>(R.id.Message_Main_Back)
        txtEsc.setOnClickListener{ _-> onBackPressed() }

        scrollViewLayout = mainLayout.findViewById(R.id.Message_Main_Scroll)

        // 指定目前畫面
        lastViewPage = BahamutStateHandler.getInstance().currentPage
        BahamutStateHandler.getInstance().currentPage = BahamutPage.BAHAMUT_MESSAGE_MAIN_PAGE

        loadMessageList()
    }

    override fun onBackPressed(): Boolean {
        // 還原目前畫面
        BahamutStateHandler.getInstance().currentPage = lastViewPage
        // 顯示小視窗
        if (TempSettings.getMessageSmall() != null)
            TempSettings.getMessageSmall()!!.show()

        return super.onBackPressed()
    }

    fun loadMessageList() {
        try {
            scrollViewLayout.removeAllViews()
            MessageDatabase(TempSettings.getMyContext()).use { db ->
                // 紀錄訊息
                val messageList = db.getAllAndNewestMessage()
                messageList.forEach { item:BahaMessageSummarize->
                    val messageMainItem = MessageMainItem(TempSettings.getMyContext()!!)
                    messageMainItem.setContent(item)
                    scrollViewLayout.addView(messageMainItem)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    /** 同步BBS訊息到DB */
    private fun sendSync() {
        // 清空資料庫
        MessageDatabase(context).use { db -> db.clearDb() }
        // 送出查詢指令
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.CTRL_R)
    }
    fun receiveSync(rows: Vector<TelnetRow>) {
        MessageDatabase(context).use { db ->
            rows.forEach { row->
                val rawString = row.rawString
                var senderName = ""
                var message = ""

                if (rawString.startsWith("☆")) {
                    // send
                    var startIndex = 1
                    var endIndex = rawString.indexOf("(")
                    senderName = rawString.substring(startIndex, endIndex).trim()

                    startIndex = rawString.indexOf("：")+1
                    message = rawString.substring(startIndex).trim()

                    db.receiveMessage(senderName, message, 1)
                } else if (rawString.startsWith("★")) {
                    // receive
                    var startIndex = 1
                    var endIndex = rawString.indexOf("(")
                    senderName = rawString.substring(startIndex, endIndex).trim()

                    startIndex = rawString.indexOf("：")+1
                    message = rawString.substring(startIndex).trim()

                    db.receiveMessage(senderName, message, 0)
                }
            }
            db.updateReceiveMessage()
        }
    }
}