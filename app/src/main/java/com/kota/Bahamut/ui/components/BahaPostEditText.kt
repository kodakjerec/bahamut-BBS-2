package com.kota.Bahamut.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.model.PostEditText
import com.kota.Bahamut.ui.theme.AppTheme

@Composable
fun BahaPostEditText(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        factory = { ctx ->
            PostEditText(ctx).apply {
                hint = ctx.getString(R.string.input_content_here)
                setHintTextColor(colors.textSecondary.toArgb())
                setTextColor(colors.textPrimary.toArgb())
                background = null // 移除原生底線
                gravity = android.view.Gravity.TOP or android.view.Gravity.START
                inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE

                // 監聽原生文字變動通知 Compose
                addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        val newText = s?.toString() ?: ""
                        if (newText != value.text) {
                            onValueChange(TextFieldValue(newText, TextRange(selectionStart, selectionEnd)))
                        }
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                })
            }
        },
        update = { editText ->
            if (editText.text.toString() != value.text) {
                editText.setText(value.text)
                editText.setSelection(value.selection.end.coerceIn(0, value.text.length))
            }
        }
    )
}