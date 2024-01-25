package com.kota.Bahamut.Pages;

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
            setMark(Objects.equals(bookmark.getMark(), "m"));
            setGYNumber(bookmark.getGy());
            return;
        }
        clear();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_extend_optional_page_bookmark_item_view, this);
        this._title_label = findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_Title);
        this._author_label = findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_Author);
        this._mark_label = findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_Mark);
        this._gy_label = findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_GY);
        this._divider_top = findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_DividerTop);
        this._divider_bottom = findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_DividerBottom);
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

    public void setAuthor(String author) {
        if (this._author_label != null) {
            if (author == null || author.length() == 0) {
                this._author_label.setText("未輸入");
            } else {
                this._author_label.setText(author);
            }
        }
    }

    public void setGYNumber(String number) {
        if (this._gy_label != null) {
            if (number == null || number.length() == 0) {
                this._gy_label.setText(Bookmark.OPTIONAL_BOOKMARK);
            } else {
                this._gy_label.setText(number);
            }
        }
    }

    public void setMark(boolean isMarked) {
        if (isMarked) {
            this._mark_label.setVisibility(View.VISIBLE);
        } else {
            this._mark_label.setVisibility(View.INVISIBLE);
        }
    }

    public void clear() {
        setTitle(null);
        setAuthor(null);
        setGYNumber(null);
        setMark(false);
    }
}
