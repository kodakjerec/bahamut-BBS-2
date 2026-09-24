package com.kota.Bahamut.pages.theme

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.TempSettings
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASAlertDialogListener
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage

class ThemeManagerPage : TelnetPage() {
    private lateinit var mainLayout: LinearLayout

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

        val themeNames = listOf("預設", "粉紅", "EInk")
        val buttonIds = listOf<Button>(
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_0),
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_1),
            mainLayout.findViewById(R.id.Theme_Manager_Page_Button_2)
        )

        // 隱藏預留但未使用的按鈕 3 與 4
        mainLayout.findViewById<View>(R.id.Theme_Manager_Page_Button_3)?.visibility = View.GONE
        mainLayout.findViewById<View>(R.id.Theme_Manager_Page_Button_4)?.visibility = View.GONE

        themeNames.forEachIndexed { index, name ->
            if (index < buttonIds.size) {
                val button: Button = buttonIds[index]
                button.text = name
                button.setOnClickListener { _ ->
                    if (index == ThemeStore.getSelectIndex()) {
                        return@setOnClickListener
                    }

                    val dialog = ASAlertDialog("THEME_CHANGE_CONFIRM")
                    dialog.setTitle("更換外觀")
                        .setMessage("更換外觀將會中斷目前的連線並重新啟動應用程式，是否確定更換?")
                        .addButton("取消")
                        .addButton("確定")
                        .setListener(object : ASAlertDialogListener {
                            override fun onAlertDialogDismissWithButtonIndex(
                                paramASAlertDialog: ASAlertDialog,
                                paramInt: Int
                            ) {
                                if (paramInt == 1) {
                                    ThemeStore.setSelectIndex(index)
                                    // 更換主題時使用系統 Toast
                                    android.widget.Toast.makeText(
                                        context,
                                        getContextString(R.string.theme_manager_page_msg01),
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    // 執行斷線流程
                                    TelnetClient.myInstance?.close()
                                    TempSettings.lastVisitArticleNumber = 0

                                    // 立即重啟 Activity 以套用原生主題
                                    context?.recreate()
                                }
                            }
                        }).show()
                }
            }
        }

        findViewById(R.id.Theme_Manager_Page_Toolbar_Back)?.setOnClickListener { onBackPressed() }
    }

    override fun onPageWillAppear() {
        super.onPageWillAppear()
        navigationController.setNavigationTitle(getContextString(R.string.theme_manager_page))
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
