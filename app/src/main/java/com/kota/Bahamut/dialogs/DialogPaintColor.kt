package com.kota.Bahamut.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaCheckbox
import com.kota.Bahamut.ui.components.BahaDropdownMenu
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog
import com.kota.telnet.reference.TelnetAnsiCode

class DialogPaintColor : ASDialog() {
    private var dialogPaintColorListener: DialogPaintColorListener? = null

    override val name: String?
        get() = "BahamutPostArticlePaintColor"

    init {
        setTitle(CommonFunctions.getContextString(R.string.post_article_page_paint_color))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val context = LocalContext.current
        val colors = AppTheme.colors
        val scrollState = rememberScrollState()

        val colorOptions = remember {
            context.resources.getStringArray(R.array.dialog_paint_color_items)
        }

        var isRecovery by remember { mutableStateOf(true) }
        var isHighlight by remember { mutableStateOf(false) }
        var frontColor by remember { mutableIntStateOf(0) }
        var backColor by remember { mutableIntStateOf(0) }

        var frontExpanded by remember { mutableStateOf(false) }
        var backExpanded by remember { mutableStateOf(false) }

        // Generate outputParam
        val outputParam = remember(isRecovery, isHighlight, frontColor, backColor) {
            if (isRecovery) {
                "*[m"
            } else {
                val sb = StringBuilder("*[")
                if (isHighlight) sb.append("1;")
                if (frontColor > 0) {
                    sb.append("3").append(frontColor - 1)
                    if (backColor > 0) {
                        sb.append(";4").append(backColor - 1)
                    }
                } else if (backColor > 0) {
                    sb.append("4").append(backColor - 1)
                }
                sb.append("m")
                sb.toString()
            }
        }

        // Compute preview textColor and bgColor
        val sampleTextColor = remember(isRecovery, isHighlight, frontColor) {
            if (isRecovery || frontColor <= 0) {
                colors.textPrimary
            } else {
                var paintColor: Byte = (frontColor - 1).toByte()
                if (isHighlight) paintColor = (paintColor + 8).toByte()
                Color(TelnetAnsiCode.getTextColor(paintColor))
            }
        }

        val sampleBgColor = remember(isRecovery, backColor) {
            if (isRecovery || backColor <= 0) {
                Color.Transparent
            } else {
                val paintColor: Byte = (backColor - 1).toByte()
                Color(TelnetAnsiCode.getBackgroundColor(paintColor))
            }
        }

        BahaAlertDialogContent(
            title = stringResource(R.string.post_article_page_paint_color),
            buttons = listOf(
                BahaDialogButton(
                    text = stringResource(R.string.cancel),
                    type = ButtonType.SECONDARY,
                    onClick = { dismiss() }
                ),
                BahaDialogButton(
                    text = stringResource(R.string.send),
                    type = ButtonType.NORMAL,
                    onClick = {
                        dialogPaintColorListener?.onPaintColorDone(outputParam)
                        dismiss()
                    }
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // 還原 Checkbox (文字在左，核取方塊在右)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newRecovery = !isRecovery
                            isRecovery = newRecovery
                            if (newRecovery) {
                                frontColor = 0
                                backColor = 0
                                isHighlight = false
                            }
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.post_article_page_paint_color_recovery),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BahaText(
                        text = stringResource(R.string.post_article_page_paint_color_recovery2),
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BahaCheckbox(
                        checked = isRecovery,
                        onCheckedChange = { isChecked ->
                            isRecovery = isChecked
                            if (isChecked) {
                                frontColor = 0
                                backColor = 0
                                isHighlight = false
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 前景色
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { frontExpanded = true }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.post_article_page_paint_color_front),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BahaText(
                        text = stringResource(R.string.post_article_page_paint_color_front2),
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            BahaText(
                                text = colorOptions.getOrElse(frontColor) { "" },
                                color = colors.textPrimary,
                                fontSize = AppTheme.fontSize.subtitle
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BahaText(
                                text = "▾",
                                color = colors.textSecondary,
                                fontSize = AppTheme.fontSize.subtitle
                            )
                        }
                        BahaDropdownMenu(
                            expanded = frontExpanded,
                            onDismissRequest = { frontExpanded = false },
                            items = colorOptions,
                            selectedIndex = frontColor,
                            onItemSelected = { index ->
                                frontColor = index
                                if (index > 0 && isRecovery) isRecovery = false
                            },
                            fontSize = AppTheme.fontSize.subtitle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 背景色
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { backExpanded = true }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.post_article_page_paint_color_back),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BahaText(
                        text = stringResource(R.string.post_article_page_paint_color_back2),
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            BahaText(
                                text = colorOptions.getOrElse(backColor) { "" },
                                color = colors.textPrimary,
                                fontSize = AppTheme.fontSize.subtitle
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BahaText(
                                text = "▾",
                                color = colors.textSecondary,
                                fontSize = AppTheme.fontSize.subtitle
                            )
                        }
                        BahaDropdownMenu(
                            expanded = backExpanded,
                            onDismissRequest = { backExpanded = false },
                            items = colorOptions,
                            selectedIndex = backColor,
                            onItemSelected = { index ->
                                backColor = index
                                if (index > 0 && isRecovery) isRecovery = false
                            },
                            fontSize = AppTheme.fontSize.subtitle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 亮色 Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newHighlight = !isHighlight
                            isHighlight = newHighlight
                            if (newHighlight && isRecovery) isRecovery = false
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.post_article_page_paint_color_highlight),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BahaText(
                        text = stringResource(R.string.post_article_page_paint_color_highlight2),
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BahaCheckbox(
                        checked = isHighlight,
                        onCheckedChange = { isChecked ->
                            isHighlight = isChecked
                            if (isChecked && isRecovery) isRecovery = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 預覽參數與預覽樣本
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = outputParam,
                        color = colors.textSecondary
                    )
                    Box(
                        modifier = Modifier
                            .background(sampleBgColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        BahaText(
                            text = stringResource(R.string.dialog_paint_color_sample_ch),
                            color = sampleTextColor
                        )
                    }
                }
            }
        }
    }

    fun setListener(listener: DialogPaintColorListener?) {
        this.dialogPaintColorListener = listener
    }
}
