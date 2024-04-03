package com.kota.Bahamut.Pages.BlockListPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kota.Bahamut.DataModels.Bookmark;
import com.kota.Bahamut.Pages.BookmarkPage.BookmarkClickListener;
import com.kota.Bahamut.Pages.BookmarkPage.HistoryViewHolder;
import com.kota.Bahamut.R;

import java.util.List;

public class BlockListAdapter extends RecyclerView.Adapter<BlockListViewHolder> {
    private final List<String> _blocklist;
    private BlockListClickListener mClickListener;

    public BlockListAdapter(List<String> dataSet) {
        _blocklist = dataSet;
    }

    @NonNull
    @Override
    public BlockListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.block_list_item_view, parent, false);
        return new BlockListViewHolder(v, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockListViewHolder holder, int position) {
        String _id = getItem(position);
        holder.setName(_id);
    }

    public String getItem(int position) {
        return this._blocklist.get(position);
    }

    @Override
    public int getItemCount() {
        return _blocklist.size();
    }

    public void setOnItemClickListener(BlockListClickListener listener) {
        this.mClickListener = listener;
    }
}
