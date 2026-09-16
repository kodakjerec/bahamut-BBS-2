package com.kota.Bahamut.pages.bbsUser

import android.content.Context
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnetUI.TelnetPage
import java.util.Vector

class UserConfigPage : TelnetPage() {

    override val pageLayout: Int
        get() = 0

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_USER_CONFIG_PAGE

    override val isPopupPage: Boolean
        get() = true

    // State for checkboxes: "0".."9", "A", "B"
    val configCheckStates = mutableStateMapOf<String, Boolean>()
    var isCantExpandState by mutableStateOf(false)

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                UserConfigPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        TelnetClient.myInstance!!.sendStringToServerInBackground("")
        TelnetClient.myInstance!!.sendKeyboardInputToServerInBackground(TelnetKeyboard.LEFT_ARROW, 1)
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        ASToast.showShortToast("返回")
        return true
    }

    /** 收到回傳的資料內容 */
    fun updateUserConfigPageContent(rows: Vector<TelnetRow>) {
        ASCoroutine.ensureMainThread {
            fun isChecked(rowIdx: Int): Boolean {
                return if (rowIdx < rows.size) {
                    val s = rows[rowIdx].toContentString()
                    if (s.length >= 3) s.substring(2, 3) == "■" else false
                } else false
            }

            configCheckStates["0"] = isChecked(5)
            configCheckStates["1"] = isChecked(6)
            configCheckStates["2"] = isChecked(7)
            configCheckStates["3"] = isChecked(8)
            configCheckStates["4"] = isChecked(9)
            configCheckStates["5"] = isChecked(10)
            configCheckStates["6"] = isChecked(11)
            configCheckStates["7"] = isChecked(12)
            configCheckStates["8"] = isChecked(13)
            configCheckStates["9"] = isChecked(14)
            configCheckStates["A"] = isChecked(15)
            configCheckStates["B"] = isChecked(16)
        }
    }

    /** 切換操作模式選項 */
    private fun changeOperationMode(mode: String) {
        TelnetClient.myInstance!!.sendStringToServerInBackground(mode)
        configCheckStates[mode] = !(configCheckStates[mode] ?: false)
        ASToast.showShortToast(CommonFunctions.getContextString(R.string.user_config_msg01))
    }

    @Composable
    fun UserConfigPageContent() {
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
                // Chapter header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.toolbarBackground)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.user_config),
                        color = colors.titleBarTitle,
                        fontSize = 14.sp
                    )
                }

                // Interactive items
                ConfigItem(
                    title = stringResource(R.string.user_config_5),
                    isChecked = configCheckStates["5"] ?: false,
                    enabled = true,
                    onClick = { changeOperationMode("5") }
                )
                ConfigItem(
                    title = stringResource(R.string.user_config_6),
                    isChecked = configCheckStates["6"] ?: false,
                    enabled = true,
                    onClick = { changeOperationMode("6") }
                )
                ConfigItem(
                    title = stringResource(R.string.user_config_8),
                    isChecked = configCheckStates["8"] ?: false,
                    enabled = true,
                    onClick = { changeOperationMode("8") }
                )
                ConfigItem(
                    title = stringResource(R.string.user_config_9),
                    isChecked = configCheckStates["9"] ?: false,
                    enabled = true,
                    onClick = { changeOperationMode("9") }
                )
                ConfigItem(
                    title = stringResource(R.string.user_config_A),
                    isChecked = configCheckStates["A"] ?: false,
                    enabled = true,
                    onClick = { changeOperationMode("A") }
                )

                // Expandable Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.toolbarBackground)
                        .clickable { isCantExpandState = !isCantExpandState }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.user_config_cant),
                        color = colors.titleBarTitle,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isCantExpandState) stringResource(R.string.post_toolbar_collapse) else stringResource(R.string.post_toolbar_show),
                        color = colors.titleBarDetail,
                        fontSize = 12.sp
                    )
                }

                AnimatedVisibility(visible = isCantExpandState) {
                    Column {
                        ConfigItem(
                            title = stringResource(R.string.user_config_0),
                            isChecked = configCheckStates["0"] ?: false,
                            enabled = false,
                            onClick = {}
                        )
                        ConfigItem(
                            title = stringResource(R.string.user_config_1),
                            isChecked = configCheckStates["1"] ?: false,
                            enabled = false,
                            onClick = {}
                        )
                        ConfigItem(
                            title = stringResource(R.string.user_config_2),
                            isChecked = configCheckStates["2"] ?: false,
                            enabled = false,
                            onClick = {}
                        )
                        ConfigItem(
                            title = stringResource(R.string.user_config_3),
                            isChecked = configCheckStates["3"] ?: false,
                            enabled = false,
                            onClick = {}
                        )
                        ConfigItem(
                            title = stringResource(R.string.user_config_4),
                            isChecked = configCheckStates["4"] ?: false,
                            enabled = false,
                            onClick = {}
                        )
                        ConfigItem(
                            title = stringResource(R.string.user_config_7),
                            isChecked = configCheckStates["7"] ?: false,
                            enabled = false,
                            onClick = {}
                        )
                        ConfigItem(
                            title = stringResource(R.string.user_config_B),
                            isChecked = configCheckStates["B"] ?: false,
                            enabled = false,
                            onClick = {}
                        )
                    }
                }
            }

            // Bottom toolbar
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            BahaButton(
                text = stringResource(R.string._back),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onBackPressed() }
            )
        }
    }

    @Composable
    private fun ConfigItem(
        title: String,
        isChecked: Boolean,
        enabled: Boolean,
        onClick: () -> Unit
    ) {
        val colors = AppTheme.colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (enabled) colors.textPrimary else colors.textSecondary,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = isChecked,
                onCheckedChange = if (enabled) { { onClick() } } else null,
                enabled = enabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.checkboxTint,
                    uncheckedColor = colors.textSecondary,
                    disabledCheckedColor = colors.checkboxTint.copy(alpha = 0.5f),
                    disabledUncheckedColor = colors.textSecondary.copy(alpha = 0.5f)
                )
            )
        }
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }
}