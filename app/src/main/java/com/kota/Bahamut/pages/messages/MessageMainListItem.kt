package com.kota.Bahamut.pages.messages

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder
import com.kota.telnet.reference.TelnetKeyboard

class MessageMainListItem(context: Context): LinearLayout(context) {
    private var mainLayout: LinearLayout
    private var txtIndex: TextView
    private var txtSenderName: TextView
    private var txtNickname: TextView
    private var txtIp: TextView
    private var txtStatus: TextView
    private var scale = context.resources.displayMetrics.density

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = VERTICAL

        mainLayout = LinearLayout(context).apply {
            id = R.id.content_view
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            orientation = VERTICAL
        }

        val row = LinearLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setPadding((12 * scale).toInt(), (8 * scale).toInt(), (12 * scale).toInt(), (8 * scale).toInt())
            }
            orientation = HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        txtIndex = TextView(context).apply {
            id = R.id.mmiIndex
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            setTextColor(ContextCompat.getColor(context, R.color.article_page_text_item_content0))
            textSize = 16f
        }

        txtSenderName = TextView(context).apply {
            id = R.id.mmiSenderName
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
            setTextColor(ContextCompat.getColor(context, R.color.article_page_text_item_author0))
            textSize = 14f
        }

        txtNickname = TextView(context).apply {
            id = R.id.mmiNickName
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
            setTextColor(ContextCompat.getColor(context, R.color.article_page_text_item_content0))
            textSize = 14f
        }

        val rightCol = LinearLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f)
            orientation = VERTICAL
        }

        txtIp = TextView(context).apply {
            id = R.id.mmiIp
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setTextColor(ContextCompat.getColor(context, R.color.halfWhite))
            textSize = 12f
        }

        txtStatus = TextView(context).apply {
            id = R.id.mmiStatus
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setTextColor(ContextCompat.getColor(context, R.color.halfWhite))
            textSize = 12f
        }

        rightCol.addView(txtIp)
        rightCol.addView(txtStatus)

        row.addView(txtIndex)
        row.addView(txtSenderName)
        row.addView(txtNickname)
        row.addView(rightCol)

        val divider = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dividerColor))
        }

        mainLayout.addView(row)
        mainLayout.addView(divider)
        addView(mainLayout)
    }

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
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean = true

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    if (title == getContextString(R.string.dialog_query_hero)) {
                        TelnetClient.myInstance!!.sendDataToServer(
                            TelnetOutputBuilder.create()
                                .pushString(txtIndex.text.toString() + "\n")
                                .pushKey(TelnetKeyboard.CTRL_Q)
                                .build()
                        )
                    } else if (title == getContextString(R.string.message_sub_send_hero)) {
                        val aPage = PageContainer.instance!!.getMessageSub()
                        ASNavigationController.currentController?.pushViewController(aPage)
                        var authorId: String = txtSenderName.text.toString()
                        if (authorId.contains("(")) {
                            authorId = authorId.substring(0, authorId.indexOf("("))
                        }
                        aPage.setSenderName(authorId)
                    }
                }
            }).show()
    }
}