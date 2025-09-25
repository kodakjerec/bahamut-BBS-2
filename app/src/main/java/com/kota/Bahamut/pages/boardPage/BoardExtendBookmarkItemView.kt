package com.kota.Bahamut.pages.boardPage

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R

class BoardExtendBookmarkItemView : LinearLayout {
    private var authorLabel: TextView? = null
    private var dividerTop: View? = null
    private var gyLabel: TextView? = null
    private var markLevel: TextView? = null
    private var titleLabel: TextView? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
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

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.board_extend_optional_page_bookmark_item_view,
            this
        )
        titleLabel = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Title)
        authorLabel =
            findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Author)
        markLevel = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark)
        gyLabel = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_GY)
        dividerTop = findViewById(R.id.BoardExtendOptionalPage_bookmarkItemView_DividerTop)
    }

    fun setDividerTopVisible(visible: Boolean) {
        if (dividerTop == null) {
            return
        }
        if (visible) {
            if (dividerTop?.visibility != VISIBLE) {
                dividerTop?.visibility = VISIBLE
            }
        } else if (dividerTop?.visibility != GONE) {
            dividerTop?.visibility = GONE
        }
    }

    fun setTitle(title: String?) {
        if (titleLabel != null) {
            if (title == null || title.isEmpty()) {
                titleLabel?.text = "未輸入"
            } else {
                titleLabel?.text = title
            }
        }
    }

    fun setAuthor(author: String?) {
        if (authorLabel != null) {
            if (author == null || author.isEmpty()) {
                authorLabel?.text = "未輸入"
            } else {
                authorLabel?.text = author
            }
        }
    }

    fun setGYNumber(number: String?) {
        if (gyLabel != null) {
            if (number == null || number.isEmpty()) {
                gyLabel?.text = Bookmark.OPTIONAL_BOOKMARK
            } else {
                gyLabel?.text = number
            }
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            markLevel?.visibility = VISIBLE
        } else {
            markLevel?.visibility = INVISIBLE
        }
    }

    fun clear() {
        setTitle(null)
        setAuthor(null)
        setGYNumber(null)
        setMark(false)
    }
}
