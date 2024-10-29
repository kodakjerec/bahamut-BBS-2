package com.kota.Bahamut.Pages.Messages

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import java.text.SimpleDateFormat
import java.util.Date

class MessageMainListItem(context: Context): LinearLayout(context) {
    private var mainLayout: LinearLayout
    private var txtIndex: TextView
    private var txtSenderName: TextView
    private var txtNickname: TextView
    private var txtIp: TextView
    private var txtStatus: TextView
    init {
        inflate(context, R.layout.message_main_list_item, this)
        mainLayout = findViewById(R.id.content_view)
        txtIndex = mainLayout.findViewById(R.id.mmiIndex)
        txtSenderName = mainLayout.findViewById(R.id.mmiSenderName)
        txtNickname = mainLayout.findViewById(R.id.mmiNickName)
        txtIp = mainLayout.findViewById(R.id.mmiIp)
        txtStatus = mainLayout.findViewById(R.id.mmiStatus)
    }

    /** 設定內容 */
    fun setContent(fromObject: MessageMainListItemStructure) {
        txtIndex.text = fromObject.index.toString()
        txtSenderName.text = fromObject.senderName
        txtNickname.text = fromObject.nickname
        txtIp.text = fromObject.ip
        txtStatus.text = fromObject.status
        mainLayout.setOnClickListener(itemClickListener)
    }

    private val itemClickListener = OnClickListener { _->
        print("click")
    }
}