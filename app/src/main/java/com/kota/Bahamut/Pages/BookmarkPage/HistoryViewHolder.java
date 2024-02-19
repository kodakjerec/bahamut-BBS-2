package com.kota.Bahamut.Pages.BookmarkPage;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView _title_label;
    private BookmarkClickListener mListener;

    public HistoryViewHolder(View view, BookmarkClickListener listener) {
        super(view);

        _title_label = view.findViewById(R.id.BoardExtendOptionalPage_historyItemView_Title);

        this.mListener = listener;
        view.setOnClickListener(this);
    }

    public void setBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            setTitle(bookmark.getKeyword());
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

    public void clear() {
        setTitle(null);
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            mListener.onItemClick(view, getAdapterPosition());
        }
    }
}