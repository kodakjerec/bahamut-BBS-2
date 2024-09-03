package com.kota.Bahamut.Pages.Theme

import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.Dialogs.DialogColorPicker
import com.kota.Bahamut.Dialogs.DialogColorPickerListener
import com.kota.Bahamut.Pages.Model.PostEditText
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.TelnetUI.TelnetPage


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
    private lateinit var btnReset:Button
    private lateinit var btnUpdate:Button

    override fun getPageType(): Int {
        return BahamutPage.BAHAMUT_THEME_MANAGER_PAGE
    }

    override fun getPageLayout(): Int {
        return R.layout.theme_manager_page
    }

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

        btnReset = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Reset)
        btnReset.setOnClickListener(btnResetOnClickListener)

        btnUpdate = mainLayout.findViewById(R.id.Theme_Manager_Page_Toolbar_Update)
        btnUpdate.setOnClickListener(btnUpdateOnClickListener)

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
    }
    /** 變更套用新設定按鈕外觀 */
    private fun paintBtnUpdate(enabled: Boolean) {
        val selectedTheme = ThemeStore.getSelectTheme()
        btnUpdate.isEnabled = enabled
        if (enabled) {
            btnUpdate.setTextColor(CommonFunctions.rgbToInt(selectedTheme.textColor))
            btnUpdate.setBackgroundColor(CommonFunctions.rgbToInt(selectedTheme.backgroundColor))
        } else {
            btnUpdate.setTextColor(CommonFunctions.rgbToInt(selectedTheme.textColorDisabled))
            btnUpdate.setBackgroundColor(CommonFunctions.rgbToInt(selectedTheme.backgroundColorDisabled))
        }
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
}