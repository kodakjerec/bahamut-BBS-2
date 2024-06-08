package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Context;
import android.widget.TextView;

import com.kota.Bahamut.R;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetArticleItemView;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.TelnetUI.TelnetHeaderItemView;

public class ArticlePage_HeaderItemView extends TelnetHeaderItemView implements TelnetArticleItemView {
    TextView titleTextView;
    TextView detailTextView1;

    public ArticlePage_HeaderItemView(Context context) {
        super(context);

        titleTextView = this.findViewById(R.id.title);
        titleTextView.setOnClickListener(titleClickListener);

        detailTextView1 = this.findViewById(R.id.detail_1);
        detailTextView1.setOnClickListener(authorClickListener);
    }

    public int getType() {
        return 2;
    }
    OnClickListener titleClickListener = view -> {
        if (titleTextView.getMaxLines()==1)
            titleTextView.setMaxLines(3);
        else
            titleTextView.setMaxLines(1);
    };

    OnClickListener authorClickListener = view -> {
        TelnetClient.getClient().sendDataToServer(
            TelnetOutputBuilder.create()
                    .pushKey(TelnetKeyboard.CTRL_Q)
                    .build());
    };
}
