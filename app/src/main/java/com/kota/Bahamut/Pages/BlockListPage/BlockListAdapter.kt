package com.kota.Bahamut.Pages.BlockListPage;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

import com.kota.Bahamut.DataModels.Bookmark
import com.kota.Bahamut.Pages.BookmarkPage.BookmarkClickListener
import com.kota.Bahamut.Pages.BookmarkPage.HistoryViewHolder
import com.kota.Bahamut.R

import java.util.List

class BlockListAdapter : RecyclerView.Adapter<BlockListViewHolder>()() {
    private final List<String> _blocklist;
    private var mClickListener: BlockListClickListener

    public BlockListAdapter(List<String> dataSet) {
        _blocklist = dataSet;
    }

    @NonNull
    @Override
    onCreateViewHolder(@NonNull ViewGroup parent, Int viewType): BlockListViewHolder {
        var v: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.block_list_item_view, parent, false);
        return BlockListViewHolder(v, mClickListener);
    }

    @Override
    onBindViewHolder(@NonNull BlockListViewHolder holder, Int position): Unit {
        var _id: String = getItem(position);
        holder.setName(_id);
    }

    getItem(Int position): String {
        return _blocklist.get(position);
    }

    @Override
    getItemCount(): Int {
        return _blocklist.size();
    }

    setOnItemClickListener(BlockListClickListener listener): Unit {
        mClickListener = listener;
    }
}


