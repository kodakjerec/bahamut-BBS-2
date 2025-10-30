package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.view.View
import android.widget.TextView
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.telnet.TelnetArticleItemView
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetHeaderItemView

class ArticlePageHeaderItemView(context: Context?) : TelnetHeaderItemView(context),
    TelnetArticleItemView {
    var titleTextView: TextView = this.findViewById(R.id.title)
    var detailTextView1: TextView

    override val type: Int
        get() = ArticlePageItemType.Companion.HEADER

    /** 點標題  */
    var titleClickListener: OnClickListener = OnClickListener { view: View? ->
        if (titleTextView.maxLines == 1) titleTextView.maxLines = 3
        else titleTextView.maxLines = 1
    }

    /** 長按標題  */
    var titleLongClickListener: OnLongClickListener = OnLongClickListener { view: View? -> false }

    /** 點作者  */
    var authorClickListener: OnClickListener = OnClickListener { view: View? ->
        ASListDialog.createDialog()
            .setTitle(detailTextView1.text.toString())
            .addItem(getContextString(R.string.dialog_query_hero))
            .addItem(getContextString(R.string.message_sub_send_hero))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ): Boolean {
                    return true
                }

                override fun onListDialogItemClicked(
                    paramASListDialog: ASListDialog?,
                    index: Int,
                    title: String?
                ) {
                    if (title == getContextString(R.string.dialog_query_hero)) {
                        TelnetClient.myInstance?.sendDataToServer(
                            create()
                                .pushKey(TelnetKeyboard.CTRL_Q)
                                .build()
                        )
                    } else if (title == getContextString(R.string.message_sub_send_hero)) {
                        val aPage = PageContainer.instance?.getMessageSub()
                        ASNavigationController.currentController!!.pushViewController(aPage)
                        var authorId = detailTextView1.text.toString()
                        if (authorId.contains("(")) authorId =
                            authorId.substring(0, authorId.indexOf("("))
                        aPage!!.setSenderName(authorId)
                    }
                }
            }).show()
    }

    init {
        titleTextView.setOnClickListener(titleClickListener)
        titleTextView.setOnLongClickListener(titleLongClickListener)

        detailTextView1 = this.findViewById(R.id.detail_1)
        detailTextView1.setOnClickListener(authorClickListener)
    }
}
