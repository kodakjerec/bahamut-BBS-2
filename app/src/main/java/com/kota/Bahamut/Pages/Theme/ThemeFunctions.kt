package com.kota.Bahamut.Pages.Theme

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.Service.CommonFunctions.rgbToInt
import com.kota.TelnetUI.TextView.TelnetTextView
import com.kota.TelnetUI.TextView.TelnetTextViewLarge
import com.kota.TelnetUI.TextView.TelnetTextViewNormal
import com.kota.TelnetUI.TextView.TelnetTextViewSmall
import com.kota.TelnetUI.TextView.TelnetTextViewUltraLarge

class ThemeFunctions {
    private lateinit var theme:Theme

    fun layoutReplaceTheme(mainLayout: ViewGroup?) {
        if (mainLayout==null) {
            return
        }
        theme = ThemeStore.getSelectTheme()
        recursiveReplace(mainLayout)
    }

    private fun recursiveReplace(mainLayout: ViewGroup) {
        for (i in 0 until  mainLayout.childCount) {
            val childView: View = mainLayout.getChildAt(i)
            // ToolbarItem
            if (childView.tag!==null && childView.tag.equals("ToolbarItem")) {
                // 避免共用bug, 每次獨立產生
                // 文字
                val colorStateList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf()), // States
                    intArrayOf(rgbToInt(theme.textColorPressed), rgbToInt(theme.textColor)) // Colors for each state
                )
                // 背景
                val backgroundDrawable = StateListDrawable()
                backgroundDrawable.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(rgbToInt(theme.backgroundColorPressed)))
                backgroundDrawable.addState(intArrayOf(), ColorDrawable(rgbToInt(theme.backgroundColor)))

                if (childView.javaClass == Button::class.java) {
                    val button = childView as Button
                    button.setTextColor(colorStateList)
                } else if (childView.javaClass == TextView::class.java) {
                    val textView = childView as TextView
                    textView.setTextColor(colorStateList)
                }
                childView.background = backgroundDrawable
            }

            // 如果有子元件, 繼續往下
            if (childView.javaClass==LinearLayout::class.java
                || childView.javaClass==RelativeLayout::class.java
                || childView.javaClass==GridLayout::class.java) {
                val tempChildView:ViewGroup = childView as ViewGroup
                if (tempChildView.childCount>0) {
                    recursiveReplace(tempChildView)
                }
            }
        }
    }
}