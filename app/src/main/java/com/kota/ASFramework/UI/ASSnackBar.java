package com.kota.ASFramework.UI;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;
import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public class ASSnackBar {
    private static Snackbar previous_snack_bar;
    /**
     * 產生 snackBar
     * @param fromView 來源view
     * @param aToastMessage 訊息
     * @param wantLargerMessage 想要粗體的訊息
     * */
    public static void show(View fromView, final String aToastMessage, final String wantLargerMessage) {
        new ASRunner() { // from class: com.kota.ASFramework.UI.ASToast.1
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                if (previous_snack_bar != null) {
                    previous_snack_bar.dismiss();
                }
                SpannableString spannableString = new SpannableString(aToastMessage);
                // 字體加粗
                int boldStart = aToastMessage.indexOf(wantLargerMessage);
                int boldEnd = boldStart + wantLargerMessage.length();
                if (boldStart>-1) {
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), boldStart, boldEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                previous_snack_bar = Snackbar.make(fromView, spannableString, Snackbar.LENGTH_LONG);

                // 靠上對齊
                View view = previous_snack_bar.getView();
                FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                params.gravity = Gravity.TOP;
                view.setLayoutParams(params);

                previous_snack_bar.show();
            }
        }.runInMainThread();
    }
}