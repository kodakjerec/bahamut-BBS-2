package com.kota.Bahamut.pages.bbsUser

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage
import java.util.Vector

class UserInfoPage : TelnetPage() {

    // Compose state
    var nickNameState by mutableStateOf("")
    var othersState by mutableStateOf("")
    var isUpdateEnabled by mutableStateOf(false)

    override val pageLayout: Int
        get() = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_USER_INFO_PAGE

    override val isPopupPage: Boolean
        get() = true

    override fun onBackPressed(): Boolean {
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                UserInfoPageContent()
                BahaGlobalDialogHost()
            }
        }
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
                nickNameState = rowString4.replace("暱    稱：", "")
                othersState =
                    "$realName\n$address\n$eMail\n$registerTime\n$lastVisitTime\n$hp\n$mp\n$mailCount\n$certificateTime\n$stayTime\n"
            }
        }
        TelnetClient.myInstance!!.sendStringToServer("N")
    }

    private fun onUpdateClicked() {
        val builder = TelnetOutputBuilder.create()
            .pushString("I\n")
            .pushString("Y\n")
            .pushString(UserSettings.propertiesPassword + "\n")
            .pushString("\n")
            .pushKey(TelnetKeyboard.CTRL_Y)
            .pushString(nickNameState + "\n")
            .pushString("\n")
            .pushString("\n")
            .pushString("Y\n")
            .build()
        TelnetClient.myInstance!!.sendDataToServer(builder)
        ASToast.showShortToast(CommonFunctions.getContextString(R.string.user_info_msg01))
        isUpdateEnabled = false
    }

    // ---------------------------------------------------------------
    // Compose UI
    // ---------------------------------------------------------------

    @Composable
    fun UserInfoPageContent() {
        val colors = AppTheme.colors
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // 標題
                BahaText(
                    text = stringResource(R.string.user_info),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.toolbarBackground)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = colors.divider, thickness = 1.dp)

                // 暱稱輸入列
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.user_info_nick_name),
                        modifier = Modifier.weight(1f)
                    )
                    BahaInputField(
                        value = nickNameState,
                        onValueChange = { newVal ->
                            nickNameState = newVal
                            isUpdateEnabled = newVal.trim().isNotEmpty()
                        },
                        modifier = Modifier.weight(3f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }

                // 套用按鈕 (隱藏/顯示)
                if (isUpdateEnabled) {
                    BahaButton(
                        text = stringResource(R.string.theme_manager_page_chapter_update),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onUpdateClicked() }
                    )
                }

                HorizontalDivider(color = colors.divider, thickness = 1.dp)

                // 其他資料
                BahaText(
                    text = othersState,
                    color = colors.textSecondary,
                    fontSize = AppTheme.fontSize.subtitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            // 2. 底部固定工具列（移出滾動區域，放在外層 Column 最底端）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            BahaButton(
                text = stringResource(R.string._back),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                onClick = { onBackPressed() }
            )
        }
    }
}