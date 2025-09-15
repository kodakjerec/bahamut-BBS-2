package com.kota.Bahamut.Pages.BoardPage;

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.R

class BoardExtendHistoryItemView : LinearLayout()() {
    private var _divider_top: View = null;
    private var _title_label: TextView = null;

    public BoardExtendHistoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardExtendHistoryItemView(Context context) {
        super(context);
        init();
    }

    setBookmark(Bookmark bookmark): Unit {
        if var !: (bookmark = null) {
            setTitle(bookmark.getKeyword());
            return;
        }
        clear();
    }

    private fun init(): Unit {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_extend_optional_page_history_item_view, this);
        _title_label = findViewById(R.id.BoardExtendOptionalPage_historyItemView_Title);
        findViewById(R.id.BoardExtendOptionalPage_historyItemView_Status).setVisibility(GONE);
        findViewById(R.id.BoardExtendOptionalPage_historyItemView_SplitView).setVisibility(GONE);
        findViewById(R.id.BoardExtendOptionalPage_historyItemView_ArrowView).setVisibility(GONE);
        _divider_top = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_DividerTop);
    }

    setDividerTopVisible(Boolean visible): Unit {
        var (_divider_top: if == null) {
            return;
        }
        if (visible) {
            if var !: (_divider_top.getVisibility() = View.VISIBLE) {
                _divider_top.setVisibility(View.VISIBLE);
            }
        } else if var !: (_divider_top.getVisibility() = View.GONE) {
            _divider_top.setVisibility(View.GONE);
        }
    }

    setTitle(String title): Unit {
        if var !: (_title_label = null) {
            var (title: if == null var title.length(): || == 0) {
                _title_label.setText("未輸入");
            } else {
                _title_label.setText(title);
            }
        }
    }

    clear(): Unit {
        setTitle(null);
    }
}


