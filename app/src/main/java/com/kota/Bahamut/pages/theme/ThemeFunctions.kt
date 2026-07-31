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
import com.kota.Bahamut.pages.articlePage.ArticlePageEditRecordItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTelnetItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTextItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTimeTimeView
import com.kota.Bahamut.service.CommonFunctions.rgbToInt
import com.kota.Bahamut.service.TempSettings

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

                    // 針對 CheckBox 處理勾選框顏色 (Tint)，但排除 RadioButton
                    if (childView is android.widget.CompoundButton && childView !is android.widget.RadioButton) {
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

    /**
     * 專門套用 HeaderItem 的主題 (跳過選單按鈕處理)
     */
    fun applyThemeToHeaderItemView(headerView: View) {
        val theme = ThemeStore.getSelectTheme()
        val hBack = rgbToInt(theme.headerBackColor)
        val hTitle = rgbToInt(theme.headerHeaderColor)
        val hManager = rgbToInt(theme.headerManagerColor)
        val hBorder = rgbToInt(theme.headerBorderColor)

        // 1. 設定最外層背景
        headerView.setBackgroundColor(hBack)

        // 2. 找到 XML 內的根容器
        val innerRoot = headerView.findViewById<ViewGroup>(R.id.header_item_view)
        innerRoot?.setBackgroundColor(hBack)

        // 3. 遍歷子元件設定背景
        if (innerRoot != null) {
            for (i in 0 until innerRoot.childCount) {
                val child = innerRoot.getChildAt(i)
                // 同時排除分隔線與選單按鈕，不強制覆蓋它們的背景色
                if (child.id != R.id.menu_divider && child.id != R.id.menu_button) {
                    child.setBackgroundColor(hBack)
                }
            }
        }

        // 4. 設定文字與分隔線顏色
        headerView.findViewById<TextView>(R.id.title)?.setTextColor(hTitle)
        headerView.findViewById<TextView>(R.id.detail_1)?.setTextColor(hManager)
        headerView.findViewById<TextView>(R.id.detail_2)?.setTextColor(hBorder)
        headerView.findViewById<TextView>(R.id.detail_vV)?.setTextColor(hBorder)
        headerView.findViewById<View>(R.id.menu_divider)?.setBackgroundColor(hBorder)

        // 註：已移除對 R.id.menu_button 的 imageTintList 處理
    }

    /**
     * 專門套用文章內文 Item 的主題
     */
    fun applyThemeToArticleTextItem(view: ArticlePageTextItemView) {
        theme = ThemeStore.getSelectTheme()
        val isQuote = view.myQuote > 0

        // 根據是否為引用選擇顏色
        val backColor = if (isQuote) rgbToInt(theme.quoteBackColor) else rgbToInt(theme.contentBackColor)
        val authorColor = if (isQuote) rgbToInt(theme.quoteAuthorColor) else rgbToInt(theme.contentAuthorColor)
        val textColor = if (isQuote) rgbToInt(theme.quoteTextColor) else rgbToInt(theme.contentTextColor)

        view.setBackgroundColor(backColor)
        view.authorLabel?.setTextColor(authorColor)
        view.contentLabel?.setTextColor(textColor)

        // 處理動態產生的預覽圖文字 (使用現有的遞迴工具)
        val container = view.contentView
        if (container is ViewGroup) {
            recursiveApplyContent(container, backColor, textColor)
        }
    }

    /**
     * 專門套用推文 Item 的主題
     */
    fun applyThemeToArticlePushItem(view: View) {
        theme = ThemeStore.getSelectTheme()
        val backColor = rgbToInt(theme.contentBackColor)
        val authorColor = rgbToInt(theme.contentAuthorColor)
        val textColor = rgbToInt(theme.contentTextColor)
        val detailColor = rgbToInt(theme.headerBorderColor) // 時間與樓層使用較暗的邊框色

        view.setBackgroundColor(backColor)
        view.findViewById<TextView>(R.id.ArticlePushItemView_Author)?.setTextColor(authorColor)
        view.findViewById<TextView>(R.id.ArticlePushItemView_Content)?.setTextColor(textColor)
        view.findViewById<TextView>(R.id.ArticlePushItemView_Datetime)?.setTextColor(detailColor)
        view.findViewById<TextView>(R.id.ArticlePushItemView_Floor)?.setTextColor(detailColor)

        // 處理推文內動態產生的連結預覽文字
        if (view is ViewGroup) {
            recursiveApplyContent(view, backColor, textColor)
        }
    }

    /**
     * 專門套用 Telnet (ANSI) Item 的主題 (例如簽名檔)
     */
    fun applyThemeToArticleTelnetItem(view: ArticlePageTelnetItemView) {
        theme = ThemeStore.getSelectTheme()
        val backColor = rgbToInt(theme.contentBackColor)
        view.setBackgroundColor(backColor)
        // TelnetView 內部的 ANSI 顏色由其 Frame 自行決定，這裡設定容器背景以確保一致性
    }

    /**
     * 專門套用發文時間 Item 的主題 (藍色橫條)
     */
    fun applyThemeToArticleTimeItem(view: ArticlePageTimeTimeView) {
        theme = ThemeStore.getSelectTheme()
        val backColor = rgbToInt(theme.headerBackColor)
        val textColor = rgbToInt(theme.headerBorderColor)

        view.setBackgroundColor(backColor)

        for (i in 0 until view.childCount) {
            view.getChildAt(i).setBackgroundColor(backColor)
        }
        view.timeLabel?.setTextColor(textColor)
        view.ipLabel?.setTextColor(textColor)
    }

    /**
     * 專門套用修改紀錄 Item 的主題
     */
    fun applyThemeToArticleEditRecordItem(view: ArticlePageEditRecordItemView) {
        theme = ThemeStore.getSelectTheme()
        val backColor = rgbToInt(theme.headerBackColor)
        val textColor = rgbToInt(theme.headerBorderColor) // 使用較淡的顏色

        view.setBackgroundColor(backColor)
        val itemViewContent = view.findViewById<TextView>(R.id.ArticleEditRecordItemView_Content)
        itemViewContent?.setTextColor(textColor)
    }

    /**
     * 專門套用看板列表 Item 的主題
     */
    fun applyThemeToBoardItem(view: View, isRead: Boolean, title: String, isReply: Boolean) {
        theme = ThemeStore.getSelectTheme()
        val lBack = rgbToInt(theme.listBackColor)

        // 1. 設定背景
        view.setBackgroundColor(lBack)
        view.findViewById<View>(R.id.BoardPage_ItemView_contentView)?.setBackgroundColor(lBack)

        // 2. 決定標題顏色
        val titleLabel = view.findViewById<TextView>(R.id.BoardPage_ItemView_Title)
        val isFollow = TempSettings.isBoardFollowTitle(title)

        val titleColor = when {
            // 關注的議題
            isFollow -> {
                if (isReply) {
                    // 回應文章 (Re)
                    if (isRead) rgbToInt(theme.listTitleFollowReadColor) else rgbToInt(theme.listTitleFollowColor)
                } else {
                    // 首篇文章 (◆)
                    if (isRead) rgbToInt(theme.listTitleFollowFirstReadColor) else rgbToInt(theme.listTitleFollowFirstColor)
                }
            }
            // 一般文章
            !isRead -> rgbToInt(theme.listTitleColor)
            else -> rgbToInt(theme.listTitleReadColor)
        }
        titleLabel?.setTextColor(titleColor)


        // 3. 設定其他欄位顏色
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Status)?.setTextColor(rgbToInt(theme.listStatusColor))
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Author)?.setTextColor(rgbToInt(theme.listAuthorColor))
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Number)?.setTextColor(rgbToInt(theme.listNumberColor))
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Date)?.setTextColor(rgbToInt(theme.listDateColor))
        view.findViewById<TextView>(R.id.BoardPage_ItemView_GY_Title)?.setTextColor(rgbToInt(theme.listNumberColor)) // GY 跟隨編號色
        view.findViewById<TextView>(R.id.BoardPage_ItemView_GY)?.setTextColor(rgbToInt(theme.listNumberColor))
        view.findViewById<TextView>(R.id.BoardPage_ItemView_mark)?.setTextColor(rgbToInt(theme.listMarkColor))

        // 分隔線
        view.findViewById<View>(R.id.BoardPage_ItemView_DividerBottom)?.setBackgroundColor(rgbToInt(theme.listDividerColor))
    }
}