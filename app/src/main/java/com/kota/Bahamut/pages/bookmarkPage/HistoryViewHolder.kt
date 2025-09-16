package com.kota.Bahamut.pages.bookmarkPage

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R

class HistoryViewHolder(view: View, private val mListener: BookmarkClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val _title_label: TextView?

    init {
        _title_label =
            view.findViewById<TextView?>(R.id.BoardExtendOptionalPage_historyItemView_Title)

        view.setOnClickListener(this)
    }

    fun setBookmark(bookmark: Bookmark?) {
        if (bookmark != null) {
            setTitle(bookmark.keyword)
            return
        }
        clear()
    }

    fun setTitle(title: String?) {
        if (this._title_label != null) {
            if (title == null || title.length == 0) {
                this._title_label.setText("未輸入")
            } else {
                this._title_label.setText(title)
            }
        }
    }

    fun clear() {
        setTitle(null)
    }

    override fun onClick(view: View?) {
        if (mListener != null) {
            mListener.onItemClick(view, getAdapterPosition())
        }
    }
}