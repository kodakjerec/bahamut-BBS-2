package com.kota.Bahamut.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetOutputBuilder

class DialogHeroStep : ASDialog() {
    private var isClickButton = false

    override val name: String?
        get() = "BahamutHeroStepDialog"

    init {
        setTitle(CommonFunctions.getContextString(R.string.main_hero_step))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        var content1 by remember { mutableStateOf("") }
        var content2 by remember { mutableStateOf("") }
        var content3 by remember { mutableStateOf("") }
        val colors = AppTheme.colors

        BahaAlertDialogContent(
            title = CommonFunctions.getContextString(R.string.main_hero_step),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.SECONDARY,
                    onClick = {
                        isClickButton = true
                        val builder = TelnetOutputBuilder.create()
                            .pushString("\n")
                            .pushString("\n")
                            .build()
                        TelnetClient.myInstance?.sendDataToServer(builder)
                        dismiss()
                    }
                ),
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.send),
                    type = ButtonType.NORMAL,
                    onClick = {
                        isClickButton = true
                        var sendContent = ""
                        if (content1.isNotEmpty()) sendContent += "$content1\n"
                        if (content2.isNotEmpty()) sendContent += "$content2\n"
                        if (content3.isNotEmpty()) sendContent += "$content3\n"
                        if (sendContent.isNotEmpty()) {
                            val builder = TelnetOutputBuilder.create()
                                .pushString(sendContent)
                                .pushString("\n")
                                .pushString("\n")
                                .build()
                            TelnetClient.myInstance?.sendDataToServer(builder)
                            ASToast.showShortToast(CommonFunctions.getContextString(R.string.main_hero_success01))
                        } else {
                            val builder = TelnetOutputBuilder.create()
                                .pushString("\n")
                                .pushString("\n")
                                .build()
                            TelnetClient.myInstance?.sendDataToServer(builder)
                        }
                        dismiss()
                    }
                )
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = CommonFunctions.getContextString(R.string.main_hero_step_msg01),
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val textFieldColors = TextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.pageBackground,
                    unfocusedContainerColor = colors.pageBackground,
                    focusedIndicatorColor = colors.toolbarBackgroundFocused,
                    unfocusedIndicatorColor = colors.divider
                )

                OutlinedTextField(
                    value = content1,
                    onValueChange = { if (it.length <= 48) content1 = it },
                    placeholder = { Text(CommonFunctions.getContextString(R.string.number_1), color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content2,
                    onValueChange = { if (it.length <= 48) content2 = it },
                    placeholder = { Text(CommonFunctions.getContextString(R.string.number_2), color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content3,
                    onValueChange = { if (it.length <= 48) content3 = it },
                    placeholder = { Text(CommonFunctions.getContextString(R.string.number_3), color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        // 不是正常的按按鈕消失
        if (!isClickButton) {
            val builder = TelnetOutputBuilder.create()
                .pushString("\n")
                .pushString("\n")
                .build()
            TelnetClient.myInstance?.sendDataToServer(builder)
        }
    }
}