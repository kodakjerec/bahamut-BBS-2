package com.kota.Bahamut.Pages.ArticlePage;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.content.Context;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASListDialog;
import com.kota.ASFramework.Dialog.ASListDialogItemClickListener;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Messages.MessageSub;
import com.kota.Bahamut.Pages.PostArticlePage;
import com.kota.Bahamut.R;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetArticleItemView;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.TelnetUI.TelnetHeaderItemView;

import java.util.Objects;

public class ArticlePage_HeaderItemView extends TelnetHeaderItemView implements TelnetArticleItemView {
    TextView titleTextView;
    TextView detailTextView1;

    public ArticlePage_HeaderItemView(Context context) {
        super(context);

        titleTextView = this.findViewById(R.id.title);
        titleTextView.setOnClickListener(titleClickListener);
        titleTextView.setOnLongClickListener(titleLongClickListener);

        detailTextView1 = this.findViewById(R.id.detail_1);
        detailTextView1.setOnClickListener(authorClickListener);
    }

    public int getType() {
        return ArticlePageItemType.Header;
    }

    /** 點標題 */
    OnClickListener titleClickListener = view -> {
        if (titleTextView.getMaxLines()==1)
            titleTextView.setMaxLines(3);
        else
            titleTextView.setMaxLines(1);
    };

    /** 長按標題 */
    OnLongClickListener titleLongClickListener = view -> {

        return false;
    };

    /** 點作者 */
    OnClickListener authorClickListener = view -> {
        ASListDialog.createDialog()
            .setTitle(getContextString(R.string.dialog_query_hero_msg01))
            .addItem(getContextString(R.string.dialog_query_hero))
            .addItem(getContextString(R.string.message_sub_send_hero))
            .setListener(new ASListDialogItemClickListener() {
                public boolean onListDialogItemLongClicked(ASListDialog aDialog, int index, String aTitle) {
                    return true;
                }

                public void onListDialogItemClicked(ASListDialog aDialog, int index, String aTitle) {
                    if (Objects.equals(aTitle, getContextString(R.string.dialog_query_hero))) {
                        TelnetClient.getClient().sendDataToServer(
                                TelnetOutputBuilder.create()
                                        .pushKey(TelnetKeyboard.CTRL_Q)
                                        .build());
                    } else if (Objects.equals(aTitle, getContextString(R.string.message_sub_send_hero))) {
                        MessageSub aPage = PageContainer.getInstance().getMessageSub();
                        ASNavigationController.getCurrentController().pushViewController(aPage);
                        String authorId = detailTextView1.getText().toString();
                        if (authorId.contains("("))
                            authorId = authorId.substring(0, authorId.indexOf("("));
                        aPage.setSenderName(authorId);
                    }
                }
            }).show();
    };
}
