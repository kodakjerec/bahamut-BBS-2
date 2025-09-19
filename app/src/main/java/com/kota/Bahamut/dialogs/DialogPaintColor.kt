package com.kota.Bahamut.dialogs

import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.kota.asFramework.dialog.ASDialog
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.telnet.reference.TelnetAnsiCode.getBackgroundColor
import com.kota.telnet.reference.TelnetAnsiCode.getTextColor

class DialogPaintColor : ASDialog(), View.OnClickListener {
    var mainLayout: LinearLayout
    var isRecovery: Boolean = true
    var isHighlight: Boolean = false
    var frontColor: Int = 0
    var backColor: Int = 0
    var outputParam: String? = null
    var listener: DialogPaintColorListener? = null
    var recoveryCheckBox: CheckBox
    var highlightCheckBox: CheckBox
    var frontColorSpinner: Spinner
    var backColorSpinner: Spinner
    var textViewParam: TextView
    var textViewSample: TextView

    var snedButton: Button
    var cancelButton: Button

    var recovertListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            isRecovery = isChecked
            if (isChecked) {
                frontColor = -1
                backColor = -1
                isHighlight = false
                frontColorSpinner.setSelection(frontColor)
                backColorSpinner.setSelection(backColor)
                highlightCheckBox.isChecked = isHighlight
            }
            generateOutputParam()
        }
    var highlightListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            isHighlight = isChecked
            if (isRecovery) {
                isRecovery = false
                recoveryCheckBox.isChecked = false
            }
            generateOutputParam()
        }

    var frontColorListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                frontColor = i
                if (i > 0 && isRecovery) {
                    isRecovery = false
                    recoveryCheckBox.isChecked = false
                }
                generateOutputParam()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                frontColor = -1
            }
        }

    var backColorListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                backColor = i
                if (i > 0 && isRecovery) {
                    isRecovery = false
                    recoveryCheckBox.isChecked = false
                }
                generateOutputParam()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                backColor = -1
            }
        }

    override val name: String?
        get() = "BahamutPostArticlePaintColor"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_paint_color)
        if (window != null) window?.setBackgroundDrawable(null)
        setTitle(getContextString(R.string.post_article_page_paint_color))
        mainLayout = findViewById<LinearLayout>(R.id.dialog_paint_color_content_view)

        // 還原
        recoveryCheckBox = mainLayout.findViewById<CheckBox>(R.id.dialog_paint_color_check_recovery)
        recoveryCheckBox.setOnCheckedChangeListener(recovertListener)
        mainLayout.findViewById<View>(R.id.dialog_paint_color_check_recovery_item)
            .setOnClickListener { view: View? ->
                recoveryCheckBox.isChecked = !recoveryCheckBox.isChecked
            }

        // 前景
        val adapterFrontColor = ArrayAdapter(
            context,
            R.layout.simple_spinner_item,
            context.resources.getStringArray(R.array.dialog_paint_color_items)
        )
        adapterFrontColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frontColorSpinner =
            mainLayout.findViewById<Spinner>(R.id.post_article_page_paint_color_front_spinner)
        frontColorSpinner.adapter = adapterFrontColor
        frontColorSpinner.onItemSelectedListener = frontColorListener

        // 背景
        val adapterBackColor = ArrayAdapter(
            context,
            R.layout.simple_spinner_item,
            context.resources.getStringArray(R.array.dialog_paint_color_items)
        )
        adapterBackColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        backColorSpinner =
            mainLayout.findViewById<Spinner>(R.id.post_article_page_paint_color_back_spinner)
        backColorSpinner.adapter = adapterBackColor
        backColorSpinner.onItemSelectedListener = backColorListener

        // 亮色
        highlightCheckBox = mainLayout.findViewById<CheckBox>(R.id.dialog_paint_color_check_highlight)
        highlightCheckBox.setOnCheckedChangeListener(highlightListener)
        mainLayout.findViewById<View>(R.id.dialog_paint_color_check_highlight_item)
            .setOnClickListener { view: View? ->
                highlightCheckBox.isChecked = !highlightCheckBox.isChecked
            }

        // 文字
        textViewParam = mainLayout.findViewById<TextView>(R.id.dialog_paint_color_param)
        textViewSample = mainLayout.findViewById<TextView>(R.id.dialog_paint_color_sample)

        // 按鈕
        snedButton = mainLayout.findViewById<Button>(R.id.send)
        snedButton.setOnClickListener(this)
        cancelButton = mainLayout.findViewById<Button>(R.id.cancel)
        cancelButton.setOnClickListener(this)

        setDialogWidth(mainLayout)
    }

    fun generateOutputParam() {
        if (isRecovery) {
            outputParam = "*[m"
        } else {
            outputParam = "*["
            if (isHighlight) outputParam += "1;"
            if (frontColor > 0) {
                outputParam += "3" + (frontColor - 1)
                if (backColor > 0) {
                    outputParam += ";4" + (backColor - 1)
                }
            } else if (backColor > 0) {
                outputParam += "4" + (backColor - 1)
            }
            outputParam += "m"
        }
        // 內容改變
        textViewParam.text = outputParam

        // 顏色改變
        val textSample = SpannableString(textViewSample.text)
        val spansToRemove = textSample.getSpans(0, textSample.length, Any::class.java)

        if (isRecovery) {
            // 還原
            for (span in spansToRemove) {
                if (span is ForegroundColorSpan || span is BackgroundColorSpan) textSample.removeSpan(
                    span
                )
            }
        } else {
            var paintColor: Byte
            if (frontColor > 0) {
                paintColor = (frontColor - 1).toByte()
                if (isHighlight) paintColor = (paintColor + 8).toByte()
                val colorSpan = ForegroundColorSpan(getTextColor(paintColor))
                textSample.setSpan(colorSpan, 0, textSample.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                for (span in spansToRemove) {
                    if (span is ForegroundColorSpan) textSample.removeSpan(span)
                }
            }

            if (backColor > 0) {
                paintColor = (backColor - 1).toByte()
                val colorSpan = BackgroundColorSpan(getBackgroundColor(paintColor))
                textSample.setSpan(colorSpan, 0, textSample.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                for (span in spansToRemove) {
                    if (span is BackgroundColorSpan) textSample.removeSpan(span)
                }
            }
        }
        textViewSample.text = textSample
    }

    fun setListener(listener: DialogPaintColorListener?) {
        this@DialogPaintColor.listener = listener
    }

    override fun onClick(view: View?) {
        if (view === snedButton && listener != null) {
            listener?.onPaintColorDone(outputParam!!)
        }
        dismiss()
    }
}
