package com.kota.Bahamut.Pages.ArticlePage

import android.content.Intent
import android.net.Uri
import android.text.style.ClickableSpan
import android.view.View

class myUrlSpan(var myUrl: String?) : ClickableSpan() {
    override fun onClick(widget: View) {
        if (widget.getContext() != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(myUrl))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            widget.getContext().startActivity(intent)
        }
    }
}
