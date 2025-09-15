package com.kota.Bahamut.Pages.BookmarkPage;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.R

import java.util.List

class BookmarkAdapter : RecyclerView.Adapter<BookmarkViewHolder>()() {
    private final List<Bookmark> _bookmarks;
    private var mClickListener: BookmarkClickListener

    public BookmarkAdapter(List<Bookmark> dataSet) {
        _bookmarks = dataSet;
    }

    @NonNull
    @Override
    onCreateViewHolder(@NonNull ViewGroup parent, Int viewType): BookmarkViewHolder {
        var v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.board_extend_optional_page_bookmark_item_view, parent, false);
        return BookmarkViewHolder(v, mClickListener);
    }

    @Override
    onBindViewHolder(@NonNull BookmarkViewHolder holder, Int position): Unit {
        var bookmark: Bookmark = getItem(position);
        holder.setBookmark(bookmark);
    }

    getItem(Int position): Bookmark {
        return _bookmarks.get(position);
    }

    @Override
    getItemCount(): Int {
        return _bookmarks.size();
    }

    setOnItemClickListener(BookmarkClickListener listener): Unit {
        mClickListener = listener;
    }
}


