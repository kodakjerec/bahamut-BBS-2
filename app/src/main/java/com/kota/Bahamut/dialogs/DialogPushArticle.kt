package com.kota.Bahamut.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

class DialogPushArticle : ASDialog() {
    private var isClickButton = false

    override val name: String?
        get() = "BahamutPushArticleDialog"

    init {
        setTitle(CommonFunctions.getContextString(R.string.do_push))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        var textContent by remember { mutableStateOf("") }
        val colors = AppTheme.colors

        BahaAlertDialogContent(
            title = CommonFunctions.getContextString(R.string.do_push),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.SECONDARY,
                    onClick = {
                        isClickButton = true
                        val builder = TelnetOutputBuilder.create()
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
                        if (textContent.isNotEmpty()) {
                            val builder = TelnetOutputBuilder.create()
                                .pushString(textContent)
                                .pushString("\n")
                                .build()
                            TelnetClient.myInstance?.sendDataToServer(builder)
                            ASToast.showShortToast(CommonFunctions.getContextString(R.string.main_push_article_success01))
                        } else {
                            val builder = TelnetOutputBuilder.create()
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
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { if (it.length <= 48) textContent = it },
                    placeholder = {
                        Text(
                            CommonFunctions.getContextString(R.string.main_push_article_msg01),
                            color = colors.textSecondary,
                            fontSize = 16.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.pageBackground,
                        unfocusedContainerColor = colors.pageBackground,
                        focusedIndicatorColor = colors.toolbarBackgroundFocused,
                        unfocusedIndicatorColor = colors.divider
                    )
                )
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        // 不是正常的按按鈕消失
        if (!isClickButton) {
            val builder = TelnetOutputBuilder.create()
                .pushString("\n") // 按[Enter]結束
                .build()
            TelnetClient.myInstance?.sendDataToServer(builder)
        }
    }
}