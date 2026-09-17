package com.kota.Bahamut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * 專案通用標籤頁 (Tab Item) Composable
 * 以 if (selected) 狀態邏輯取代舊 XML Selector (tab_item_background_color_selected.xml 等)
 */
@Composable
fun RowScope.BahaTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    val backgroundColor = if (isSelected) {
        colors.tabSelectedBackground
    } else {
        colors.tabUnselectedBackground
    }

    val textColor = if (isSelected) {
        colors.tabSelectedText
    } else {
        colors.tabUnselectedText
    }

    Box(
        modifier = modifier
            .weight(1f)
            .clip(RoundedCornerShape(3.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * 專案通用標籤列外框容器
 */
@Composable
fun BahaTabBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.divider, RoundedCornerShape(4.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

