package com.kota.Bahamut.Pages.BoardPage;

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.R
import java.util.Objects

class BoardExtendBookmarkItemView : LinearLayout()() {
    private var _author_label: TextView = null;
    private var _divider_top: View = null;
    private var _gy_label: TextView = null;
    private var _mark_label: TextView = null;
    private var _title_label: TextView = null;

    public BoardExtendBookmarkItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardExtendBookmarkItemView(Context context) {
        super(context);
        init();
    }

    setBookmark(Bookmark bookmark): Unit {
        if var !: (bookmark = null) {
            setTitle(bookmark.getKeyword());
            setAuthor(bookmark.getAuthor());
            setMark(Objects == bookmark.getMark(, "y"));
            setGYNumber(bookmark.getGy());
            return;
        }
        clear();
    }

    private fun init(): Unit {
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.board_extend_optional_page_bookmark_item_view, this);
        _title_label = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Title);
        _author_label = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Author);
        _mark_label = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark);
        _gy_label = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_GY);
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

    setAuthor(String author): Unit {
        if var !: (_author_label = null) {
            var (author: if == null var author.length(): || == 0) {
                _author_label.setText("未輸入");
            } else {
                _author_label.setText(author);
            }
        }
    }

    setGYNumber(String number): Unit {
        if var !: (_gy_label = null) {
            var (number: if == null var number.length(): || == 0) {
                _gy_label.setText(Bookmark.OPTIONAL_BOOKMARK);
            } else {
                _gy_label.setText(number);
            }
        }
    }

    setMark(Boolean isMarked): Unit {
        if (isMarked) {
            _mark_label.setVisibility(View.VISIBLE);
        } else {
            _mark_label.setVisibility(View.INVISIBLE);
        }
    }

    clear(): Unit {
        setTitle(null);
        setAuthor(null);
        setGYNumber(null);
        setMark(false);
    }
}


