package com.kota.Bahamut.Pages.BlockListPage;

import android.view.View
import android.widget.Button
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.kota.Bahamut.R

class BlockListViewHolder : RecyclerView.ViewHolder()() implements View.OnClickListener {
    private final var _name_label: TextView
    var index: Int = 0;
    var mListener: BlockListClickListener

    public BlockListViewHolder(View view, BlockListClickListener listener) {
        super(view)

        _name_label = view.findViewById(R.id.BlockListItemView_Name);
        var _delete_button: Button = view.findViewById(R.id.BlockListItemView_Delete);
        _delete_button.setOnClickListener(view1 -> {
            var (mListener!: if =null)
                mListener.onBlockListPage_ItemView_delete_clicked(BlockListViewHolder.this);
        });

        mListener = listener;
        view.setOnClickListener(this);
    }

    setName(String aName): Unit {
        if var !: (_name_label = null) {
            _name_label.setText(aName);
            _name_label.setContentDescription("從名單中剔除"+aName);
        }
    }

    @Override
    onClick(View v): Unit {
        if var !: (mListener = null) {
            mListener.onBlockListPage_ItemView_clicked(this);
        }
    }
}


