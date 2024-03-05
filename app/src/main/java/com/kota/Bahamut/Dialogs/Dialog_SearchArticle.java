package com.kota.Bahamut.Dialogs;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.Bahamut.R;

import java.util.Objects;
import java.util.Vector;

public class Dialog_SearchArticle extends ASDialog implements View.OnClickListener {
    LinearLayout _author_block;
    EditText _author_label;
    Button _cancel_button;
    EditText _gy_field;
    LinearLayout _header_block;
    LinearLayout _keyword_block;
    EditText _keyword_label;
    Dialog_SearchArticle_Listener _listener;
    LinearLayout _mark_block;
    RadioGroup _mark_radio;
    Button _search_button;

    public String getName() {
        return "BahamutBoardSearchDialog";
    }

    public Dialog_SearchArticle() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_search_article);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(null);
        setTitle("搜尋文章");
        _keyword_label = findViewById(R.id.Bahamut_Dialog_Search_keyword);
        _author_label = findViewById(R.id.Bahamut_Dialog_Search_Author);
        _mark_radio = findViewById(R.id.Bahamut_Dialog_Search_mark);
        _gy_field = findViewById(R.id.gy_number_field);
        _search_button = findViewById(R.id.Bahamut_Dialog_Search_Search_Button);
        _cancel_button = findViewById(R.id.Bahamut_Dialog_Search_Cancel_Button);
        _header_block = findViewById(R.id.SearchArticleDialog_headerBlock);
        _keyword_block = findViewById(R.id.SearchArticleDialog_keywordBlock);
        _author_block = findViewById(R.id.SearchArticleDialog_AuthorBlock);
        _mark_block = findViewById(R.id.SearchArticleDialog_markBlock);
        _search_button.setOnClickListener(this);
        _cancel_button.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (view == _search_button && _listener != null) {
            Vector<String> search_options = new Vector<>();
            String keyword = _keyword_label.getText().toString().replace("\n", "");
            String author = _author_label.getText().toString().replace("\n", "");
            String mark = "NO";
            if (_mark_radio.getCheckedRadioButtonId() == R.id.Bahamut_Dialog_Search_mark_YES) {
                mark = "YES";
            }
            String gy = _gy_field.getText().toString();
            search_options.add(keyword);
            search_options.add(author);
            search_options.add(mark);
            search_options.add(gy);
            _listener.onSearchDialogSearchButtonClickedWithValues(search_options);
        }
        dismiss();
        _listener.onSearchDialogCancelButtonClicked();
    }

    public void setListener(Dialog_SearchArticle_Listener listener) {
        _listener = listener;
    }

    public void show() {
        super.show();
    }

    public void editContent(Vector<String> search_options) {
        setTitle("修改搜尋內容");
        _search_button.setText("確定");
        _keyword_label.setText(search_options.get(0));
        _author_label.setText(search_options.get(1));
        if (Objects.equals(search_options.get(2), "y"))
            _mark_radio.check(R.id.Bahamut_Dialog_Search_mark_YES);
        _gy_field.setText(search_options.get(3));
    }
}
