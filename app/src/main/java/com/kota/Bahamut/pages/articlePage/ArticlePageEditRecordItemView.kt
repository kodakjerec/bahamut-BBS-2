package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kota.Bahamut.R
import com.kota.telnet.TelnetArticleEditRecord
import com.kota.telnetUI.textView.TelnetTextViewSmall

class ArticlePageEditRecordItemView(context: Context) : ConstraintLayout(context) {
    private var txtContent: TextView

    init {
        val density = context.resources.displayMetrics.density
        fun dp(value: Float): Int = (value * density + 0.5f).toInt()

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setBackgroundColor(Color.TRANSPARENT)
        setPadding(dp(8f), dp(4f), dp(8f), dp(4f))

        txtContent = TelnetTextViewSmall(context).apply {
            id = R.id.ArticleEditRecordItemView_Content
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                startToStart = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
            }
            setTextColor(com.kota.Bahamut.service.CommonFunctions.getThemeColor(R.attr.bahamut_articleContentColor1))
            setTextIsSelectable(false)
        }
        addView(txtContent)
    }

    fun setContent(item: TelnetArticleEditRecord) {
        txtContent.text = item.rawString
    }
}
