package com.kota.ASFramework.UI;

import static com.kota.Bahamut.Service.TempSettings.getActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.R;
import com.kota.Telnet.Reference.TelnetAnsiCode;

import java.util.Objects;

/* loaded from: classes.dex */
public class ASSnackBar {
    private static Snackbar previous_snack_bar;
    /**
     * 產生 snackBar
     * @param largeMessage 想要粗體的訊息
     * @param normalMessage 訊息
     * */
    public static void show(final String largeMessage, final String normalMessage) {
        new ASRunner() { // from class: com.kota.ASFramework.UI.ASToast.1
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                if (previous_snack_bar != null) {
                    previous_snack_bar.dismiss();
                }
                String totalMessage = largeMessage+" "+normalMessage;
                SpannableString spannableString = new SpannableString(totalMessage);
                int boldStart = totalMessage.indexOf(largeMessage);
                int boldEnd = boldStart + largeMessage.length();
                if (boldStart>-1) {
                    // 字體加粗
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), boldStart, boldEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // 傳送者:添加大字體 (20sp)
                    spannableString.setSpan(new AbsoluteSizeSpan(24, true), boldStart, boldEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 傳送者:黃色
                    spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), boldStart, boldEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // 其他訊息:普通字體
                    spannableString.setSpan(new AbsoluteSizeSpan(20, true), boldEnd, totalMessage.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 其他訊息:白色
                    spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), boldEnd, totalMessage.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                View tempView = ASNavigationController.getCurrentController().getTopController().getPageView();
                previous_snack_bar = Snackbar.make(tempView, spannableString, Snackbar.LENGTH_LONG);

                // 靠上對齊
                View view = previous_snack_bar.getView();
                FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                params.gravity = Gravity.TOP;
                view.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.list_page_item_arrow_background));
                view.setLayoutParams(params);

                previous_snack_bar.show();
            }
        }.runInMainThread();
    }
}