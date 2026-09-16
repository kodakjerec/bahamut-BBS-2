package com.kota.Bahamut.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.theme.AppTheme

data class BahaDialogButton(
    val text: String,
    val type: ButtonType = ButtonType.NORMAL,
    val onClick: () -> Unit
)

/**
 * 對話框內容本體 Composable (可用於預覽或自訂容器)
 */
@Composable
fun BahaAlertDialogContent(
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    buttons: List<BahaDialogButton> = emptyList(),
    content: (@Composable () -> Unit)? = null
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .widthIn(min = 280.dp, max = 340.dp)
            .background(colors.pageBackground)
            .border(1.5.dp, colors.dialogBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. 標題列
            if (!title.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.dialogTitleBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.divider)
                )
            }

            // 2. 內容文字或客製化區塊
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (!message.isNullOrEmpty()) {
                    Text(
                        text = message,
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                if (content != null) {
                    if (!message.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    content()
                }
            }

            // 3. 底部按鈕列 (滿版無縫，深紅底白字，按鈕間帶垂直細線)
            if (buttons.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.dialogButtonDivider)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    buttons.forEachIndexed { index, btn ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(colors.dialogButtonDivider)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(colors.dialogButtonBackground)
                                .clickable { btn.onClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = btn.text,
                                color = colors.dialogButtonText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 專案通用提示與確認對話框 (Alert Dialog) Composable
 * 取代舊有 ASAlertDialog，具備多主題與深淺色模式自適應邊框與標題色
 */
@Composable
fun BahaAlertDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    message: String? = null,
    buttons: List<BahaDialogButton> = emptyList(),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: (@Composable () -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        BahaAlertDialogContent(
            title = title,
            message = message,
            buttons = buttons,
            content = content
        )
    }
}

/**
 * 簡易確認 / 取消對話框過載函式
 */
@Composable
fun BahaConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmText: String = "確定",
    dismissText: String = "取消",
    confirmButtonType: ButtonType = ButtonType.NORMAL
) {
    BahaAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        buttons = listOf(
            BahaDialogButton(text = dismissText, type = ButtonType.SECONDARY, onClick = onDismissRequest),
            BahaDialogButton(text = confirmText, type = confirmButtonType, onClick = onConfirm)
        )
    )
}
