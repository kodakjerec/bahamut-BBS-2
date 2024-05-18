package com.kota.Bahamut.Dialogs;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kota.ASFramework.Dialog.ASAlertDialog;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.Pages.BoardPage.BoardMainPage;
import com.kota.Bahamut.R;

import java.util.Objects;

public class Dialog_SelectArticle extends ASDialog implements View.OnClickListener {
    Button _button_0_p;
    Button _button_1_p;
    Button _button_2_p;
    Button _button_3_p;
    Button _button_4_p;
    Button _button_5_p;
    Button _button_6_p;
    Button _button_7_p;
    Button _button_8_p;
    Button _button_9_p;
    Button _button_back_space_p;
    Button _cancel_button;
    TextView _content;
    String _content_string = "";
    Dialog_SelectArticle_Listener _listener;
    Button _search_button;

    public String getName() {
        return "BahamutBoardSelectDialog";
    }

    public Dialog_SelectArticle() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_select_article);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(null);
        this._content = findViewById(R.id.Bahamut_Dialog_Select_content_Label);
        this._button_0_p = findViewById(R.id.Bahamut_Dialog_Select_Button_0_p);
        this._button_1_p = findViewById(R.id.Bahamut_Dialog_Select_Button_1_p);
        this._button_2_p = findViewById(R.id.Bahamut_Dialog_Select_Button_2_p);
        this._button_3_p = findViewById(R.id.Bahamut_Dialog_Select_Button_3_p);
        this._button_4_p = findViewById(R.id.Bahamut_Dialog_Select_Button_4_p);
        this._button_5_p = findViewById(R.id.Bahamut_Dialog_Select_Button_5_p);
        this._button_6_p = findViewById(R.id.Bahamut_Dialog_Select_Button_6_p);
        this._button_7_p = findViewById(R.id.Bahamut_Dialog_Select_Button_7_p);
        this._button_8_p = findViewById(R.id.Bahamut_Dialog_Select_Button_8_p);
        this._button_9_p = findViewById(R.id.Bahamut_Dialog_Select_Button_9_p);
        this._button_back_space_p = findViewById(R.id.Bahamut_Dialog_Select_Button_backSpace_p);
        this._search_button = findViewById(R.id.Bahamut_Dialog_Select_Button_Search);
        this._cancel_button = findViewById(R.id.Bahamut_Dialog_Select_Button_Cancel);
        this._button_0_p.setOnClickListener(this);
        this._button_1_p.setOnClickListener(this);
        this._button_2_p.setOnClickListener(this);
        this._button_3_p.setOnClickListener(this);
        this._button_4_p.setOnClickListener(this);
        this._button_5_p.setOnClickListener(this);
        this._button_6_p.setOnClickListener(this);
        this._button_7_p.setOnClickListener(this);
        this._button_8_p.setOnClickListener(this);
        this._button_9_p.setOnClickListener(this);
        this._button_back_space_p.setOnClickListener(this);
        this._search_button.setOnClickListener(this);
        this._cancel_button.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (view == this._button_0_p) {
            this._content_string += 0;
        } else if (view == this._button_1_p) {
            this._content_string += 1;
        } else if (view == this._button_2_p) {
            this._content_string += 2;
        } else if (view == this._button_3_p) {
            this._content_string += 3;
        } else if (view == this._button_4_p) {
            this._content_string += 4;
        } else if (view == this._button_5_p) {
            this._content_string += 5;
        } else if (view == this._button_6_p) {
            this._content_string += 6;
        } else if (view == this._button_7_p) {
            this._content_string += 7;
        } else if (view == this._button_8_p) {
            this._content_string += 8;
        } else if (view == this._button_9_p) {
            this._content_string += 9;
        } else if (view == this._button_back_space_p) {
            if (this._content_string.length() > 0) {
                this._content_string = this._content_string.substring(0, this._content_string.length() - 1);
            }
        } else if (view == this._search_button) {
            if (this._content_string.isEmpty()) {
                ASToast.showShortToast(getContextString(R.string.please_input_article_number));
                return;
            }
            if (this._listener != null) {
                this._listener.onSelectDialogDismissWIthIndex(this._content_string);
            }
            dismiss();
        } else if (view == this._cancel_button) {
            dismiss();
        }
        if (this._content_string.length() > 5) {
            this._content_string = this._content_string.substring(0, 5);
        }
        this._content.setText(this._content_string);
    }

    public void setListener(Dialog_SelectArticle_Listener listener) {
        this._listener = listener;
    }

    public void show() {
        super.show();
    }
}
