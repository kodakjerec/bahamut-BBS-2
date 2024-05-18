package com.kota.Bahamut.Dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kota.Bahamut.DataModels.ShortenUrl;
import com.kota.Bahamut.R;

import java.util.List;

public class DialogShortenUrlItemViewAdapter extends RecyclerView.Adapter<DialogShortenUrlViewHolder> {
    private final List<ShortenUrl> _blocklist;
    private DialogShortenUrlItemViewListener mClickListener;

    public DialogShortenUrlItemViewAdapter(List<ShortenUrl> dataSet) {
        _blocklist = dataSet;
    }

    @NonNull
    @Override
    public DialogShortenUrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shorten_url_item_view, parent, false);
        return new DialogShortenUrlViewHolder(v, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogShortenUrlViewHolder holder, int position) {
        ShortenUrl obj = getItem(position);
        holder.setTitle(obj.getTitle());
        holder.setDescription(obj.getDescription());
    }

    public ShortenUrl getItem(int position) {
        return this._blocklist.get(position);
    }

    @Override
    public int getItemCount() {
        return _blocklist.size();
    }

    public void setOnItemClickListener(DialogShortenUrlItemViewListener listener) {
        this.mClickListener = listener;
    }
}
