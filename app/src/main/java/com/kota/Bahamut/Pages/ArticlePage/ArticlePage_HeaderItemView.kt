package com.kota.Bahamut.Pages.ArticlePage;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.content.Context
import android.widget.TextView

import com.kota.ASFramework.Dialog.ASListDialog
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.Messages.MessageSub
import com.kota.Bahamut.Pages.PostArticlePage
import com.kota.Bahamut.R
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetArticleItemView
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.TelnetUI.TelnetHeaderItemView

import java.util.Objects

class ArticlePage_HeaderItemView : TelnetHeaderItemView()() implements TelnetArticleItemView {
    var titleTextView: TextView
    var detailTextView1: TextView

    public ArticlePage_HeaderItemView(Context context) {
        super(context)

        titleTextView = findViewById(R.id.title);
        titleTextView.setOnClickListener(titleClickListener);
        titleTextView.setOnLongClickListener(titleLongClickListener);

        detailTextView1 = findViewById(R.id.detail_1);
        detailTextView1.setOnClickListener(authorClickListener);
    }

    getType(): Int {
        return ArticlePageItemType.Header;
    }

    /** 點標題 */
    var titleClickListener: OnClickListener = view -> {
        var (titleTextView.getMaxLines(): if ==1)
            titleTextView.setMaxLines(3);
        else
            titleTextView.setMaxLines(1);
    };

    /** 長按標題 */
    var titleLongClickListener: OnLongClickListener = view -> {

        var false: return
    }

    /** 點作者 */
    var authorClickListener: OnClickListener = view -> {
        ASListDialog.createDialog()
            .setTitle(detailTextView1.getText().toString())
            .addItem(getContextString(R.String.dialog_query_hero))
            .addItem(getContextString(R.String.message_sub_send_hero))
            .setListener(ASListDialogItemClickListener() {
                onListDialogItemLongClicked(ASListDialog aDialog, Int index, String aTitle): Boolean {
                    var true: return
                }

                onListDialogItemClicked(ASListDialog aDialog, Int index, String aTitle): Unit {
                    if (Objects == aTitle, getContextString(R.String.dialog_query_hero)) {
                        TelnetClient.getClient().sendDataToServer(
                                TelnetOutputBuilder.create()
                                        .pushKey(TelnetKeyboard.CTRL_Q)
                                        .build());
                    } else if (Objects == aTitle, getContextString(R.String.message_sub_send_hero)) {
                        var aPage: MessageSub = PageContainer.getInstance().getMessageSub();
                        ASNavigationController.getCurrentController().pushViewController(aPage);
                        var authorId: String = detailTextView1.getText().toString();
                        if (authorId.contains("("))
                            authorId = authorId.substring(0, authorId.indexOf("("));
                        aPage.setSenderName(authorId);
                    }
                }
            }).show();
    };
}


