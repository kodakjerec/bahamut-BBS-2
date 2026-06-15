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
    private lateinit var txnTextColor:TextView
    private lateinit var txnTextColorPressed:TextView
    private lateinit var txnTextColorDisabled:TextView
    private lateinit var txnBackColor:TextView
    private lateinit var txnBackColorPressed:TextView
    private lateinit var txnBackColorDisabled:TextView
    private lateinit var txnHeaderBackColor: TextView
    private lateinit var txnHeaderHeaderColor: TextView
    private lateinit var txnHeaderManagerColor: TextView
    private lateinit var txnHeaderBorderColor: TextView
    private lateinit var txnContentBackColor: TextView
    private lateinit var txnContentAuthorColor: TextView
    private lateinit var txnContentTextColor: TextView
    private lateinit var txnQuoteBackColor: TextView
    private lateinit var txnQuoteAuthorColor: TextView
    private lateinit var txnQuoteTextColor: TextView
    private lateinit var editToolbar: LinearLayout
    private lateinit var btnReset:Button
    private lateinit var btnUpdate:Button
    private lateinit var btnBack:Button

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_THEME_MANAGER_PAGE

    override val pageLayout: Int
        get() = R.layout.theme_manager_page

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

                // 套用版面
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
        txnName.doOnTextChanged { _, _, _, _ ->
            paintBtnUpdate(true)
        }
        txnTextColor.setOnClickListener(textClickListener)
        txnTextColorPressed.setOnClickListener(textClickListener)
        txnTextColorDisabled.setOnClickListener(textClickListener)
        txnBackColor.setOnClickListener(textClickListener)
        txnBackColorPressed.setOnClickListener(textClickListener)
        txnBackColorDisabled.setOnClickListener(textClickListener)

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

        txnHeaderBackColor.setOnClickListener(textClickListener)
        txnHeaderHeaderColor.setOnClickListener(textClickListener)
        txnHeaderManagerColor.setOnClickListener(textClickListener)
        txnHeaderBorderColor.setOnClickListener(textClickListener)
        txnContentAuthorColor.setOnClickListener(textClickListener)
        txnContentBackColor.setOnClickListener(textClickListener)
        txnContentTextColor.setOnClickListener(textClickListener)
        txnQuoteBackColor.setOnClickListener(textClickListener)
        txnQuoteAuthorColor.setOnClickListener(textClickListener)
        txnQuoteTextColor.setOnClickListener(textClickListener)

        editToolbar = mainLayout.findViewById(R.id.Theme_Manager_Page_Edit_Toolbar)
        btnReset = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Reset)
        btnReset.setOnClickListener(btnResetOnClickListener)

        btnUpdate = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Update)
        btnUpdate.setOnClickListener(btnUpdateOnClickListener)

        btnBack = findViewById(R.id.Theme_Manager_Page_Toolbar_Back) as Button
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // 按下指定的外觀
        val selectedIndex = ThemeStore.getSelectIndex()
        buttonIds[selectedIndex].performClick()
    }

    /** 變更工具列文字 */
    private fun paintToolbarText() {
        val selectedTheme = ThemeStore.getSelectTheme()
        txnName.setText(selectedTheme.name)
        txnTextColor.text = selectedTheme.textColor
        txnTextColorPressed.text = selectedTheme.textColorPressed
        txnTextColorDisabled.text = selectedTheme.textColorDisabled
        txnBackColor.text = selectedTheme.backgroundColor
        txnBackColorPressed.text = selectedTheme.backgroundColorPressed
        txnBackColorDisabled.text = selectedTheme.backgroundColorDisabled
        txnHeaderBackColor.text = selectedTheme.headerBackColor
        txnHeaderHeaderColor.text = selectedTheme.headerHeaderColor
        txnHeaderManagerColor.text = selectedTheme.headerManagerColor
        txnHeaderBorderColor.text = selectedTheme.headerBorderColor
        txnContentBackColor.text = selectedTheme.contentBackColor
        txnContentAuthorColor.text = selectedTheme.contentAuthorColor
        txnContentTextColor.text = selectedTheme.contentTextColor
        txnQuoteBackColor.text = selectedTheme.quoteBackColor
        txnQuoteAuthorColor.text = selectedTheme.quoteAuthorColor
        txnQuoteTextColor.text = selectedTheme.quoteTextColor
    }
    /** 變更工具列示範按鈕外觀 */
    private fun paintToolbarButtons() {
        val button1:Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_1)
        button1.setTextColor(CommonFunctions.rgbToInt(txnTextColor.text.toString()))
        button1.setBackgroundColor(CommonFunctions.rgbToInt(txnBackColor.text.toString()))

        val button2:Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_2)
        button2.setTextColor(CommonFunctions.rgbToInt(txnTextColorPressed.text.toString()))
        button2.setBackgroundColor(CommonFunctions.rgbToInt(txnBackColorPressed.text.toString()))

        val button3:Button = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Sample_3)
        button3.setTextColor(CommonFunctions.rgbToInt(txnTextColorDisabled.text.toString()))
        button3.setBackgroundColor(CommonFunctions.rgbToInt(txnBackColorDisabled.text.toString()))

        // 預覽底部的導覽列與編輯工具列
        val textColor = CommonFunctions.rgbToInt(txnTextColor.text.toString())
        val textColorPressed = CommonFunctions.rgbToInt(txnTextColorPressed.text.toString())
        val textColorDisabled = CommonFunctions.rgbToInt(txnTextColorDisabled.text.toString())
        val backColor = CommonFunctions.rgbToInt(txnBackColor.text.toString())
        val backColorPressed = CommonFunctions.rgbToInt(txnBackColorPressed.text.toString())
        val backColorDisabled = CommonFunctions.rgbToInt(txnBackColorDisabled.text.toString())

        val toolbars = listOfNotNull(
            findViewById(R.id.toolbar) as? ViewGroup,
            editToolbar
        )

        for (toolbar in toolbars) {
            for (i in 0 until toolbar.childCount) {
                val child = toolbar.getChildAt(i)
                if (child is Button) {
                    // 套用文字顏色狀態 (按下、一般、停用)
                    child.setTextColor(ColorStateList(
                        arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_enabled), intArrayOf()),
                        intArrayOf(textColorPressed, textColor, textColorDisabled)
                    ))
                    // 套用背景顏色狀態 (按下、一般、停用)
                    val backgroundDrawable = StateListDrawable()
                    backgroundDrawable.addState(intArrayOf(android.R.attr.state_pressed), backColorPressed.toDrawable())
                    backgroundDrawable.addState(intArrayOf(android.R.attr.state_enabled), backColor.toDrawable())
                    backgroundDrawable.addState(intArrayOf(), backColorDisabled.toDrawable())
                    child.background = backgroundDrawable
                }
            }
        }

        // 標題列預覽: 背景(Col2)+標題(Col3)+版主(Col4)+版面(Col5)
        val hBack = CommonFunctions.rgbToInt(txnHeaderBackColor.text.toString())
        val hTitle = CommonFunctions.rgbToInt(txnHeaderHeaderColor.text.toString())
        val hManager = CommonFunctions.rgbToInt(txnHeaderManagerColor.text.toString())
        val hBorder = CommonFunctions.rgbToInt(txnHeaderBorderColor.text.toString())

        txnHeaderBackColor.setTextColor(hTitle)
        txnHeaderBackColor.setBackgroundColor(hBack)
        txnHeaderHeaderColor.setTextColor(hTitle)
        txnHeaderHeaderColor.setBackgroundColor(hBack)
        txnHeaderManagerColor.setTextColor(hManager)
        txnHeaderManagerColor.setBackgroundColor(hBack)
        txnHeaderBorderColor.setTextColor(hBorder)
        txnHeaderBorderColor.setBackgroundColor(hBack)

        // 內文預覽: 背景(Col2)+作者(Col3)+內文(Col4)
        val cBack = CommonFunctions.rgbToInt(txnContentBackColor.text.toString())
        val cAuthor = CommonFunctions.rgbToInt(txnContentAuthorColor.text.toString())
        val cText = CommonFunctions.rgbToInt(txnContentTextColor.text.toString())

        txnContentBackColor.setTextColor(cText)
        txnContentBackColor.setBackgroundColor(cBack)
        txnContentAuthorColor.setTextColor(cAuthor)
        txnContentAuthorColor.setBackgroundColor(cBack)
        txnContentTextColor.setTextColor(cText)
        txnContentTextColor.setBackgroundColor(cBack)

        // 引用預覽: 背景(Col2)+作者(Col3)+內文(Col4)
        val qBack = CommonFunctions.rgbToInt(txnQuoteBackColor.text.toString())
        val qAuthor = CommonFunctions.rgbToInt(txnQuoteAuthorColor.text.toString())
        val qText = CommonFunctions.rgbToInt(txnQuoteTextColor.text.toString())

        txnQuoteBackColor.setTextColor(qText)
        txnQuoteBackColor.setBackgroundColor(qBack)
        txnQuoteAuthorColor.setTextColor(qAuthor)
        txnQuoteAuthorColor.setBackgroundColor(qBack)
        txnQuoteTextColor.setTextColor(qText)
        txnQuoteTextColor.setBackgroundColor(qBack)
    }
    /** 變更套用新設定按鈕外觀 */
    private fun paintBtnUpdate(enabled: Boolean) {
        btnUpdate.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    /** 按下顏色文字跳出調色盤 */
    private val textClickListener = OnClickListener { view ->
        val colorRes = (view as TextView).text.toString()

        val dialogColorPicker = DialogColorPicker()
        dialogColorPicker.setFromRes(colorRes)
        dialogColorPicker.setListener(object: DialogColorPickerListener {
            override fun onSelectColor(colorRes: String) {
                view.text = colorRes
                paintToolbarButtons()
                paintBtnUpdate(true)
            }
        })
        dialogColorPicker.show()
    }

    /** 還原外觀 */
    private val btnResetOnClickListener = OnClickListener { _->
        // 還原
        val selectedIndex = ThemeStore.getSelectIndex()
        val theme = ThemeStore.getDefaultTheme(selectedIndex)
        ThemeStore.updateTheme(selectedIndex, theme)

        // 套用版面
        paintToolbarText()
        paintToolbarButtons()
        paintBtnUpdate(false)

        mainLayout.clearFocus()

        ASToast.showShortToast(getContextString(R.string.theme_manager_page_msg02))
    }
    /** 更新外觀 */
    private val btnUpdateOnClickListener = OnClickListener { _ ->
        val selectedTheme = ThemeStore.getSelectTheme()
        selectedTheme.name = txnName.text.toString()
        selectedTheme.textColor = txnTextColor.text.toString()
        selectedTheme.textColorPressed = txnTextColorPressed.text.toString()
        selectedTheme.textColorDisabled = txnTextColorDisabled.text.toString()
        selectedTheme.backgroundColor = txnBackColor.text.toString()
        selectedTheme.backgroundColorPressed = txnBackColorPressed.text.toString()
        selectedTheme.backgroundColorDisabled = txnBackColorDisabled.text.toString()
        selectedTheme.headerBackColor = txnHeaderBackColor.text.toString()
        selectedTheme.headerHeaderColor = txnHeaderHeaderColor.text.toString()
        selectedTheme.headerManagerColor = txnHeaderManagerColor.text.toString()
        selectedTheme.headerBorderColor = txnHeaderBorderColor.text.toString()
        selectedTheme.contentBackColor = txnContentBackColor.text.toString()
        selectedTheme.contentAuthorColor = txnContentAuthorColor.text.toString()
        selectedTheme.contentTextColor = txnContentTextColor.text.toString()
        selectedTheme.quoteBackColor = txnQuoteBackColor.text.toString()
        selectedTheme.quoteAuthorColor = txnQuoteAuthorColor.text.toString()
        selectedTheme.quoteTextColor = txnQuoteTextColor.text.toString()

        // 更新
        ThemeStore.updateTheme(ThemeStore.getSelectIndex(), selectedTheme)

        // 套用版面
        paintToolbarText()
        paintToolbarButtons()
        paintBtnUpdate(false)

        // 產生外觀列的按鈕
        val themes = ThemeStore.getThemeStore()
        themes.forEachIndexed { index, theme ->
            val button: Button = buttonIds[index]
            button.text = theme.name
        }

        mainLayout.clearFocus()

        ASToast.showShortToast(getContextString(R.string.theme_manager_page_msg01))
    }

    override fun onBackPressed(): Boolean {
        PageContainer.instance!!.cleanThemeManagerPage()
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }
}