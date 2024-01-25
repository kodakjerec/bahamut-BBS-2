package com.kota.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.R;

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
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_extend_optional_page_history_item_view, this);
        this._title_label = findViewById(R.id.BoardExtendOptionalPage_HistoryItemView_Title);
        this._divider_top = findViewById(R.id.BoardExtendOptionalPage_HistoryItemView_DividerTop);
        this._divider_bottom = findViewById(R.id.BoardExtendOptionalPage_HistoryItemView_DividerBottom);
    }

    public void setDividerTopVisible(boolean visible) {
        if (this._divider_top == null) {
            return;
        }
        if (visible) {
            if (this._divider_top.getVisibility() != View.VISIBLE) {
                this._divider_top.setVisibility(View.VISIBLE);
            }
        } else if (this._divider_top.getVisibility() != View.GONE) {
            this._divider_top.setVisibility(View.GONE);
        }
    }

    public void setDividerBottomVisible(boolean visible) {
        if (this._divider_bottom == null) {
            return;
        }
        if (visible) {
            if (this._divider_bottom.getVisibility() != View.VISIBLE) {
                this._divider_bottom.setVisibility(View.VISIBLE);
            }
        } else if (this._divider_bottom.getVisibility() != View.GONE) {
            this._divider_bottom.setVisibility(View.GONE);
        }
    }

    public void setTitle(String title) {
        if (this._title_label != null) {
            if (title == null || title.length() == 0) {
                this._title_label.setText("未輸入");
            } else {
                this._title_label.setText(title);
            }
        }
    }

    public void clear() {
        setTitle(null);
    }
}
