package com.kota.Bahamut.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * 專案通用主題文字 Composable (BahaText)
 * 預設大小為 App 基準 24.sp (AppTheme.fontSize.base)，預設顏色為主題主要文字色 (AppTheme.colors.textPrimary)。
 * 支援語意化字級層級與自訂字級，並無縫支援未來全域動態字體縮放。
 */
@Composable
fun BahaText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = AppTheme.fontSize.base,
    color: Color = AppTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
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

/**
 * BahaText 重載版本，支援 [AnnotatedString]
 */
@Composable
fun BahaText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = AppTheme.fontSize.base,
    color: Color = AppTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
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
