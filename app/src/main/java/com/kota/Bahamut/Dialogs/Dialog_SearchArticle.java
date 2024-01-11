package com.kota.Bahamut.Dialogs;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.bahamut_bbs_2.R;;
import java.util.Vector;

public class Dialog_SearchArticle extends ASDialog implements View.OnClickListener {
    LinearLayout _author_block = null;
    EditText _author_label = null;
    Button _cancel_button = null;
    EditText _gy_field = null;
    LinearLayout _header_block = null;
    LinearLayout _keyword_block = null;
    EditText _keyword_label = null;
    Dialog_SearchArticle_Listener _listener = null;
    LinearLayout _mark_block = null;
    RadioGroup _mark_radio = null;
    Button _search_button = null;

    public String getName() {
        return "BahamutBoardSearchDialog";
    }

    public Dialog_SearchArticle() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_search_article);
        getWindow().setBackgroundDrawable((Drawable) null);
        setTitle("搜尋文章");
        this._keyword_label = (EditText) findViewById(R.id.Bahamut_Dialog_Search_Keyword);
        this._author_label = (EditText) findViewById(R.id.Bahamut_Dialog_Search_Author);
        this._mark_radio = (RadioGroup) findViewById(R.id.Bahamut_Dialog_Search_Mark);
        this._gy_field = (EditText) findViewById(R.id.gy_number_field);
        this._search_button = (Button) findViewById(R.id.Bahamut_Dialog_Search_Search_Button);
        this._cancel_button = (Button) findViewById(R.id.Bahamut_Dialog_Search_Cancel_Button);
        this._header_block = (LinearLayout) findViewById(R.id.SearchArticleDialog_HeaderBlock);
        this._keyword_block = (LinearLayout) findViewById(R.id.SearchArticleDialog_KeywordBlock);
        this._author_block = (LinearLayout) findViewById(R.id.SearchArticleDialog_AuthorBlock);
        this._mark_block = (LinearLayout) findViewById(R.id.SearchArticleDialog_MarkBlock);
        this._search_button.setOnClickListener(this);
        this._cancel_button.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (view == this._search_button && this._listener != null) {
            Vector<String> search_options = new Vector<>();
            String keyword = this._keyword_label.getText().toString().replace("\n", "");
            String author = this._author_label.getText().toString().replace("\n", "");
            String mark = "NO";
            if (this._mark_radio.getCheckedRadioButtonId() == R.id.Bahamut_Dialog_Search_Mark_YES) {
                mark = "YES";
            }
            String gy = this._gy_field.getText().toString();
            search_options.add(keyword);
            search_options.add(author);
            search_options.add(mark);
            search_options.add(gy);
            this._listener.onSearchDialogSearchButtonClickedWithValues(search_options);
        }
        dismiss();
    }

    public void setListener(Dialog_SearchArticle_Listener listener) {
        this._listener = listener;
    }

    public void show() {
        super.show();
    }
}
