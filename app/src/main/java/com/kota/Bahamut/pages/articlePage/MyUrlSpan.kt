package com.kota.Bahamut.pages.articlePage

import android.content.Intent
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.net.toUri

class MyUrlSpan(var myUrl: String) : ClickableSpan() {
    override fun onClick(widget: View) {
        if (widget.context != null) {
            val intent = Intent(Intent.ACTION_VIEW, myUrl.toUri())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            widget.context.startActivity(intent)
        }
    }
}
