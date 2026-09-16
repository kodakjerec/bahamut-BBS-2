package com.kota.Bahamut.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * 處理中載入內容本體 Composable (可用於預覽或自訂容器)
 */
@Composable
fun BahaProcessingDialogContent(
    modifier: Modifier = Modifier,
    message: String = "處理中..."
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.pageBackground)
            .border(1.dp, colors.dialogBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = colors.titleBarTitle,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                color = colors.textPrimary,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * 專案通用處理中 / 連線中載入對話框 (Processing Dialog) Composable
 * 取代舊有 ASProcessingDialog
 */
@Composable
fun BahaProcessingDialog(
    message: String = "處理中...",
    onDismissRequest: (() -> Unit)? = null,
    isCancelable: Boolean = false
) {
    Dialog(
        onDismissRequest = {
            if (isCancelable && onDismissRequest != null) {
                onDismissRequest()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isCancelable,
            dismissOnClickOutside = false
        )
    ) {
        BahaProcessingDialogContent(message = message)
    }
}

