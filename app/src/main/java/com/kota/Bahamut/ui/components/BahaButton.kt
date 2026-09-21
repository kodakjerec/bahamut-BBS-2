package com.kota.Bahamut.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.ui.theme.AppTheme

enum class ButtonType {
    NORMAL,    // 工具列/標準按鈕
    DANGER,    // 危險/刪除操作按鈕
    SECONDARY  // 次要卡片/邊框按鈕
}

/**
 * 專案通用按鈕 Composable
 * 以狀態邏輯取代舊 XML Selector (button_background_*.xml, pink_toolbar_item_background.xml 等)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BahaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    type: ButtonType = ButtonType.NORMAL,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    fontSize: TextUnit = AppTheme.fontSize.base,
    minHeight: Dp = 48.dp
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val hapticLongClick = onLongClick?.let { action ->
        { 
            action();
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // 依據狀態決定背景色與文字色
    val (backgroundColor, textColor) = when (type) {
        ButtonType.NORMAL -> {
            when {
                !enabled -> colors.toolbarBackgroundDisabled to colors.buttonTextDisabled
                isPressed -> colors.toolbarBackgroundPressed to colors.buttonTextPressed
                isSelected -> colors.toolbarBackgroundPressed to colors.buttonTextPressed
                else -> colors.toolbarBackground to colors.buttonText
            }
        }
        ButtonType.DANGER -> {
            when {
                !enabled -> colors.buttonDangerDisabled to colors.buttonTextDisabled
                isPressed -> colors.buttonDangerPressed to colors.buttonTextDangerPressed
                else -> colors.buttonDangerBackground to colors.buttonTextDanger
            }
        }
        ButtonType.SECONDARY -> {
            when {
                !enabled -> colors.surface to colors.textSecondary
                isPressed -> colors.toolbarBackgroundPressed to colors.buttonTextPressed
                else -> colors.surface to colors.textPrimary
            }
        }
    }

    Box(
        modifier = Modifier
            .heightIn(min = minHeight)
            .then(modifier)
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // 自訂狀態色彩切換已包含即時反饋
                enabled = enabled,
                onClick = onClick,
                onLongClick = hapticLongClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        BahaText(
            text = text,
            color = textColor,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}

