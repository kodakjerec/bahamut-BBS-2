package com.kota.Bahamut.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog

class DialogSelectSign : ASDialog() {
    var dialogSelectSignListener: DialogSelectSignListener? = null

    override val name: String?
        get() = "BahamutSelectSignDialog"

    init {
        setTitle(CommonFunctions.getContextString(R.string.select_sign))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        var signText by remember { mutableStateOf("0") }
        val colors = AppTheme.colors

        BahaAlertDialogContent(
            title = CommonFunctions.getContextString(R.string.select_sign),
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
                        dialogSelectSignListener?.onSelectSign(signText.replace("\n", ""))
                        dismiss()
                    }
                )
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = CommonFunctions.getContextString(R.string.select_sign_msg),
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = signText,
                    onValueChange = { input ->
                        signText = input.filter { it.isDigit() }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
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

    fun setListener(listener: DialogSelectSignListener?) {
        this.dialogSelectSignListener = listener
    }
}
