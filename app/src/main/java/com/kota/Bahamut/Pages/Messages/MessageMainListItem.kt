package com.kota.Bahamut.Pages.Messages

import android.content.Context
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.ASFramework.Dialog.ASListDialog
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder

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
        txtIndex.text = fromObject.index
        txtSenderName.text = fromObject.senderName
        txtNickname.text = fromObject.nickname
        txtIp.text = fromObject.ip
        txtStatus.text = fromObject.status
        mainLayout.setOnClickListener(itemClickListener)
    }

    private val itemClickListener = OnClickListener { _ ->
        ASListDialog.createDialog()
            .setTitle(txtSenderName.text.toString())
            .addItem(getContextString(R.string.dialog_query_hero))
            .addItem(getContextString(R.string.message_sub_send_hero))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    aDialog: ASListDialog,
                    index: Int,
                    aTitle: String
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    aDialog: ASListDialog,
                    index: Int,
                    aTitle: String
                ) {
                    if (aTitle == getContextString(R.string.dialog_query_hero)) {
                        TelnetClient.getClient().sendDataToServer(
                            TelnetOutputBuilder.create()
                                .pushString(txtIndex.text.toString()+"\n")
                                .pushKey(TelnetKeyboard.CTRL_Q)
                                .build()
                        )
                    } else if (aTitle == getContextString(R.string.message_sub_send_hero)) {
                        val aPage = PageContainer.getInstance().messageSub
                        ASNavigationController.getCurrentController().pushViewController(aPage)
                        var authorId: String = txtSenderName.text.toString()
                        if (authorId.contains("(")) authorId =
                            authorId.substring(0, authorId.indexOf("("))
                        aPage.setSenderName(authorId)
                    }
                }
            }).show()
    }
}