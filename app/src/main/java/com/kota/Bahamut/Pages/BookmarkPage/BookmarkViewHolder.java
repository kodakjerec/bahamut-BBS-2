package com.kota.Bahamut.Pages.BookmarkPage;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.R;

import java.util.Objects;

public class BookmarkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView _author_label;
    private final TextView _gy_label;
    private final TextView _mark_label;
    private final TextView _title_label;
    private BookmarkClickListener mListener;

    public BookmarkViewHolder(View view, BookmarkClickListener listener) {
        super(view);

        _title_label = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Title);
        _author_label = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Author);
        _mark_label = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark);
        _gy_label = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_GY);

        this.mListener = listener;
        view.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            mListener.onItemClick(view, getAdapterPosition());
        }
    }
}