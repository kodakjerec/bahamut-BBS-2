package com.kota.Bahamut.Pages.Article;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.kota.bahamut_bbs_2.R;;
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.TelnetArticleItemView;
import com.kota.TelnetUI.DividerView;
import com.kota.TelnetUI.TelnetView;

public class ArticlePage_TelnetItemView extends LinearLayout implements TelnetArticleItemView {
    DividerView _divider_view = null;
    TelnetView _telnet_view = null;

    public ArticlePage_TelnetItemView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.article_page_telnet_item_view, this);
        this._telnet_view = (TelnetView) findViewById(R.id.ArticlePage_TelnetItemView_TelnetView);
        this._divider_view = (DividerView) findViewById(R.id.ArticlePage_TelnetItemView_DividerView);
        setBackgroundDrawable((Drawable) null);
    }

    public void setFrame(TelnetFrame aFrame) {
        this._telnet_view.setFrame(aFrame);
    }

    public int getType() {
        return 1;
    }

    public void setDividerhidden(boolean isHidden) {
        if (isHidden) {
            this._divider_view.setVisibility(8);
        } else {
            this._divider_view.setVisibility(0);
        }
    }
}
