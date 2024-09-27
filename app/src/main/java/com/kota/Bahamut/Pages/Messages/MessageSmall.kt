package com.kota.Bahamut.Pages.Messages

import android.widget.LinearLayout
import com.kota.Bahamut.R
import android.content.Context
import android.widget.TextView

class MessageSmall(context: Context): LinearLayout(context) {
    private var badgeView: TextView
    private var iconView: TextView

    init {
        inflate(context, R.layout.message_small, this)

        badgeView = findViewById(R.id.Message_Small_Badge)
        iconView = findViewById(R.id.Message_Small_Icon)
    }

    /** 更新Badge */
    fun updateBadge(aNumber: String) {
        if (aNumber.isNotEmpty() || aNumber!="0") {
            badgeView.text = aNumber
            badgeView.visibility = VISIBLE
        } else {
            badgeView.text = ""
            badgeView.visibility = GONE
        }
    }
}