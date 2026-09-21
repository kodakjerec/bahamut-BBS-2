package com.kota.Bahamut.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.AppThemeStyle

/**
 * 對話框預覽展示組合包
 */
@Composable
private fun DialogShowcase() {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.pageBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Alert Dialog 預覽
        BahaText(
            text = "BahaAlertDialog (提示與確認)",
            color = colors.textPrimary,
            fontSize = AppTheme.fontSize.body
        )
        BahaAlertDialogContent(
            title = "系統提示",
            message = "確定要登出目前帳號並中斷 BBS 連線嗎？",
            buttons = listOf(
                BahaDialogButton(text = "取消", type = ButtonType.SECONDARY, onClick = {}),
                BahaDialogButton(text = "登出", type = ButtonType.DANGER, onClick = {})
            )
        )

        // 2. Processing Dialog 預覽
        BahaText(
            text = "BahaProcessingDialog (連線與載入)",
            color = colors.textPrimary,
            fontSize = AppTheme.fontSize.body
        )
        BahaProcessingDialog(
            message = "連線至 bbs.gamer.com.tw..."
        )

        // 3. List Dialog 預覽
        BahaText(
            text = "BahaListDialog (清單選擇)",
            color = colors.textPrimary,
            fontSize = AppTheme.fontSize.body
        )
        BahaListDialogContent(
            title = "選擇表情符號",
            items = listOf("(^o^)/ 歡呼", "(T_T) 流淚", "(>_<) 難過", "(*'▽'*) 燦笑", "(￣▽￣) 裝傻"),
            selectedIndex = 1,
            onItemSelected = { _, _ -> }
        )
    }
}

// ---------------------------------------------------------
// 預設主題 (DEFAULT)
// ---------------------------------------------------------
@Preview(name = "Default - Light", group = "Dialog_Default")
@Composable
fun PreviewDialogDefaultLight() {
    AppTheme(style = AppThemeStyle.DEFAULT, darkTheme = false) {
        DialogShowcase()
    }
}

@Preview(name = "Default - Dark", group = "Dialog_Default")
@Composable
fun PreviewDialogDefaultDark() {
    AppTheme(style = AppThemeStyle.DEFAULT, darkTheme = true) {
        DialogShowcase()
    }
}

// ---------------------------------------------------------
// 粉紅主題 (PINK)
// ---------------------------------------------------------
@Preview(name = "Pink - Light", group = "Dialog_Pink")
@Composable
fun PreviewDialogPinkLight() {
    AppTheme(style = AppThemeStyle.PINK, darkTheme = false) {
        DialogShowcase()
    }
}

@Preview(name = "Pink - Dark", group = "Dialog_Pink")
@Composable
fun PreviewDialogPinkDark() {
    AppTheme(style = AppThemeStyle.PINK, darkTheme = true) {
        DialogShowcase()
    }
}

// ---------------------------------------------------------
// eInk 電子紙主題 (EINK)
// ---------------------------------------------------------
@Preview(name = "eInk - Light", group = "Dialog_eInk")
@Composable
fun PreviewDialogEInkLight() {
    AppTheme(style = AppThemeStyle.EINK, darkTheme = false) {
        DialogShowcase()
    }
}

@Preview(name = "eInk - Dark", group = "Dialog_eInk")
@Composable
fun PreviewDialogEInkDark() {
    AppTheme(style = AppThemeStyle.EINK, darkTheme = true) {
        DialogShowcase()
    }
}

