package com.kota.Bahamut.pages.articlePage

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kota.Bahamut.R
import com.kota.telnet.TelnetArticleEditRecord

class ArticlePageEditRecordItemView(context: Context) : ConstraintLayout(context) {
    private var txtContent: TextView

    init {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.article_page_edit_record_item_view,
            this
        )
        txtContent = findViewById(R.id.ArticleEditRecordItemView_Content)
    }

    fun setContent(item: TelnetArticleEditRecord) {
        txtContent.text = item.rawString
    }
}
