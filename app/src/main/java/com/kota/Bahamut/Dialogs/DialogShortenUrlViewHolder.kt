package com.kota.Bahamut.Dialogs

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R

class DialogShortenUrlViewHolder(view: View, var mListener: DialogShortenUrlItemViewListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val title: TextView?
    private val description: TextView?
    var index: Int = 0

    init {
        title = view.findViewById<TextView?>(R.id.thumbnail_title)
        description = view.findViewById<TextView?>(R.id.thumbnail_description)

        view.setOnClickListener(this)
    }

    fun setTitle(aTitle: String?) {
        if (title != null) {
            title.setText(aTitle)
            title.setContentDescription("Url Title: " + aTitle)
        }
    }

    fun setDescription(aDescription: String?) {
        if (description != null) {
            description.setText(aDescription)
            description.setContentDescription("Url Description: " + aDescription)
        }
    }

    override fun onClick(v: View?) {
        if (mListener != null) {
            mListener!!.onDialogShortenUrlItemViewClicked(this)
        }
    }
}
