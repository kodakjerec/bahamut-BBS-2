package com.kota.Bahamut.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.ReferenceAuthor
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.NotificationSettings
import com.kota.Bahamut.ui.components.BahaCheckbox
import com.kota.Bahamut.ui.components.BahaCheckboxLeft
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog

class DialogReference : ASDialog() {
    private var dialogReferenceListener: DialogReferenceListener? = null
    private var myAuthors: MutableList<ReferenceAuthor> = mutableListOf()

    private var author0Available by mutableStateOf(false)
    private var author0Name by mutableStateOf("")
    private var author0Enabled by mutableStateOf(false)
    private var author0RemoveBlank by mutableStateOf(NotificationSettings.getDialogReferenceAuthor0RemoveBlank())
    private var author0ReservedType by mutableIntStateOf(NotificationSettings.getDialogReferenceAuthor0ReservedType())

    private var author1Available by mutableStateOf(false)
    private var author1Name by mutableStateOf("")
    private var author1Enabled by mutableStateOf(false)
    private var author1RemoveBlank by mutableStateOf(NotificationSettings.getDialogReferenceAuthor1RemoveBlank())
    private var author1ReservedType by mutableIntStateOf(NotificationSettings.getDialogReferenceAuthor1ReservedType())

    private var authorNoneChecked by mutableStateOf(false)

    override val name: String?
        get() = "BahamutSelectSignDialog"

    init {
        setTitle(CommonFunctions.getContextString(R.string.dialog_reference_title))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val colors = AppTheme.colors
        val scrollState = rememberScrollState()

        BahaAlertDialogContent(
            title = CommonFunctions.getContextString(R.string.dialog_reference_title),
            buttons = listOf(
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.cancel),
                    type = ButtonType.SECONDARY,
                    onClick = {
                        saveSettings()
                        dismiss()
                    }
                ),
                BahaDialogButton(
                    text = CommonFunctions.getContextString(R.string.send),
                    type = ButtonType.NORMAL,
                    onClick = {
                        if (myAuthors.isNotEmpty()) {
                            myAuthors[0].enabled = author0Enabled
                            if (author0Enabled) {
                                myAuthors[0].removeBlank = author0RemoveBlank
                                myAuthors[0].reservedType = author0ReservedType
                            }
                        }
                        if (myAuthors.size > 1) {
                            myAuthors[1].enabled = author1Enabled
                            if (author1Enabled) {
                                myAuthors[1].removeBlank = author1RemoveBlank
                                myAuthors[1].reservedType = author1ReservedType
                            }
                        }
                        dialogReferenceListener?.onSelectAuthor(myAuthors)
                        saveSettings()
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
                // Author 1 (前二)
                if (author1Available) {
                    AuthorSection(
                        title = CommonFunctions.getContextString(R.string.dialog_reference_author1) + author1Name,
                        enabled = author1Enabled,
                        onEnabledChange = { isChecked ->
                            author1Enabled = isChecked
                            if (isChecked) authorNoneChecked = false
                            else if (!author0Enabled) authorNoneChecked = true
                        },
                        removeBlank = author1RemoveBlank,
                        onRemoveBlankChange = { author1RemoveBlank = it },
                        reservedType = author1ReservedType,
                        onReservedTypeChange = { author1ReservedType = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Author 0 (前一)
                if (author0Available) {
                    AuthorSection(
                        title = CommonFunctions.getContextString(R.string.dialog_reference_author0) + author0Name,
                        enabled = author0Enabled,
                        onEnabledChange = { isChecked ->
                            author0Enabled = isChecked
                            if (isChecked) authorNoneChecked = false
                            else if (!author1Enabled) authorNoneChecked = true
                        },
                        removeBlank = author0RemoveBlank,
                        onRemoveBlankChange = { author0RemoveBlank = it },
                        reservedType = author0ReservedType,
                        onReservedTypeChange = { author0ReservedType = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Author None (無)
                BahaCheckboxLeft(
                    text = CommonFunctions.getContextString(R.string.dialog_reference_authorNone),
                    checked = authorNoneChecked,
                    onCheckedChange = { isChecked ->
                        authorNoneChecked = isChecked
                        if (isChecked) {
                            author0Enabled = false
                            author1Enabled = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.dialogBlockBackground)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }

    @Composable
    private fun AuthorSection(
        title: String,
        enabled: Boolean,
        onEnabledChange: (Boolean) -> Unit,
        removeBlank: Boolean,
        onRemoveBlankChange: (Boolean) -> Unit,
        reservedType: Int,
        onReservedTypeChange: (Int) -> Unit
    ) {
        val colors = AppTheme.colors

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.dialogBlockBackground)
                .padding(8.dp)
        ) {
            BahaCheckboxLeft(
                text = title,
                checked = enabled,
                onCheckedChange = onEnabledChange,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp
            )

            if (enabled) {
                Spacer(modifier = Modifier.height(6.dp))

                // 去除空白行 (文字在左，核取方塊在右)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRemoveBlankChange(!removeBlank) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = CommonFunctions.getContextString(R.string.dialog_reference_remove_blank),
                        color = colors.textPrimary,
                        fontSize = BahaTextSize.CAPTION
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BahaCheckbox(
                        checked = removeBlank,
                        onCheckedChange = onRemoveBlankChange
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 保留行數
                BahaText(
                    text = CommonFunctions.getContextString(R.string.dialog_reference_reserved_type),
                    color = colors.textPrimary,
                    fontSize = BahaTextSize.CAPTION
                )

                val types = listOf(
                    0 to CommonFunctions.getContextString(R.string.dialog_reference_reserved_type_0),
                    1 to CommonFunctions.getContextString(R.string.dialog_reference_reserved_type_1),
                    2 to CommonFunctions.getContextString(R.string.dialog_reference_reserved_type_2)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    types.forEach { (index, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onReservedTypeChange(index) }
                        ) {
                            RadioButton(
                                selected = (reservedType == index),
                                onClick = { onReservedTypeChange(index) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.checkboxTint,
                                    unselectedColor = colors.checkboxUncheckedTint
                                )
                            )
                            BahaText(
                                text = label,
                                color = colors.textPrimary,
                                fontSize = BahaTextSize.CAPTION
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveSettings() {
        NotificationSettings.setDialogReferenceAuthor0RemoveBlank(author0RemoveBlank)
        NotificationSettings.setDialogReferenceAuthor1RemoveBlank(author1RemoveBlank)
        NotificationSettings.setDialogReferenceAuthor0ReservedType(author0ReservedType)
        NotificationSettings.setDialogReferenceAuthor1ReservedType(author1ReservedType)
    }

    fun setAuthors(authors: MutableList<ReferenceAuthor>) {
        myAuthors = authors
        if (authors.isNotEmpty()) {
            author0Available = authors[0].enabled
            author0Name = authors[0].authorName ?: ""
            author0Enabled = authors[0].enabled
        }
        if (authors.size > 1) {
            author1Available = authors[1].enabled
            author1Name = authors[1].authorName ?: ""
            author1Enabled = authors[1].enabled
        }
    }

    fun setListener(listener: DialogReferenceListener?) {
        dialogReferenceListener = listener
    }
}
