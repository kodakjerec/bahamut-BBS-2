package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.telnet.TelnetArticleItemView

class ArticlePageTimeTimeView : RelativeLayout, TelnetArticleItemView {
    var timeLabel: TextView? = null
    var ipLabel: TextView? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {
        val density = context.resources.displayMetrics.density
        fun dp(value: Float): Int = (value * density + 0.5f).toInt()

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setBackgroundColor(com.kota.Bahamut.service.CommonFunctions.getThemeColor(R.attr.bahamut_titleBarBackground))
        setPadding(dp(8f), dp(4f), dp(8f), dp(4f))

        timeLabel = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.ArticleTimeItemView_Time
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(ALIGN_PARENT_END)
                addRule(CENTER_VERTICAL)
            }
        }
        addView(timeLabel)

        ipLabel = com.kota.telnetUI.textView.TelnetTextViewSmall(context).apply {
            id = R.id.ArticleTimeItemView_IP
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(ALIGN_PARENT_START)
                addRule(START_OF, R.id.ArticleTimeItemView_Time)
                marginEnd = dp(8f)
                addRule(CENTER_VERTICAL)
            }
            ellipsize = android.text.TextUtils.TruncateAt.END
            isSingleLine = true
            setTextIsSelectable(true)
        }
        addView(ipLabel)
    }

    fun setTime(aTime: String) {
        if (timeLabel != null) {
            timeLabel?.text = aTime
        }
    }

    fun setIP(aIP: String) {
        if (ipLabel != null) {
            ipLabel?.text = aIP
        }
    }

    override val type: Int
        get() = ArticlePageItemType.Companion.POST_TIME
}
