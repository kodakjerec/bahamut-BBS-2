package com.kota.Bahamut.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog

class DialogInsertExpression : ASDialog() {
    private var listener: DialogInsertExpressionListener? = null
    private var dialogTitle by mutableStateOf("表情符號")
    private val itemList = mutableStateListOf<String>()

    override val name: String?
        get() = "BahamutInsertExpressionDialog"

    init {
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val colors = AppTheme.colors

        BahaAlertDialogContent(
            title = dialogTitle,
            titleAction = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(colors.toolbarBackground)
                        .clickable {
                            listener?.onListDialogSettingClicked()
                            dismiss()
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BahaText(
                        text = CommonFunctions.getContextString(R.string.setting),
                        color = colors.buttonText,
                        fontSize = AppTheme.fontSize.body
                    )
                }
            },
            contentPadding = PaddingValues(0.dp),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.DANGER,
                    onClick = { dismiss() }
                )
            )
        ) {
            // 項目列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                itemsIndexed(itemList) { index, itemTitle ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                listener?.onListDialogItemClicked(
                                    this@DialogInsertExpression,
                                    index,
                                    itemTitle
                                )
                                dismiss()
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BahaText(
                            text = itemTitle,
                            color = colors.textPrimary
                        )
                    }
                    if (index < itemList.size - 1) {
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

    fun setListener(aListener: DialogInsertExpressionListener?): DialogInsertExpression {
        listener = aListener
        return this
    }

    fun setTitle(aTitle: String): DialogInsertExpression {
        dialogTitle = aTitle
        return this
    }

    fun addItems(aItemList: Array<String>): DialogInsertExpression {
        itemList.addAll(aItemList)
        return this
    }

    fun addItem(aItemTitle: String): DialogInsertExpression {
        itemList.add(aItemTitle)
        return this
    }

    companion object {
        @JvmStatic
        fun createDialog(): DialogInsertExpression {
            return DialogInsertExpression()
        }
    }
}
