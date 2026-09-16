package com.kota.Bahamut.dialogs

import android.widget.LinearLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.CommonFunctions.intToRGB
import com.kota.Bahamut.service.CommonFunctions.rgbToInt
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.asFramework.dialog.ASDialog
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar

class DialogColorPicker : ASDialog() {
    private var fromRes: String = ""
    private var initialColorInt by mutableIntStateOf(0)
    private var colorListener: DialogColorPickerListener? = null
    private var colorPickerViewRef: ColorPickerView? = null
    private var selectedColorHex by mutableStateOf("")

    init {
        setTitle(CommonFunctions.getContextString(R.string.theme_manager_palette))
        setComposeContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val colors = AppTheme.colors

        BahaAlertDialogContent(
            title = CommonFunctions.getContextString(R.string.theme_manager_palette),
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
                        colorListener?.onSelectColor(selectedColorHex)
                        dismiss()
                    }
                )
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        LinearLayout(ctx).apply {
                            orientation = LinearLayout.VERTICAL
                            gravity = android.view.Gravity.CENTER

                            val density = ctx.resources.displayMetrics.density
                            val pickerSize = (240 * density).toInt()

                            val cpView = ColorPickerView(ctx).apply {
                                layoutParams = LinearLayout.LayoutParams(pickerSize, pickerSize).apply {
                                    gravity = android.view.Gravity.CENTER
                                }
                                preferenceName = "MyColorPicker"
                                if (initialColorInt != 0) {
                                    setInitialColor(initialColorInt)
                                }
                                setColorListener(ColorListener { color, _ ->
                                    selectedColorHex = intToRGB(color)
                                })
                            }
                            colorPickerViewRef = cpView

                            val alphaBar = AlphaSlideBar(ctx).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    topMargin = (10 * density).toInt()
                                }
                            }
                            cpView.attachAlphaSlider(alphaBar)

                            addView(cpView)
                            addView(alphaBar)
                        }
                    },
                    update = {
                        if (initialColorInt != 0) {
                            colorPickerViewRef?.setInitialColor(initialColorInt)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = selectedColorHex,
                    onValueChange = { selectedColorHex = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.pageBackground,
                        unfocusedContainerColor = colors.pageBackground,
                        focusedIndicatorColor = colors.toolbarBackgroundFocused,
                        unfocusedIndicatorColor = colors.divider
                    )
                )
            }
        }
    }

    fun setListener(listener: DialogColorPickerListener) {
        colorListener = listener
    }

    fun setFromRes(res: String) {
        fromRes = res
        selectedColorHex = res
        initialColorInt = rgbToInt(res)
        colorPickerViewRef?.setInitialColor(initialColorInt)
    }
}