package com.kota.Bahamut.Pages.BBSUser

import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.TelnetUI.TelnetPage
import java.util.Vector

class UserConfigPage: TelnetPage() {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var cantExpandLayout: LinearLayout
    
    override fun getPageLayout(): Int {
        return R.layout.bbs_user_config_page
    }

    override fun getPageType(): Int {
        return BahamutPage.BAHAMUT_USER_CONFIG_PAGE
    }

    override fun isPopupPage(): Boolean {
        return true
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
        cantExpandLayout = mainLayout.findViewById(R.id.User_Config_Layout_Cant_Expand_Layout)
        mainLayout.findViewById<RelativeLayout>(R.id.User_Config_Layout_Cant).setOnClickListener(showExpandLayoutListener)
        mainLayout.findViewById<Button>(R.id.User_Config_Page_Reset).setOnClickListener { _->
            onBackPressed()
        }
        // 給予操作模式變更的 onCheckListener
        mainLayout.findViewById<RelativeLayout>(R.id.User_Config_Item_5).setOnClickListener { _-> changeOperationMode(5) }
        mainLayout.findViewById<RelativeLayout>(R.id.User_Config_Item_6).setOnClickListener { _-> changeOperationMode(6) }
        mainLayout.findViewById<RelativeLayout>(R.id.User_Config_Item_8).setOnClickListener { _-> changeOperationMode(8) }
        mainLayout.findViewById<RelativeLayout>(R.id.User_Config_Item_9).setOnClickListener { _-> changeOperationMode(9) }
        mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_5_Enable).setOnClickListener { _-> changeOperationMode(5) }
        mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_6_Enable).setOnClickListener { _-> changeOperationMode(6) }
        mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_8_Enable).setOnClickListener { _-> changeOperationMode(8) }
        mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_9_Enable).setOnClickListener { _-> changeOperationMode(9) }
    }

    /** 收到回傳的資料內容 */
    fun updateUserConfigPageContent(rows: Vector<TelnetRow>) {
        object:ASRunner() {
            override fun run() {
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_0_Enable).isChecked = rows[5].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_1_Enable).isChecked = rows[6].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_2_Enable).isChecked = rows[7].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_3_Enable).isChecked = rows[8].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_4_Enable).isChecked = rows[9].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_5_Enable).isChecked = rows[10].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_6_Enable).isChecked = rows[11].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_7_Enable).isChecked = rows[12].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_8_Enable).isChecked = rows[13].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_9_Enable).isChecked = rows[14].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_A_Enable).isChecked = rows[15].toContentString().substring(2,3) == "■"
                mainLayout.findViewById<CheckBox>(R.id.User_Config_Item_B_Enable).isChecked = rows[16].toContentString().substring(2,3) == "■"
            }

        }.runInMainThread()
    }

    /** 展開/摺疊 不適用區塊 */
    private val showExpandLayoutListener = OnClickListener { view ->
        val textView: TextView = mainLayout.findViewById(R.id.User_Config_Layout_Cant_Text)
        if (textView.text.equals(CommonFunctions.getContextString(R.string.post_toolbar_show))) {
            textView.text = CommonFunctions.getContextString(R.string.post_toolbar_collapse)
            cantExpandLayout.visibility = GONE
        } else {
            textView.text = CommonFunctions.getContextString(R.string.post_toolbar_show)
            cantExpandLayout.visibility = VISIBLE

        }

    }

    /** 切換操作模式選項 */
    private fun changeOperationMode(mode: Int) {
        TelnetClient.getClient().sendStringToServerInBackground(mode.toString())
        ASToast.showShortToast(CommonFunctions.getContextString(R.string.user_config_msg01))
    }
}