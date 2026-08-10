package com.kota.Bahamut.pages.theme

import android.content.res.ColorStateList
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.doOnTextChanged
import com.kota.asFramework.ui.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.dialogs.DialogColorPicker
import com.kota.Bahamut.dialogs.DialogColorPickerListener
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.model.PostEditText
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.telnetUI.TelnetPage

class ThemeManagerPage: TelnetPage() {
    private lateinit var mainLayout:LinearLayout
    private lateinit var buttonIds:List<Button>
    private lateinit var txnName: PostEditText
    
    // 一般按鈕設定
    private lateinit var txnTextColor:TextView
    private lateinit var txnTextColorPressed:TextView
    private lateinit var txnTextColorDisabled:TextView
    private lateinit var txnBackColor:TextView
    private lateinit var txnBackColorPressed:TextView
    private lateinit var txnBackColorDisabled:TextView

    // 警示按鈕設定
    private lateinit var txnTextColorDanger:TextView
    private lateinit var txnTextColorDangerPressed:TextView
    private lateinit var txnTextColorDangerDisabled:TextView
    private lateinit var txnBackColorDanger:TextView
    private lateinit var txnBackColorDangerPressed:TextView
    private lateinit var txnBackColorDangerDisabled:TextView
    
    // 標題列
    private lateinit var txnHeaderBackColor: TextView
    private lateinit var txnHeaderHeaderColor: TextView
    private lateinit var txnHeaderManagerColor: TextView
    private lateinit var txnHeaderBorderColor: TextView
    
    // 內文
    private lateinit var txnContentBackColor: TextView
    private lateinit var txnContentAuthorColor: TextView
    private lateinit var txnContentTextColor: TextView
    
    // 引用
    private lateinit var txnQuoteBackColor: TextView
    private lateinit var txnQuoteAuthorColor: TextView
    private lateinit var txnQuoteTextColor: TextView
    
    // 看板列表
    private lateinit var txnListBackColor: TextView
    private lateinit var txnListTitleColor: TextView
    private lateinit var txnListTitleReadColor: TextView
    private lateinit var txnListTitleFollowFirstColor: TextView
    private lateinit var txnListTitleFollowFirstReadColor: TextView
    private lateinit var txnListTitleFollowColor: TextView
    private lateinit var txnListTitleFollowReadColor: TextView
    private lateinit var txnListNumberColor: TextView
    private lateinit var txnListDateColor: TextView
    private lateinit var txnListAuthorColor: TextView
    private lateinit var txnListMarkColor: TextView
    private lateinit var txnListStatusColor: TextView
    private lateinit var txnListDividerColor: TextView

    private lateinit var editToolbar: LinearLayout
    private lateinit var btnReset:Button
    private lateinit var btnUpdate:Button
    private lateinit var btnBack:Button

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_THEME_MANAGER_PAGE

    override val pageLayout: Int
        get() = R.layout.theme_manager_page

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as LinearLayout

