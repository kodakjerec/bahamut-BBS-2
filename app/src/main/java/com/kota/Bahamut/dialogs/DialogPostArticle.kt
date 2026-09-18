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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog
import com.kota.telnet.TelnetArticle

class DialogPostArticle(private val myTarget: Int) : ASDialog() {
    var dialogPostArticleListener: DialogPostArticleListener? = null

    override val name: String?
        get() = "BahamutBoardsPostArticle"

    init {
        setTitle(CommonFunctions.getContextString(R.string.confirm))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val context = LocalContext.current
        val colors = AppTheme.colors
        val signList = remember {
            context.resources.getStringArray(R.array.reply_target_list)
        }

        var selectedTarget by remember { mutableStateOf("F") } // "F" = Board, "M" = Mail, "B" = Both
        var selectedSignIndex by remember { mutableIntStateOf(0) }
        var isSignDropdownExpanded by remember { mutableStateOf(false) }

        val scrollState = rememberScrollState()

        BahaAlertDialogContent(
            modifier = Modifier.widthIn(min = 280.dp, max = 340.dp),
            title = stringResource(R.string.confirm),
            buttons = listOf(
                BahaDialogButton(
                    text = stringResource(R.string.cancel),
                    onClick = { dismiss() }
                ),
                BahaDialogButton(
                    text = stringResource(R.string.send),
                    onClick = {
                        val sign = if (selectedSignIndex > 0) (selectedSignIndex - 1).toString() else ""
                        dialogPostArticleListener?.onPostArticleDoneWithTarget(selectedTarget, sign)
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
                BahaText(
                    text = stringResource(R.string.is_post_article),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp),
                    size = BahaTextSize.TITLE
                )

                if (myTarget != TelnetArticle.NEW) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BahaText(
                        text = stringResource(R.string.is_post_article_reply),
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    val targets = listOf(
                        "F" to stringResource(R.string.post_to_board),
                        "M" to stringResource(R.string.post_to_mail),
                        "B" to stringResource(R.string.post_to_both)
                    )

                    targets.forEach { (key, label) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable { selectedTarget = key }
                                .padding(horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = (selectedTarget == key),
                                onClick = { selectedTarget = key },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.checkboxTint,
                                    unselectedColor = colors.textSecondary
                                ),
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            BahaText(
                                text = label,
                                color = colors.textPrimary,
                                modifier = Modifier.align(Alignment.Center),
                                size = BahaTextSize.TITLE
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                BahaText(
                    text = stringResource(R.string.select_sign),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { isSignDropdownExpanded = true }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BahaText(
                            text = signList.getOrElse(selectedSignIndex) { "" },
                            color = colors.textPrimary,
                            size = BahaTextSize.TITLE
                        )
                        BahaText(
                            text = "▼",
                            color = colors.textPrimary,
                            modifier = Modifier.padding(end = 4.dp),
                            size = BahaTextSize.TITLE
                        )
                    }

                    DropdownMenu(
                        expanded = isSignDropdownExpanded,
                        onDismissRequest = { isSignDropdownExpanded = false },
                        modifier = Modifier.background(colors.pageBackground)
                    ) {
                        signList.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = {
                                    BahaText(
                                        text = name,
                                        color = if (selectedSignIndex == index) colors.checkboxTint else colors.textPrimary
                                    )
                                },
                                onClick = {
                                    selectedSignIndex = index
                                    isSignDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    fun setListener(listener: DialogPostArticleListener?) {
        this.dialogPostArticleListener = listener
    }
}
