package com.kota.Bahamut.pages.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.articlePage.ArticlePageEditRecordItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTelnetItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTextItemView
import com.kota.Bahamut.pages.articlePage.ArticlePageTimeTimeView
import com.kota.Bahamut.service.CommonFunctions.rgbToInt
import com.kota.Bahamut.service.TempSettings
import com.kota.telnetUI.TelnetHeaderItemView
import androidx.core.view.isNotEmpty

class ThemeFunctions {
    private lateinit var theme:Theme

    /**
     * 專門套用內文樣式
     * 使用者要求只改按鈕顏色，因此這裡只處理容器內的按鈕
     */
    fun applyThemeToContent(container: ViewGroup?) {
        if (container == null) return
        theme = ThemeStore.getSelectTheme()
        // 遞迴處理子元件，僅處理按鈕
        recursiveApplyContent(container)
    }

    /**
     * 遞迴處理元件樣式，僅針對按鈕進行套用
     */
    private fun recursiveApplyContent(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val childView: View = viewGroup.getChildAt(i)

            if (childView.tag != null) {
                val tagStr = childView.tag.toString()
                if (tagStr == "normalButton" || tagStr == "ToolbarItem" || tagStr == "ToolbarItem.Danger") {
                    applyButtonStyle(childView, tagStr == "ToolbarItem.Danger")
                    continue
                }
            }

            if (childView is Button) {
                applyButtonStyle(childView, false)
            } else if (childView is ViewGroup) {
                recursiveApplyContent(childView)
            }
        }
    }

    /** 套用按鈕樣式 */
    fun applyButtonStyle(view: View, isDanger: Boolean) {
        theme = ThemeStore.getSelectTheme()
        // 文字顏色狀態
        val colorStateList = if (isDanger) {
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_enabled), intArrayOf()),
                intArrayOf(rgbToInt(theme.textColorDangerPressed), rgbToInt(theme.textColorDanger), rgbToInt(theme.textColorDangerDisabled))
            )
        } else {
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_enabled), intArrayOf()),
                intArrayOf(rgbToInt(theme.textColorPressed), rgbToInt(theme.textColor), rgbToInt(theme.textColorDisabled))
            )
        }

        // 背景圖案狀態
        val backgroundDrawable = StateListDrawable()
        if (isDanger) {
            backgroundDrawable.addState(intArrayOf(android.R.attr.state_pressed), rgbToInt(theme.backgroundColorDangerPressed).toDrawable())
            backgroundDrawable.addState(intArrayOf(android.R.attr.state_enabled), rgbToInt(theme.backgroundColorDanger).toDrawable())
            backgroundDrawable.addState(intArrayOf(), rgbToInt(theme.backgroundColorDangerDisabled).toDrawable())
        } else {
            backgroundDrawable.addState(intArrayOf(android.R.attr.state_pressed), rgbToInt(theme.backgroundColorPressed).toDrawable())
            backgroundDrawable.addState(intArrayOf(android.R.attr.state_enabled), rgbToInt(theme.backgroundColor).toDrawable())
            backgroundDrawable.addState(intArrayOf(), rgbToInt(theme.backgroundColorDisabled).toDrawable())
        }

        if (view is Button) {
            view.setTextColor(colorStateList)
        } else if (view is TextView) {
            view.setTextColor(colorStateList)
        } else if (view is ViewGroup) {
            for (j in 0 until view.childCount) {
                val subView = view.getChildAt(j)
                if (subView is TextView) {
                    subView.setTextColor(colorStateList)
                }
            }
        }
        view.background = backgroundDrawable
    }

    /**
     * 專門套用 HeaderItem 的主題 (僅處理其中的按鈕)
     */
    fun applyThemeToHeaderItemView(headerView: View) {
        // 使用者要求不改標題列顏色
        if (headerView is ViewGroup) {
            recursiveApplyContent(headerView)
        }
    }

    /**
     * 專門套用文章內文 Item 的主題
     */
    fun applyThemeToArticleTextItem(view: ArticlePageTextItemView) {
        // 使用者要求不改內文顏色
        val container = view.contentView
        if (container is ViewGroup) {
            recursiveApplyContent(container)
        }
    }

    /**
     * 專門套用推文 Item 的主題
     */
    fun applyThemeToArticlePushItem(view: View) {
        // 使用者要求不改推文顏色
        if (view is ViewGroup) {
            recursiveApplyContent(view)
        }
    }

    /**
     * 專門套用 Telnet (ANSI) Item 的主題
     */
    fun applyThemeToArticleTelnetItem(view: ArticlePageTelnetItemView) {
        // 不處理
    }

    /**
     * 專門套用發表時間 Item 的主題
     */
    fun applyThemeToArticleTimeItem(view: ArticlePageTimeTimeView) {
        // 不處理
    }

    /**
     * 專門套用修改紀錄 Item 的主題
     */
    fun applyThemeToArticleEditRecordItem(view: ArticlePageEditRecordItemView) {
        // 不處理
    }

    /**
     * 專門套用看板列表 Item 的主題
     */
    fun applyThemeToBoardItem(view: View, isRead: Boolean, title: String, isReply: Boolean) {
        // 不處理列表顏色
    }

    /**
     * 專門套用勇者足跡 Item 的主題
     */
    fun applyThemeToHeroStepItem(view: View) {
        if (view is ViewGroup) {
            recursiveApplyContent(view)
        }
    }

    /**
     * 專門套用精華區列表 Item 的主題
     */
    fun applyThemeToBoardEssenceItem(view: View) {
        // 不處理
    }

    /**
    ◦
    專門套用分類列表 Item 的主題 */
    fun applyThemeToClassItem(view: View) {
        // 不處理
     }

    /**
     * 套用看板側邊選單 (抽屜) 容器的主題
     */
    fun applyThemeToBoardDrawer(drawerView: ViewGroup?) {
        if (drawerView == null) return
        recursiveApplyContent(drawerView)
    }
    /**
     * 專門套用看板側邊選單 (抽屜) Item 的主題
     */
    fun applyThemeToBoardDrawerItem(view: View) {
        if (view is ViewGroup) {
            recursiveApplyContent(view)
        }
    }

    /**
     * 專門套用信箱列表 Item 的主題
     */
    fun applyThemeToMailBoxItem(view: View, isRead: Boolean) {
        // 不處理
    }

    /**
     * 專門套用右方箭頭的主題
     */
    private fun applyThemeToRightArrow(arrowView: View) {
        // 不處理
    }

    private fun applyThemeToSpinner(spinner: Spinner) {
        // 不處理
    }

    /**
     * 專門套用連結預覽 (ThumbnailItemView) 的主題
     */
    fun applyThemeToThumbnailItem(view: View) {
        if (view is ViewGroup) {
            recursiveApplyContent(view)
        }
    }

    /**
     * 專門套用發表文章頁面的主題
     */
    fun applyThemeToPostArticle(container: ViewGroup?) {
        if (container == null) return
        recursiveApplyContent(container)
    }

    /**
     * 專門套用對話框 (如 ASListDialog) 的主題
     */
    fun applyThemeToDialog(container: ViewGroup?) {
        if (container == null) return
        recursiveApplyContent(container)
    }
}
