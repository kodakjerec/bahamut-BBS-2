package com.kota.Bahamut.Pages.Article;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetArticleItemView;
import com.kota.TelnetUI.DividerView;

public class ArticlePage_TextItemView extends LinearLayout implements TelnetArticleItemView {
    TextView _author_label = null;
    TextView _content_label = null;
    ViewGroup _content_view = null;
    DividerView _divider_view = null;
    int _quote = 0;

    public ArticlePage_TextItemView(Context context) {
        super(context);
        init();
    }

    public ArticlePage_TextItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.article_page_text_item_view, this);
        this._author_label = (TextView) findViewById(R.id.ArticleTextItemView_Title);
        this._content_label = (TextView) findViewById(R.id.ArticleTextItemView_Content);
        this._divider_view = (DividerView) findViewById(R.id.ArticleTextItemView_DividerView);
        this._content_view = (ViewGroup) findViewById(R.id.ArticleTextItemView_ContentView);
        setBackgroundDrawable((Drawable) null);
    }

    public void setAuthor(String author, String nickname) {
        if (this._author_label != null) {
            StringBuffer author_buffer = new StringBuffer();
            if (author != null) {
                author_buffer.append(author);
            }
            if (nickname != null && nickname.length() > 0) {
                author_buffer.append("(" + nickname + ")");
            }
            author_buffer.append(" 說:");
            this._author_label.setText(author_buffer.toString());
        }
    }

    public void setContent(String content) {
        if (this._content_label != null) {
            this._content_label.setText(content);
        }
    }

    public void setQuote(int quote) {
        this._quote = quote;
        if (quote > 0) {
            this._author_label.setTextColor(-8323200);
            this._content_label.setTextColor(-14614752);
            return;
        }
        this._author_label.setTextColor(-1);
        this._content_label.setTextColor(-4144960);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    public int getType() {
        return 0;
    }

    public void setDividerhidden(boolean isHidden) {
        if (isHidden) {
            this._divider_view.setVisibility(8);
        } else {
            this._divider_view.setVisibility(0);
        }
    }

    public void setVisible(boolean visible) {
        if (visible) {
            this._content_view.setVisibility(0);
        } else {
            this._content_view.setVisibility(8);
        }
    }
}
