package com.kota.Bahamut.pages.boardPage

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.dataModels.Bookmark
import com.kota.Bahamut.R

class BoardExtendHistoryItemView : LinearLayout {
    private var _divider_top: View? = null
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
            return
        }
        clear()
    }

    private fun init() {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.board_extend_optional_page_history_item_view,
            this
        )
        _title_label = findViewById<TextView?>(R.id.BoardExtendOptionalPage_historyItemView_Title)
        findViewById<View?>(R.id.BoardExtendOptionalPage_historyItemView_Status).setVisibility(GONE)
        findViewById<View?>(R.id.BoardExtendOptionalPage_historyItemView_SplitView).setVisibility(
            GONE
        )
        findViewById<View?>(R.id.BoardExtendOptionalPage_historyItemView_ArrowView).setVisibility(
            GONE
        )
        _divider_top = findViewById<View?>(R.id.BoardExtendOptionalPage_bookmarkItemView_DividerTop)
    }

    fun setDividerTopVisible(visible: Boolean) {
        if (_divider_top == null) {
            return
        }
        if (visible) {
            if (_divider_top?.getVisibility() != VISIBLE) {
                _divider_top?.setVisibility(VISIBLE)
            }
        } else if (_divider_top?.getVisibility() != GONE) {
            _divider_top?.setVisibility(GONE)
        }
    }

    fun setTitle(title: String?) {
        if (_title_label != null) {
            if (title == null || title.length == 0) {
                _title_label?.setText("未輸入")
            } else {
                _title_label?.setText(title)
            }
        }
    }

    fun clear() {
        setTitle(null)
    }
}
