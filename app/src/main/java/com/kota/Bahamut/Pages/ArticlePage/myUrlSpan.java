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
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myUrl));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            widget.getContext().startActivity(intent);
        }
    }
}
