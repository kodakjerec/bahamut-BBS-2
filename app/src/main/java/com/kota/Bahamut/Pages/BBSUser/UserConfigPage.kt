package com.kota.Bahamut.Pages.BBSUser

import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.widget.doOnTextChanged
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.Pages.Model.PostEditText
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetOutputBuilder
import com.kota.TelnetUI.TelnetPage
import java.util.Vector

class UserConfigPage: TelnetPage() {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var txnNickName: PostEditText
    private lateinit var btnUpdate: Button

    override fun getPageLayout(): Int {
        return R.layout.bbs_user_config_page
    }

    override fun getPageType(): Int {
        return BahamutPage.BAHAMUT_USER_CONFIG_PAGE
    }

    override fun onBackPressed(): Boolean {
        // 返回
        TelnetClient.getClient().sendStringToServerInBackground("")
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout
        mainLayout.findViewById<Button>(R.id.User_Config_Page_Reset).setOnClickListener { _->
            onBackPressed()
        }
    }

    override fun onPageDidAppear() {
        // 取得暱稱資料
        TelnetClient.getClient().sendStringToServer("C")
    }

    /** 收到回傳的資料內容 */
    fun updateUserConfigPageContent(rows: Vector<TelnetRow>) {
        val rowString5 = rows[5].toContentString()
        println(rowString5)
    }
}