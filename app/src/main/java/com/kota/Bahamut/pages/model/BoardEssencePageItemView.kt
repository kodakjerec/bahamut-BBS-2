package com.kota.Bahamut.pages.model

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions
import java.util.Objects

class BoardEssencePageItemView : LinearLayout {
    private var authorLabel: TextView? = null
    private var contentView: ViewGroup? = null
    private var dateLabel: TextView? = null
    private var dividerBottom: View? = null
    private var numberLabel: TextView? = null
    private var statusLabel: TextView? = null
    private var titleLabel: TextView? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    fun setItem(aItem: BoardEssencePageItem?) {
        if (aItem != null) {
            setTitle(aItem.title)
            setNumber(aItem.itemNumber)
            setDate(aItem.date)
            author = aItem.author
            setDirectory(aItem.isDirectory, aItem.isBBSClickable)
            return
        }
        clear()
    }

    private fun init() {
        val density = context.resources.displayMetrics.density
        fun dp(value: Float): Int = (value * density + 0.5f).toInt()

        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        val root = LinearLayout(context).apply {
            id = R.id.BoardPage_ItemView_contentView
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        contentView = root

        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val bgView = LinearLayout(context).apply {
            id = R.id.BoardPage_ItemView_backgroundView
            orientation = VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_pageBackground))
            setPadding(dp(8f), dp(4f), dp(8f), dp(4f))
        }

        val titleRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        statusLabel = com.kota.telnetUI.textView.TelnetTextViewNormal(context).apply {
            id = R.id.BoardPage_ItemView_Status
            layoutParams = LayoutParams(dp(30f), LayoutParams.WRAP_CONTENT)
            setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_boardItemStatusColor))
        }
        titleRow.addView(statusLabel)

        titleLabel = com.kota.telnetUI.textView.TelnetTextViewNormal(context).apply {
            id = R.id.BoardPage_ItemView_Title
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            ellipsize = android.text.TextUtils.TruncateAt.END
            setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_boardItemNormalColor))
        }
        titleRow.addView(titleLabel)
        bgView.addView(titleRow)

        val metaRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        numberLabel = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.BoardPage_ItemView_Number
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_boardItemNumberColor))
        }
        metaRow.addView(numberLabel)

        dateLabel = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.BoardPage_ItemView_Date
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(12f)
            }
            setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_boardItemDateColor))
        }
        metaRow.addView(dateLabel)

        authorLabel = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.BoardPage_ItemView_Author
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(12f)
            }
            gravity = android.view.Gravity.END
            isSingleLine = true
            setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_boardItemAuthorColor))
        }
        metaRow.addView(authorLabel)

        bgView.addView(metaRow)
        row.addView(bgView)

        val verticalDivider = View(context).apply {
            layoutParams = LayoutParams(dp(1f), LayoutParams.MATCH_PARENT)
            setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dividerColor))
        }
        row.addView(verticalDivider)

        val arrowView = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.ListItem_ArrowView
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            gravity = android.view.Gravity.CENTER
            setPadding(dp(6f), 0, dp(6f), 0)
            text = CommonFunctions.getContextString(R.string.arrow_right)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_arrowColor))
            setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_arrowBackground))
        }
        row.addView(arrowView)

        root.addView(row)

        dividerBottom = View(context).apply {
            id = R.id.BoardPage_ItemView_DividerBottom
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(1f))
            setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dividerColor))
        }
        root.addView(dividerBottom)

        addView(root)
    }

    fun setTitle(title: String?) {
        if (titleLabel != null) {
            titleLabel?.text = Objects.requireNonNullElse(
                title,
                CommonFunctions.getContextString(R.string.loading_)
            )
        }
    }

    var author: String?
        get() = authorLabel?.text as String
        set(author) {
            if (authorLabel != null) {
                authorLabel?.text = Objects.requireNonNullElse(
                    author,
                    CommonFunctions.getContextString(R.string.loading)
                )
            }
        }

    fun setDate(date: String?) {
        if (dateLabel != null) {
            dateLabel?.text = Objects.requireNonNullElse(
                date,
                CommonFunctions.getContextString(R.string.loading)
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun setNumber(number: Int) {
        if (numberLabel != null) {
            if (number > 0) {
                numberLabel?.text = String.format("%1$05d", number)
                return
            }
            numberLabel?.text = CommonFunctions.getContextString(R.string.loading)
        }
    }

    fun clear() {
        setTitle(null)
        setDate(null)
        author = null
        setNumber(0)
        setDirectory(isDirectory = false, isBBSClickable = false)
    }

    private fun setDirectory(isDirectory: Boolean, isBBSClickable: Boolean) {
        var statusText: String = if (isDirectory) {
            "◆"
        } else {
            "◇"
        }
        if (!isBBSClickable) {
            statusText += "("
            isClickable = true
        }

        statusLabel?.text = statusText
    }
}
