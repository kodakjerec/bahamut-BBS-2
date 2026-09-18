package com.kota.Bahamut.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.login.WebAutoSignInManager
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.components.BahaCheckbox
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.ui.ASToast

class DialogWebLoginSettings(private val onSaved: (() -> Unit)? = null) : ASDialog() {

    override val name: String?
        get() = "BahamutWebLoginSettingsDialog"

    init {
        setTitle(CommonFunctions.getContextString(R.string.login_web_settings_title))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val context = LocalContext.current
        val colors = AppTheme.colors

        var username by remember { mutableStateOf(UserSettings.propertiesWebUsername) }
        var password by remember { mutableStateOf(UserSettings.propertiesWebPassword) }
        var showDebugView by remember { mutableStateOf(WebAutoSignInManager.showDebugView) }

        BahaAlertDialogContent(
            title = CommonFunctions.getContextString(R.string.login_web_settings_title),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.SECONDARY,
                    onClick = { dismiss() }
                ),
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.confirm),
                    type = ButtonType.NORMAL,
                    onClick = {
                        val newUsername = username.trim()
                        val newPassword = password
                        UserSettings.propertiesWebUsername = newUsername
                        UserSettings.propertiesWebPassword = newPassword
                        WebAutoSignInManager.showDebugView = showDebugView

                        ASToast.showShortToast(context.getString(R.string.login_web_settings_saved))
                        dismiss()
                        onSaved?.invoke()
                    }
                )
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BahaText(
                    text = CommonFunctions.getContextString(R.string.login_web_settings_tip),
                    color = colors.textPrimary,
                    fontSize = BahaTextSize.BODY,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                BahaText(
                    text = CommonFunctions.getContextString(R.string.account),
                    color = colors.textPrimary,
                    fontSize = BahaTextSize.SUBTITLE,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                BahaInputField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "",
                    singleLine = true,
                    maxLength = 20,
                    height = 42.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                BahaText(
                    text = CommonFunctions.getContextString(R.string.password),
                    color = colors.textPrimary,
                    fontSize = BahaTextSize.SUBTITLE,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                BahaInputField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "",
                    singleLine = true,
                    isPassword = true,
                    maxLength = 30,
                    height = 42.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showDebugView = !showDebugView }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaCheckbox(
                        checked = showDebugView,
                        onCheckedChange = { showDebugView = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BahaText(
                        text = CommonFunctions.getContextString(R.string.login_web_debug_view_toggle),
                        color = colors.textPrimary,
                        fontSize = BahaTextSize.CAPTION
                    )
                }
            }
        }
    }
}
