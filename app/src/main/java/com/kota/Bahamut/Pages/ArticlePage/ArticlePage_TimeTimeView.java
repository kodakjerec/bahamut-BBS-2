package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticleItemView;

public class ArticlePage_TimeTimeView extends RelativeLayout implements TelnetArticleItemView {
    TextView _time_label = null;

    public ArticlePage_TimeTimeView(Context context) {
        super(context);
        init();
    }

    public ArticlePage_TimeTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ArticlePage_TimeTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.article_page_time_item_view, this);
        this._time_label = (TextView) findViewById(R.id.ArticleTimeItemView_Time);
    }

    public void setTime(String aTime) {
        if (this._time_label != null) {
            this._time_label.setText(aTime);
        }
    }

    public int getType() {
        return 3;
    }
}
