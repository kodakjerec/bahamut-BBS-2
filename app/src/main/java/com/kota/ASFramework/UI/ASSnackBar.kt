package com.kota.ASFramework.UI

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
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.TempSettings

object ASSnackBar {
    private var previousSnackBar: Snackbar? = null
    
    /**
     * 產生 snackBar
     * @param largeMessage 想要粗體的訊息
     * @param normalMessage 訊息
     */
    fun show(largeMessage: String, normalMessage: String) {
        object : ASRunner() {
            override fun run() {
                previousSnackBar?.dismiss()
                
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
                    
                    // 傳送者:添加大字體 (24sp)
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
                
                val tempView = ASNavigationController.getCurrentController().topController.pageView
                previousSnackBar = Snackbar.make(tempView, spannableString, Snackbar.LENGTH_LONG)
                
                // 靠上對齊
                val view = previousSnackBar?.view
                view?.let { snackView ->
                    val params = snackView.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    TempSettings.myActivity?.let { activity ->
                        snackView.setBackgroundColor(
                            ContextCompat.getColor(activity, R.color.list_page_item_arrow_background)
                        )
                    }
                    snackView.layoutParams = params
                }
                
                previousSnackBar?.show()
            }
        }.runInMainThread()
    }
}
