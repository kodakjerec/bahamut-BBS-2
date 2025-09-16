package com.kota.Bahamut.pages.bookmarkPage

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R

class BookmarkViewHolder(view: View, private val mListener: BookmarkClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val _author_label: TextView?
    private val _gy_label: TextView?
    private val _mark_label: TextView
    private val _title_label: TextView?

    init {
        _title_label =
            view.findViewById<TextView?>(R.id.BoardExtendOptionalPage_bookmarkItemView_Title)
        _author_label =
            view.findViewById<TextView?>(R.id.BoardExtendOptionalPage_bookmarkItemView_Author)
        _mark_label =
            view.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark)
        _gy_label = view.findViewById<TextView?>(R.id.BoardExtendOptionalPage_bookmarkItemView_GY)

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
        if (this._title_label != null) {
            if (title == null || title.length == 0) {
                this._title_label.setText("未輸入")
            } else {
                this._title_label.setText(title)
            }
        }
    }

    fun setAuthor(author: String?) {
        if (this._author_label != null) {
            if (author == null || author.length == 0) {
                this._author_label.setText("未輸入")
            } else {
                this._author_label.setText(author)
            }
        }
    }

    fun setGYNumber(number: String?) {
        if (this._gy_label != null) {
            if (number == null || number.length == 0) {
                this._gy_label.setText(Bookmark.OPTIONAL_BOOKMARK)
            } else {
                this._gy_label.setText(number)
            }
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            this._mark_label.setVisibility(View.VISIBLE)
        } else {
            this._mark_label.setVisibility(View.INVISIBLE)
        }
    }

    fun clear() {
        setTitle(null)
        setAuthor(null)
        setGYNumber(null)
        setMark(false)
    }

    override fun onClick(view: View?) {
        if (mListener != null) {
            mListener.onItemClick(view, getAdapterPosition())
        }
    }
}