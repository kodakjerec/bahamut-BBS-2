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
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.article_page_telnet_item_view,
            this
        )
        telnetView = findViewById(R.id.ArticlePage_TelnetItemView_TelnetView)
        dividerView = findViewById(R.id.ArticlePage_TelnetItemView_DividerView)
        setBackgroundResource(android.R.color.transparent)
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
