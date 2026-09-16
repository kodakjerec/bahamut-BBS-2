package com.kota.Bahamut.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.ButtonType
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
            title = CommonFunctions.getContextString(R.string.confirm),
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
                Text(
                    text = CommonFunctions.getContextString(R.string.is_post_article),
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (myTarget != TelnetArticle.NEW) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = CommonFunctions.getContextString(R.string.is_post_article_reply),
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    val targets = listOf(
                        "F" to CommonFunctions.getContextString(R.string.post_to_board),
                        "M" to CommonFunctions.getContextString(R.string.post_to_mail),
                        "B" to CommonFunctions.getContextString(R.string.post_to_both)
                    )

                    targets.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTarget = key }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedTarget == key),
                                onClick = { selectedTarget = key },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.toolbarBackgroundFocused,
                                    unselectedColor = colors.textSecondary
                                )
                            )
                            Text(
                                text = label,
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = CommonFunctions.getContextString(R.string.select_sign),
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSignDropdownExpanded = true }
                    ) {
                        Text(
                            text = signList.getOrElse(selectedSignIndex) { "" },
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isSignDropdownExpanded,
                        onDismissRequest = { isSignDropdownExpanded = false }
                    ) {
                        signList.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(text = name, color = colors.textPrimary) },
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
