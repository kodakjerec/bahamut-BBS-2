package com.kota.asFramework.ui

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.kota.Bahamut.R
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.pageController.ASPageView
import com.kota.asFramework.thread.ASCoroutine

object ASSnackBar {
    private var previousSnackBar: Snackbar? = null

    /**
     * 產生 snackBar
     * @param largeMessage 想要粗體的訊息
     * @param normalMessage 訊息
     */
    @JvmStatic
    fun show(largeMessage: String, normalMessage: String?) {
        ASCoroutine.ensureMainThread {
            if (previousSnackBar != null) {
                previousSnackBar?.dismiss()
            }
            val totalMessage = "$largeMessage $normalMessage"
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
            val tempView: ASPageView? =
                ASNavigationController.currentController?.topController?.pageView

            // 確保有有效的 View 才能顯示 Snackbar
            if (tempView != null) {
                previousSnackBar = Snackbar.make(tempView, spannableString, Snackbar.LENGTH_LONG)

                // 靠上對齊
                val view = previousSnackBar?.view
                val params = view!!.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.TOP
                if (TempSettings.myActivity != null) {
                    view.setBackgroundColor(
                        ContextCompat.getColor(
                            TempSettings.myActivity!!,
                            R.color.list_page_item_arrow_background
                        )
                    )
                }
                view.layoutParams = params

                previousSnackBar?.show()
            } else {
                return@ensureMainThread // 如果沒有有效的 View，就不顯示 Snackbar
            }
        }
    }
}