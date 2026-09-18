package com.kota.Bahamut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kota.Bahamut.ui.theme.AppTheme

/**
 * 專案通用主題適配輸入框 (String 版本)
 * 自動適配螢幕/容器寬度，當 [singleLine] 為 false 時可隨內容長度自動換行並延展高度。
 */
@Composable
fun BahaInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    maxLength: Int = Int.MAX_VALUE,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    height: Dp = 48.dp,
    keyboardOptions: KeyboardOptions = if (isPassword) {
        KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
    } else {
        KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        )
    },
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    fontColor: Color = AppTheme.colors.inputBoxText,
    backgroundColor: Color = AppTheme.colors.inputBoxBackground
) {
    BasicTextField(
        value = value,
        onValueChange = {
            if (it.length <= maxLength) {
                onValueChange(it)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (singleLine) Modifier.height(height)
                else Modifier.heightIn(min = height)
            )
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .padding(
                horizontal = 8.dp,
                vertical = if (singleLine) 0.dp else 8.dp
            ),
        textStyle = TextStyle(
            color = fontColor,
            fontSize = AppTheme.fontSize.large
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(fontColor),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    BahaText(
                        text = placeholder,
                        color = AppTheme.colors.textSecondary,
                        fontSize = BahaTextSize.BASE
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * 專案通用主題適配輸入框 (TextFieldValue 版本)
 * 自動適配螢幕/容器寬度，當 [singleLine] 為 false 時可隨內容長度自動換行並延展高度。
 */
@Composable
fun BahaInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    maxLength: Int = Int.MAX_VALUE,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    height: Dp = 48.dp,
    keyboardOptions: KeyboardOptions = if (isPassword) {
        KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
    } else {
        KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        )
    },
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    fontSize: BahaTextSize = BahaTextSize.TITLE,
    fontColor: Color = AppTheme.colors.inputBoxText,
    backgroundColor: Color = AppTheme.colors.inputBoxBackground
) {
    BasicTextField(
        value = value,
        onValueChange = {
            if (it.text.length <= maxLength) {
                onValueChange(it)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (singleLine) Modifier.height(height)
                else Modifier.heightIn(min = height)
            )
            .background(backgroundColor)
            .padding(
                horizontal = 8.dp,
                vertical = if (singleLine) 0.dp else 8.dp
            ),
        textStyle = TextStyle(
            color = fontColor,
            fontSize = AppTheme.fontSize.title
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(fontColor),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
            ) {
                if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                    BahaText(
                        text = placeholder,
                        color = fontColor,
                        fontSize = fontSize
                    )
                }
                innerTextField()
            }
        }
    )
}
