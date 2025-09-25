package com.kota.Bahamut.pages.bookmarkPage

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R

class BookmarkViewHolder(view: View, private val mListener: BookmarkClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val authorLabel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Author)
    private val gyLabel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_GY)
    private val markLevel: TextView = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark)
    private val titleLevel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Title)

    init {

        view.setOnClickListener(this)
    }

    fun setBookmark(bookmark: Bookmark?) {
        if (bookmark != null) {
            setTitle(bookmark.keyword)
            setAuthor(bookmark.author)
            setMark(bookmark.mark == "y")
            setGYNumber(bookmark.gy)
            return
        }
        clear()
    }

    fun setTitle(title: String?) {
        if (this.titleLevel != null) {
            if (title == null || title.isEmpty()) {
                this.titleLevel.text = "未輸入"
            } else {
                this.titleLevel.text = title
            }
        }
    }

    fun setAuthor(author: String?) {
        if (this.authorLabel != null) {
            if (author == null || author.isEmpty()) {
                this.authorLabel.text = "未輸入"
            } else {
                this.authorLabel.text = author
            }
        }
    }

    fun setGYNumber(number: String?) {
        if (this.gyLabel != null) {
            if (number == null || number.isEmpty()) {
                this.gyLabel.text = Bookmark.OPTIONAL_BOOKMARK
            } else {
                this.gyLabel.text = number
            }
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            this.markLevel.visibility = View.VISIBLE
        } else {
            this.markLevel.visibility = View.INVISIBLE
        }
    }

    fun clear() {
        setTitle(null)
        setAuthor(null)
        setGYNumber(null)
        setMark(false)
    }

    override fun onClick(view: View?) {
        mListener?.onItemClick(view, bindingAdapterPosition)
    }
}