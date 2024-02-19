package com.kota.Bahamut.Pages.BookmarkPage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.R;
import java.util.Objects;

public class BoardExtendOptionalPageBookmarkItemView extends LinearLayout {
    private TextView _author_label = null;
    private View _divider_bottom = null;
    private View _divider_top = null;
    private TextView _gy_label = null;
    private TextView _mark_label = null;
    private TextView _title_label = null;

    public BoardExtendOptionalPageBookmarkItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardExtendOptionalPageBookmarkItemView(Context context) {
        super(context);
        init();
    }

    public void setBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            setTitle(bookmark.getKeyword());
            setAuthor(bookmark.getAuthor());
            setMark(Objects.equals(bookmark.getMark(), "y"));
            setGYNumber(bookmark.getGy());
            return;
        }
        clear();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_extend_optional_page_bookmark_item_view, this);
        _title_label = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Title);
        _author_label = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Author);
        _mark_label = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark);
        _gy_label = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_GY);
        _divider_top = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_DividerTop);
        _divider_bottom = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_DividerBottom);
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

    public void setDividerBottomVisible(boolean visible) {
        if (_divider_bottom == null) {
            return;
        }
        if (visible) {
            if (_divider_bottom.getVisibility() != View.VISIBLE) {
                _divider_bottom.setVisibility(View.VISIBLE);
            }
        } else if (_divider_bottom.getVisibility() != View.GONE) {
            _divider_bottom.setVisibility(View.GONE);
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

    public void setAuthor(String author) {
        if (_author_label != null) {
            if (author == null || author.length() == 0) {
                _author_label.setText("未輸入");
            } else {
                _author_label.setText(author);
            }
        }
    }

    public void setGYNumber(String number) {
        if (_gy_label != null) {
            if (number == null || number.length() == 0) {
                _gy_label.setText(Bookmark.OPTIONAL_BOOKMARK);
            } else {
                _gy_label.setText(number);
            }
        }
    }

    public void setMark(boolean isMarked) {
        if (isMarked) {
            _mark_label.setVisibility(View.VISIBLE);
        } else {
            _mark_label.setVisibility(View.INVISIBLE);
        }
    }

    public void clear() {
        setTitle(null);
        setAuthor(null);
        setGYNumber(null);
        setMark(false);
    }
}
