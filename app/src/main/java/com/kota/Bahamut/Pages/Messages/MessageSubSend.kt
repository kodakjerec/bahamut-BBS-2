package com.kota.Bahamut.Pages.Messages

import android.content.Context
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.R
import java.text.SimpleDateFormat
import java.util.Date

class MessageSubSend(context: Context): RelativeLayout(context) {
    private var txtMessage: TextView
    private var txtSendDate: TextView
    init {
        inflate(context, R.layout.message_sub_send, this)
        txtMessage = findViewById(R.id.Message_Sub_Sender_Content)
        txtSendDate = findViewById(R.id.Message_Sub_Sender_Time)
    }

    fun setContent(fromObject: BahaMessage) {
        txtMessage.text = fromObject.message
        // 設定日期格式
        val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm")
        // 將時間戳轉換為 Date 物件
        val date = Date(fromObject.receivedDate)
        txtSendDate.text = sdf.format(date).substring(11)
        val wordWidth:Int = (txtSendDate.paint.measureText(txtSendDate.text.toString())*1.2).toInt()
        val screenWidth = context.resources.displayMetrics.widthPixels
        txtMessage.maxWidth = screenWidth - wordWidth
    }
}