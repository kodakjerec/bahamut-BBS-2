package com.kota.Bahamut.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.ui.ASToast

class DialogSelectArticle : ASDialog() {
    var dialogSelectArticleListener: DialogSelectArticleListener? = null

    override val name: String?
        get() = "BahamutBoardSelectDialog"

    init {
        setTitle(CommonFunctions.getContextString(R.string.select_article))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        var contentString by remember { mutableStateOf("") }
        val colors = AppTheme.colors

        fun onDigit(d: String) {
            if (contentString.length < 5) {
                contentString += d
            }
        }

        fun onBackspace() {
            if (contentString.isNotEmpty()) {
                contentString = contentString.dropLast(1)
            }
        }

        BahaAlertDialogContent(
            title = CommonFunctions.getContextString(R.string.select_article),
            contentPadding = PaddingValues(0.dp),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.DANGER,
                    onClick = { dismiss() }
                ),
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.search),
                    type = ButtonType.DANGER,
                    onClick = {
                        if (contentString.isEmpty()) {
                            ASToast.showShortToast(CommonFunctions.getContextString(R.string.please_input_article_number))
                            return@BahaDialogButton
                        }
                        dialogSelectArticleListener?.onSelectDialogDismissWIthIndex(contentString)
                        dismiss()
                    }
                )
            )
        ) {
            // 顯示目前輸入的數字
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                BahaText(
                    text = if (contentString.isEmpty()) CommonFunctions.getContextString(R.string.please_input_article_number) else contentString,
                    color = if (contentString.isEmpty()) colors.textSecondary else colors.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider)
            )

            // 數字鍵盤
            val rows = listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    row.forEachIndexed { index, digit ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(colors.divider)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(colors.dialogBlockBackground)
                                .clickable { onDigit(digit) },
                            contentAlignment = Alignment.Center
                        ) {
                            BahaText(text = digit, color = colors.textPrimary)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.divider)
                )
            }

            // 0 與 DEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .background(colors.dialogBlockBackground)
                        .clickable { onDigit("0") },
                    contentAlignment = Alignment.Center
                ) {
                    BahaText(text = "0", color = colors.textPrimary)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.divider)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors.dialogBlockBackground)
                        .clickable { onBackspace() },
                    contentAlignment = Alignment.Center
                ) {
                    BahaText(
                        text = CommonFunctions.getContextString(R.string.del),
                        color = colors.textPrimary
                    )
                }
            }
        }
    }

    fun setListener(listener: DialogSelectArticleListener?) {
        this.dialogSelectArticleListener = listener
    }
}
