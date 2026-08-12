package com.kota.Bahamut.pages.bookmarkPage

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.theme.ThemeFunctions

class BookmarkViewHolder(view: View, private val mListener: BookmarkClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val authorLabel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Author)
    private val gyLabel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_GY)
    private val markLevel: TextView = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark)
    private val titleLevel: TextView? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Title)
    private val btnEdit: Button? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Edit)
    private val btnDelete: Button? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Delete)
    private val buttonBlock: View? = view.findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_ButtonBlock)

    init {
        view.setOnClickListener(this)
        btnEdit?.setOnClickListener(this)
        btnDelete?.setOnClickListener(this)
        buttonBlock?.visibility = View.VISIBLE
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
            this.titleLevel.text = if (title.isNullOrEmpty()) "未輸入" else title
        }
    }

    fun setAuthor(author: String?) {
        if (this.authorLabel != null) {
            this.authorLabel.text = if (author.isNullOrEmpty()) "未輸入" else author
        }
    }

    fun setGYNumber(number: String?) {
        if (this.gyLabel != null) {
            this.gyLabel.text = if (number.isNullOrEmpty()) Bookmark.OPTIONAL_BOOKMARK else number
        }
    }

    fun setMark(isMarked: Boolean) {
        this.markLevel.visibility = if (isMarked) View.VISIBLE else View.INVISIBLE
    }

    fun clear() {
        setTitle(null)
        setAuthor(null)
        setGYNumber(null)
        setMark(false)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.BoardExtendOptionalPage_bookmarkItemView_Edit -> mListener?.onEditClick(view, bindingAdapterPosition)
            R.id.BoardExtendOptionalPage_bookmarkItemView_Delete -> mListener?.onDeleteClick(view, bindingAdapterPosition)
            else -> mListener?.onItemClick(view, bindingAdapterPosition)
        }
    }
}