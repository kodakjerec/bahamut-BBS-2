package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.view.View
import android.widget.TextView
import com.kota.asFramework.dialog.ASListDialog
import com.kota.asFramework.dialog.ASListDialogItemClickListener
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASNavigationController.pushViewController
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetArticleItemView
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnetUI.TelnetHeaderItemView

class ArticlePage_HeaderItemView(context: Context?) : TelnetHeaderItemView(context),
    TelnetArticleItemView {
    var titleTextView: TextView
    var detailTextView1: TextView

    val type: Int
        get() = ArticlePageItemType.Companion.Header

    /** 點標題  */
    var titleClickListener: OnClickListener = OnClickListener { view: View? ->
        if (titleTextView.getMaxLines() == 1) titleTextView.setMaxLines(3)
        else titleTextView.setMaxLines(1)
    }

    /** 長按標題  */
    var titleLongClickListener: OnLongClickListener = OnLongClickListener { view: View? -> false }

    /** 點作者  */
    var authorClickListener: OnClickListener = OnClickListener { view: View? ->
        ASListDialog.createDialog()
            .setTitle(detailTextView1.getText().toString())
            .addItem(getContextString(R.string.dialog_query_hero))
            .addItem(getContextString(R.string.message_sub_send_hero))
            .setListener(object : ASListDialogItemClickListener {
                override fun onListDialogItemLongClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ): Boolean {
                    return@OnClickListener true
                }

                override fun onListDialogItemClicked(
                    aDialog: ASListDialog?,
                    index: Int,
                    aTitle: String?
                ) {
                    if (aTitle == getContextString(R.string.dialog_query_hero)) {
                        TelnetClient.getClient().sendDataToServer(
                            create()
                                .pushKey(TelnetKeyboard.CTRL_Q)
                                .build()
                        )
                    } else if (aTitle == getContextString(R.string.message_sub_send_hero)) {
                        val aPage = PageContainer.getInstance().getMessageSub()
                        ASNavigationController.getCurrentController().pushViewController(aPage)
                        var authorId = detailTextView1.getText().toString()
                        if (authorId.contains("(")) authorId =
                            authorId.substring(0, authorId.indexOf("("))
                        aPage.setSenderName(authorId)
                    }
                }
            }).show()
    }

    init {
        titleTextView = this.findViewById<TextView>(R.id.title)
        titleTextView.setOnClickListener(titleClickListener)
        titleTextView.setOnLongClickListener(titleLongClickListener)

        detailTextView1 = this.findViewById<TextView>(R.id.detail_1)
        detailTextView1.setOnClickListener(authorClickListener)
    }
}
