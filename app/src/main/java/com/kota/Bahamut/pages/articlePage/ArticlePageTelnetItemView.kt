package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.kota.Bahamut.R
import com.kota.telnet.TelnetArticleItemView
import com.kota.telnet.model.TelnetFrame
import com.kota.telnetUI.DividerView
import com.kota.telnetUI.TelnetView

class ArticlePageTelnetItemView(context: Context?) : LinearLayout(context), TelnetArticleItemView {
    var dividerView: DividerView? = null
    var telnetView: TelnetView? = null

    init {
        init(context)
    }

    private fun init(context: Context?) {
        val density = context?.resources?.displayMetrics?.density ?: 1f
        fun dp(value: Float): Int = (value * density + 0.5f).toInt()

        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setBackgroundResource(android.R.color.transparent)

        telnetView = TelnetView(context).apply {
            id = R.id.ArticlePage_TelnetItemView_TelnetView
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(10f)
                bottomMargin = dp(10f)
            }
        }
        addView(telnetView)

        dividerView = DividerView(context).apply {
            id = R.id.ArticlePage_TelnetItemView_DividerView
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(1f))
        }
        addView(dividerView)
    }

    fun setFrame(aFrame: TelnetFrame) {
        telnetView?.frame = aFrame
    }

    override val type: Int
        get() = ArticlePageItemType.Companion.SIGN

    fun setDividerHidden(isHidden: Boolean) {
        if (isHidden) {
            dividerView?.visibility = GONE
        } else {
            dividerView?.visibility = VISIBLE
        }
    }
}
