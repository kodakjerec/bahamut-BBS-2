package com.kota.Bahamut.Pages.BookmarkPage;

import android.view.View
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.R

class HistoryViewHolder : RecyclerView.ViewHolder()() implements View.OnClickListener {
    private final var _title_label: TextView
    private var mListener: BookmarkClickListener

    public HistoryViewHolder(View view, BookmarkClickListener listener) {
        super(view)

        _title_label = view.findViewById(R.id.BoardExtendOptionalPage_historyItemView_Title);

        mListener = listener;
        view.setOnClickListener(this);
    }

    setBookmark(Bookmark bookmark): Unit {
        if var !: (bookmark = null) {
            setTitle(bookmark.getKeyword());
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

    clear(): Unit {
        setTitle(null);
    }

    @Override
    onClick(View view): Unit {
        if var !: (mListener = null) {
            mListener.onItemClick(view, getAdapterPosition());
        }
    }
}

