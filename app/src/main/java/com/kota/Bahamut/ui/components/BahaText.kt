package com.kota.Bahamut.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * 專案文字字級層級列舉
 */
enum class BahaTextSize {
    ULTRA_LARGE, // 28sp (超大字 / 無障礙 / 特大標題)
    LARGE,       // 26sp (主頁目錄項目 / 勇者足跡按鈕)
    BASE,        // 24sp (全域基準大小 / 終端機大字 / 輸入框文字)
    TITLE,       // 20sp (狀態數值 / 區塊標題)
    SUBTITLE,    // 18sp (按鈕文字 / 輸入框標籤 / 選項文字)
    BODY,        // 16sp (內文 / 核取方塊文字 / 對話框說明)
    CAPTION,     // 14sp (提示說明 / 章節標題 / 輔助文字)
    TINY         // 12sp (極小標籤 / 狀態指示)
}

/**
 * 專案通用主題文字 Composable (BahaText)
 * 預設大小為 App 基準 24.sp (BahaTextSize.BASE)，預設顏色為主題主要文字色 (AppTheme.colors.textPrimary)。
 * 支援語意化字級層級與自訂字級，並無縫支援未來全域動態字體縮放。
 */
@Composable
fun BahaText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: BahaTextSize = BahaTextSize.BASE,
    color: Color = AppTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE
) {
    val fontSizes = AppTheme.fontSize
    val resolvedFontSize = when (fontSize) {
        BahaTextSize.ULTRA_LARGE -> fontSizes.ultraLarge
        BahaTextSize.LARGE -> fontSizes.large
        BahaTextSize.BASE -> fontSizes.base
        BahaTextSize.TITLE -> fontSizes.title
        BahaTextSize.SUBTITLE -> fontSizes.subtitle
        BahaTextSize.BODY -> fontSizes.body
        BahaTextSize.CAPTION -> fontSizes.caption
        BahaTextSize.TINY -> fontSizes.tiny
    }

    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = resolvedFontSize,
        textAlign = textAlign,
        lineHeight = lineHeight,
        textDecoration = textDecoration,
        overflow = overflow,
        maxLines = maxLines,
        style = LocalTextStyle.current.copy(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both
            )
        )
    )
}

