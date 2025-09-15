package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Intent
import android.net.Uri
import android.text.style.ClickableSpan
import android.view.View

class myUrlSpan : ClickableSpan()() {
    var myUrl: String
    public myUrlSpan(String url) {
        myUrl = url;
    }

    @Override
    onClick(View widget): Unit {
        if var !: (widget.getContext() = null) {
            var intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(myUrl));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            widget.getContext().startActivity(intent);
        }
    }
}


