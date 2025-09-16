package com.kota.Bahamut.pages.blockListPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R

class BlockListAdapter(private val _blocklist: MutableList<String?>) :
    RecyclerView.Adapter<BlockListViewHolder?>() {
    private var mClickListener: BlockListClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockListViewHolder {
        val v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.block_list_item_view, parent, false)
        return BlockListViewHolder(v, mClickListener)
    }

    override fun onBindViewHolder(holder: BlockListViewHolder, position: Int) {
        val _id = getItem(position)
        holder.setName(_id)
    }

    fun getItem(position: Int): String? {
        return this._blocklist.get(position)
    }

    override fun getItemCount(): Int {
        return _blocklist.size
    }

    fun setOnItemClickListener(listener: BlockListClickListener?) {
        this.mClickListener = listener
    }
}
