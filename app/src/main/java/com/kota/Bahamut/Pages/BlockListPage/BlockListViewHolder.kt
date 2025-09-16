package com.kota.Bahamut.Pages.BlockListPage

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R

class BlockListViewHolder(view: View, listener: BlockListClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val _name_label: TextView?
    var index: Int = 0
    var mListener: BlockListClickListener?

    init {
        _name_label = view.findViewById<TextView?>(R.id.BlockListItemView_Name)
        val _delete_button = view.findViewById<Button>(R.id.BlockListItemView_Delete)
        _delete_button.setOnClickListener(View.OnClickListener { view1: View? ->
            if (mListener != null) mListener!!.onBlockListPage_ItemView_delete_clicked(this@BlockListViewHolder)
        })

        this.mListener = listener
        view.setOnClickListener(this)
    }

    fun setName(aName: String?) {
        if (_name_label != null) {
            _name_label.setText(aName)
            _name_label.setContentDescription("從名單中剔除" + aName)
        }
    }

    override fun onClick(v: View?) {
        if (mListener != null) {
            mListener!!.onBlockListPage_ItemView_clicked(this)
        }
    }
}
