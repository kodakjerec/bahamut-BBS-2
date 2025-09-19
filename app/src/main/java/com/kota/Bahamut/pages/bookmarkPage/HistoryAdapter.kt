package com.kota.Bahamut.pages.bookmarkPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R

class HistoryAdapter(private val bookmarks: MutableList<Bookmark?>) :
    RecyclerView.Adapter<HistoryViewHolder?>() {
    private var mClickListener: BookmarkClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.board_extend_optional_page_history_item_view, parent, false)
        return HistoryViewHolder(v, mClickListener)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val bookmark = getItem(position)
        holder.setBookmark(bookmark)
    }

    fun getItem(position: Int): Bookmark? {
        return this.bookmarks[position]
    }

    override fun getItemCount(): Int {
        return bookmarks.size
    }

    fun setOnItemClickListener(listener: BookmarkClickListener?) {
        this.mClickListener = listener
    }
}
