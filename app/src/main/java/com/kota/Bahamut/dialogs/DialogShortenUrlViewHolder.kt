package com.kota.Bahamut.dialogs

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R

class DialogShortenUrlViewHolder(view: View, var mListener: DialogShortenUrlItemViewListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val title: TextView? = view.findViewById(R.id.shorten_url_title)
    private val description: TextView? = view.findViewById(R.id.shorten_url_description)
    var index: Int = 0

    init {

        view.setOnClickListener(this)
    }

    fun setTitle(aTitle: String?) {
        if (title != null) {
            title.text = aTitle
            title.contentDescription = "Url Title: $aTitle"
        }
    }

    fun setDescription(aDescription: String?) {
        if (description != null) {
            description.text = aDescription
            description.contentDescription = "Url Description: $aDescription"
        }
    }

    override fun onClick(v: View?) {
        if (mListener != null) {
            mListener?.onDialogShortenUrlItemViewClicked(this)
        }
    }
}
