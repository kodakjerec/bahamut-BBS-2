package com.kota.Bahamut.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 主題元件預覽內容
 */
@Composable
fun ThemeShowcase(
    themeName: String,
    style: AppThemeStyle,
    isDark: Boolean
) {
    AppTheme(style = style, darkTheme = isDark) {
        val colors = AppTheme.colors

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.pageBackground)
                .padding(16.dp)
        ) {
            // 標題
            Text(
                text = "$themeName (${if (isDark) "Dark" else "Light"})",
                color = colors.textPrimary,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 1. 模擬頂部標題列 (TitleBar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.toolbarBackground)
                    .border(1.dp, colors.dialogBorder, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "看板名稱 (Title)",
                            color = colors.titleBarTitle,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "文章標題與副標 (Detail)",
                            color = colors.titleBarDetail,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "分類/板規",
                        color = colors.titleBarDetail2,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. 標籤頁 (Tab Preview)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.divider, RoundedCornerShape(4.dp))
                    .padding(2.dp)
            ) {
                // 選中的 Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.tabSelectedBackground)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "選中 Tab",
                        color = colors.tabSelectedText,
                        fontSize = 13.sp
                    )
                }
                // 未選中的 Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.tabUnselectedBackground)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "未選中 Tab",
                        color = colors.tabUnselectedText,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. 按鈕外觀 (Buttons Preview)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 一般按鈕
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.toolbarBackground)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "一般按鈕", color = colors.buttonText, fontSize = 12.sp)
                }

                // 按壓狀態模擬
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.toolbarBackgroundPressed)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "按壓效果", color = colors.buttonTextPressed, fontSize = 12.sp)
                }

                // 停用按鈕
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.toolbarBackgroundDisabled)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "停用", color = colors.buttonTextDisabled, fontSize = 12.sp)
                }

                // 危險按鈕
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.buttonDangerBackground)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "刪除/危險", color = colors.buttonTextDanger, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. BBS 項目色彩預覽
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.divider, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "● 追蹤看板(首)", color = colors.bbsBoardFollowFirst, fontSize = 12.sp)
                    Text(text = "● 追蹤看板(次)", color = colors.bbsBoardFollowOther, fontSize = 12.sp)
                    Text(text = "一般看板", color = colors.bbsBoardNormal, fontSize = 12.sp)
                    Text(text = "已讀看板", color = colors.bbsBoardNormalRead, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "作者0: 正常", color = colors.bbsAuthor0, fontSize = 12.sp)
                    Text(text = "內文0: 內容", color = colors.bbsContent0, fontSize = 12.sp)
                    Text(text = "引言作者", color = colors.bbsAuthor1, fontSize = 12.sp)
                    Text(text = "引言內文", color = colors.bbsContent1, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. 色票方塊一覽
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorSwatch(name = "Page", color = colors.pageBackground, borderColor = colors.dialogBorder)
                ColorSwatch(name = "Surface", color = colors.surface, borderColor = colors.dialogBorder)
                ColorSwatch(name = "Toolbar", color = colors.toolbarBackground, borderColor = colors.dialogBorder)
                ColorSwatch(name = "TabSel", color = colors.tabSelectedBackground, borderColor = colors.dialogBorder)
                ColorSwatch(name = "Danger", color = colors.buttonDangerBackground, borderColor = colors.dialogBorder)
                ColorSwatch(name = "Text", color = colors.textPrimary, borderColor = colors.dialogBorder)
            }
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color, borderColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
        )
        Text(
            text = name,
            fontSize = 9.sp,
            maxLines = 1,
            color = borderColor
        )
    }
}

// -------------------------------------------------------------
// 單項主題預覽
// -------------------------------------------------------------

@Preview(name = "1. Default Light", showBackground = true)
@Composable
fun PreviewDefaultLight() {
    ThemeShowcase(themeName = "預設主題", style = AppThemeStyle.DEFAULT, isDark = false)
}

@Preview(name = "2. Default Dark", showBackground = true)
@Composable
fun PreviewDefaultDark() {
    ThemeShowcase(themeName = "預設主題", style = AppThemeStyle.DEFAULT, isDark = true)
}

@Preview(name = "3. Pink Light", showBackground = true)
@Composable
fun PreviewPinkLight() {
    ThemeShowcase(themeName = "粉紅主題", style = AppThemeStyle.PINK, isDark = false)
}

@Preview(name = "4. Pink Dark", showBackground = true)
@Composable
fun PreviewPinkDark() {
    ThemeShowcase(themeName = "粉紅主題", style = AppThemeStyle.PINK, isDark = true)
}

@Preview(name = "5. eInk Light", showBackground = true)
@Composable
fun PreviewEInkLight() {
    ThemeShowcase(themeName = "eInk 電子紙", style = AppThemeStyle.EINK, isDark = false)
}

@Preview(name = "6. eInk Dark", showBackground = true)
@Composable
fun PreviewEInkDark() {
    ThemeShowcase(themeName = "eInk 電子紙", style = AppThemeStyle.EINK, isDark = true)
}

// -------------------------------------------------------------
// 綜合預覽 (6 種主題並列)
// -------------------------------------------------------------

@Preview(name = "全部主題綜合比較", showBackground = true, heightDp = 1800)
@Composable
fun PreviewAllThemes() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ThemeShowcase("1. 預設淺色", AppThemeStyle.DEFAULT, false)
        ThemeShowcase("2. 預設深色", AppThemeStyle.DEFAULT, true)
        ThemeShowcase("3. 粉紅淺色", AppThemeStyle.PINK, false)
        ThemeShowcase("4. 粉紅深色", AppThemeStyle.PINK, true)
        ThemeShowcase("5. eInk 淺色", AppThemeStyle.EINK, false)
        ThemeShowcase("6. eInk 深色", AppThemeStyle.EINK, true)
    }
}

/**
 * 混合過渡期驗證元件：嵌入於 XML 頁面中的 Compose 展示卡片
 */
@Composable
fun ThemeHybridShowcase(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val style by ThemeBridge.themeStyle.collectAsState()
    val isDark by ThemeBridge.isDarkTheme.collectAsState()
    var clickCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.dialogBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "Compose 混合過渡預覽 (Theme Bridge)",
            color = colors.textPrimary,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "當前主題: $style | 模式: ${if (isDark) "深色 (Dark)" else "淺色 (Light)"}",
            color = colors.textSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 色票預覽
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ColorSwatch(name = "Page", color = colors.pageBackground, borderColor = colors.dialogBorder)
            ColorSwatch(name = "Surface", color = colors.surface, borderColor = colors.dialogBorder)
            ColorSwatch(name = "Toolbar", color = colors.toolbarBackground, borderColor = colors.dialogBorder)
            ColorSwatch(name = "Tab", color = colors.tabSelectedBackground, borderColor = colors.dialogBorder)
            ColorSwatch(name = "Danger", color = colors.buttonDangerBackground, borderColor = colors.dialogBorder)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 互動按鈕
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(colors.toolbarBackground)
                .clickable { clickCount++ }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Compose 按鈕點擊測試: $clickCount 次",
                color = colors.buttonText,
                fontSize = 13.sp
            )
        }
    }
}

