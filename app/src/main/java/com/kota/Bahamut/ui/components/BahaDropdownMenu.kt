package com.kota.Bahamut.ui.components

import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * 專案通用下拉選單項目元件 (BahaDropdownMenu)
 * 封裝 DropdownMenu 與 DropdownMenuItem，當前選中項目以主題黃色 (titleBarTitle) 醒目標示。
 * 支援透過 fontSize 調整項目文字大小。
 */
@Composable
fun BahaDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: BahaTextSize = BahaTextSize.TITLE
) {
    val colors = AppTheme.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.background(colors.surface)
    ) {
        items.forEachIndexed { index, item ->
            DropdownMenuItem(
                text = {
                    BahaText(
                        text = item,
                        color = if (index == selectedIndex) colors.titleBarTitle else colors.textPrimary,
                        fontSize = fontSize
                    )
                },
                onClick = {
                    onItemSelected(index)
                    onDismissRequest()
                }
            )
        }
    }
}

/**
 * 支援 Array<String> 的便利多載
 */
@Composable
fun BahaDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: Array<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: BahaTextSize = BahaTextSize.TITLE
) {
    BahaDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        items = items.toList(),
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
        fontSize = fontSize
    )
}

