package com.kota.Bahamut.pages.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.R
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isNotEmpty
import com.kota.Bahamut.service.CommonFunctions.rgbToInt

class ThemeFunctions {
    private lateinit var theme:Theme

    fun layoutReplaceTheme(mainLayout: ViewGroup?) {
        if (mainLayout==null) {
            return
        }
        theme = ThemeStore.getSelectTheme()
        recursiveReplace(mainLayout)
    }

    // 遞迴替換ToolbarItem的顏色
    private fun recursiveReplace(mainLayout: ViewGroup) {
        for (i in 0 until  mainLayout.childCount) {
            val childView: View = mainLayout.getChildAt(i)
            // ToolbarItem
            if (childView.tag!==null && childView.tag.equals("ToolbarItem")) {
                // 避免共用bug, 每次獨立產生
                // 文字
                val colorStateList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_enabled), intArrayOf()), // States
                    intArrayOf(rgbToInt(theme.textColorPressed), rgbToInt(theme.textColor), rgbToInt(theme.textColorDisabled)) // Colors for each state
                )
                // 背景
                val backgroundDrawable = StateListDrawable()
                backgroundDrawable.addState(intArrayOf(android.R.attr.state_pressed),
                    rgbToInt(theme.backgroundColorPressed).toDrawable())
                backgroundDrawable.addState(intArrayOf(android.R.attr.state_enabled),
                    rgbToInt(theme.backgroundColor).toDrawable())
                backgroundDrawable.addState(intArrayOf(),
                    rgbToInt(theme.backgroundColorDisabled).toDrawable())

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
                || childView.javaClass==GridLayout::class.java
                || childView.javaClass==ConstraintLayout::class.java) {
                val tempChildView:ViewGroup = childView as ViewGroup
                if (tempChildView.isNotEmpty()) {
                    recursiveReplace(tempChildView)
                }
            }
        }
    }

    /**
     * 套用主題顏色到 TelnetHeaderItemView
     */
    fun applyThemeToHeaderItemView(headerView: View) {
        val theme = ThemeStore.getSelectTheme()
        val hBack = rgbToInt(theme.headerBackColor)
        val hTitle = rgbToInt(theme.headerHeaderColor)
        val hManager = rgbToInt(theme.headerManagerColor)
        val hBorder = rgbToInt(theme.headerBorderColor)

        // 1. 設定最外層(自定義View本身)的背景
        headerView.setBackgroundColor(hBack)

        // 2. 找到 XML 內的根容器 (id: header_item_view)
        val innerRoot = headerView.findViewById<ViewGroup>(R.id.header_item_view)
        innerRoot?.setBackgroundColor(hBack)

        // 3. 遍歷 innerRoot 的子元件 (即真正設定了 XML 背景色的 Level 2 元件)
        if (innerRoot != null) {
            for (i in 0 until innerRoot.childCount) {
                val child = innerRoot.getChildAt(i)
                // 排除分隔線，其餘元件（標題容器、選單按鈕）都要覆蓋背景色
                if (child.id != R.id.menu_divider) {
                    child.setBackgroundColor(hBack)
                }
            }
        }

        headerView.findViewById<TextView>(R.id.title)?.setTextColor(hTitle)
        headerView.findViewById<TextView>(R.id.detail_1)?.setTextColor(hManager)
        headerView.findViewById<TextView>(R.id.detail_2)?.setTextColor(hBorder)
        headerView.findViewById<TextView>(R.id.detail_vV)?.setTextColor(hBorder) // 支援 BoardHeaderView 特有的標記顏色
        headerView.findViewById<View>(R.id.menu_divider)?.setBackgroundColor(hBorder)
        headerView.findViewById<ImageButton>(R.id.menu_button)?.imageTintList = ColorStateList.valueOf(hTitle)
    }

    /**
     * 專門套用內文樣式
     * 有設定 backgroundColor 就套用 contentBackColor
     * 有設定 textColor 就套用 contentTextColor
     */
    fun applyThemeToContent(container: ViewGroup?) {
        if (container == null) return
        theme = ThemeStore.getSelectTheme()
        val cBack = rgbToInt(theme.contentBackColor)
        val cText = rgbToInt(theme.contentTextColor)

        // 設定根容器背景色 (僅當原本背景是黑色時才替換)
        val background = container.background
        if (background is ColorDrawable && background.color == Color.BLACK) {
            container.setBackgroundColor(cBack)
        }
        recursiveApplyContent(container, cBack, cText)
    }

    /**
     * 遞迴處理內文元件樣式
     */
    private fun recursiveApplyContent(viewGroup: ViewGroup, backColor: Int, textColor: Int) {
        for (i in 0 until viewGroup.childCount) {
            val childView: View = viewGroup.getChildAt(i)

            // 避開工具列元件 (ToolbarItem)
            if (childView.tag != null && childView.tag.equals("ToolbarItem")) {
                // 避免共用bug, 每次獨立產生
                // 文字
                val colorStateList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_enabled), intArrayOf()), // States
                    intArrayOf(rgbToInt(theme.textColorPressed), rgbToInt(theme.textColor), rgbToInt(theme.textColorDisabled)) // Colors for each state
                )
                // 背景
                val backgroundDrawable = StateListDrawable()
                backgroundDrawable.addState(intArrayOf(android.R.attr.state_pressed),
                    rgbToInt(theme.backgroundColorPressed).toDrawable())
                backgroundDrawable.addState(intArrayOf(android.R.attr.state_enabled),
                    rgbToInt(theme.backgroundColor).toDrawable())
                backgroundDrawable.addState(intArrayOf(),
                    rgbToInt(theme.backgroundColorDisabled).toDrawable())

                if (childView.javaClass == Button::class.java) {
                    val button = childView as Button
                    button.setTextColor(colorStateList)
                } else if (childView.javaClass == TextView::class.java) {
                    val textView = childView as TextView
                    textView.setTextColor(colorStateList)
                }
                childView.background = backgroundDrawable
            } else if (childView.javaClass == Button::class.java) {
                continue
            } else if (childView is TextView) {
                // 一般文字元件 (Label, CheckBox, RadioButton)
                // 只有原本設定為白色的文字才套用主題內文顏色 (-1 代表 Color.WHITE)
                if (childView.currentTextColor == -1) {
                    childView.setTextColor(textColor)

                    // 針對 CheckBox 和 RadioButton 額外處理勾選框顏色 (Tint)
                    if (childView is android.widget.CompoundButton) {
                        childView.buttonTintList = ColorStateList.valueOf(textColor)
                    }
                }
            } else if (childView is ViewGroup) {
                // 如果是容器, 遞迴處理
                // 僅當原本背景是黑色時才替換為主題內文背景色
                val background = childView.background
                if (background is ColorDrawable && background.color == Color.BLACK) {
                    childView.setBackgroundColor(backColor)
                }
                recursiveApplyContent(childView, backColor, textColor)
            }
        }
    }
}