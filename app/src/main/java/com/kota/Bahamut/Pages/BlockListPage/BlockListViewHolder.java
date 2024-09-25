package com.kota.Bahamut.Pages.BlockListPage;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kota.Bahamut.R;

public class BlockListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView _name_label;
    public int index = 0;
    public BlockListClickListener mListener;

    public BlockListViewHolder(View view, BlockListClickListener listener) {
        super(view);

        _name_label = view.findViewById(R.id.BlockListItemView_Name);
        Button _delete_button = view.findViewById(R.id.BlockListItemView_Delete);
        _delete_button.setOnClickListener(view1 -> {
            if (mListener!=null)
                mListener.onBlockListPage_ItemView_delete_clicked(BlockListViewHolder.this);
        });

        this.mListener = listener;
        view.setOnClickListener(this);
    }

    public void setName(String aName) {
        if (_name_label != null) {
            _name_label.setText(aName);
            _name_label.setContentDescription("從名單中剔除"+aName);
        }
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onBlockListPage_ItemView_clicked(this);
        }
    }
}
