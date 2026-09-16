package com.kota.Bahamut.pages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.getThemeColor
import java.util.Objects

class ClassPageItemView : LinearLayout {
    private lateinit var boardManagerLabel: TextView
    private lateinit var boardNameLabel: TextView
    private lateinit var boardTitleLabel: TextView
    private lateinit var dividerBottom: View

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
        updateThemeColors()
    }

    fun setDividerBottomVisible(visible: Boolean) {
        if (visible) {
            if (this.dividerBottom.visibility != VISIBLE) {
                this.dividerBottom.visibility = VISIBLE
            }
        } else if (this.dividerBottom.visibility != GONE) {
            this.dividerBottom.visibility = GONE
        }
    }

    fun setBoardTitleText(title: String?) {
        this.boardTitleLabel.text = Objects.requireNonNullElse<String?>(
            title,
            getContextString(R.string.loading_)
        )
    }

    fun setBoardNameText(boardName: String?) {
        this.boardNameLabel.text = Objects.requireNonNullElse<String?>(
            boardName,
            getContextString(R.string.loading)
        )
    }

    fun setBoardManagerText(boardManager: String?) {
        this.boardManagerLabel.text = Objects.requireNonNullElse<String?>(
            boardManager,
            getContextString(R.string.loading)
        )
    }

    fun setItem(aItem: ClassPageItem?) {
        if (aItem != null) {
            setBoardTitleText(aItem.title)
            setBoardNameText(aItem.name)
            setBoardManagerText(aItem.manager)
            updateThemeColors()
            return
        }
        clear()
    }

    /** 動態更新各文字與背景色彩以符合當前主題與深淺色模式 */
    fun updateThemeColors() {
        this.boardTitleLabel.setTextColor(getThemeColor(R.attr.bahamut_defaultTextColor))
        this.boardNameLabel.setTextColor(getThemeColor(R.attr.bahamut_classItemNameColor))
        this.boardManagerLabel.setTextColor(getThemeColor(R.attr.bahamut_classItemManagerColor))

        val bgView = findViewById<View>(R.id.ClassPage_ItemView_backgroundView)
        if (bgView != null) {
            bgView.setBackgroundColor(getThemeColor(R.attr.bahamut_pageBackground))
        }

        val arrowView = findViewById<TextView>(R.id.ListItem_ArrowView)
        if (arrowView != null) {
            arrowView.setTextColor(getThemeColor(R.attr.bahamut_arrowColor))
            arrowView.setBackgroundColor(getThemeColor(R.attr.bahamut_arrowBackground))
        }
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
