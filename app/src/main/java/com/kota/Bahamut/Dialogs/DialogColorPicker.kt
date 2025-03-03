package com.kota.Bahamut.Dialogs

import android.widget.Button
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.Bahamut.Pages.Model.PostEditText
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.intToRGB
import com.kota.Bahamut.Service.CommonFunctions.rgbToInt
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import java.util.Objects


class DialogColorPicker : ASDialog() {
    private var colorPickerView: ColorPickerView
    private var textView: PostEditText
    private var fromRes: String = ""
    private lateinit var colorListener: DialogColorPickerListener

    init {
        val layoutId = R.layout.dialog_color_picker
        requestWindowFeature(1)
        setContentView(layoutId)
        window!!.setBackgroundDrawable(null)

        colorPickerView = findViewById(R.id.dialog_color_picker_view)
        val alphaSlideBar: AlphaSlideBar = findViewById(R.id.dialog_color_picker_alpha)
        textView = findViewById(R.id.dialog_color_picker_sample)

        colorPickerView.setColorListener(ColorListener { color, _ ->
            textView.setText(intToRGB(color))
        })
        colorPickerView.attachAlphaSlider(alphaSlideBar)

        // 取消
        val cancelButton: Button = findViewById(R.id.cancel)
        cancelButton.setOnClickListener { _->
            dismiss()
        }
        // 送出
        val sendButton: Button = findViewById(R.id.send)
        sendButton.setOnClickListener { _->
            colorListener.onSelectColor(textView.text.toString())
            dismiss()
        }
    }

    fun setListener(listener: DialogColorPickerListener) {
        colorListener = listener
    }

    fun setFromRes(res: String) {
        fromRes = res
        colorPickerView.setInitialColor(rgbToInt(res))
    }
}