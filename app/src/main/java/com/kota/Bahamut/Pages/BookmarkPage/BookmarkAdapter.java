package com.kota.Bahamut.Pages.BookmarkPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.R;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkViewHolder> {
    private final List<Bookmark> _bookmarks;
    private BookmarkClickListener mClickListener;

    public BookmarkAdapter(List<Bookmark> dataSet) {
        _bookmarks = dataSet;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.board_extend_optional_page_bookmark_item_view, parent, false);
        return new BookmarkViewHolder(v, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Bookmark bookmark = getItem(position);
        holder.setBookmark(bookmark);
    }

    public Bookmark getItem(int position) {
        return this._bookmarks.get(position);
    }

    @Override
    public int getItemCount() {
        return _bookmarks.size();
    }

    public void setOnItemClickListener(BookmarkClickListener listener) {
        this.mClickListener = listener;
    }
}
