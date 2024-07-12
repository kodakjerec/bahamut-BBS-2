package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.kota.Bahamut.R;
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
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.article_page_telnet_item_view, this);
        this._telnet_view = findViewById(R.id.ArticlePage_TelnetItemView_TelnetView);
        this._divider_view = findViewById(R.id.ArticlePage_TelnetItemView_DividerView);
        setBackgroundResource(android.R.color.transparent);
    }

    public void setFrame(TelnetFrame aFrame) {
        this._telnet_view.setFrame(aFrame);
    }

    public int getType() {
        return ArticlePageItemType.Sign;
    }

    public void setDividerhidden(boolean isHidden) {
        if (isHidden) {
            this._divider_view.setVisibility(View.GONE);
        } else {
            this._divider_view.setVisibility(View.VISIBLE);
        }
    }
}
