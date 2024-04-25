package com.kota.Bahamut.Pages.BoardPage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.R;

public class BoardExtendHistoryItemView extends LinearLayout {
    private View _divider_top = null;
    private TextView _title_label = null;

    public BoardExtendHistoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardExtendHistoryItemView(Context context) {
        super(context);
        init();
    }

    public void setBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            setTitle(bookmark.getKeyword());
            return;
        }
        clear();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_extend_optional_page_history_item_view, this);
        _title_label = findViewById(R.id.BoardExtendOptionalPage_historyItemView_Title);
        findViewById(R.id.BoardExtendOptionalPage_historyItemView_Status).setVisibility(GONE);
        findViewById(R.id.BoardExtendOptionalPage_historyItemView_SplitView).setVisibility(GONE);
        findViewById(R.id.BoardExtendOptionalPage_historyItemView_ArrowView).setVisibility(GONE);
        _divider_top = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_DividerTop);
    }

    public void setDividerTopVisible(boolean visible) {
        if (_divider_top == null) {
            return;
        }
        if (visible) {
            if (_divider_top.getVisibility() != View.VISIBLE) {
                _divider_top.setVisibility(View.VISIBLE);
            }
        } else if (_divider_top.getVisibility() != View.GONE) {
            _divider_top.setVisibility(View.GONE);
        }
    }

    public void setTitle(String title) {
        if (_title_label != null) {
            if (title == null || title.length() == 0) {
                _title_label.setText("未輸入");
            } else {
                _title_label.setText(title);
            }
        }
    }

    public void clear() {
        setTitle(null);
    }
}
