package com.kota.Bahamut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * BBS 經典深海藍頂部標題列 (雙行文字資訊，可選右側動作區塊)
 */
@Composable
fun BBSTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    subtitleTrailing: String = "",
    titleColor: Color = AppTheme.colors.titleBarTitle,
    onTitleClick: (() -> Unit)? = null,
    onSubtitleClick: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.titleBarBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側導覽按鈕 (可選)
            if (navigationIcon != null) {
                navigationIcon()
            }

            // 中間資訊區塊 (雙行)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = if (navigationIcon != null) 2.dp else 10.dp,
                        end = if (actions != null) 4.dp else 10.dp,
                        top = 6.dp,
                        bottom = 6.dp
                    )
            ) {
                // 第一行：標題 (黃色)
                BahaText(
                    text = title,
                    color = titleColor,
                    fontSize = AppTheme.fontSize.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (onTitleClick != null) Modifier.clickable(onClick = onTitleClick) else Modifier
                )

                if (subtitle.isNotEmpty() || subtitleTrailing.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    // 第二行：副標題 (白色) 與 右側副資訊 (青色)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BahaText(
                            text = subtitle,
                            color = colors.titleBarDetail,
                            fontSize = AppTheme.fontSize.body,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (onSubtitleClick != null) {
                                Modifier
                                    .weight(1f, fill = false)
                                    .clickable(onClick = onSubtitleClick)
                            } else {
                                Modifier.weight(1f, fill = false)
                            }
                        )

                        if (subtitleTrailing.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            BahaText(
                                text = subtitleTrailing,
                                color = colors.titleBarDetail2,
                                fontSize = AppTheme.fontSize.body,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 右側動作按鈕 (可選)
            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }

        // 底部分隔線
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider)
        )
    }
}

/**
 * BBS 底部操作工具列 (固定 50dp，頂部分隔線，全寬無縫按鈕)
 */
@Composable
fun BBSToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.toolbarDivider)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(colors.toolbarBackground),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * BBS 底部操作工具列按鈕間的垂直分隔線
 */
@Composable
fun BBSToolbarDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(AppTheme.colors.toolbarDivider)
    )
}

