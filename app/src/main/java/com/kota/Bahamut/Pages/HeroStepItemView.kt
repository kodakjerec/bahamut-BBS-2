package com.kota.Bahamut.Pages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.HeroStep
import java.util.Objects

class HeroStepItemView : LinearLayout {
    private lateinit var txtViewAuthor: TextView
    private lateinit var txtViewDatetime: TextView
    private lateinit var txtViewContent: TextView

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.hero_step_item_view,
            this
        )
        txtViewAuthor = findViewById(R.id.HeroStep_ItemView_Name)
        txtViewDatetime = findViewById(R.id.HeroStep_ItemView_Datetime)
        txtViewContent = findViewById(R.id.HeroStep_ItemView_Content)
    }

    fun setAuthor(author: String?) {
        txtViewAuthor.text =
            Objects.requireNonNullElse(author, getContextString(R.string.loading_))
    }

    private fun setDatetime(dateTime: String?) {
        txtViewDatetime.text =
            Objects.requireNonNullElse(dateTime, getContextString(R.string.loading))
    }

    fun setContent(content: String?) {
        txtViewContent.text =
            Objects.requireNonNullElse(content, getContextString(R.string.loading))
    }

    fun setItem(aItem: HeroStep) {
        setAuthor(aItem.authorNickname)
        setDatetime(aItem.datetime)
        setContent(aItem.content)
    }
}
