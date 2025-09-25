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
    private var dividerTop: View? = null
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
            return
        }
        clear()
    }

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.board_extend_optional_page_history_item_view,
            this
        )
        titleLabel = findViewById(R.id.BoardExtendOptionalPage_historyItemView_Title)
        findViewById<View?>(R.id.BoardExtendOptionalPage_historyItemView_Status)!!.visibility = GONE
        findViewById<View?>(R.id.BoardExtendOptionalPage_historyItemView_SplitView)!!.visibility =
            GONE
        findViewById<View?>(R.id.BoardExtendOptionalPage_historyItemView_ArrowView)!!.visibility =
            GONE
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

    fun clear() {
        setTitle(null)
    }
}
