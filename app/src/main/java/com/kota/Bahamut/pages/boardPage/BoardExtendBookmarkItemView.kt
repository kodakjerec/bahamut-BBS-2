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
    private var _author_label: TextView? = null
    private var _divider_top: View? = null
    private var _gy_label: TextView? = null
    private var _mark_label: TextView? = null
    private var _title_label: TextView? = null

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
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.board_extend_optional_page_bookmark_item_view,
            this
        )
        _title_label = findViewById<TextView?>(R.id.BoardExtendOptionalPage_bookmarkItemView_Title)
        _author_label =
            findViewById<TextView?>(R.id.BoardExtendOptionalPage_bookmarkItemView_Author)
        _mark_label = findViewById<TextView?>(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark)
        _gy_label = findViewById<TextView?>(R.id.BoardExtendOptionalPage_bookmarkItemView_GY)
        _divider_top = findViewById<View?>(R.id.BoardExtendOptionalPage_bookmarkItemView_DividerTop)
    }

    fun setDividerTopVisible(visible: Boolean) {
        if (_divider_top == null) {
            return
        }
        if (visible) {
            if (_divider_top!!.getVisibility() != VISIBLE) {
                _divider_top!!.setVisibility(VISIBLE)
            }
        } else if (_divider_top!!.getVisibility() != GONE) {
            _divider_top!!.setVisibility(GONE)
        }
    }

    fun setTitle(title: String?) {
        if (_title_label != null) {
            if (title == null || title.length == 0) {
                _title_label!!.setText("未輸入")
            } else {
                _title_label!!.setText(title)
            }
        }
    }

    fun setAuthor(author: String?) {
        if (_author_label != null) {
            if (author == null || author.length == 0) {
                _author_label!!.setText("未輸入")
            } else {
                _author_label!!.setText(author)
            }
        }
    }

    fun setGYNumber(number: String?) {
        if (_gy_label != null) {
            if (number == null || number.length == 0) {
                _gy_label!!.setText(Bookmark.OPTIONAL_BOOKMARK)
            } else {
                _gy_label!!.setText(number)
            }
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            _mark_label!!.setVisibility(VISIBLE)
        } else {
            _mark_label!!.setVisibility(INVISIBLE)
        }
    }

    fun clear() {
        setTitle(null)
        setAuthor(null)
        setGYNumber(null)
        setMark(false)
    }
}
