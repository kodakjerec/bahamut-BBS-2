package com.kota.Bahamut.pages.blockListPage

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R

class BlockListViewHolder(view: View, listener: BlockListClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val nameLabel: TextView? = view.findViewById(R.id.BlockListItemView_Name)
    var index: Int = 0
    var mListener: BlockListClickListener?

    init {
        val deleteButton = view.findViewById<Button>(R.id.BlockListItemView_Delete)
        deleteButton.setOnClickListener { view1: View? ->
            if (mListener != null) mListener?.onBlockListPageItemViewDeleteClicked(this@BlockListViewHolder)
        }

        this.mListener = listener
        view.setOnClickListener(this)
    }

    fun setName(aName: String?) {
        if (nameLabel != null) {
            nameLabel.text = aName
            nameLabel.contentDescription = "從名單中剔除$aName"
        }
    }

    override fun onClick(v: View?) {
        if (mListener != null) {
            mListener?.onBlockListPageItemViewClicked(this)
        }
    }
}
