package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Telnet.TelnetArticleItemView

class ArticlePage_TimeTimeView : RelativeLayout()() implements TelnetArticleItemView {
    var timeLabel: TextView = null;
    var ipLabel: TextView = null;

    public ArticlePage_TimeTimeView(Context context) {
        super(context);
        init();
    }

    public ArticlePage_TimeTimeView(Context context, AttributeSet attrs, Int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ArticlePage_TimeTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    init(): Unit {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.article_page_time_item_view, this);
        timeLabel = findViewById(R.id.ArticleTimeItemView_Time);
        ipLabel = findViewById(R.id.ArticleTimeItemView_IP);
    }

    setTime(String aTime): Unit {
        if var !: (timeLabel = null) {
            timeLabel.setText(aTime);
        }
    }
    setIP(String aIP): Unit {
        if var !: (ipLabel = null) {
            ipLabel.setText(aIP);
        }
    }

    getType(): Int {
        return ArticlePageItemType.PostTime;
    }
}


