package com.kota.Bahamut.Dialogs;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.Bahamut.R;

import java.util.Objects;

public class Dialog_SearchBoard extends ASDialog implements View.OnClickListener {
    Button _cancel_button;
    EditText _keyword_label;
    Dialog_SearchBoard_Listener _listener = null;
    Button _search_button;

    public String getName() {
        return "BahamutBoardsSearchDialog";
    }

    public Dialog_SearchBoard() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_search_board);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(null);
        setTitle("搜尋看板");
        this._keyword_label = findViewById(R.id.Bahamut_Dialog_Search_board_keyword);
        this._search_button = findViewById(R.id.Bahamut_Dialog_Search_board_Search_Button);
        this._cancel_button = findViewById(R.id.Bahamut_Dialog_Search_board_Cancel_Button);
        this._search_button.setOnClickListener(this);
        this._cancel_button.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (view == this._search_button && this._listener != null) {
            this._listener.onSearchButtonClickedWithKeyword(this._keyword_label.getText().toString().replace("\n", ""));
        }
        dismiss();
    }

    public void setListener(Dialog_SearchBoard_Listener listener) {
        this._listener = listener;
    }
}
