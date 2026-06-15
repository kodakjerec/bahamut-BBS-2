package com.kota.Bahamut.pages.boardPage

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.telnetUI.TelnetHeaderItemView

class BoardHeaderView : TelnetHeaderItemView {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    override fun getLayoutId(): Int {
        return R.layout.board_header_view
    }

    /** 設定點擊功能 detail1  */
    override fun setDetail1ClickListener(aListener: OnClickListener?) {
        super.setDetail1ClickListener(aListener)
        if (aListener != null) {
            val detailVV = findViewById<TextView>(R.id.detail_vV)
            detailVV.visibility = VISIBLE
        }
    }
}
