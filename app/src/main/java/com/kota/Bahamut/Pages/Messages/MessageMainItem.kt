package com.kota.Bahamut.Pages.Messages

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kota.Bahamut.R
import java.text.SimpleDateFormat
import java.util.Date

class MessageMainItem(context: Context): LinearLayout(context) {
    private var mainLayout: ConstraintLayout
    private var txtSenderName: TextView
    private var txtMessage: TextView
    private var txtReceivedDate: TextView
    private var txtUnReadCount: TextView
    init {
        inflate(context, R.layout.message_main_item, this)
        mainLayout = findViewById(R.id.content_view)
        txtSenderName = mainLayout.findViewById(R.id.mmiSenderName)
        txtMessage = mainLayout.findViewById(R.id.mmiMessage)
        txtReceivedDate = mainLayout.findViewById(R.id.mmiReceivedDate)
        txtUnReadCount = mainLayout.findViewById(R.id.mmiUnReadCount)
    }

    fun setContent(fromObject: BahaMessageSummarize) {
        txtSenderName.text = fromObject.senderName
        txtMessage.text = fromObject.message
        // 設定日期格式
        val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm")
        // 將時間戳轉換為 Date 物件
        val date = Date(fromObject.receivedDate)
        txtReceivedDate.text = sdf.format(date)
        txtUnReadCount.text = fromObject.unReadCount.toString()
        if (fromObject.unReadCount == 0) {
            txtUnReadCount.visibility = GONE
        } else {
            txtUnReadCount.visibility = VISIBLE
        }
    }
}