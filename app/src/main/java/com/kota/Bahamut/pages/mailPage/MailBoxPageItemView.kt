package com.kota.Bahamut.pages.mailPage

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.model.MailBoxPageItem
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.getThemeColor
import java.util.Objects

class MailBoxPageItemView : LinearLayout {
    var authorTextView: TextView? = null
    var dateTextView: TextView? = null
    var dividerBottom: View? = null
    var markTextView: TextView? = null
    var numberTextView: TextView? = null
    var replyTextView: TextView? = null
    var statusTextView: TextView? = null
    var titleTextView: TextView? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    fun setItem(aItem: MailBoxPageItem?) {
        if (aItem != null) {
            setTitle(aItem.title)
            setIndex(aItem.itemNumber)
            setDate(aItem.date)
            setAuthor(aItem.author)
            setReply(aItem.isReply)
            setRead(aItem.isRead)
            setMark(aItem.isMarked)
            return
        }
        clear()
    }

    fun init() {
        val density = context.resources.displayMetrics.density
        fun dp(value: Float): Int = (value * density + 0.5f).toInt()

        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val bgView = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            setBackgroundColor(getThemeColor(R.attr.bahamut_pageBackground))
            setPadding(dp(8f), dp(4f), dp(8f), dp(4f))
        }

        val titleRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        statusTextView = com.kota.telnetUI.textView.TelnetTextViewNormal(context).apply {
            id = R.id.MailBoxPage_ItemView_Status
            layoutParams = LayoutParams(dp(30f), LayoutParams.WRAP_CONTENT)
            setTextColor(getThemeColor(R.attr.bahamut_boardItemStatusColor))
        }
        titleRow.addView(statusTextView)

        titleTextView = com.kota.telnetUI.textView.TelnetTextViewNormal(context).apply {
            id = R.id.MailBoxPage_ItemView_Title
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            ellipsize = android.text.TextUtils.TruncateAt.END
            isSingleLine = true
            setTextColor(getThemeColor(R.attr.bahamut_boardItemNormalColor))
        }
        titleRow.addView(titleTextView)
        bgView.addView(titleRow)

        val metaRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        numberTextView = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.MailBoxPage_ItemView_Number
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setTextColor(getThemeColor(R.attr.bahamut_boardItemNumberColor))
        }
        metaRow.addView(numberTextView)

        markTextView = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.MailBoxPage_ItemView_mark
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(12f)
            }
            text = getContextString(R.string.word_m)
            setTextColor(getThemeColor(R.attr.bahamut_boardItemMarkColor))
        }
        metaRow.addView(markTextView)

        replyTextView = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.MailBoxPage_ItemView_Reply
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(12f)
            }
            text = getContextString(R.string.word_r)
            setTextColor(getThemeColor(R.attr.bahamut_mailItemReplyColor))
        }
        metaRow.addView(replyTextView)

        dateTextView = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.MailBoxPage_ItemView_Date
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(12f)
            }
            setTextColor(getThemeColor(R.attr.bahamut_boardItemDateColor))
        }
        metaRow.addView(dateTextView)

        authorTextView = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.MailBoxPage_ItemView_Author
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(12f)
            }
            gravity = android.view.Gravity.END
            isSingleLine = true
            setTextColor(getThemeColor(R.attr.bahamut_boardItemAuthorColor))
        }
        metaRow.addView(authorTextView)

        bgView.addView(metaRow)
        row.addView(bgView)

        val verticalDivider = View(context).apply {
            layoutParams = LayoutParams(dp(1f), LayoutParams.MATCH_PARENT)
            setBackgroundColor(getThemeColor(R.attr.bahamut_dividerColor))
        }
        row.addView(verticalDivider)

        val arrowView = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.ListItem_ArrowView
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            gravity = android.view.Gravity.CENTER
            setPadding(dp(6f), 0, dp(6f), 0)
            text = getContextString(R.string.arrow_right)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(getThemeColor(R.attr.bahamut_arrowColor))
            setBackgroundColor(getThemeColor(R.attr.bahamut_arrowBackground))
        }
        row.addView(arrowView)

        addView(row)

        dividerBottom = View(context).apply {
            id = R.id.MailBoxPage_ItemView_DividerBottom
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(1f))
            setBackgroundColor(getThemeColor(R.attr.bahamut_dividerColor))
        }
        addView(dividerBottom)
    }

    fun setTitle(title: String?) {
        if (titleTextView != null) {
            titleTextView?.text = Objects.requireNonNullElse<String?>(
                title,
                getContextString(R.string.loading_)
            )
        }
    }

    fun setAuthor(author: String?) {
        if (this@MailBoxPageItemView.authorTextView != null) {
            this@MailBoxPageItemView.authorTextView?.text = Objects.requireNonNullElse<String?>(
                author,
                getContextString(R.string.loading)
            )
        }
    }

    fun setDate(date: String?) {
        if (dateTextView != null) {
            dateTextView?.text = Objects.requireNonNullElse<String?>(
                date,
                getContextString(R.string.loading)
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun setIndex(number: Int) {
        if (numberTextView != null) {
            if (number > 0) {
                numberTextView?.text = String.format("%1$05d", number)
                return
            }
            numberTextView?.text = getContextString(R.string.loading)
        }
    }

    fun setReply(isReply: Boolean) {
        if (isReply) {
            replyTextView?.visibility = VISIBLE
        } else {
            replyTextView?.visibility = INVISIBLE
        }
    }

    fun setMark(isMarked: Boolean) {
        if (isMarked) {
            markTextView?.visibility = VISIBLE
        } else {
            markTextView?.visibility = INVISIBLE
        }
    }

    fun setRead(isRead: Boolean) {
        // 戰巴哈信件只要看到有沒有讀取
        if (isRead) {
            statusTextView?.text = "◇"
            titleTextView?.setTextColor(getThemeColor(R.attr.bahamut_boardItemNormalReadColor))
            return
        }
        statusTextView?.text = "◆"
        titleTextView?.setTextColor(getThemeColor(R.attr.bahamut_boardItemNormalColor))
    }

    fun clear() {
        setTitle(null)
        setDate(null)
        setAuthor(null)
        setIndex(0)
        setRead(true)
        setReply(false)
        setMark(false)
    }

    fun setDividerBottomVisible(visible: Boolean) {
        if (dividerBottom == null) {
            return
        }
        if (visible) {
            if (dividerBottom?.visibility != VISIBLE) {
                dividerBottom?.visibility = VISIBLE
            }
        } else if (dividerBottom?.visibility != GONE) {
            dividerBottom?.visibility = GONE
        }
    }
}
