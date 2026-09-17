package com.kota.Bahamut.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.ButtonType
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

        Box(
            modifier = Modifier
                .widthIn(min = 260.dp, max = 300.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.pageBackground)
                .border(1.dp, colors.dialogBorder, RoundedCornerShape(6.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 標題列
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.dialogTitleBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = CommonFunctions.getContextString(R.string.select_article),
                        color = colors.titleBarTitle,
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.divider)
                )

                // 顯示目前輸入的數字
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (contentString.isEmpty()) CommonFunctions.getContextString(R.string.please_input_article_number) else contentString,
                        color = if (contentString.isEmpty()) colors.textSecondary else colors.textPrimary,
                        fontSize = if (contentString.isEmpty()) 15.sp else 24.sp,
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
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEachIndexed { index, digit ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable { onDigit(digit) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = digit, color = colors.textPrimary, fontSize = 20.sp)
                            }
                            if (index < row.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .padding(vertical = 4.dp)
                                        .border(0.5.dp, colors.divider)
                                )
                            }
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                }

                // 0 與 DEL
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { onDigit("0") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "0", color = colors.textPrimary, fontSize = 20.sp)
                    }
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(vertical = 4.dp)
                            .border(0.5.dp, colors.divider)
                    )
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .clickable { onBackspace() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = CommonFunctions.getContextString(R.string.del), color = colors.textPrimary, fontSize = 18.sp)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))

                // 底部按鈕列
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.dialogTitleBackground)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    BahaButton(
                        text = CommonFunctions.getContextString(R.string.cancel),
                        type = ButtonType.SECONDARY,
                        onClick = { dismiss() },
                        modifier = Modifier.weight(1f, fill = false),
                        minHeight = 36.dp
                    )
                    BahaButton(
                        text = CommonFunctions.getContextString(R.string.search),
                        type = ButtonType.NORMAL,
                        onClick = {
                            if (contentString.isEmpty()) {
                                ASToast.showShortToast(CommonFunctions.getContextString(R.string.please_input_article_number))
                                return@BahaButton
                            }
                            dialogSelectArticleListener?.onSelectDialogDismissWIthIndex(contentString)
                            dismiss()
                        },
                        modifier = Modifier.weight(1f, fill = false),
                        minHeight = 36.dp
                    )
                }
            }
        }
    }

    fun setListener(listener: DialogSelectArticleListener?) {
        this.dialogSelectArticleListener = listener
    }
}
