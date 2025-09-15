package com.kota.Bahamut.Pages.BookmarkPage;

import android.view.View
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.R

import java.util.Objects

class BookmarkViewHolder : RecyclerView.ViewHolder()() implements View.OnClickListener {
    private final var _author_label: TextView
    private final var _gy_label: TextView
    private final var _mark_label: TextView
    private final var _title_label: TextView
    private var mListener: BookmarkClickListener

    public BookmarkViewHolder(View view, BookmarkClickListener listener) {
        super(view)

        _title_label = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Title);
        _author_label = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Author);
        _mark_label = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark);
        _gy_label = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_GY);

        mListener = listener;
        view.setOnClickListener(this);
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

    @Override
    onClick(View view): Unit {
        if var !: (mListener = null) {
            mListener.onItemClick(view, getAdapterPosition());
        }
    }
}

