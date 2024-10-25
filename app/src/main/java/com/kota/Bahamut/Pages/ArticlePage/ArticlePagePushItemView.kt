package com.kota.Bahamut.Pages.ArticlePage

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Telnet.TelnetArticlePush

class ArticlePagePushItemView(context: Context) : LinearLayout(context) {
    private var txtAuthor: TextView
    private var txtContent: TextView
    private var txtDatetime: TextView
    private var txtFloor: TextView

    init {
        txtAuthor = findViewById(R.id.ArticlePushItemView_Author)
        txtContent = findViewById(R.id.ArticlePushItemView_Content)
        txtDatetime = findViewById(R.id.ArticlePushItemView_Datetime)
        txtFloor = findViewById(R.id.ArticlePushItemView_Floor)
    }

    @SuppressLint("SetTextI18n")
    fun setContent(item: TelnetArticlePush) {
        txtAuthor.text = item.author
        txtContent.text = item.content
        txtDatetime.text = "$item.date $item.time"
    }

    @SuppressLint("SetTextI18n")
    fun setFloor(floor: Int) {
        txtFloor.text = "[$floor 樓]"
    }
}