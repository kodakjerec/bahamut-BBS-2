package com.kota.Bahamut.Dialogs;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.Bahamut.R;;

public class Dialog_SearchBoard extends ASDialog implements View.OnClickListener {
    Button _cancel_button = null;
    EditText _keyword_label = null;
    Dialog_SearchBoard_Listener _listener = null;
    Button _search_button = null;

    public String getName() {
        return "BahamutBoardsSearchDialog";
    }

    public Dialog_SearchBoard() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_search_board);
        getWindow().setBackgroundDrawable((Drawable) null);
        setTitle("搜尋看板");
        this._keyword_label = (EditText) findViewById(R.id.Bahamut_Dialog_Search_Board_Keyword);
        this._search_button = (Button) findViewById(R.id.Bahamut_Dialog_Search_Board_Search_Button);
        this._cancel_button = (Button) findViewById(R.id.Bahamut_Dialog_Search_Board_Cancel_Button);
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
