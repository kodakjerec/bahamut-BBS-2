package com.kota.Bahamut.Dialogs;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.bahamut_bbs_2.R;;

public class Dialog_SelectSign extends ASDialog implements View.OnClickListener {
    Button _cancel_button = null;
    Button _confirm_button = null;
    Dialog_SelectSign_Listener _listener = null;
    EditText _sign_field = null;

    public String getName() {
        return "BahamutSelectSignDialog";
    }

    public Dialog_SelectSign() {
        requestWindowFeature(1);
        setContentView(R.layout.dialog_select_sign);
        getWindow().setBackgroundDrawable((Drawable) null);
        setTitle(getContext().getString(R.string.select_sign));
        this._sign_field = (EditText) findViewById(R.id.Bahamut_Dialog_Select_Sign_Input_Field);
        this._confirm_button = (Button) findViewById(R.id.Bahamut_Dialog_Select_Sign_Confirm_Button);
        this._cancel_button = (Button) findViewById(R.id.Bahamut_Dialog_Select_Sign_Cancel_Button);
        this._confirm_button.setOnClickListener(this);
        this._cancel_button.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (view == this._confirm_button && this._listener != null) {
            this._listener.onSelectSign(this._sign_field.getText().toString().replace("\n", ""));
        }
        dismiss();
    }

    public void setListener(Dialog_SelectSign_Listener listener) {
        this._listener = listener;
    }
}
