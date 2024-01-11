package com.kota.Bahamut.Dialogs;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.Bahamut.R;;

public class Dialog_SelectArticle extends ASDialog implements View.OnClickListener {
    Button _button_0_p = null;
    Button _button_1_p = null;
    Button _button_2_p = null;
    Button _button_3_p = null;
    Button _button_4_p = null;
    Button _button_5_p = null;
    Button _button_6_p = null;
    Button _button_7_p = null;
    Button _button_8_p = null;
    Button _button_9_p = null;
    Button _button_back_space_p = null;
    Button _cancel_button = null;
    TextView _content = null;
    String _content_string = "";
    Dialog_SelectArticle_Listener _listener = null;
    Button _search_button = null;

    public String getName() {
        return "BahamutBoardSelectDialog";
    }

    public Dialog_SelectArticle() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_select_article);
        getWindow().setBackgroundDrawable((Drawable) null);
        this._content = (TextView) findViewById(R.id.Bahamut_Dialog_Select_Content_Label);
        this._button_0_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_0_p);
        this._button_1_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_1_p);
        this._button_2_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_2_p);
        this._button_3_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_3_p);
        this._button_4_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_4_p);
        this._button_5_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_5_p);
        this._button_6_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_6_p);
        this._button_7_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_7_p);
        this._button_8_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_8_p);
        this._button_9_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_9_p);
        this._button_back_space_p = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_BackSpace_p);
        this._search_button = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_Search);
        this._cancel_button = (Button) findViewById(R.id.Bahamut_Dialog_Select_Button_Cancel);
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
