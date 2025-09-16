package com.kota.Bahamut.Dialogs

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
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Telnet.Reference.TelnetAnsiCode.getBackgroundColor
import com.kota.Telnet.Reference.TelnetAnsiCode.getTextColor

class Dialog_PaintColor : ASDialog(), View.OnClickListener {
    var mainLayout: LinearLayout
    var _isRecovery: Boolean = true
    var _isHighlight: Boolean = false
    var _frontColor: Int = 0
    var _backColor: Int = 0
    var _outputParam: String? = null
    var _listener: Dialog_PaintColor_Listener? = null
    var _recovery_box: CheckBox
    var _highlight_box: CheckBox
    var _front_color_spinner: Spinner
    var _back_color_spinner: Spinner
    var _textview_param: TextView
    var _textview_sample: TextView

    var _send_button: Button
    var _cancel_button: Button

    var _recovery_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            _isRecovery = isChecked
            if (isChecked) {
                _frontColor = -1
                _backColor = -1
                _isHighlight = false
                _front_color_spinner.setSelection(_frontColor)
                _back_color_spinner.setSelection(_backColor)
                _highlight_box.setChecked(_isHighlight)
            }
            generateOutputParam()
        }
    var _highlight_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            _isHighlight = isChecked
            if (_isRecovery) {
                _isRecovery = false
                _recovery_box.setChecked(false)
            }
            generateOutputParam()
        }

    var _frontColor_listener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                _frontColor = i
                if (i > 0 && _isRecovery) {
                    _isRecovery = false
                    _recovery_box.setChecked(false)
                }
                generateOutputParam()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                _frontColor = -1
            }
        }

    var _backColor_listener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                _backColor = i
                if (i > 0 && _isRecovery) {
                    _isRecovery = false
                    _recovery_box.setChecked(false)
                }
                generateOutputParam()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                _backColor = -1
            }
        }

    val name: String?
        get() = "BahamutPostArticlePaintColor"

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_paint_color)
        if (getWindow() != null) getWindow()!!.setBackgroundDrawable(null)
        setTitle(getContextString(R.string.post_article_page_paint_color))
        mainLayout = findViewById<LinearLayout>(R.id.dialog_paint_color_content_view)

        // 還原
        _recovery_box = mainLayout.findViewById<CheckBox>(R.id.dialog_paint_color_check_recovery)
        _recovery_box.setOnCheckedChangeListener(_recovery_listener)
        mainLayout.findViewById<View?>(R.id.dialog_paint_color_check_recovery_item)
            .setOnClickListener(
                View.OnClickListener { view: View? -> _recovery_box.setChecked(!_recovery_box.isChecked()) })

        // 前景
        val adapter_front_color = ArrayAdapter<String?>(
            getContext(),
            R.layout.simple_spinner_item,
            getContext().getResources().getStringArray(R.array.dialog_paint_color_items)
        )
        adapter_front_color.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        _front_color_spinner =
            mainLayout.findViewById<Spinner>(R.id.post_article_page_paint_color_front_spinner)
        _front_color_spinner.setAdapter(adapter_front_color)
        _front_color_spinner.setOnItemSelectedListener(_frontColor_listener)

        // 背景
        val adapter_back_color = ArrayAdapter<String?>(
            getContext(),
            R.layout.simple_spinner_item,
            getContext().getResources().getStringArray(R.array.dialog_paint_color_items)
        )
        adapter_back_color.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        _back_color_spinner =
            mainLayout.findViewById<Spinner>(R.id.post_article_page_paint_color_back_spinner)
        _back_color_spinner.setAdapter(adapter_back_color)
        _back_color_spinner.setOnItemSelectedListener(_backColor_listener)

        // 亮色
        _highlight_box = mainLayout.findViewById<CheckBox>(R.id.dialog_paint_color_check_highlight)
        _highlight_box.setOnCheckedChangeListener(_highlight_listener)
        mainLayout.findViewById<View?>(R.id.dialog_paint_color_check_highlight_item)
            .setOnClickListener(
                View.OnClickListener { view: View? -> _highlight_box.setChecked(!_highlight_box.isChecked()) })

        // 文字
        _textview_param = mainLayout.findViewById<TextView>(R.id.dialog_paint_color_param)
        _textview_sample = mainLayout.findViewById<TextView>(R.id.dialog_paint_color_sample)

        // 按鈕
        _send_button = mainLayout.findViewById<Button>(R.id.send)
        _send_button.setOnClickListener(this)
        _cancel_button = mainLayout.findViewById<Button>(R.id.cancel)
        _cancel_button.setOnClickListener(this)

        setDialogWidth()
    }

    fun generateOutputParam() {
        if (_isRecovery) {
            _outputParam = "*[m"
        } else {
            _outputParam = "*["
            if (_isHighlight) _outputParam += "1;"
            if (_frontColor > 0) {
                _outputParam += "3" + (_frontColor - 1)
                if (_backColor > 0) {
                    _outputParam += ";4" + (_backColor - 1)
                }
            } else if (_backColor > 0) {
                _outputParam += "4" + (_backColor - 1)
            }
            _outputParam += "m"
        }
        // 內容改變
        _textview_param.setText(_outputParam)

        // 顏色改變
        val _sample = SpannableString(_textview_sample.getText())
        val spansToRemove = _sample.getSpans<Any?>(0, _sample.length, Any::class.java)

        if (_isRecovery) {
            // 還原
            for (span in spansToRemove) {
                if (span is ForegroundColorSpan || span is BackgroundColorSpan) _sample.removeSpan(
                    span
                )
            }
        } else {
            var paintColor: Byte
            if (_frontColor > 0) {
                paintColor = (_frontColor - 1).toByte()
                if (_isHighlight) paintColor = (paintColor + 8).toByte()
                val colorSpan = ForegroundColorSpan(getTextColor(paintColor))
                _sample.setSpan(colorSpan, 0, _sample.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                for (span in spansToRemove) {
                    if (span is ForegroundColorSpan) _sample.removeSpan(span)
                }
            }

            if (_backColor > 0) {
                paintColor = (_backColor - 1).toByte()
                val colorSpan = BackgroundColorSpan(getBackgroundColor(paintColor))
                _sample.setSpan(colorSpan, 0, _sample.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                for (span in spansToRemove) {
                    if (span is BackgroundColorSpan) _sample.removeSpan(span)
                }
            }
        }
        _textview_sample.setText(_sample)
    }

    fun setListener(listener: Dialog_PaintColor_Listener?) {
        _listener = listener
    }

    override fun onClick(view: View?) {
        if (view === _send_button && _listener != null) {
            _listener!!.onPaintColorDone(_outputParam)
        }
        dismiss()
    }

    // 變更dialog寬度
    fun setDialogWidth() {
        val screenWidth = getContext().getResources().getDisplayMetrics().widthPixels
        val dialog_width = (screenWidth * 0.7).toInt()
        val oldLayoutParams = mainLayout.getLayoutParams()
        oldLayoutParams.width = dialog_width
        mainLayout.setLayoutParams(oldLayoutParams)
    }
}
