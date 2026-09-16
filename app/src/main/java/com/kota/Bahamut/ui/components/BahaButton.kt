package com.kota.Bahamut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
@Composable
fun BahaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.NORMAL,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    shape: Shape = RoundedCornerShape(4.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    fontSize: TextUnit = 14.sp,
    minHeight: Dp = 42.dp
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

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
        modifier = modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = minHeight)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (type == ButtonType.SECONDARY) {
                    Modifier.border(1.dp, colors.dialogBorder, shape)
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 自訂狀態色彩切換已包含即時反饋
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = if (isSelected || isPressed) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

