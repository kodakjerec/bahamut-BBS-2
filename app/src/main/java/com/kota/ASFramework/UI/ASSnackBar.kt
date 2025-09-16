package com.kota.ASFramework.UI

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings

/* loaded from: classes.dex */
object ASSnackBar {
    private var previous_snack_bar: Snackbar? = null

    /**
     * 產生 snackBar
     * @param largeMessage 想要粗體的訊息
     * @param normalMessage 訊息
     */
    @JvmStatic
    fun show(largeMessage: String, normalMessage: String?) {
        object : ASRunner() {
            // from class: com.kota.ASFramework.UI.ASToast.1
            // com.kota.ASFramework.Thread.ASRunner
            override fun run() {
                if (previous_snack_bar != null) {
                    previous_snack_bar!!.dismiss()
                }
                val totalMessage = largeMessage + " " + normalMessage
                val spannableString = SpannableString(totalMessage)
                val boldStart = totalMessage.indexOf(largeMessage)
                val boldEnd = boldStart + largeMessage.length
                if (boldStart > -1) {
                    // 字體加粗
                    spannableString.setSpan(
                        StyleSpan(Typeface.BOLD),
                        boldStart,
                        boldEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // 傳送者:添加大字體 (20sp)
                    spannableString.setSpan(
                        AbsoluteSizeSpan(24, true),
                        boldStart,
                        boldEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    // 傳送者:黃色
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.YELLOW),
                        boldStart,
                        boldEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // 其他訊息:普通字體
                    spannableString.setSpan(
                        AbsoluteSizeSpan(20, true),
                        boldEnd,
                        totalMessage.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    // 其他訊息:白色
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.WHITE),
                        boldEnd,
                        totalMessage.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                val tempView: View =
                    ASNavigationController.getCurrentController().getTopController().getPageView()
                previous_snack_bar = Snackbar.make(tempView, spannableString, Snackbar.LENGTH_LONG)

                // 靠上對齊
                val view = previous_snack_bar!!.getView()
                val params = view.getLayoutParams() as FrameLayout.LayoutParams
                params.gravity = Gravity.TOP
                if (TempSettings.myActivity != null) view.setBackgroundColor(
                    ContextCompat.getColor(
                        TempSettings.myActivity!!,
                        R.color.list_page_item_arrow_background
                    )
                )
                view.setLayoutParams(params)

                previous_snack_bar!!.show()
            }
        }.runInMainThread()
    }
}