package com.kota.Bahamut.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * 清單對話框內容本體 Composable (可用於預覽或自訂容器)
 */
@Composable
fun BahaListDialogContent(
    items: List<String>,
    onItemSelected: (index: Int, text: String) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    selectedIndex: Int? = null,
    onItemLongClicked: ((index: Int, text: String) -> Unit)? = null
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .widthIn(min = 280.dp, max = 340.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.pageBackground)
            .border(1.dp, colors.dialogBorder, RoundedCornerShape(6.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. 標題列
            if (!title.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.dialogTitleBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    BahaText(
                        text = title,
                        color = colors.titleBarTitle,
                        fontSize = AppTheme.fontSize.title
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.divider)
                )
            }

            // 2. 可捲動的選項清單
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                itemsIndexed(items) { index, itemText ->
                    val isSelected = (index == selectedIndex)
                    val itemBg = if (isSelected) {
                        colors.dialogSelectArticleFocused
                    } else {
                        colors.surface
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(itemBg)
                            .clickable {
                                onItemSelected(index, itemText)
                            }
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BahaText(
                                text = itemText,
                                color = colors.textPrimary,
                                fontSize = AppTheme.fontSize.body,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                BahaText(
                                    text = "✓",
                                    color = colors.titleBarTitle,
                                    fontSize = AppTheme.fontSize.body
                                )
                            }
                        }
                    }

                    // 分隔線
                    if (index < items.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.divider)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 專案通用清單選單對話框 (List Dialog) Composable
 * 取代舊有 ASListDialog (表情符號、文章選單等)
 */
@Composable
fun BahaListDialog(
    items: List<String>,
    onItemSelected: (index: Int, text: String) -> Unit,
    onDismissRequest: () -> Unit,
    title: String? = null,
    selectedIndex: Int? = null,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false)
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        BahaListDialogContent(
            items = items,
            onItemSelected = { index, text ->
                onItemSelected(index, text)
                onDismissRequest()
            },
            title = title,
            selectedIndex = selectedIndex
        )
    }
}
