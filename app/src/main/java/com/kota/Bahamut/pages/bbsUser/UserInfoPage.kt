package com.kota.Bahamut.pages.bbsUser

import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.model.PostEditText
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage
import java.util.Vector

class UserInfoPage: TelnetPage() {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var txnNickName: PostEditText
    private lateinit var btnUpdate: Button
    private lateinit var txnOthers: TextView

    override val pageLayout: Int
        get() = R.layout.bbs_user_info_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_USER_INFO_PAGE

    override val isPopupPage: Boolean
        get() = true

    override fun onBackPressed(): Boolean {
        // 返回
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as RelativeLayout
        txnNickName = mainLayout.findViewById(R.id.User_Info_Page_Nick_Name)
        mainLayout.findViewById<Button>(R.id.User_Info_Page_Reset).setOnClickListener { _->
            onBackPressed()
        }
        btnUpdate = mainLayout.findViewById(R.id.User_Info_Page_Update)
        btnUpdate.setOnClickListener(btnUpdateOnClickListener)
        txnOthers = mainLayout.findViewById(R.id.User_Info_Others)

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout)
        paintBtnUpdate(false)
    }

    /** 收到回傳的資料內容 */
    fun updateUserInfoPageContent(rows: Vector<TelnetRow>) {
        val rowString4 = rows[4].toContentString()
        val realName = rows[5].toContentString()
        val address = rows[6].toContentString()
        val eMail = rows[7].toContentString()
        val registerTime = rows[8].toContentString()
        val lastVisitTime = rows[9].toContentString()
        val hp = rows[10].toContentString()
        val mp = rows[11].toContentString()
        val mailCount = rows[12].toContentString()
        val certificateTime = rows[13].toContentString()
        val stayTime = rows[14].toContentString()
        ASCoroutine.ensureMainThread {
            if (rowString4.contains("暱    稱：")) {
                txnNickName.setText(rowString4.replace("暱    稱：", ""))
                txnOthers.text =
                    "$realName\n$address\n$eMail\n$registerTime\n$lastVisitTime\n$hp\n$mp\n$mailCount\n$certificateTime\n$stayTime\n"


                txnNickName.doOnTextChanged { content, _, _, _ ->
                    if (content!!.trim().isEmpty())
                        paintBtnUpdate(false)
                    else
                        paintBtnUpdate(true)
                }
            }
        }

        // 返回
        TelnetClient.myInstance!!.sendStringToServer("N")
    }

    /** 套用新設定 */
    private val btnUpdateOnClickListener = OnClickListener { _ ->
        val builder = TelnetOutputBuilder.create()
            .pushString("I\n")
            .pushString("Y\n")
            .pushString(UserSettings.propertiesPassword+"\n") // 請確認密碼：
            .pushString("\n") // 設定新密碼(不改請按 Enter)
            .pushKey(TelnetKeyboard.CTRL_Y) // 暱    稱：
            .pushString(txnNickName.text.toString()+"\n")
            .pushString("\n") // 真實姓名：
            .pushString("\n") // 居住地址：
            .pushString("Y\n") // 請您確定(Y/N)
            .build()
        TelnetClient.myInstance!!.sendDataToServer(builder)

        ASToast.showShortToast(CommonFunctions.getContextString(R.string.user_info_msg01))
        paintBtnUpdate(false)
    }

    /** 變更套用新設定按鈕外觀 */
    private fun paintBtnUpdate(enabled: Boolean) {
        btnUpdate.isEnabled = enabled
    }
}