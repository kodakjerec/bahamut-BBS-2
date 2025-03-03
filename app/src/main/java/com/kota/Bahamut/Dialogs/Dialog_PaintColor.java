package com.kota.Bahamut.Dialogs;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.Bahamut.R;
import com.kota.Telnet.Reference.TelnetAnsiCode;

import java.util.Objects;

public class Dialog_PaintColor extends ASDialog implements View.OnClickListener {
    LinearLayout mainLayout;
    boolean _isRecovery = true;
    boolean _isHighlight = false;
    int _frontColor = 0;
    int _backColor = 0;
    String _outputParam;
    Dialog_PaintColor_Listener _listener;
    CheckBox _recovery_box;
    CheckBox _highlight_box;
    Spinner _front_color_spinner;
    Spinner _back_color_spinner;
    TextView _textview_param;
    TextView _textview_sample;

    Button _send_button;
    Button _cancel_button;

    CompoundButton.OnCheckedChangeListener _recovery_listener = (buttonView, isChecked) -> {
        _isRecovery = isChecked;
        if (isChecked) {
            _frontColor = -1;
            _backColor = -1;
            _isHighlight = false;
            _front_color_spinner.setSelection(_frontColor);
            _back_color_spinner.setSelection(_backColor);
            _highlight_box.setChecked(_isHighlight);

        }
        generateOutputParam();
    };
    CompoundButton.OnCheckedChangeListener _highlight_listener = (buttonView, isChecked) -> {
        _isHighlight = isChecked;
        if (_isRecovery) {
            _isRecovery = false;
            _recovery_box.setChecked(false);
        }
        generateOutputParam();
    };

    AdapterView.OnItemSelectedListener _frontColor_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            _frontColor = i;
            if (i>0 && _isRecovery) {
                _isRecovery = false;
                _recovery_box.setChecked(false);
            }
            generateOutputParam();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            _frontColor = -1;
        }
    };

    AdapterView.OnItemSelectedListener _backColor_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            _backColor = i;
            if (i>0 && _isRecovery) {
                _isRecovery = false;
                _recovery_box.setChecked(false);
            }
            generateOutputParam();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            _backColor = -1;
        }
    };

    public String getName() {
        return "BahamutPostArticlePaintColor";
    }

    public Dialog_PaintColor() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_paint_color);
        if (getWindow()!=null)
            getWindow().setBackgroundDrawable(null);
        setTitle(getContextString(R.string.post_article_page_paint_color));
        mainLayout = findViewById(R.id.dialog_paint_color_content_view);

        // 還原
        _recovery_box = mainLayout.findViewById(R.id.dialog_paint_color_check_recovery);
        _recovery_box.setOnCheckedChangeListener(_recovery_listener);
        mainLayout.findViewById(R.id.dialog_paint_color_check_recovery_item).setOnClickListener(view -> _recovery_box.setChecked(!_recovery_box.isChecked()));

        // 前景
        ArrayAdapter<String> adapter_front_color = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.dialog_paint_color_items));
        adapter_front_color.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _front_color_spinner = mainLayout.findViewById(R.id.post_article_page_paint_color_front_spinner);
        _front_color_spinner.setAdapter(adapter_front_color);
        _front_color_spinner.setOnItemSelectedListener(_frontColor_listener);

        // 背景
        ArrayAdapter<String> adapter_back_color = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.dialog_paint_color_items));
        adapter_back_color.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _back_color_spinner = mainLayout.findViewById(R.id.post_article_page_paint_color_back_spinner);
        _back_color_spinner.setAdapter(adapter_back_color);
        _back_color_spinner.setOnItemSelectedListener(_backColor_listener);

        // 亮色
        _highlight_box = mainLayout.findViewById(R.id.dialog_paint_color_check_highlight);
        _highlight_box.setOnCheckedChangeListener(_highlight_listener);
        mainLayout.findViewById(R.id.dialog_paint_color_check_highlight_item).setOnClickListener(view -> _highlight_box.setChecked(!_highlight_box.isChecked()));

        // 文字
        _textview_param = mainLayout.findViewById(R.id.dialog_paint_color_param);
        _textview_sample = mainLayout.findViewById(R.id.dialog_paint_color_sample);

        // 按鈕
        _send_button = mainLayout.findViewById(R.id.send);
        _send_button.setOnClickListener(this);
        _cancel_button = mainLayout.findViewById(R.id.cancel);
        _cancel_button.setOnClickListener(this);

        setDialogWidth();
    }

    void generateOutputParam() {
        if (_isRecovery) {
            _outputParam = "*[m";
        } else {
            _outputParam = "*[";
            if (_isHighlight)
                _outputParam+="1;";
            if (_frontColor>0) {
                _outputParam += "3" + (_frontColor-1);
                if (_backColor>0) {
                    _outputParam += ";4" + (_backColor-1);
                }
            } else
            if (_backColor>0) {
                _outputParam += "4" + (_backColor-1);
            }
            _outputParam += "m";
        }
        // 內容改變
        _textview_param.setText(_outputParam);

        // 顏色改變
        SpannableString _sample = new SpannableString(_textview_sample.getText());
        Object[] spansToRemove = _sample.getSpans(0, _sample.length(), Object.class);

        if (_isRecovery) {
            // 還原
            for (Object span:spansToRemove) {
                if (span instanceof ForegroundColorSpan || span instanceof BackgroundColorSpan)
                    _sample.removeSpan(span);
            }
        } else {
            byte paintColor;
            if (_frontColor>0) {
                paintColor = (byte) (_frontColor-1);
                if (_isHighlight)
                    paintColor+=8;
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(TelnetAnsiCode.getTextColor(paintColor));
                _sample.setSpan(colorSpan, 0, _sample.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                for (Object span:spansToRemove) {
                    if (span instanceof ForegroundColorSpan)
                        _sample.removeSpan(span);
                }
            }

            if (_backColor>0) {
                paintColor = (byte) (_backColor-1);
                BackgroundColorSpan colorSpan = new BackgroundColorSpan(TelnetAnsiCode.getBackgroundColor(paintColor));
                _sample.setSpan(colorSpan, 0, _sample.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                for (Object span:spansToRemove) {
                    if (span instanceof BackgroundColorSpan)
                        _sample.removeSpan(span);
                }
            }
        }
        _textview_sample.setText(_sample);
    }

    public void setListener(Dialog_PaintColor_Listener listener) {
        _listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (view == _send_button && _listener != null) {
            _listener.onPaintColorDone(_outputParam);
        }
        dismiss();
    }
    // 變更dialog寬度
    void setDialogWidth() {
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        int dialog_width = (int) (screenWidth*0.7);
        ViewGroup.LayoutParams oldLayoutParams = mainLayout.getLayoutParams();
        oldLayoutParams.width = dialog_width;
        mainLayout.setLayoutParams(oldLayoutParams);
    }
}
