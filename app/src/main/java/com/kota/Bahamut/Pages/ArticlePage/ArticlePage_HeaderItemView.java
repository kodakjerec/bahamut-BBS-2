package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Context;
import com.kota.Telnet.TelnetArticleItemView;
import com.kota.TelnetUI.TelnetHeaderItemView;

public class ArticlePage_HeaderItemView extends TelnetHeaderItemView implements TelnetArticleItemView {
    public ArticlePage_HeaderItemView(Context context) {
        super(context);
    }

    public int getType() {
        return 2;
    }
}
