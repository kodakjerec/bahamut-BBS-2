package com.kota.Bahamut.pages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import java.util.Objects

class ClassPageItemView : LinearLayout {
    private var boardManagerLabel: TextView? = null
    private var boardNameLabel: TextView? = null
    private var boardTitleLabel: TextView? = null
    private var dividerBottom: View? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.class_page_item_view,
            this
        )
        this.boardTitleLabel = findViewById(R.id.ClassPage_ItemView_classTitle)
        this.boardNameLabel = findViewById(R.id.ClassPage_ItemView_className)
        this.boardManagerLabel = findViewById(R.id.ClassPage_ItemView_classManager)
        this.dividerBottom = findViewById(R.id.ClassPage_ItemView_DividerBottom)
    }

    fun setDividerBottomVisible(visible: Boolean) {
        if (this.dividerBottom == null) {
            return
        }
        if (visible) {
            if (this.dividerBottom?.visibility != VISIBLE) {
                this.dividerBottom?.visibility = VISIBLE
            }
        } else if (this.dividerBottom?.visibility != GONE) {
            this.dividerBottom?.visibility = GONE
        }
    }

    fun setBoardTitleText(title: String?) {
        if (this.boardTitleLabel == null) {
            return
        }
        this.boardTitleLabel?.text = Objects.requireNonNullElse<String?>(
            title,
            getContextString(R.string.loading_)
        )
    }

    fun setBoardNameText(boardName: String?) {
        if (this.boardNameLabel == null) {
            return
        }
        this.boardNameLabel?.text = Objects.requireNonNullElse<String?>(
            boardName,
            getContextString(R.string.loading)
        )
    }

    fun setBoardManagerText(boardManager: String?) {
        if (this.boardManagerLabel == null) {
            return
        }
        this.boardManagerLabel?.text = Objects.requireNonNullElse<String?>(
            boardManager,
            getContextString(R.string.loading)
        )
    }

    fun setItem(aItem: ClassPageItem?) {
        if (aItem != null) {
            setBoardTitleText(aItem.title)
            setBoardNameText(aItem.name)
            setBoardManagerText(aItem.manager)
            return
        }
        clear()
    }

    fun clear() {
        setBoardTitleText(null)
        setBoardNameText(null)
        setBoardManagerText(null)
    }

    companion object {
        private const val COUNT = 0
    }
}
