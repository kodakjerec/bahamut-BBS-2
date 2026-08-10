package com.kota.Bahamut.pages.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class ThemeFunctions {
    private lateinit var theme:Theme

    /**
     * 專門套用內文樣式
     * 整合了處理 ArticlePage 的背景色邏輯，解決空白處黑色問題
     */
    fun applyThemeToContent(container: ViewGroup?) {
        if (container == null) return
        theme = ThemeStore.getSelectTheme()
        val cBack = rgbToInt(theme.contentBackColor)
        val cText = rgbToInt(theme.contentTextColor)

        // 1. 設定根容器背景色 (若原本背景是黑色，或是主容器 ID，則替換)
        val background = container.background
        if ((background is ColorDrawable && background.color == Color.BLACK) || container.id == R.id.content_view) {
            container.setBackgroundColor(cBack)
        }

        // 2. 針對 ArticlePage 特有元件優先處理 (即使背景非黑色也強制設定，解決空白溢出問題)
        container.findViewById<View>(R.id.Article_contentList)?.setBackgroundColor(cBack)
        container.findViewById<View>(R.id.Article_listEmptyView)?.setBackgroundColor(cBack)

        // 3. 遞迴處理所有子元件
        recursiveApplyContent(container, cBack, cText)
    }

    /**
     * 遞迴處理內文元件樣式
     */
    private fun recursiveApplyContent(viewGroup: ViewGroup, backColor: Int, textColor: Int) {
        for (i in 0 until viewGroup.childCount) {
            val childView: View = viewGroup.getChildAt(i)

            // 避開工具列元件 (ToolbarItem)
            if (childView.tag != null) {
                when (childView.tag.toString()) {
                    "normalButton", "ToolbarItem" -> {
                        // 避免共用bug, 每次獨立產生
                        // 文字
                        val colorStateList = ColorStateList(
                            arrayOf(
                                intArrayOf(android.R.attr.state_pressed),
                                intArrayOf(android.R.attr.state_enabled),
                                intArrayOf()
                            ), // States
                            intArrayOf(
                                rgbToInt(theme.textColorPressed),
                                rgbToInt(theme.textColor),
                                rgbToInt(theme.textColorDisabled)
                            ) // Colors for each state
                        )
                        // 背景
                        val backgroundDrawable = StateListDrawable()
                        backgroundDrawable.addState(
                            intArrayOf(android.R.attr.state_pressed),
                            rgbToInt(theme.backgroundColorPressed).toDrawable()
                        )
                        backgroundDrawable.addState(
                            intArrayOf(android.R.attr.state_enabled),
                            rgbToInt(theme.backgroundColor).toDrawable()
                        )
                        backgroundDrawable.addState(
                            intArrayOf(),
                            rgbToInt(theme.backgroundColorDisabled).toDrawable()
                        )

                        if (childView is Button) {
                            childView.setTextColor(colorStateList)
                        } else if (childView is TextView) {
                            childView.setTextColor(colorStateList)
                        }
                        childView.background = backgroundDrawable
                    }
                    "ToolbarItem.Danger" -> {
                        // 文字
                        val colorStateList = ColorStateList(
                            arrayOf(
                                intArrayOf(android.R.attr.state_pressed),
                                intArrayOf(android.R.attr.state_enabled),
                                intArrayOf()
                            ), // States
                            intArrayOf(
                                rgbToInt(theme.textColorDangerPressed),
                                rgbToInt(theme.textColorDanger),
                                rgbToInt(theme.textColorDangerDisabled)
                            ) // Colors for each state
                        )
                        // 背景
                        val backgroundDrawable = StateListDrawable()
                        backgroundDrawable.addState(
                            intArrayOf(android.R.attr.state_pressed),
                            rgbToInt(theme.backgroundColorDangerPressed).toDrawable()
                        )
                        backgroundDrawable.addState(
                            intArrayOf(android.R.attr.state_enabled),
                            rgbToInt(theme.backgroundColorDanger).toDrawable()
                        )
                        backgroundDrawable.addState(
                            intArrayOf(),
                            rgbToInt(theme.backgroundColorDangerDisabled).toDrawable()
                        )

                        if (childView is Button) {
                            childView.setTextColor(colorStateList)
                        } else if (childView is TextView) {
                            childView.setTextColor(colorStateList)
                        }
                        childView.background = backgroundDrawable
                    }
                    "normalText" -> {
                        val tempChildView = childView as TextView
                        tempChildView.setTextColor(rgbToInt(theme.contentAuthorColor))
                    }
                    "rightArrow" -> {
                        applyThemeToRightArrow(childView)
                    }
                }
            } else if (childView is android.widget.CompoundButton) {
                // 針對 RadioButton 處理文字顏色，但不再強制變更 CheckBox 的勾選框顏色 (Tint)
                if (childView is android.widget.RadioButton) {
                    childView.setTextColor(rgbToInt(theme.contentAuthorColor))
                }
            } else if (childView is Spinner) {
                applyThemeToSpinner(childView)
                continue
            } else if (childView is ViewGroup) {
                // 排除 telnetHeaderView
                if (childView is TelnetHeaderItemView) {
                    continue
                }
                childView.setBackgroundColor(backColor)
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
                if (child.id != R.id.menu_button) {
                    child.setBackgroundColor(hBack)
                }
            }
        }

        // 4. 設定文字與分隔線顏色
        headerView.findViewById<TextView>(R.id.title)?.setTextColor(hTitle)
        headerView.findViewById<TextView>(R.id.detail_1)?.setTextColor(hManager)
        headerView.findViewById<TextView>(R.id.detail_2)?.setTextColor(hBorder)
        headerView.findViewById<TextView>(R.id.detail_vV)?.setTextColor(hBorder)
        // 5. 處理選單按鈕 (前景跟隨標題色，背景比標題列暗 30%)
        val menuButton = headerView.findViewById<ImageButton>(R.id.menu_button)
        if (menuButton != null) {
            val hBackDarker = Color.argb(
                Color.alpha(hBack),
                (Color.red(hBack) * 0.7f).toInt(),
                (Color.green(hBack) * 0.7f).toInt(),
                (Color.blue(hBack) * 0.7f).toInt()
            )
            menuButton.setBackgroundColor(hBackDarker)
            menuButton.imageTintList = ColorStateList.valueOf(hManager)
        }
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
        val lTitle = rgbToInt(theme.listTitleColor)

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
            !isRead -> lTitle
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

        // 5. 右方箭頭
        applyThemeToRightArrow(view.findViewById(R.id.ListItem_ArrowView))
    }

    /**
     * 專門套用勇者足跡 Item 的主題
     */
    fun applyThemeToHeroStepItem(view: View) {
        theme = ThemeStore.getSelectTheme()
        val cBack = rgbToInt(theme.contentBackColor)
        val cText = rgbToInt(theme.contentTextColor)
        val hBack = rgbToInt(theme.headerBackColor)
        val hTitle = rgbToInt(theme.headerHeaderColor)
        val hBorder = rgbToInt(theme.headerBorderColor)

        // 設定背景
        view.setBackgroundColor(cBack)

        // 作者欄位 (使用標題列配色)
        val authorLabel = view.findViewById<TextView>(R.id.HeroStep_ItemView_Name)
        authorLabel?.setBackgroundColor(hBack)
        authorLabel?.setTextColor(hTitle)

        // 時間欄位 (背景使用標題列邊框色，文字使用標題列背景色)
        val dateLabel = view.findViewById<TextView>(R.id.HeroStep_ItemView_Datetime)
        dateLabel?.setBackgroundColor(hBorder)
        dateLabel?.setTextColor(hBack)

        // 內容欄位
        val contentLabel = view.findViewById<TextView>(R.id.HeroStep_ItemView_Content)
        contentLabel?.setTextColor(cText)

        // 分隔線
        view.findViewById<View>(R.id.HeroStep_ItemView_DividerBottom)?.setBackgroundColor(rgbToInt(theme.listDividerColor))
    }

    /**
     * 專門套用精華區列表 Item 的主題
     */
    fun applyThemeToBoardEssenceItem(view: View) {
        theme = ThemeStore.getSelectTheme()
        val lBack = rgbToInt(theme.listBackColor)
        val lTitle = rgbToInt(theme.listTitleColor)
        val lDivider = rgbToInt(theme.listDividerColor)

        // 1. 設定背景
        view.setBackgroundColor(lBack)
        view.findViewById<View>(R.id.BoardPage_ItemView_contentView)?.setBackgroundColor(lBack)

        // 2. 設定標題顏色
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Title)?.setTextColor(lTitle)

        // 3. 設定其他欄位顏色
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Status)?.setTextColor(rgbToInt(theme.listStatusColor))
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Author)?.setTextColor(rgbToInt(theme.listAuthorColor))
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Number)?.setTextColor(rgbToInt(theme.listNumberColor))
        view.findViewById<TextView>(R.id.BoardPage_ItemView_Date)?.setTextColor(rgbToInt(theme.listDateColor))

        // 4. 分隔線
        view.findViewById<View>(R.id.BoardPage_ItemView_DividerBottom)?.setBackgroundColor(lDivider)

        // 5. 右方箭頭
        applyThemeToRightArrow(view.findViewById(R.id.ListItem_ArrowView))
    }

    /**
    ◦
    專門套用分類列表 Item 的主題 (配色等同 BoardItem) */
    fun applyThemeToClassItem(view: View) {
        theme = ThemeStore.getSelectTheme()
        val lBack = rgbToInt(theme.listBackColor)
        val lTitle = rgbToInt(theme.listTitleColor)
        val lDivider = rgbToInt(theme.listDividerColor)

        // 1. 設定背景
        view.setBackgroundColor(lBack)
        // 2. 設定文字顏色 // 看板標題 (白)
        view.findViewById<TextView>(R.id.ClassPage_ItemView_classTitle)?.setTextColor(lTitle)
        // 看板名稱 (黃)
         view.findViewById<TextView>(R.id.ClassPage_ItemView_className)?.setTextColor(rgbToInt(theme.listStatusColor))
        // 板主 (粉藍)
         view.findViewById<TextView>(R.id.ClassPage_ItemView_classManager)?.setTextColor(rgbToInt(theme.listAuthorColor))
        // 分隔線
        view.findViewById<View>(R.id.ClassPage_ItemView_DividerBottom)?.setBackgroundColor(lDivider)
        
        // 3. 右方箭頭
        applyThemeToRightArrow(view.findViewById(R.id.ListItem_ArrowView))
     }

    /**
     * 套用看板側邊選單 (抽屜) 容器的主題，解決背景黑色問題
     */
    fun applyThemeToBoardDrawer(drawerView: ViewGroup?) {
        if (drawerView == null) return
        theme = ThemeStore.getSelectTheme()
        val lBack = rgbToInt(theme.listBackColor)
        val lTitle = rgbToInt(theme.listTitleColor)

        // 1. 設定抽屜根容器背景 (id: menu_view)
        drawerView.setBackgroundColor(lBack)

        // 2. 設定書籤 ListView 背景 (id: bookmark_list_view)
        val listView = drawerView.findViewById<View>(R.id.bookmark_list_view)
        listView?.setBackgroundColor(lBack)

        // 3. 設定列表為空時的文字背景 (id: bookmark_list_view_none)
        val emptyView = drawerView.findViewById<View>(R.id.bookmark_list_view_none)
        emptyView?.setBackgroundColor(lBack)

        // 4. 對其子元件進行遞迴著色 (處理標籤、Checkbox 等)
        recursiveApplyContent(drawerView, lBack, lTitle)
    }
    /**
     * 專門套用看板側邊選單 (抽屜) Item 的主題
     */
    fun applyThemeToBoardDrawerItem(view: View) {
        theme = ThemeStore.getSelectTheme()
        val lBack = rgbToInt(theme.listBackColor)
        val lTitle = rgbToInt(theme.listTitleColor)
        val lAuthor = rgbToInt(theme.listAuthorColor)
        val lNumber = rgbToInt(theme.listNumberColor) // GY 使用編號色
        val lMark = rgbToInt(theme.listMarkColor)
        val lDivider = rgbToInt(theme.listDividerColor)

        // 1. 設定背景
        view.setBackgroundColor(lBack)

        // 2. 設定標題 (關鍵字)
        // 兼容 BookmarkItemView 與 HistoryItemView 的 ID
        view.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_Title)?.setTextColor(lTitle)
        view.findViewById<TextView>(R.id.BoardExtendOptionalPage_historyItemView_Title)?.setTextColor(lTitle)

        // 3. 設定作者 (包含 "作者:" 標籤與 ID)
        view.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_Author_Title)?.setTextColor(lNumber) // 標籤用次要色
        view.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_Author)?.setTextColor(lAuthor)

        // 4. 設定 GY (包含 "GY:" 標籤與 數值)
        view.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_GY_Title)?.setTextColor(lNumber)
        view.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_GY)?.setTextColor(lNumber)

        // 5. 設定 M文 標記
        view.findViewById<TextView>(R.id.BoardExtendOptionalPage_bookmarkItemView_Mark)?.setTextColor(lMark)

        // 6. 設定分隔線 (抽屜選單通常使用 DividerTop)
        view.findViewById<View>(R.id.BoardExtendOptionalPage_bookmarkItemView_DividerTop)?.setBackgroundColor(lDivider)
    }

    /**
     * 專門套用信箱列表 Item 的主題
     */
    fun applyThemeToMailBoxItem(view: View, isRead: Boolean) {
        theme = ThemeStore.getSelectTheme()
        val lBack = rgbToInt(theme.listBackColor)
        val lTitle = rgbToInt(theme.listTitleColor)
        val lTitleRead = rgbToInt(theme.listTitleReadColor)
        val lDivider = rgbToInt(theme.listDividerColor)

        // 1. 設定背景
        view.setBackgroundColor(lBack)

        // 2. 決定標題顏色
        val titleLabel = view.findViewById<TextView>(R.id.MailBoxPage_ItemView_Title)
        titleLabel?.setTextColor(if (isRead) lTitleRead else lTitle)

        // 3. 設定其他欄位顏色
        view.findViewById<TextView>(R.id.MailBoxPage_ItemView_Status)?.setTextColor(rgbToInt(theme.listStatusColor))
        view.findViewById<TextView>(R.id.MailBoxPage_ItemView_Author)?.setTextColor(rgbToInt(theme.listAuthorColor))
        view.findViewById<TextView>(R.id.MailBoxPage_ItemView_Number)?.setTextColor(rgbToInt(theme.listNumberColor))
        view.findViewById<TextView>(R.id.MailBoxPage_ItemView_Date)?.setTextColor(rgbToInt(theme.listDateColor))
        view.findViewById<TextView>(R.id.MailBoxPage_ItemView_mark)?.setTextColor(rgbToInt(theme.listMarkColor))
        view.findViewById<TextView>(R.id.MailBoxPage_ItemView_Reply)?.setTextColor(rgbToInt(theme.listStatusColor))

        // 4. 分隔線
        view.findViewById<View>(R.id.MailBoxPage_ItemView_DividerBottom)?.setBackgroundColor(lDivider)

        // 5. 右方箭頭
        applyThemeToRightArrow(view.findViewById(R.id.ListItem_ArrowView))
    }

    /**
     * 專門套用右方箭頭的主題
     */
    private fun applyThemeToRightArrow(arrowView: View) {

        val lTitle = rgbToInt(theme.listTitleColor)
        val lDivider = rgbToInt(theme.listDividerColor)

        if (arrowView is TextView) {
            arrowView.setBackgroundColor(darkenColor(lDivider, 0.7f))
            arrowView.setTextColor(lTitle)
        }
    }

    private fun applyThemeToSpinner(spinner: Spinner) {
        // --- 針對 Spinner 套用標題列配色 ---
        val hBack = rgbToInt(theme.contentBackColor)
        val hTitle = rgbToInt(theme.contentTextColor)

        // 設定 Spinner 本身的背景色 (標題列背景)
        spinner.setBackgroundColor(hBack)

        // --- 設定下拉選單的背景色 ---
        spinner.setPopupBackgroundDrawable(hBack.toDrawable())

        // 遍歷 Spinner 的子元件 (通常是顯示選中項目的 TextView) 並設定顏色
        for (j in 0 until spinner.childCount) {
            val subView = spinner.getChildAt(j)
            if (subView is TextView) {
                print(subView.text)
                subView.setTextColor(hTitle)
            }
        }
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        return Color.argb(
            Color.alpha(color),
            (Color.red(color) * factor).toInt(),
            (Color.green(color) * factor).toInt(),
            (Color.blue(color) * factor).toInt()
        )
    }

    /**
     * 專門套用連結預覽 (ThumbnailItemView) 的主題
     */
    fun applyThemeToThumbnailItem(view: View) {
        // 設定背景 (通常預覽圖背景比內文稍微亮一點點或透明)
        view.setBackgroundColor(Color.TRANSPARENT)

        // --- 新增：從主題取得顏色並著色 ---
        val theme = ThemeStore.getSelectTheme()
        val textColor = rgbToInt(theme.contentTextColor)
        // 顏色減半：保留 RGB，並將 Alpha 設為 0x80 (約 50% 透明度)
        val dimmedColor = (textColor and 0x00FFFFFF) or 0x80000000.toInt()

        // 取得標題、描述、網址等 TextView 並上色
        // 注意：這裡直接強制設定顏色，不判斷是否為白色
        val title = view.findViewById<TextView>(R.id.thumbnail_title)
        val desc = view.findViewById<TextView>(R.id.thumbnail_description)
        val url = view.findViewById<TextView>(R.id.thumbnail_url)

        title?.setTextColor(textColor)
        desc?.setTextColor(dimmedColor)
        // 網址可以使用主題的邊框色(較淡)或是維持內文色
        url?.setTextColor(dimmedColor)
    }

    /**
     * 專門套用發表文章頁面的主題
     */
    fun applyThemeToPostArticle(container: ViewGroup?) {
        if (container == null) return
        theme = ThemeStore.getSelectTheme()
        val backColor = rgbToInt(theme.contentBackColor)
        val titleColor = rgbToInt(theme.contentAuthorColor)

        // 設定整體背景
        container.setBackgroundColor(backColor)

        // 設定輸入框顏色
        val titleField = container.findViewById<TextView>(R.id.ArticlePostDialog_TitleField)
        val editField = container.findViewById<TextView>(R.id.ArticlePostDialog_EditField)
        val titleFieldBackground = container.findViewById<TextView>(R.id.ArticlePostDialog_TitleFieldBackground)
        val headSelect = container.findViewById<Spinner>(R.id.Post_headerSelector)

        titleField?.setTextColor(titleColor)
        titleField?.setBackgroundColor(backColor)

        editField?.setTextColor(titleColor)
        editField?.setBackgroundColor(backColor)

        // 這裡設定背景 TextView 的顏色 (平時顯示用)
        titleFieldBackground?.setTextColor(titleColor)
        titleFieldBackground?.setBackgroundColor(backColor)

        applyThemeToSpinner(headSelect)
    }
}