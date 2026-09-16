package com.kota.Bahamut.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaCheckboxLeft
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
            title = CommonFunctions.getContextString(R.string.post_article_page_paint_color),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.SECONDARY,
                    onClick = { dismiss() }
                ),
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.send),
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
                // 還原 Checkbox
                BahaCheckboxLeft(
                    text = CommonFunctions.getContextString(R.string.post_article_page_paint_color_recovery),
                    checked = isRecovery,
                    onCheckedChange = { isChecked ->
                        isRecovery = isChecked
                        if (isChecked) {
                            frontColor = 0
                            backColor = 0
                            isHighlight = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    trailingContent = {
                        Text(
                            text = CommonFunctions.getContextString(R.string.post_article_page_paint_color_recovery2),
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 前景色
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = CommonFunctions.getContextString(R.string.post_article_page_paint_color_front),
                            color = colors.textPrimary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CommonFunctions.getContextString(R.string.post_article_page_paint_color_front2),
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }

                    Box {
                        OutlinedCard(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { frontExpanded = true }
                        ) {
                            Text(
                                text = colorOptions.getOrElse(frontColor) { "" },
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = frontExpanded,
                            onDismissRequest = { frontExpanded = false }
                        ) {
                            colorOptions.forEachIndexed { index, optionName ->
                                DropdownMenuItem(
                                    text = { Text(text = optionName, color = colors.textPrimary) },
                                    onClick = {
                                        frontColor = index
                                        if (index > 0 && isRecovery) isRecovery = false
                                        frontExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 背景色
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = CommonFunctions.getContextString(R.string.post_article_page_paint_color_back),
                            color = colors.textPrimary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CommonFunctions.getContextString(R.string.post_article_page_paint_color_back2),
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }

                    Box {
                        OutlinedCard(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { backExpanded = true }
                        ) {
                            Text(
                                text = colorOptions.getOrElse(backColor) { "" },
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = backExpanded,
                            onDismissRequest = { backExpanded = false }
                        ) {
                            colorOptions.forEachIndexed { index, optionName ->
                                DropdownMenuItem(
                                    text = { Text(text = optionName, color = colors.textPrimary) },
                                    onClick = {
                                        backColor = index
                                        if (index > 0 && isRecovery) isRecovery = false
                                        backExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 亮色 Checkbox
                BahaCheckboxLeft(
                    text = CommonFunctions.getContextString(R.string.post_article_page_paint_color_highlight),
                    checked = isHighlight,
                    onCheckedChange = { isChecked ->
                        isHighlight = isChecked
                        if (isChecked && isRecovery) isRecovery = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    trailingContent = {
                        Text(
                            text = CommonFunctions.getContextString(R.string.post_article_page_paint_color_highlight2),
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                Spacer(modifier = Modifier.height(10.dp))

                // 預覽參數與預覽樣本
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = outputParam,
                        color = colors.textSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(sampleBgColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = CommonFunctions.getContextString(R.string.dialog_paint_color_sample_ch),
                            color = sampleTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
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
