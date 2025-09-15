package com.kota.Bahamut.Dialogs

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R

class DialogShortenUrlViewHolder(
    view: View,
    var mListener: DialogShortenUrlItemViewListener?
) : RecyclerView.ViewHolder(view), View.OnClickListener {
    
    private val title: TextView = view.findViewById(R.id.thumbnail_title)
    private val description: TextView = view.findViewById(R.id.thumbnail_description)
    var index = 0

    init {
        view.setOnClickListener(this)
    }

    fun setTitle(aTitle: String) {
        title.text = aTitle
        title.contentDescription = "Url Title: $aTitle"
    }

    fun setDescription(aDescription: String) {
        description.text = aDescription
        description.contentDescription = "Url Description: $aDescription"
    }

    override fun onClick(v: View) {
        mListener?.onDialogShortenUrlItemViewClicked(this)
    }
}
