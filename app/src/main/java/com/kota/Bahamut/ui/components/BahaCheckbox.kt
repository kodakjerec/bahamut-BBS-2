package com.kota.Bahamut.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * 專案基礎主題適配核取方塊 (自動套用當前主題之邊框、底色與勾選符號色彩)
 */
@Composable
fun BahaCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = AppTheme.colors
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = CheckboxDefaults.colors(
            checkedColor = colors.checkboxTint,
            uncheckedColor = colors.checkboxUncheckedTint,
            checkmarkColor = colors.checkboxCheckmark,
            disabledCheckedColor = colors.checkboxTint.copy(alpha = 0.5f),
            disabledUncheckedColor = colors.checkboxUncheckedTint.copy(alpha = 0.5f)
        )
    )
}

/**
 * 專案通用 Checkbox 規格一 (LoginPage 式 / 對話框式)：
 * Checkbox 在左邊，文字在右邊。整列可點擊觸發勾選切換。
 */
@Composable
fun BahaCheckboxLeft(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = AppTheme.fontSize.base,
    textColor: Color = AppTheme.colors.textPrimary,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        BahaCheckbox(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(6.dp))
        BahaText(
            text = text,
            color = if (enabled) textColor else colors.textSecondary,
            fontSize = AppTheme.fontSize.base
        )
        if (trailingContent != null) {
            Spacer(modifier = Modifier.weight(1f))
            trailingContent()
        }
    }
}

/**
 * 專案通用 Checkbox 規格二 (SystemSettingsPage 式 / UserConfigPage 式)：
 * 文字靠左對齊，Checkbox 在右邊，並附帶底部細分隔線。整列可點擊觸發勾選切換。
 */
@Composable
fun SettingsCheckboxItem(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showDivider: Boolean = true,
    paddingHorizontal: Dp = 12.dp,
    paddingVertical: Dp = 8.dp
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BahaText(
            text = title,
            color = if (enabled) colors.textPrimary else colors.textSecondary,
            fontSize = AppTheme.fontSize.base,
            modifier = Modifier.weight(1f)
        )
        BahaCheckbox(
            checked = isChecked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled
        )
    }
    if (showDivider) {
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }
}
