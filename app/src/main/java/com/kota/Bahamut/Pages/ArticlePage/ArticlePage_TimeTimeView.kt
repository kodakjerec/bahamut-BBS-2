package com.kota.Bahamut.Pages.ArticlePage

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Telnet.TelnetArticleItemView

class ArticlePage_TimeTimeView : RelativeLayout, TelnetArticleItemView {
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
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.article_page_time_item_view,
            this
        )
        timeLabel = findViewById<TextView?>(R.id.ArticleTimeItemView_Time)
        ipLabel = findViewById<TextView?>(R.id.ArticleTimeItemView_IP)
    }

    fun setTime(aTime: String?) {
        if (timeLabel != null) {
            timeLabel!!.setText(aTime)
        }
    }

    fun setIP(aIP: String?) {
        if (ipLabel != null) {
            ipLabel!!.setText(aIP)
        }
    }

    val type: Int
        get() = ArticlePageItemType.Companion.PostTime
}
