package com.kota.Bahamut.Pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.bahamut_bbs_2.R;;

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
            setMark(bookmark.getMark() == "m");
            setGYNumber(bookmark.getGy());
            return;
        }
        clear();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.board_extend_optional_page_bookmark_item_view, this);
        this._title_label = (TextView) findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_Title);
        this._author_label = (TextView) findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_Author);
        this._mark_label = (TextView) findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_Mark);
        this._gy_label = (TextView) findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_GY);
        this._divider_top = findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_DividerTop);
        this._divider_bottom = findViewById(R.id.BoardExtendOptionalPage_BookmarkItemView_DividerBottom);
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

    public void setAuthor(String author) {
        if (this._author_label != null) {
            if (author == null || author.length() <= 0) {
                this._author_label.setText("未輸入");
            } else {
                this._author_label.setText(author);
            }
        }
    }

    public void setGYNumber(String number) {
        if (this._gy_label != null) {
            if (number == null || number.length() <= 0) {
                this._gy_label.setText(Bookmark.OPTIONAL_BOOKMARK);
            } else {
                this._gy_label.setText(number);
            }
        }
    }

    public void setMark(boolean isMarked) {
        if (isMarked) {
            this._mark_label.setVisibility(0);
        } else {
            this._mark_label.setVisibility(4);
        }
    }

    public void clear() {
        setTitle((String) null);
        setAuthor((String) null);
        setGYNumber((String) null);
        setMark(false);
    }
}
