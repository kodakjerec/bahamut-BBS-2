package com.kota.Bahamut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * 專案通用主題適配單行輸入框 (String 版本)
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
    height: Dp = 40.dp,
    keyboardOptions: KeyboardOptions = if (isPassword) {
        KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
    } else {
        KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next)
    },
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val colors = AppTheme.colors

    BasicTextField(
        value = value,
        onValueChange = {
            if (it.length <= maxLength) {
                onValueChange(it)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.inputBoxBackground)
            .padding(horizontal = 4.dp),
        textStyle = TextStyle(
            color = colors.inputBoxText,
            fontSize = AppTheme.fontSize.large
        ),
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(colors.inputBoxText),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    BahaText(
                        text = placeholder,
                        color = colors.textSecondary,
                        fontSize = BahaTextSize.BASE
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * 專案通用主題適配單行輸入框 (TextFieldValue 版本)
 */
@Composable
fun BahaInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    maxLength: Int = Int.MAX_VALUE,
    singleLine: Boolean = true,
    height: Dp = 40.dp,
    keyboardOptions: KeyboardOptions = if (isPassword) {
        KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
    } else {
        KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next)
    },
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val colors = AppTheme.colors

    BasicTextField(
        value = value,
        onValueChange = {
            if (it.text.length <= maxLength) {
                onValueChange(it)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.inputBoxBackground)
            .padding(horizontal = 8.dp),
        textStyle = TextStyle(
            color = colors.inputBoxText,
            fontSize = AppTheme.fontSize.base
        ),
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(colors.inputBoxText),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.text.isEmpty() && placeholder.isNotEmpty()) {
                    BahaText(
                        text = placeholder,
                        color = colors.textSecondary,
                        fontSize = BahaTextSize.SUBTITLE
                    )
                }
                innerTextField()
            }
        }
    )
}

