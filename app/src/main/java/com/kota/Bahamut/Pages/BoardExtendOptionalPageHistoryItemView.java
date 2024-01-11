package com.kota.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.R;;

public class BoardExtendOptionalPageHistoryItemView extends LinearLayout {
    private View _divider_bottom = null;
    private View _divider_top = null;
    private TextView _title_label = null;

    public BoardExtendOptionalPageHistoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardExtendOptionalPageHistoryItemView(Context context) {
        super(context);
        init();
    }

    public void setBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            setTitle(bookmark.getKeyword());
        } else {
            clear();
        }
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.board_extend_optional_page_history_item_view, this);
        this._title_label = (TextView) findViewById(R.id.BoardExtendOptionalPage_HistoryItemView_Title);
        this._divider_top = findViewById(R.id.BoardExtendOptionalPage_HistoryItemView_DividerTop);
        this._divider_bottom = findViewById(R.id.BoardExtendOptionalPage_HistoryItemView_DividerBottom);
    }

    public void setDividerTopVisible(boolean visible) {
        if (this._divider_top == null) {
            return;
        }
        if (visible) {
            if (this._divider_top.getVisibility() != 0) {
                this._divider_top.setVisibility(0);
            }
        } else if (this._divider_top.getVisibility() != 8) {
            this._divider_top.setVisibility(8);
        }
    }

    public void setDividerBottomVisible(boolean visible) {
        if (this._divider_bottom == null) {
            return;
        }
        if (visible) {
            if (this._divider_bottom.getVisibility() != 0) {
                this._divider_bottom.setVisibility(0);
            }
        } else if (this._divider_bottom.getVisibility() != 8) {
            this._divider_bottom.setVisibility(8);
        }
    }

    public void setTitle(String title) {
        if (this._title_label != null) {
            if (title == null || title.length() <= 0) {
                this._title_label.setText("未輸入");
            } else {
                this._title_label.setText(title);
            }
        }
    }

    public void clear() {
        setTitle((String) null);
    }
}
