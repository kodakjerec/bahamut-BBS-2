package com.kota.Bahamut.pages.bookmarkPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R

class BookmarkAdapter(private val _bookmarks: MutableList<Bookmark?>) :
    RecyclerView.Adapter<BookmarkViewHolder?>() {
    private var mClickListener: BookmarkClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.board_extend_optional_page_bookmark_item_view, parent, false)
        return BookmarkViewHolder(v, mClickListener)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmark = getItem(position)
        holder.setBookmark(bookmark)
    }

    fun getItem(position: Int): Bookmark? {
        return this._bookmarks[position]
    }

    override fun getItemCount(): Int {
        return _bookmarks.size
    }

    fun setOnItemClickListener(listener: BookmarkClickListener?) {
        this.mClickListener = listener
    }
}