        // 產生外觀列的按鈕
        val themes = ThemeStore.getThemeStore()
        buttonIds = listOf(
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_0),
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_1),
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_2),
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_3),
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_4)
        )

        themes.forEachIndexed { index, theme ->
            val button:Button = buttonIds[index]
            button.text = theme.name
            button.setOnClickListener{aView ->
                ThemeStore.setSelectIndex(index)
                val selectedTheme = ThemeStore.getSelectTheme()
                for (tabButton in buttonIds) {
                    if (tabButton == aView) {
                        tabButton.setTextColor(CommonFunctions.rgbToInt(selectedTheme.textColor))
                        tabButton.setBackgroundColor(CommonFunctions.rgbToInt(selectedTheme.backgroundColor))
                    } else {
                        tabButton.setTextColor(CommonFunctions.rgbToInt(selectedTheme.textColorDisabled))
                        tabButton.setBackgroundColor(CommonFunctions.rgbToInt(selectedTheme.backgroundColorDisabled))
                    }
                }
                paintToolbarText()
                paintToolbarButtons()
                paintBtnUpdate(false)
            }
        }

        txnName = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Name)
        txnTextColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Text_Color)
        txnTextColorPressed = mainLayout.findViewById(R.id.Theme_Manager_Page_Text_Color_Pressed)
        txnTextColorDisabled = mainLayout.findViewById(R.id.Theme_Manager_Page_Text_Color_Disabled)
        txnBackColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Back_Color)
        txnBackColorPressed = mainLayout.findViewById(R.id.Theme_Manager_Page_Back_Color_Pressed)
        txnBackColorDisabled = mainLayout.findViewById(R.id.Theme_Manager_Page_Back_Color_Disabled)

        txnTextColorDanger = mainLayout.findViewById(R.id.Theme_Manager_Page_Text_Color_Danger)
        txnTextColorDangerPressed = mainLayout.findViewById(R.id.Theme_Manager_Page_Text_Color_Danger_Pressed)
        txnTextColorDangerDisabled = mainLayout.findViewById(R.id.Theme_Manager_Page_Text_Color_Danger_Disabled)
        txnBackColorDanger = mainLayout.findViewById(R.id.Theme_Manager_Page_Back_Color_Danger)
        txnBackColorDangerPressed = mainLayout.findViewById(R.id.Theme_Manager_Page_Back_Color_Danger_Pressed)
        txnBackColorDangerDisabled = mainLayout.findViewById(R.id.Theme_Manager_Page_Back_Color_Danger_Disabled)

        txnHeaderBackColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Header_Back_Color)
        txnHeaderHeaderColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Header_Header_Color)
        txnHeaderManagerColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Header_Manager_Color)
        txnHeaderBorderColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Header_Border_Color)

        txnContentBackColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Content_Back_Color)
        txnContentAuthorColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Content_Author_Color)
        txnContentTextColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Content_Color)

        txnQuoteBackColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Quote_Back_Color)
        txnQuoteAuthorColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Quote_Author_Color)
        txnQuoteTextColor = mainLayout.findViewById(R.id.Theme_Manager_Page_Quote_Color)

        txnListBackColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Back_Color)
        txnListTitleColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Title_Color)
        txnListTitleReadColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Title_Read_Color)
        txnListTitleFollowFirstColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Title_Follow_First_Color)
        txnListTitleFollowFirstReadColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Title_Follow_First_Read_Color)
        txnListTitleFollowColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Title_Follow_Color)
        txnListTitleFollowReadColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Title_Follow_Read_Color)
        txnListNumberColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Number_Color)
        txnListDateColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Date_Color)
        txnListAuthorColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Author_Color)
        txnListMarkColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Mark_Color)
        txnListStatusColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Status_Color)
        txnListDividerColor = mainLayout.findViewById(R.id.Theme_Manager_Page_List_Divider_Color)

        // 綁定監聽
        listOf(
            txnTextColor, txnTextColorPressed, txnTextColorDisabled,
            txnBackColor, txnBackColorPressed, txnBackColorDisabled,
            txnTextColorDanger, txnTextColorDangerPressed, txnTextColorDangerDisabled,
            txnBackColorDanger, txnBackColorDangerPressed, txnBackColorDangerDisabled,
            txnHeaderBackColor, txnHeaderHeaderColor, txnHeaderManagerColor, txnHeaderBorderColor,
            txnContentBackColor, txnContentAuthorColor, txnContentTextColor,
            txnQuoteBackColor, txnQuoteAuthorColor, txnQuoteTextColor,
            txnListBackColor, txnListTitleColor, txnListTitleReadColor,
            txnListTitleFollowFirstColor, txnListTitleFollowFirstReadColor,
            txnListTitleFollowColor, txnListTitleFollowReadColor,
            txnListNumberColor, txnListDateColor, txnListAuthorColor,
            txnListMarkColor, txnListStatusColor, txnListDividerColor
        ).forEach { it.setOnClickListener(textClickListener) }

        txnName.doOnTextChanged { _, _, _, _ -> paintBtnUpdate(true) }

        editToolbar = mainLayout.findViewById(R.id.Theme_Manager_Page_Edit_Toolbar)
        btnReset = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Reset)
        btnReset.setOnClickListener(btnResetOnClickListener)

        btnUpdate = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Update)
        btnUpdate.setOnClickListener(btnUpdateOnClickListener)

        btnBack = findViewById(R.id.Theme_Manager_Page_Toolbar_Back) as Button
        btnBack.setOnClickListener { onBackPressed() }

        // 按下預設外觀
        buttonIds[ThemeStore.getSelectIndex()].performClick()
    }

    private fun paintToolbarText() {
        val theme = ThemeStore.getSelectTheme()
        txnName.setText(theme.name)
        txnTextColor.text = theme.textColor
        txnTextColorPressed.text = theme.textColorPressed
        txnTextColorDisabled.text = theme.textColorDisabled
        txnBackColor.text = theme.backgroundColor
        txnBackColorPressed.text = theme.backgroundColorPressed
        txnBackColorDisabled.text = theme.backgroundColorDisabled

        txnTextColorDanger.text = theme.textColorDanger
        txnTextColorDangerPressed.text = theme.textColorDangerPressed
        txnTextColorDangerDisabled.text = theme.textColorDangerDisabled
        txnBackColorDanger.text = theme.backgroundColorDanger
        txnBackColorDangerPressed.text = theme.backgroundColorDangerPressed
        txnBackColorDangerDisabled.text = theme.backgroundColorDangerDisabled
        
        txnHeaderBackColor.text = theme.headerBackColor
        txnHeaderHeaderColor.text = theme.headerHeaderColor
        txnHeaderManagerColor.text = theme.headerManagerColor
        txnHeaderBorderColor.text = theme.headerBorderColor
        
        txnContentBackColor.text = theme.contentBackColor
        txnContentAuthorColor.text = theme.contentAuthorColor
        txnContentTextColor.text = theme.contentTextColor
        
        txnQuoteBackColor.text = theme.quoteBackColor
        txnQuoteAuthorColor.text = theme.quoteAuthorColor
        txnQuoteTextColor.text = theme.quoteTextColor

        txnListBackColor.text = theme.listBackColor
        txnListTitleColor.text = theme.listTitleColor
        txnListTitleReadColor.text = theme.listTitleReadColor
        txnListTitleFollowFirstColor.text = theme.listTitleFollowFirstColor
        txnListTitleFollowFirstReadColor.text = theme.listTitleFollowFirstReadColor
        txnListTitleFollowColor.text = theme.listTitleFollowColor
        txnListTitleFollowReadColor.text = theme.listTitleFollowReadColor
        txnListNumberColor.text = theme.listNumberColor
        txnListDateColor.text = theme.listDateColor
        txnListAuthorColor.text = theme.listAuthorColor
        txnListMarkColor.text = theme.listMarkColor
        txnListStatusColor.text = theme.listStatusColor
        txnListDividerColor.text = theme.listDividerColor
    }

    private fun paintToolbarButtons() {
        val theme = ThemeStore.getSelectTheme()

        // 1. 底部的返回按鈕工具列
        ThemeFunctions().applyThemeToContent(findViewById(R.id.toolbar) as? ViewGroup)

        // 一般按鈕範例
        val btnSample1: Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_1)
        val btnSample2: Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_2)
        val btnSample3: Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_3)
        btnSample1.setTextColor(CommonFunctions.rgbToInt(theme.textColor))
        btnSample1.setBackgroundColor(CommonFunctions.rgbToInt(theme.backgroundColor))
        btnSample2.setTextColor(CommonFunctions.rgbToInt(theme.textColorPressed))
        btnSample2.setBackgroundColor(CommonFunctions.rgbToInt(theme.backgroundColorPressed))
        btnSample3.setTextColor(CommonFunctions.rgbToInt(theme.textColorDisabled))
        btnSample3.setBackgroundColor(CommonFunctions.rgbToInt(theme.backgroundColorDisabled))

        // Danger 按鈕範例
        val btnDanger1: Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_Danger_1)
        val btnDanger2: Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_Danger_2)
        val btnDanger3: Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_Danger_3)
        btnDanger1.setTextColor(CommonFunctions.rgbToInt(theme.textColorDanger))
        btnDanger1.setBackgroundColor(CommonFunctions.rgbToInt(theme.backgroundColorDanger))
        btnDanger2.setTextColor(CommonFunctions.rgbToInt(theme.textColorDangerPressed))
        btnDanger2.setBackgroundColor(CommonFunctions.rgbToInt(theme.backgroundColorDangerPressed))
        btnDanger3.setTextColor(CommonFunctions.rgbToInt(theme.textColorDangerDisabled))
        btnDanger3.setBackgroundColor(CommonFunctions.rgbToInt(theme.backgroundColorDangerDisabled))

        // 標題列預覽
        val hBack = CommonFunctions.rgbToInt(theme.headerBackColor)
        txnHeaderBackColor.setBackgroundColor(hBack)
        txnHeaderHeaderColor.setBackgroundColor(hBack)
        txnHeaderManagerColor.setBackgroundColor(hBack)
        txnHeaderBorderColor.setBackgroundColor(hBack)
        txnHeaderHeaderColor.setTextColor(CommonFunctions.rgbToInt(theme.headerHeaderColor))
        txnHeaderManagerColor.setTextColor(CommonFunctions.rgbToInt(theme.headerManagerColor))
        txnHeaderBorderColor.setTextColor(CommonFunctions.rgbToInt(theme.headerBorderColor))

        // 內文/引用預覽
        val cBack = CommonFunctions.rgbToInt(theme.contentBackColor)
        txnContentBackColor.setBackgroundColor(cBack)
        txnContentAuthorColor.setBackgroundColor(cBack)
        txnContentTextColor.setBackgroundColor(cBack)
        txnContentAuthorColor.setTextColor(CommonFunctions.rgbToInt(theme.contentAuthorColor))
        txnContentTextColor.setTextColor(CommonFunctions.rgbToInt(theme.contentTextColor))

        val qBack = CommonFunctions.rgbToInt(theme.quoteBackColor)
        txnQuoteBackColor.setBackgroundColor(qBack)
        txnQuoteAuthorColor.setBackgroundColor(qBack)
        txnQuoteTextColor.setBackgroundColor(qBack)
        txnQuoteAuthorColor.setTextColor(CommonFunctions.rgbToInt(theme.quoteAuthorColor))
        txnQuoteTextColor.setTextColor(CommonFunctions.rgbToInt(theme.quoteTextColor))

        // 看板列表預覽
        val lBack = CommonFunctions.rgbToInt(theme.listBackColor)
        val lDiv = CommonFunctions.rgbToInt(theme.listDividerColor)
        val listFields = listOf(
            txnListBackColor, txnListTitleColor, txnListTitleReadColor,
            txnListTitleFollowFirstColor, txnListTitleFollowFirstReadColor,
            txnListTitleFollowColor, txnListTitleFollowReadColor,
            txnListMarkColor, txnListStatusColor, txnListNumberColor,
            txnListDateColor, txnListAuthorColor
        )
        listFields.forEach { 
            it.setBackgroundColor(lBack) 
            it.setTextColor(CommonFunctions.rgbToInt(theme.listTitleColor)) // 預設白字
        }
        
        txnListTitleReadColor.setTextColor(CommonFunctions.rgbToInt(theme.listTitleReadColor))
        txnListTitleFollowFirstColor.setTextColor(CommonFunctions.rgbToInt(theme.listTitleFollowFirstColor))
        txnListTitleFollowFirstReadColor.setTextColor(CommonFunctions.rgbToInt(theme.listTitleFollowFirstReadColor))
        txnListTitleFollowColor.setTextColor(CommonFunctions.rgbToInt(theme.listTitleFollowColor))
        txnListTitleFollowReadColor.setTextColor(CommonFunctions.rgbToInt(theme.listTitleFollowReadColor))
        txnListMarkColor.setTextColor(CommonFunctions.rgbToInt(theme.listMarkColor))
        txnListStatusColor.setTextColor(CommonFunctions.rgbToInt(theme.listStatusColor))
        txnListNumberColor.setTextColor(CommonFunctions.rgbToInt(theme.listNumberColor))
        txnListDateColor.setTextColor(CommonFunctions.rgbToInt(theme.listDateColor))
        txnListAuthorColor.setTextColor(CommonFunctions.rgbToInt(theme.listAuthorColor))
        
        txnListDividerColor.setBackgroundColor(lDiv)
        txnListDividerColor.setTextColor(lBack)
    }

    private fun paintBtnUpdate(enabled: Boolean) {
        btnUpdate.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private val textClickListener = OnClickListener { view ->
        val colorRes = (view as TextView).text.toString()
        val dialogColorPicker = DialogColorPicker()
        dialogColorPicker.setFromRes(colorRes)
        dialogColorPicker.setListener(object: DialogColorPickerListener {
            override fun onSelectColor(colorRes: String) {
                view.text = colorRes
                // 即時反應到 theme 物件並重繪按鈕
                val theme = ThemeStore.getSelectTheme()
                updateThemeFromUI(theme)
                paintToolbarButtons()
                paintBtnUpdate(true)
            }
        })
        dialogColorPicker.show()
    }

    private val btnResetOnClickListener = OnClickListener { _->
        val selectedIndex = ThemeStore.getSelectIndex()
        ThemeStore.updateTheme(selectedIndex, ThemeStore.getDefaultTheme(selectedIndex))
        paintToolbarText()
        paintToolbarButtons()
        paintBtnUpdate(false)
        mainLayout.clearFocus()
        ASToast.showShortToast(getContextString(R.string.theme_manager_page_msg02))
    }

    private val btnUpdateOnClickListener = OnClickListener { _ ->
        val theme = ThemeStore.getSelectTheme()
        updateThemeFromUI(theme)
        ThemeStore.updateTheme(ThemeStore.getSelectIndex(), theme)
        paintToolbarText()
        paintToolbarButtons()
        paintBtnUpdate(false)
        
        val themes = ThemeStore.getThemeStore()
        themes.forEachIndexed { index, t -> buttonIds[index].text = t.name }
        mainLayout.clearFocus()
        ASToast.showShortToast(getContextString(R.string.theme_manager_page_msg01))
    }

    private fun updateThemeFromUI(theme: Theme) {
        theme.name = txnName.text.toString()
        theme.textColor = txnTextColor.text.toString()
        theme.textColorPressed = txnTextColorPressed.text.toString()
        theme.textColorDisabled = txnTextColorDisabled.text.toString()
        theme.backgroundColor = txnBackColor.text.toString()
        theme.backgroundColorPressed = txnBackColorPressed.text.toString()
        theme.backgroundColorDisabled = txnBackColorDisabled.text.toString()

        theme.textColorDanger = txnTextColorDanger.text.toString()
        theme.textColorDangerPressed = txnTextColorDangerPressed.text.toString()
        theme.textColorDangerDisabled = txnTextColorDangerDisabled.text.toString()
        theme.backgroundColorDanger = txnBackColorDanger.text.toString()
        theme.backgroundColorDangerPressed = txnBackColorDangerPressed.text.toString()
        theme.backgroundColorDangerDisabled = txnBackColorDangerDisabled.text.toString()
        
        theme.headerBackColor = txnHeaderBackColor.text.toString()
        theme.headerHeaderColor = txnHeaderHeaderColor.text.toString()
        theme.headerManagerColor = txnHeaderManagerColor.text.toString()
        theme.headerBorderColor = txnHeaderBorderColor.text.toString()
        
        theme.contentBackColor = txnContentBackColor.text.toString()
        theme.contentAuthorColor = txnContentAuthorColor.text.toString()
        theme.contentTextColor = txnContentTextColor.text.toString()
        
        theme.quoteBackColor = txnQuoteBackColor.text.toString()
        theme.quoteAuthorColor = txnQuoteAuthorColor.text.toString()
        theme.quoteTextColor = txnQuoteTextColor.text.toString()
        
        theme.listBackColor = txnListBackColor.text.toString()
        theme.listTitleColor = txnListTitleColor.text.toString()
        theme.listTitleReadColor = txnListTitleReadColor.text.toString()
        theme.listTitleFollowFirstColor = txnListTitleFollowFirstColor.text.toString()
        theme.listTitleFollowFirstReadColor = txnListTitleFollowFirstReadColor.text.toString()
        theme.listTitleFollowColor = txnListTitleFollowColor.text.toString()
        theme.listTitleFollowReadColor = txnListTitleFollowReadColor.text.toString()
        theme.listNumberColor = txnListNumberColor.text.toString()
        theme.listDateColor = txnListDateColor.text.toString()
        theme.listAuthorColor = txnListAuthorColor.text.toString()
        theme.listMarkColor = txnListMarkColor.text.toString()
        theme.listStatusColor = txnListStatusColor.text.toString()
        theme.listDividerColor = txnListDividerColor.text.toString()
    }

    override fun onBackPressed(): Boolean {
        PageContainer.instance!!.cleanThemeManagerPage()
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        return true
    }
}
