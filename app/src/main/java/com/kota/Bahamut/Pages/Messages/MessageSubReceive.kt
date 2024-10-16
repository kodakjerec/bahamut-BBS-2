package com.kota.Bahamut.Pages.Messages

import android.content.Context
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.R
import java.text.SimpleDateFormat
import java.util.Date

class MessageSubReceive(context: Context): RelativeLayout(context) {
    private var txtMessage: TextView
    private var txtReceivedDate: TextView
    init {
        inflate(context, R.layout.message_sub_receive, this)
        txtMessage = findViewById(R.id.Message_Sub_Receive_Content)
        txtReceivedDate = findViewById(R.id.Message_Sub_Receive_Time)
    }

    fun setContent(fromObject: BahaMessage) {
        txtMessage.text = fromObject.message
        // 設定日期格式
        val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm")
        // 將時間戳轉換為 Date 物件
        val date = Date(fromObject.receivedDate)
        txtReceivedDate.text = sdf.format(date).substring(11)
        val wordWidth:Int = (txtReceivedDate.paint.measureText(txtReceivedDate.text.toString())*1.2).toInt()
        val screenWidth = context.resources.displayMetrics.widthPixels
        txtMessage.maxWidth = screenWidth - wordWidth
    }
}