package com.kota.Bahamut.Pages

import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.widget.doOnTextChanged
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.Model.PostEditText
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.Pages.Theme.ThemeStore
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.TelnetUI.TelnetPage
import java.util.Vector

class UserPage: TelnetPage() {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var txnNickName: PostEditText
    private lateinit var btnUpdate: Button

    override fun getPageLayout(): Int {
        return R.layout.user_page
    }

    override fun getPageType(): Int {
        return BahamutPage.BAHAMUT_USER_PAGE
    }

    override fun onBackPressed(): Boolean {
        clear()
        PageContainer.getInstance().popClassPage()
        navigationController.popViewController()
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return true
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout
        txnNickName = mainLayout.findViewById(R.id.User_Page_Nick_Name)
        mainLayout.findViewById<Button>(R.id.User_Page_Reset).setOnClickListener { _->
            onBackPressed()
        }
        btnUpdate = mainLayout.findViewById(R.id.User_Page_Update)
        btnUpdate.setOnClickListener(btnUpdateOnClickListener)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout)
        paintBtnUpdate(false)
    }

    override fun onPageDidAppear() {
        // 取得暱稱資料
        TelnetClient.getClient().sendStringToServer("I")
    }

    /** 收到回傳的資料內容 */
    fun updateUserPageContent(rows: Vector<TelnetRow>) {
        val row_string_23: String = rows[23].toContentString();
        if (row_string_23.contains("修改資料(Y/N)?[N]")) {
            val row_string_4 = rows[4].toContentString();
            object : ASRunner() {
                override fun run() {
                    txnNickName.setText(row_string_4.replace("暱    稱：", ""))

                    txnNickName.doOnTextChanged { content, _, _, _ ->
                        if (content!!.trim().isEmpty())
                            paintBtnUpdate(false)
                        else
                            paintBtnUpdate(true)
                    }
                }
            }.runInMainThread()

            // 返回
            TelnetClient.getClient().sendStringToServer("N")
        }
        println(rows)
    }

    /** 套用新設定 */
    private val btnUpdateOnClickListener = OnClickListener { _ ->
        val builder = TelnetOutputBuilder.create()
            .pushString("I\n")
            .pushString("Y\n")
            .pushString(UserSettings.getPropertiesPassword()+"\n") // 請確認密碼：
            .pushString("\n") // 設定新密碼(不改請按 Enter)
            .pushKey(TelnetKeyboard.CTRL_Y) // 暱    稱：
            .pushString(txnNickName.text.toString()+"\n")
            .pushString("\n") // 真實姓名：
            .pushString("\n") // 居住地址：
            .pushString("Y\n") // 請您確定(Y/N)
            .build()
        TelnetClient.getClient().sendDataToServer(builder)

        ASToast.showShortToast(CommonFunctions.getContextString(R.string.user_info_msg01))
        paintBtnUpdate(false)
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
}