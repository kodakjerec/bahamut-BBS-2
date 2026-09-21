package com.kota.Bahamut.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaInputField
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.ui.ASToast
import java.util.Vector

class DialogSearchArticle : ASDialog() {
    private var listener: DialogSearchArticleListener? = null
    override val name: String?
        get() = "BahamutBoardSearchDialog"

    private var dialogTitle by mutableStateOf("搜尋文章")
    private var searchButtonText by mutableStateOf(CommonFunctions.getContextString(R.string.search))
    private var keyword by mutableStateOf("")
    private var author by mutableStateOf("")
    private var markOnly by mutableStateOf(false) // false = un_limit (NO), true = limit (YES)
    private var gy by mutableStateOf("")

    init {
        setTitle("搜尋文章")
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val colors = AppTheme.colors
        val scrollState = rememberScrollState()

        val textFieldColors = TextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedContainerColor = colors.pageBackground,
            unfocusedContainerColor = colors.pageBackground,
            focusedIndicatorColor = colors.toolbarBackgroundFocused,
            unfocusedIndicatorColor = colors.divider
        )

        BahaAlertDialogContent(
            title = dialogTitle,
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.DANGER,
                    onClick = {
                        dismiss()
                        listener?.onSearchDialogCancelButtonClicked()
                    }
                ),
                BahaDialogButton(
                    text = searchButtonText,
                    type = ButtonType.DANGER,
                    onClick = {
                        val cleanKeyword = keyword.replace("\n", "").trim()
                        val cleanAuthor = author.replace("\n", "").trim()
                        val mark = if (markOnly) "YES" else "NO"
                        val cleanGy = gy.trim()

                        if (cleanKeyword.isEmpty() && cleanAuthor.isEmpty() && cleanGy.isEmpty()) {
                            ASToast.showShortToast(CommonFunctions.getContextString(R.string.input_search_article))
                            return@BahaDialogButton
                        }

                        val searchOptions = Vector<String>()
                        searchOptions.add(cleanKeyword)
                        searchOptions.add(cleanAuthor)
                        searchOptions.add(mark)
                        searchOptions.add(cleanGy)

                        listener?.onSearchDialogSearchButtonClickedWithValues(searchOptions)
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
                // 關鍵字
                BahaText(
                    text = CommonFunctions.getContextString(R.string.keyword),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                BahaInputField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    placeholder = stringResource(R.string.keyword_hint)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 作者
                BahaText(
                    text = CommonFunctions.getContextString(R.string.author),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                BahaInputField(
                    value = author,
                    onValueChange = { author = it },
                    placeholder = stringResource(R.string.author_hint)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 標記限定
                BahaText(
                    text = CommonFunctions.getContextString(R.string.mark_only),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { markOnly = true }
                    ) {
                        RadioButton(
                            selected = markOnly,
                            onClick = { markOnly = true },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.checkboxTint,
                                unselectedColor = colors.checkboxUncheckedTint
                            )
                        )
                        BahaText(
                            text = CommonFunctions.getContextString(R.string.limit),
                            color = colors.textPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { markOnly = false }
                    ) {
                        RadioButton(
                            selected = !markOnly,
                            onClick = { markOnly = false },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.checkboxTint,
                                unselectedColor = colors.checkboxUncheckedTint
                            )
                        )
                        BahaText(
                            text = CommonFunctions.getContextString(R.string.un_limit),
                            color = colors.textPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GY 最低值
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BahaText(
                        text = CommonFunctions.getContextString(R.string.gy_minimum),
                        color = colors.textPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    BahaInputField(
                        value = gy,
                        onValueChange = { if (it.length <= 2) gy = it },
                        placeholder = stringResource(R.string.GY_hint),
                        maxLength = 12,
                        height = 48.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    fun setListener(listener: DialogSearchArticleListener) {
        this.listener = listener
    }

    fun editContent(searchOptions: Vector<String?>) {
        dialogTitle = "修改搜尋內容"
        searchButtonText = CommonFunctions.getContextString(R.string.confirm)
        if (searchOptions.size > 0) keyword = searchOptions[0] ?: ""
        if (searchOptions.size > 1) author = searchOptions[1] ?: ""
        if (searchOptions.size > 2) markOnly = (searchOptions[2] == "y" || searchOptions[2] == "YES")
        if (searchOptions.size > 3) gy = searchOptions[3] ?: ""
    }
}
