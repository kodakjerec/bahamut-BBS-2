package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.kota.Bahamut.R
import com.kota.Telnet.Model.TelnetFrame
import com.kota.Telnet.TelnetArticleItemView
import com.kota.TelnetUI.DividerView
import com.kota.TelnetUI.TelnetView

class ArticlePage_TelnetItemView : LinearLayout()() implements TelnetArticleItemView {
    var dividerView: DividerView = null;
    var telnetView: TelnetView = null;

    public ArticlePage_TelnetItemView(Context context) {
        super(context);
        init(context);
    }

    private fun init(Context context): Unit {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.article_page_telnet_item_view, this);
        telnetView = findViewById(R.id.ArticlePage_TelnetItemView_TelnetView);
        dividerView = findViewById(R.id.ArticlePage_TelnetItemView_DividerView);
        setBackgroundResource(android.R.color.transparent);
    }

    setFrame(TelnetFrame aFrame): Unit {
        telnetView.setFrame(aFrame);
    }

    getType(): Int {
        return ArticlePageItemType.Sign;
    }

    setDividerHidden(Boolean isHidden): Unit {
        if (isHidden) {
            dividerView.setVisibility(View.GONE);
        } else {
            dividerView.setVisibility(View.VISIBLE);
        }
    }
}


