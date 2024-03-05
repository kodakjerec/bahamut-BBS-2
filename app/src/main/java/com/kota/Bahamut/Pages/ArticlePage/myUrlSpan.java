package com.kota.Bahamut.Pages.ArticlePage;

import android.content.Intent;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.view.View;

public class myUrlSpan extends ClickableSpan {
    String myUrl;
    public myUrlSpan(String url) {
        myUrl = url;
    }

    @Override
    public void onClick(View widget) {
        if (widget.getContext() != null) {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(myUrl));
            widget.getContext().startActivity(intent);
        }
    }
}
